use crate::cache::{CacheFile, round};
use crate::source::Source;
use anyhow::{Context, Result, bail};
use serde::Serialize;
use std::collections::BTreeMap;
use std::path::Path;

const RAW: &str = "data/raw/cdaweb/ISS_DOSANL_TEPC_2017-09-04_2017-09-20.csv";
const SOURCE_URL: &str = "https://cdaweb.gsfc.nasa.gov/hapi/data?id=ISS_DOSANL_TEPC&time.min=2017-09-04T00:00:00Z&time.max=2017-09-20T00:00:00Z&parameters=DOSE,DOSE_EQ60,TIME_INTERVAL&format=csv";
const FETCHED_AT: &str = "2026-11-14T18:25:48Z";
const INSTRUMENT: &str = "ISS TEPC (NASA SRAG)";

/// ISS dose from NASA SRAG's TEPC on CDAWeb: 1-minute data averaged per hour.
pub struct IssDose;

/// `Environment` entity (TECH_OVERVIEW §4.2), dose fields only.
#[derive(Serialize)]
struct Environment {
    #[serde(rename = "doseRate_uGy_h")]
    dose_rate_ugy_h: f64,
    #[serde(rename = "doseEqRate_uSv_h")]
    dose_eq_rate_usv_h: f64,
    instrument: &'static str,
    timestamp: String,
    provenance: &'static str,
}

#[derive(Default)]
struct HourSum {
    absorbed_per_min: f64,
    equivalent_per_min: f64,
    samples: u32,
}

impl Source for IssDose {
    fn name(&self) -> &'static str {
        "iss-dose"
    }

    fn output_file(&self) -> &'static str {
        "iss_dose.json"
    }

    fn build(&self, repo: &Path) -> Result<CacheFile> {
        let path = repo.join(RAW);
        let mut reader = csv::ReaderBuilder::new()
            .has_headers(false)
            .from_path(&path)
            .with_context(|| format!("open {}", path.display()))?;

        let mut hours: BTreeMap<String, HourSum> = BTreeMap::new();
        let mut rejected = 0u32;
        for record in reader.records() {
            let record = record.with_context(|| format!("read {}", path.display()))?;
            let (Some(time), Some(dose), Some(dose_eq)) = (record.get(0), record.get(1), record.get(2)) else {
                rejected += 1;
                continue;
            };
            let (Ok(dose), Ok(dose_eq)) = (dose.parse::<f64>(), dose_eq.parse::<f64>()) else {
                rejected += 1;
                continue;
            };
            // HAPI fill values are large negative numbers; reject anything non-physical.
            if time.len() < 13 || !(0.0..1e6).contains(&dose) || !(0.0..1e6).contains(&dose_eq) {
                rejected += 1;
                continue;
            }
            let hour = hours.entry(format!("{}:00:00Z", &time[..13])).or_default();
            hour.absorbed_per_min += dose;
            hour.equivalent_per_min += dose_eq;
            hour.samples += 1;
        }
        if hours.is_empty() {
            bail!("no valid rows in {}", path.display());
        }
        if rejected > 0 {
            eprintln!("iss-dose: skipped {rejected} invalid rows");
        }

        // Source units are per minute; ×60 gives μGy/h and μSv/h (docs/BACKEND.md §1.1).
        let items: Vec<Environment> = hours
            .into_iter()
            .map(|(timestamp, h)| Environment {
                dose_rate_ugy_h: round(h.absorbed_per_min / f64::from(h.samples) * 60.0, 3),
                dose_eq_rate_usv_h: round(h.equivalent_per_min / f64::from(h.samples) * 60.0, 3),
                instrument: INSTRUMENT,
                timestamp,
                provenance: "real",
            })
            .collect();
        CacheFile::new(SOURCE_URL, FETCHED_AT, &items)
    }
}

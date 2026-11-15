use crate::cache::{CacheFile, round};
use crate::source::Source;
use anyhow::{Context, Result, bail};
use serde::Serialize;
use std::collections::BTreeMap;
use std::path::Path;

const RAW: &str = "data/raw/radlab/DosTel2_2017-09-04_2017-09-20.csv";
const SOURCE_URL: &str = "https://visualization.osdr.nasa.gov/radlab/api/?instrument=DosTel2&timestamp%3E=2017-09-04&timestamp%3C2017-09-20&absorbed_dose_rate&format=csv";
const FETCHED_AT: &str = "2026-11-15T07:04:04Z";
const INSTRUMENT: &str = "DosTel2 (DLR), ISS Columbus";

/// Partner-instrument dose (team decision #13): DLR's DosTel2 silicon telescope in the ISS Columbus
/// module, served by NASA OSDR RadLab. Absorbed dose rate only (μGy/h), averaged per hour.
pub struct PartnerDose;

/// `Environment` entity (TECH_OVERVIEW §4.2), absorbed dose only.
#[derive(Serialize)]
struct Environment {
    #[serde(rename = "doseRate_uGy_h")]
    dose_rate_ugy_h: f64,
    instrument: &'static str,
    timestamp: String,
    provenance: &'static str,
}

impl Source for PartnerDose {
    fn name(&self) -> &'static str {
        "partner-dose"
    }

    fn output_file(&self) -> &'static str {
        "iss_dose_partner.json"
    }

    fn build(&self, repo: &Path) -> Result<CacheFile> {
        let path = repo.join(RAW);
        let mut reader = csv::Reader::from_path(&path).with_context(|| format!("open {}", path.display()))?;
        // Columns: timestamp, instrument_id, absorbed_dose_rate (μGy/h), instrument.
        let mut hours: BTreeMap<String, (f64, u32)> = BTreeMap::new();
        let mut rejected = 0u32;
        for record in reader.records() {
            let record = record.with_context(|| format!("read {}", path.display()))?;
            let (Some(time), Some(rate)) = (record.get(0), record.get(2)) else {
                rejected += 1;
                continue;
            };
            let Ok(rate) = rate.parse::<f64>() else {
                rejected += 1;
                continue;
            };
            if time.len() < 13 || !(0.0..1e6).contains(&rate) {
                rejected += 1;
                continue;
            }
            let hour = hours.entry(format!("{}:00:00Z", &time[..13])).or_default();
            hour.0 += rate;
            hour.1 += 1;
        }
        if hours.is_empty() {
            bail!("no valid rows in {}", path.display());
        }
        if rejected > 0 {
            eprintln!("partner-dose: skipped {rejected} invalid rows");
        }
        let items: Vec<Environment> = hours
            .into_iter()
            .map(|(timestamp, (sum, n))| Environment {
                dose_rate_ugy_h: round(sum / f64::from(n), 3),
                instrument: INSTRUMENT,
                timestamp,
                provenance: "real",
            })
            .collect();
        CacheFile::new(SOURCE_URL, FETCHED_AT, &items)
    }
}

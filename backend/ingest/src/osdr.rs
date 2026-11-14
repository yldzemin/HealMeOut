use crate::cache::{CacheFile, round};
use crate::source::Source;
use anyhow::{Context, Result, anyhow, bail};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::collections::BTreeSet;
use std::path::Path;

const SOURCE: &str = "data/osdr/ (NASA OSDR OSD-575, OSD-569); mission tag from https://osdr.nasa.gov/osdr/data/osd/meta/<id>";
const FETCHED_AT: &str = "2026-11-14T18:27:06Z";

/// Missions shown with the commercial-mission tag (#30). Name, dates and orbit come from OSDR.
const COMMERCIAL_MISSIONS: &[&str] = &["SpaceX Inspiration4"];

/// Cytokines kept from the Eve panels. These columns have no reference range.
const EVE_ANALYTES: &[&str] = &["crp", "fibrinogen", "il_6", "il_10", "ifnγ"];

/// Readable names for a few source column names.
const RENAMES: &[(&str, &str)] = &[("urea_nitrogen_bun", "bun"), ("ifnγ", "ifn_gamma")];

/// Pre- and post-flight blood baselines of the Inspiration4 crew (OSD-575, OSD-569).
pub struct OsdrBaselines;

/// `MissionTag` entity (TECH_OVERVIEW §4.2).
#[derive(Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct MissionTag {
    name: String,
    kind: &'static str,
    start: String,
    end: String,
    orbit: String,
    crew: String,
    hosted_by: &'static str,
    study_id: String,
    link: String,
}

/// `Baseline` entity (TECH_OVERVIEW §4.2): one crew × analyte × timepoint.
#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct Baseline {
    crew_id: String,
    source: String,
    analyte: String,
    timepoint: String,
    value: f64,
    unit: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    ref_low: Option<f64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    ref_high: Option<f64>,
    mission: MissionTag,
}

impl Source for OsdrBaselines {
    fn name(&self) -> &'static str {
        "baselines"
    }

    fn output_file(&self) -> &'static str {
        "baselines.json"
    }

    fn build(&self, repo: &Path) -> Result<CacheFile> {
        let osd575 = mission_tag(repo, "OSD-575")?;
        let osd569 = mission_tag(repo, "OSD-569")?;

        let mut items = Vec::new();
        items.extend(wide_panel(repo, "data/osdr/OSD-575_LSDS-8_CMP_TRANSFORMED.csv", &osd575, None)?);
        items.extend(wide_panel(
            repo,
            "data/osdr/OSD-575_LSDS-8_Cardio_EvePanel_TRANSFORMED.csv",
            &osd575,
            Some(EVE_ANALYTES),
        )?);
        items.extend(wide_panel(
            repo,
            "data/osdr/OSD-575_LSDS-8_Immune_EvePanel_TRANSFORMED.csv",
            &osd575,
            Some(EVE_ANALYTES),
        )?);
        items.extend(cbc_panel(repo, "data/osdr/OSD-569_LSDS-7_CBC_SUBMITTED.csv", &osd569)?);

        items.sort_by(|a, b| {
            (a.crew_id.as_str(), a.analyte.as_str(), timepoint_days(&a.timepoint))
                .cmp(&(b.crew_id.as_str(), b.analyte.as_str(), timepoint_days(&b.timepoint)))
        });
        let mut seen = BTreeSet::new();
        for b in &items {
            if !seen.insert((b.crew_id.as_str(), b.analyte.as_str(), b.timepoint.as_str())) {
                bail!("duplicate baseline {} {} {}", b.crew_id, b.analyte, b.timepoint);
            }
        }
        CacheFile::new(SOURCE, FETCHED_AT, &items)
    }
}

/// Builds the mission tag from the study's OSDR metadata (`data/raw/osdr/<id>_meta.json`).
fn mission_tag(repo: &Path, study_id: &str) -> Result<MissionTag> {
    let path = repo.join(format!("data/raw/osdr/{study_id}_meta.json"));
    let text = std::fs::read_to_string(&path).with_context(|| format!("read {}", path.display()))?;
    let meta: Value = serde_json::from_str(&text).with_context(|| format!("parse {}", path.display()))?;
    let study = &meta["study"][study_id]["studies"][0];
    if study.is_null() {
        bail!("no study block in {}", path.display());
    }
    let comment = |name: &str| -> Option<String> {
        study["comments"]
            .as_array()?
            .iter()
            .find(|c| c["name"] == name)
            .and_then(|c| c["value"].as_str())
            .map(str::trim)
            .filter(|v| !v.is_empty())
            .map(String::from)
    };
    let required = |name: &str| comment(name).ok_or_else(|| anyhow!("{study_id}: metadata has no '{name}'"));

    let name = required("Mission Name")?;
    let kind = if COMMERCIAL_MISSIONS.contains(&name.as_str()) { "commercial" } else { "nasa" };
    Ok(MissionTag {
        kind,
        start: us_date(&required("Mission Start")?)?,
        end: us_date(&required("Mission End")?)?,
        orbit: study["studyDesignDescriptors"][0]["annotationValue"].as_str().unwrap_or_default().to_string(),
        crew: crew_phrase(study["description"].as_str().unwrap_or_default()).unwrap_or_default(),
        hosted_by: "NASA OSDR",
        study_id: study_id.to_string(),
        link: comment("Mission Link").unwrap_or_default().replace(' ', "%20"),
        name,
    })
}

/// Reads a wide panel: one row per sample (`C001_serum_L-3`), one `<analyte>_value_<unit>` or
/// `<analyte>_concentration_<unit>` column per analyte, with optional range columns.
fn wide_panel(repo: &Path, file: &str, mission: &MissionTag, keep: Option<&[&str]>) -> Result<Vec<Baseline>> {
    struct Column {
        analyte: String,
        unit: String,
        value: usize,
        low: Option<usize>,
        high: Option<usize>,
    }

    let path = repo.join(file);
    let mut reader = csv::Reader::from_path(&path).with_context(|| format!("open {}", path.display()))?;
    let headers: Vec<String> = reader.headers()?.iter().map(|h| h.trim_start_matches('\u{feff}').to_string()).collect();
    let position = |prefixes: &[String]| headers.iter().position(|h| prefixes.iter().any(|p| h.starts_with(p.as_str())));

    let mut columns = Vec::new();
    for (index, header) in headers.iter().enumerate() {
        let Some((analyte, unit_key)) = split_value_column(header) else { continue };
        // The source gives only race-based eGFR variants; they are not used.
        if analyte.starts_with("egfr") || keep.is_some_and(|k| !k.contains(&analyte.as_str())) {
            continue;
        }
        columns.push(Column {
            low: position(&[format!("{analyte}_range_min")]),
            high: position(&[format!("{analyte}_range_max"), format!("{analyte}_max_")]),
            unit: unit_label(&unit_key),
            analyte: rename(&analyte),
            value: index,
        });
    }
    if columns.is_empty() {
        bail!("no value columns in {}", path.display());
    }

    let mut out = Vec::new();
    let mut skipped = 0;
    for record in reader.records() {
        let record = record.with_context(|| format!("read {}", path.display()))?;
        let Some((crew_id, timepoint)) = parse_sample(record.get(0).unwrap_or_default()) else {
            skipped += 1;
            continue;
        };
        for c in &columns {
            let Some(value) = number(record.get(c.value)) else {
                skipped += 1;
                continue;
            };
            out.push(Baseline {
                crew_id: crew_id.clone(),
                source: mission.study_id.clone(),
                analyte: c.analyte.clone(),
                timepoint: timepoint.clone(),
                value: round(value, 4),
                unit: c.unit.clone(),
                ref_low: c.low.and_then(|i| number(record.get(i))),
                ref_high: c.high.and_then(|i| number(record.get(i))),
                mission: mission.clone(),
            });
        }
    }
    if skipped > 0 {
        eprintln!("baselines: {file}: skipped {skipped} empty or non-numeric cells");
    }
    Ok(out)
}

/// Row of the long-format CBC file (`OSD-569_LSDS-7_CBC_SUBMITTED.csv`).
#[derive(Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
struct CbcRow {
    analyte: String,
    value: String,
    range_min: String,
    range_max: String,
    units: String,
    subject_id: String,
    test_date: String,
}

/// Reads the long-format CBC file. "(FEMALE)" rows carry female reference ranges for the same analyte.
fn cbc_panel(repo: &Path, file: &str, mission: &MissionTag) -> Result<Vec<Baseline>> {
    let path = repo.join(file);
    let mut reader = csv::Reader::from_path(&path).with_context(|| format!("open {}", path.display()))?;
    let headers = csv::StringRecord::from(
        reader.headers()?.iter().map(|h| h.trim_start_matches('\u{feff}').to_string()).collect::<Vec<_>>(),
    );

    let mut out = Vec::new();
    let mut skipped = 0;
    for record in reader.records() {
        let row: CbcRow = record?.deserialize(Some(&headers)).with_context(|| format!("parse {}", path.display()))?;
        let (Some(crew_id), Some(value)) = (crew_id(&row.subject_id), number(Some(&row.value))) else {
            skipped += 1;
            continue;
        };
        out.push(Baseline {
            crew_id,
            source: mission.study_id.clone(),
            analyte: snake_case(&row.analyte.replace(" (FEMALE)", "")),
            timepoint: row.test_date.trim().to_string(),
            value: round(value, 4),
            unit: row.units.trim().to_string(),
            ref_low: number(Some(&row.range_min)),
            ref_high: number(Some(&row.range_max)),
            mission: mission.clone(),
        });
    }
    if skipped > 0 {
        eprintln!("baselines: {file}: skipped {skipped} rows");
    }
    Ok(out)
}

/// `glucose_value_milligram_per_deciliter` → (`glucose`, `milligram_per_deciliter`);
/// `crp_concentration_picogram_per_milliliter` → (`crp`, `picogram_per_milliliter`).
fn split_value_column(header: &str) -> Option<(String, String)> {
    if let Some(i) = header.find("_value") {
        let rest = &header[i + "_value".len()..];
        if rest.is_empty() || rest.starts_with('_') {
            return Some((header[..i].to_string(), rest.trim_start_matches('_').to_string()));
        }
    }
    let i = header.find("_concentration_")?;
    Some((header[..i].to_string(), header[i + "_concentration_".len()..].to_string()))
}

fn unit_label(key: &str) -> String {
    match key {
        "" => "ratio",
        "gram_per_deciliter" => "g/dL",
        "milligram_per_deciliter" => "mg/dL",
        "millimol_per_liter" => "mmol/L",
        "units_per_liter" => "U/L",
        "picogram_per_milliliter" => "pg/mL",
        "nanogram_per_milliliter" => "ng/mL",
        other => other,
    }
    .to_string()
}

fn rename(analyte: &str) -> String {
    RENAMES.iter().find(|(from, _)| *from == analyte).map_or(analyte, |(_, to)| to).to_string()
}

/// `WHITE BLOOD CELL COUNT` → `white_blood_cell_count`.
fn snake_case(text: &str) -> String {
    let lower = text.trim().to_lowercase();
    let mut out = String::with_capacity(lower.len());
    for ch in lower.chars() {
        if ch.is_alphanumeric() {
            out.push(ch);
        } else if !out.ends_with('_') {
            out.push('_');
        }
    }
    out.trim_matches('_').to_string()
}

/// `C001_serum_L-3` → (`C1`, `L-3`).
fn parse_sample(sample: &str) -> Option<(String, String)> {
    let mut parts = sample.split('_');
    let crew = crew_id(parts.next()?)?;
    let timepoint = parts.next_back()?.trim();
    (timepoint.starts_with('L') || timepoint.starts_with('R')).then(|| (crew, timepoint.to_string()))
}

/// Inspiration4 subject `C001`…`C004` → app crew id `C1`…`C4` (TECH_OVERVIEW §4.4).
fn crew_id(subject: &str) -> Option<String> {
    let n: u32 = subject.trim().strip_prefix('C')?.parse().ok()?;
    (1..=4).contains(&n).then(|| format!("C{n}"))
}

/// `L-92` → -92, `R+194` → 194; used only for sorting.
fn timepoint_days(timepoint: &str) -> i32 {
    timepoint.get(1..).and_then(|d| d.parse().ok()).unwrap_or(0)
}

fn number(cell: Option<&str>) -> Option<f64> {
    cell?.trim().parse::<f64>().ok().filter(|v| v.is_finite())
}

/// OSDR uses `09/16/2021` and `9/16/21`.
fn us_date(text: &str) -> Result<String> {
    let parts: Vec<&str> = text.split('/').map(str::trim).collect();
    let [m, d, y] = parts.as_slice() else { bail!("bad date '{text}'") };
    let (m, d, mut y) = (m.parse::<u32>()?, d.parse::<u32>()?, y.parse::<u32>()?);
    if y < 100 {
        y += 2000;
    }
    if !(1..=12).contains(&m) || !(1..=31).contains(&d) {
        bail!("bad date '{text}'");
    }
    Ok(format!("{y:04}-{m:02}-{d:02}"))
}

/// "…with four private astronauts…" → "four private astronauts".
fn crew_phrase(description: &str) -> Option<String> {
    let index = description.find(" private astronauts")?;
    let count = description[..index].rsplit(' ').next()?;
    Some(format!("{count} private astronauts"))
}

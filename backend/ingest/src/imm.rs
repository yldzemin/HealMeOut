use crate::cache::CacheFile;
use crate::source::Source;
use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::path::Path;

const RAW: &str = "data/raw/imm/imm_leo_6mo.csv";
const SOURCE_URL: &str =
    "https://www.nasa.gov/wp-content/uploads/2026/08/ranking-of-medical-conditions-by-probability.pdf";
const FETCHED_AT: &str = "2026-11-14T00:00:00Z";

/// NASA Integrated Medical Model: probability of at least one event, LEO 6 months, 1 EVA.
pub struct Imm;

#[derive(Deserialize, Serialize)]
struct ConditionProbability {
    condition: String,
    probability: f64,
}

impl Source for Imm {
    fn name(&self) -> &'static str {
        "imm"
    }

    fn output_file(&self) -> &'static str {
        "imm_leo.json"
    }

    fn build(&self, repo: &Path) -> Result<CacheFile> {
        let path = repo.join(RAW);
        let mut reader = csv::Reader::from_path(&path).with_context(|| format!("open {}", path.display()))?;
        let mut items: Vec<ConditionProbability> = reader
            .deserialize()
            .collect::<Result<_, _>>()
            .with_context(|| format!("parse {}", path.display()))?;
        items.sort_by(|a, b| b.probability.total_cmp(&a.probability));
        CacheFile::new(SOURCE_URL, FETCHED_AT, &items)
    }
}

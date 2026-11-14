use anyhow::{Context, Result};
use serde::Serialize;
use std::path::Path;

/// The envelope every `data/cache/*.json` file uses (`docs/BACKEND.md` §1.1).
pub struct CacheFile {
    pub source: String,
    pub fetched_at: String,
    /// Items already serialized, one JSON object each, in struct field order.
    pub items: Vec<String>,
}

impl CacheFile {
    pub fn new<T: Serialize>(source: &str, fetched_at: &str, items: &[T]) -> Result<Self> {
        let items = items
            .iter()
            .map(serde_json::to_string)
            .collect::<Result<Vec<_>, _>>()
            .context("serialize items")?;
        Ok(Self { source: source.to_string(), fetched_at: fetched_at.to_string(), items })
    }

    /// Writes one item per line so that diffs stay readable.
    pub fn write(&self, path: &Path) -> Result<()> {
        let mut out = String::from("{\n");
        out.push_str(&format!("  \"source\": {},\n", serde_json::to_string(&self.source)?));
        out.push_str(&format!("  \"fetchedAt\": {},\n", serde_json::to_string(&self.fetched_at)?));
        out.push_str("  \"items\": [\n");
        for (i, item) in self.items.iter().enumerate() {
            out.push_str("    ");
            out.push_str(item);
            if i + 1 < self.items.len() {
                out.push(',');
            }
            out.push('\n');
        }
        out.push_str("  ]\n}\n");
        std::fs::write(path, out).with_context(|| format!("write {}", path.display()))
    }
}

/// Rounds to a fixed number of decimals so the JSON stays short and stable.
pub fn round(value: f64, decimals: i32) -> f64 {
    let factor = 10f64.powi(decimals);
    (value * factor).round() / factor
}

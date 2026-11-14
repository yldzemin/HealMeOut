use crate::cache::CacheFile;
use anyhow::Result;
use std::path::Path;

/// One raw NASA source that becomes one file in `data/cache/`.
pub trait Source {
    /// Name used on the command line, e.g. `baselines`.
    fn name(&self) -> &'static str;
    /// File name inside `data/cache/`.
    fn output_file(&self) -> &'static str;
    /// Reads the raw files under `repo` and builds the cache file.
    fn build(&self, repo: &Path) -> Result<CacheFile>;
}

//! ASTRA-TWIN ingest tool.
//!
//! Reads the raw NASA files committed under `data/osdr/` and `data/raw/` and writes
//! `data/cache/*.json` in the envelope from `docs/BACKEND.md` §1.1. It does not use the
//! network, so it runs offline; the raw files are listed in `data/raw/README.md`.
//!
//! Usage (from the repo root):
//! `cargo run --manifest-path backend/ingest/Cargo.toml -- [--repo <path>] [all|baselines|iss-dose|partner-dose|imm]`

mod cache;
mod imm;
mod iss_dose;
mod osdr;
mod partner_dose;
mod source;

use anyhow::{Context, Result, bail};
use source::Source;
use std::path::PathBuf;

const USAGE: &str = "usage: astra-ingest [--repo <path>] [all|baselines|iss-dose|partner-dose|imm]";

fn main() -> Result<()> {
    let mut repo = PathBuf::from(".");
    let mut target = String::from("all");
    let mut args = std::env::args().skip(1);
    while let Some(arg) = args.next() {
        match arg.as_str() {
            "--repo" => repo = PathBuf::from(args.next().context(USAGE)?),
            "-h" | "--help" => {
                println!("{USAGE}");
                return Ok(());
            }
            other => target = other.to_string(),
        }
    }
    if !repo.join("data/osdr").is_dir() {
        bail!("{} is not the repo root (no data/osdr). {USAGE}", repo.display());
    }

    let sources: Vec<Box<dyn Source>> = vec![
        Box::new(osdr::OsdrBaselines),
        Box::new(iss_dose::IssDose),
        Box::new(partner_dose::PartnerDose),
        Box::new(imm::Imm),
    ];
    let selected: Vec<&dyn Source> = sources
        .iter()
        .map(|s| s.as_ref())
        .filter(|s| target == "all" || s.name() == target)
        .collect();
    if selected.is_empty() {
        bail!("unknown target '{target}'. {USAGE}");
    }

    let out_dir = repo.join("data/cache");
    std::fs::create_dir_all(&out_dir).with_context(|| format!("create {}", out_dir.display()))?;
    for source in selected {
        let file = source.build(&repo).with_context(|| format!("source '{}'", source.name()))?;
        let path = out_dir.join(source.output_file());
        file.write(&path)?;
        println!("{:<10} {:>5} items -> {}", source.name(), file.items.len(), path.display());
    }
    Ok(())
}

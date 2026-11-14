"""Build frontend mock baselines from the real NASA OSDR OSD-575 CMP file.

Run from the repo root:  python3 -I frontend/scripts/make_baselines.py
Writes frontend/src/mock/baselines.json. Mock only: the backend's Rust
ingest will replace this (docs/BACKEND.md).
"""

import csv
import json
import statistics
from pathlib import Path

SRC = Path("data/osdr/OSD-575_LSDS-8_CMP_TRANSFORMED.csv")
OUT = Path("frontend/src/mock/baselines.json")

# analyte id -> (CSV column stem, unit, label)
ANALYTES = {
    "glucose": ("glucose", "milligram_per_deciliter", "mg/dL", "Glucose"),
    "sodium": ("sodium", "millimol_per_liter", "mmol/L", "Sodium"),
    "potassium": ("potassium", "millimol_per_liter", "mmol/L", "Potassium"),
    "bun": ("urea_nitrogen_bun", "milligram_per_deciliter", "mg/dL", "Urea nitrogen (BUN)"),
    "creatinine": ("creatinine", "milligram_per_deciliter", "mg/dL", "Creatinine"),
    "calcium": ("calcium", "milligram_per_deciliter", "mg/dL", "Calcium"),
}
PRE = ["L-92", "L-44", "L-3"]
ORDER = ["L-92", "L-44", "L-3", "R+1", "R+45", "R+82", "R+194"]


def num(value: str):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def main() -> None:
    rows = list(csv.DictReader(SRC.open(encoding="utf-8-sig")))
    out: dict = {
        "source": "NASA OSDR OSD-575 (Inspiration4), CMP, TRANSFORMED file",
        "sourceUrl": "https://osdr.nasa.gov/bio/repo/data/studies/OSD-575",
        "note": "Pre- and post-flight values only. Baseline = mean and SD of L-92, L-44, L-3.",
        "crew": {},
    }
    for n in range(1, 5):
        code = f"C00{n}"
        crew_rows = {r["Sample Name"].split("_")[-1]: r for r in rows if r["Sample Name"].startswith(code)}
        analytes = {}
        for aid, (stem, unit_key, unit, label) in ANALYTES.items():
            col = f"{stem}_value_{unit_key}"
            lo_col, hi_col = f"{stem}_range_min_{unit_key}", f"{stem}_range_max_{unit_key}"
            points = [
                {"timepoint": tp, "value": num(crew_rows[tp].get(col))}
                for tp in ORDER
                if tp in crew_rows and num(crew_rows[tp].get(col)) is not None
            ]
            pre = [p["value"] for p in points if p["timepoint"] in PRE]
            any_row = next(iter(crew_rows.values()))
            analytes[aid] = {
                "label": label,
                "unit": unit,
                "refLow": num(any_row.get(lo_col)),
                "refHigh": num(any_row.get(hi_col)),
                "baselineMean": round(statistics.mean(pre), 2) if pre else None,
                "baselineSd": round(statistics.stdev(pre), 2) if len(pre) > 1 else None,
                "points": points,
            }
        out["crew"][f"C{n}"] = analytes
    OUT.write_text(json.dumps(out, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()

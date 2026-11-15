# Raw source files

Downloaded once and committed so that `backend/ingest` runs offline. Do not edit these files; re-download them instead.

| File | Source | Fetched (UTC) |
|---|---|---|
| `cdaweb/ISS_DOSANL_TEPC_2017-09-04_2017-09-20.csv` | NASA SRAG ISS TEPC via CDAWeb HAPI: `https://cdaweb.gsfc.nasa.gov/hapi/data?id=ISS_DOSANL_TEPC&time.min=2017-09-04T00:00:00Z&time.max=2017-09-20T00:00:00Z&parameters=DOSE,DOSE_EQ60,TIME_INTERVAL&format=csv`. Columns: time, `DOSE` (μGy/min), `DOSE_EQ60` (μSv/min, ICRP-60), `TIME_INTERVAL` (min). No header row. | 2026-11-14 18:25 |
| `radlab/DosTel2_2017-09-04_2017-09-20.csv` | NASA OSDR RadLab, partner instrument DLR DosTel2 (DOSIS 3D) in the ISS Columbus module: `https://visualization.osdr.nasa.gov/radlab/api/?instrument=DosTel2&timestamp%3E=2017-09-04&timestamp%3C2017-09-20&absorbed_dose_rate&format=csv`. Columns: timestamp, instrument_id, `absorbed_dose_rate` (μGy/h), instrument. Module "Columbus" checked with the `module` field. | 2026-11-15 07:04 |
| `osdr/OSD-575_meta.json` | NASA OSDR study metadata: `https://osdr.nasa.gov/osdr/data/osd/meta/575` | 2026-11-14 18:27 |
| `osdr/OSD-569_meta.json` | NASA OSDR study metadata: `https://osdr.nasa.gov/osdr/data/osd/meta/569` | 2026-11-14 18:27 |
| `imm/imm_leo_6mo.csv` | Transcribed from NASA IMM "Ranking of Medical Conditions by Probability" (iMED v5.0), LEO 6 months, 1 EVA: `https://www.nasa.gov/wp-content/uploads/2026/08/ranking-of-medical-conditions-by-probability.pdf`, via `NASA_DATA_SOURCES.md` §10.1 | 2026-11-14 |

The OSDR panel CSVs used for baselines are in `data/osdr/`.

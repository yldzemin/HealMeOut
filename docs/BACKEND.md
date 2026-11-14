# ASTRA-TWIN — Backend

Owner: backend developer. Shared rules and data model: [`TECH_OVERVIEW.md`](TECH_OVERVIEW.md).
The backend turns NASA data into clean JSON, runs the twin and rules engines, drives scenarios and enforces privacy.

---

## 1. Stack (PROPOSED: pick what you know best)

| Option | Use if | Notes |
|---|---|---|
| **Java 17/21 + Spring Boot 3** (Web, Validation) | You know Java well | Apache Commons CSV for OSDR files; `java.net.http.HttpClient` for APIs; Jackson for JSON |
| **Python 3.11+ + FastAPI** | You know Python | pandas for CSV; httpx for APIs |

Either way:

- Enable CORS for the frontend dev server and the GitHub Pages origin.
- Run fully offline from `data/cache/` (flag `OFFLINE=true`).
- Pin dependency versions.

```
backend/
  src/…/ingest/     ← OSDR, RadLab, EDA, IMM loaders → data/cache/*.json
  src/…/twin/       ← baselines, z-scores, forecasts
  src/…/rules/      ← thresholds → status levels and alerts
  src/…/scenario/   ← scripted scenario timelines
  src/…/privacy/    ← filters what each role/screen may see
  src/…/api/        ← controllers
```

---

## 2. Data ingestion

| Source | What | How | Reference |
|---|---|---|---|
| **OSDR OSD-575** (CMP, cardio, immune) | Real crew baselines: glucose, Na, K, BUN, creatinine, IL-6, CRP | Read `data/osdr/OSD-575_*_TRANSFORMED.csv` | `NASA_DATA_SOURCES.md` §2.1 |
| **OSDR OSD-569** (CBC) | Haematology baselines, 4 crew × 7 time points (L-92 … R+194) | Read `data/osdr/OSD-569_*` | same |
| **OSDR RadLab** | ISS dose rate (μGy/h) | `https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Earth&timestamp%3E=2017-09-04&timestamp%3C2017-09-20&absorbed_dose_rate&format=csv` | §4.2, §11.3 |
| **OSDR EDA** | ISS cabin CO2, temperature, humidity | https://visualization.osdr.nasa.gov/eda (app was unreachable on 2026-11-14; retry and cache) | §4.1 |
| **NASA IMM** | LEO 6-month condition probabilities | Transcribe the table in `NASA_DATA_SOURCES.md` §10.1 into `data/cache/imm_leo.json` | §10.1 |

**CSV gotchas:**

- Files start with a UTF-8 BOM. Python: `encoding="utf-8-sig"`. Java: strip `﻿` from the first header.
- OSD-569/575 are **pre- and post-flight only**. They give baselines, not in-flight values.
- Use only crews C001–C004 and map them to fictional callsigns (TECH_OVERVIEW §4.4).

**RadLab instruments:** use only NASA instruments: `RAD-*-pre` (ISS-RAD), `REM-*`, `TEPC-*`/`IV-TEPC-*`. Do **not** use DosTel (DLR) until #13 is decided.

- For Sep 2017: `RAD-COL1A2-pre` daily ≈12.2–13.3, **18.39 on Sep 11**, 15.02 on Sep 12. IV-TEPC has a gap Sep 7–14.
- `instrument=ISS-RAD` returns no rows; filter by the `RAD-` prefix instead.

---

## 3. Twin engine

Rule-based and model-based. **No machine learning**: 4 people × 7 time points is not enough to train on.

### 3.1 Personal baseline and z-score

- Baseline per analyte from the person's pre-flight points (L-92, L-44, L-3).
- `z = (value − baseline_mean) / baseline_sd`. If `sd` is undefined, fall back to the reference range in the CSV.
- Simulated in-flight indicators (HR, BP, SpO2, temperature) are generated around the baseline and labelled `provenance: "simulated"`.

### 3.2 Forecasts (T+6, T+12)

Labelled `"modelled"`. Each uses only sourced rates:

| Forecast | Model | Source |
|---|---|---|
| Sleep debt | scheduled 8.5 h vs logged sleep; ISS mean 6.09 h | Barger 2014 |
| Hydration | 2.5 L/day intake target; loss ~0.13% body mass/h at rest; ≥2.5% mass loss → performance drop | `CLAUDE_HAFIZASI.md` §4.1 |
| Cumulative dose | ISS ~0.2–0.5 mSv/day; career limit 600 mSv | Simonsen 2025 (NTRS 20250006168) |
| Temperature drift | +~1 °C over ~2.5 months | Stahn 2017 |
| Energy intake | ISS crews eat ~74–80% of estimated need | NTRS 20060049121; Smith 2005 |

When a new reading arrives, compare it with the previous forecast and store the error (for the "forecast vs actual" view).

---

## 4. Rules engine

Input: readings, check-ins, environment. Output: `Alert` objects with level L0–L4 (TECH_OVERVIEW §4.1).

| Indicator | Rule | Source |
|---|---|---|
| Glucose | <70 mg/dL → L1; <54 → L3; confusion reported → L4. Action: 15 g fast carbs, re-check in 15 min | ADA 2025 (doi:10.2337/dc25-S006) |
| Cabin CO2 | 1-h average >3 mmHg → L1; ≥4.5 mmHg or headache → L2; ≥6.8 mmHg → L3. Also flag 7-day average >2.5 mmHg (headache risk) | 3 mmHg and 2.5 mmHg: OCHMO-TB-004 (NASA-STD-3001 V2 6004); headache at 2.8–4.5 mmHg: ARASTIRMA_RAPORU §4; 6.8 mmHg: NASA contingency proposal "6.8–10 mmHg for ≤4 h", not yet a standard ([ICES-2025-402](https://ntrs.nasa.gov/api/citations/20250006273/downloads/ICES-2025-402.pdf)) |
| SpO2 | <90% → L4 | <90% = hypoxaemia (ARASTIRMA_RAPORU §4, SpO2 row). NASA's own limit is PIO2 ≥122 mmHg (OCHMO-TB-003) |
| Dose rate | > 5× personal/station baseline → L2 (shelter advice) | our design; data RadLab |
| Lab analytes, HR, BP | |z| ≥ 2 vs personal baseline → L1; persists or combined with symptoms → L2 | our design (no universal in-flight threshold) |
| Psych check-in trend | persistent decline over N days → private prompt "discuss at next PPC" | our design; never on Crew Deck |

Each alert carries `evidence` and `source` strings for the frontend "?" card. Thresholds marked "our design" must say so in `evidence`.

**Recommendations:** medication only from the ISS formulary (Diaz 2024; decision #11), always `status: "open"` until the CMO approves. Never auto-apply anything.

---

## 5. Scenario engine

A scenario is a timeline of events (time offset → reading/alert/state change). `POST start` plays it; `reset` restores nominal.

### `chest-pain` (MVP)
1. t=0: crew member's HR/activity pattern indicates collapse (simulated) → `L4` alert.
2. Broadcast to all crew (wrist alert) and Crew Deck emergency mode.
3. Assign roles: CMO1 lead, CMO2 assist, one crew fetches ALSP and defibrillator, one comms/log.
4. Return the checklist steps (Hinkelbein 2020: Evetts-Russomano CPR → Reverse Bear Hug fallback; CMRS restraint for ALS and defibrillation).
5. Emergency card: allergies and current medication only.
6. Mark "ground informed".

### `co2` (MVP)
Replay a rising CO2 curve (EDA data if available, else a sourced synthetic ramp from 3 to 6.8 mmHg) through the rules in §4.

### `spe-iss` (Target)
Replay `RAD-COL1A2-pre` Sep 4–19, 2017; shelter advice when the dose rule fires.

### `bad-news` (Target)
Inject a family-event flag; personal support plan; Earth Twin 24–72 h plan for ground approval; Crew Deck sees only "reduced tasking requested".

---

## 6. Privacy filter (server-side)

Enforce here, not only in the UI:

| Data | Self | CMO | Other crew (Crew Deck) | Emergency |
|---|---|---|---|---|
| Status level, module | ✔ | ✔ | ✔ | ✔ |
| Vitals detail | ✔ | ✔ | — | ✔ |
| Medication | ✔ | ✔ | only if opted in | allergies + current meds |
| Psych check-in | ✔ | — (shared to BHP by the astronaut) | **never** | **never** |
| Crew climate | own answers | aggregate | aggregate, min. response threshold | — |

Basis: NASA-STD-3001 Vol 1 V1 6002/6003/6010/6011 (private medical and psychological communications); AMIS 2024 (NTRS 20240004505); IOM *Safe Passage*.

---

## 7. Cache and offline

- `ingest` writes everything to `data/cache/*.json` with a `fetchedAt` timestamp and the source URL.
- At start-up, load from cache; try live sources only if `OFFLINE=false`, and fall back to cache on any error.
- Commit cache files that the demo needs (small JSON only).

---

## 8. Definition of done (backend)

- Starts with no internet and serves all MVP data from `data/cache/`.
- JSON field names match TECH_OVERVIEW §4 exactly.
- Every alert has `evidence` and `source`.
- Privacy filter has a test for "psych data never reaches the Crew Deck".

# ASTRA-TWIN — Technical Overview (shared)

Read this first, then the file for your side:

- **Frontend:** [`FRONTEND.md`](FRONTEND.md)
- **Backend:** [`BACKEND.md`](BACKEND.md)

**Scope:** International Space Station (ISS) only. Moon and Mars are a future extension for the presentation (#16).
**Status:** written 2026-11-14 (hackathon day 1). Choices marked **PROPOSED** still need the team's agreement.

---

## 1. What we are building

A health-monitoring and decision-support tool for a 7-person ISS crew. It does not make medical decisions: it explains, warns and proposes, and the Crew Medical Officer (CMO) or the ground team decides.

| Part | Job |
|---|---|
| **Personal screen** | One astronaut: clickable body figure, indicators, plain-language explanations, measurement entry, daily check-in, reminders |
| **Crew Deck** | Whole crew at a glance; becomes the incident view in an emergency |
| **Wrist alert (simulated)** | Pushes a critical alert to crewmates ("C2 needs help, Node 2") |
| **Onboard Twin** | Immediate evaluation and emergency guidance; works without a ground link |
| **Earth Twin** | T+6 h and T+12 h forecast and plan; ground team can review and adjust |

**Modes:** Nominal → Alert → Emergency. (Conjunction mode is Mars-only: future extension.)

---

## 2. Architecture

```
┌───────── frontend/ (Tauri + React) ───────────────┐        ┌──────────── backend/ ────────────┐
│ Personal screen · Crew Deck · Emergency view      │  JSON  │ Data ingestion (OSDR, RadLab, …) │
│ Check-in · Reaction test · Environment · Twin     │ <────> │ Twin engine · Rules engine       │
│ mock/*.json until the backend is ready            │        │ Scenario engine · Privacy filter │
└───────────────────────────────────────────────────┘        │ data/cache/ (offline)            │
                                                             └──────────────────────────────────┘
```

- Frontend and backend live in separate folders, so the two developers never touch the same files.
- The frontend works on its own with mock JSON. The backend replaces the mocks later.
- The demo must work **without internet**: every external source is cached in `data/cache/`.

---

## 3. Folder layout and ownership

```
frontend/          ← frontend developer only
backend/           ← backend developer only
data/osdr/         ← raw NASA OSDR CSVs (read-only)
data/cache/        ← backend writes cached API responses here
docs/              ← shared; change through PR
```

| Area | Owner | Branch prefix |
|---|---|---|
| `frontend/` | frontend developer | `feat/frontend-*` |
| `backend/` | backend developer | `feat/backend-*` |
| `docs/`, `data/` | either, via PR | `docs/*`, `data/*` |

Rules from `CLAUDE.md` still apply: no push to `main`, one topic per branch, squash merge.

---

## 4. Shared data model

Both sides use these names and values. If one changes, update this section in the same PR.

### 4.1 Status levels

| Level | Colour token | Who decides | Meaning |
|---|---|---|---|
| `L0` | `--ok` | — | Nominal |
| `L1` | `--self` | astronaut | Self-care advice |
| `L2` | `--cmo` | CMO | Recommendation needs CMO approval |
| `L3` | `--ground` | CMO + flight surgeon | Ground is informed |
| `L4` | `--emergency` | CMO acts on protocol now | Emergency; ground informed, not awaited |

### 4.2 Core entities

| Entity | Key fields |
|---|---|
| `CrewMember` | `id` (C1–C7), `callsign` (fictional), `role` (`CMO1`, `CMO2`, `CREW`), `module`, `level` |
| `Baseline` | `crewId`, `source` (`OSD-575`/`OSD-569`), `analyte`, `value`, `unit`, `refLow`, `refHigh` |
| `Reading` | `crewId`, `metric`, `value`, `unit`, `timestamp`, `provenance`, `measuredHow` |
| `CheckIn` | `crewId`, `date`, mood/fatigue/stress/conflict (0–100), sleep hours and quality, workload, crew climate, MCC frustration, family, restorative activities, food |
| `Alert` | `id`, `crewId`, `level`, `title`, `explanation`, `recommendation`, `evidence`, `source`, `status` (`open`/`approved`/`rejected`) |
| `Forecast` | `crewId`, `horizonHours` (6/12), `metric`, `points[]`, `label: "modelled"` |
| `Environment` | `co2_mmHg`, `temp_C`, `rh_pct`, `doseRate_uGy_h`, `timestamp`, `provenance` |
| `Scenario` | `id`, `title`, `steps[]` |

### 4.3 Labels every value carries

- **`provenance`**: `real` (NASA data) or `simulated` (our model). The UI shows it next to the value.
- **`measuredHow`**: how it is measured on the ISS today: `live`, `periodic`, `sample-return`, `research-only`.

| Indicator group | `measuredHow` | Basis |
|---|---|---|
| Heart rate, SpO2, skin temperature, breathing | `live` | CSA Bio-Monitor (on ISS since Dec 2018): https://www.asc-csa.gc.ca/eng/sciences/bio-monitor.asp |
| Blood pressure | `periodic` | Bio-Monitor lists blood pressure; continuous cuffless BP is not validated (ESH 2023, doi:10.1097/HJH.0000000000003483). Label `simulated, cuff-calibrated`. |
| Sleep and activity | `live` | Actigraphy (Sleep-Long; Artemis II ARCHeR) |
| Mood, stress, cognition | `periodic` | VAS items, Reaction Self Test, WinSCAT monthly (`PSYCHOLOGY_AND_LONG_DURATION.md`) |
| Food intake | `periodic` | ISS Food Intake Tracker app (ESA) |
| Blood panel (CBC/CMP) | `sample-return` | Samples are frozen and analysed on Earth (NTRS 20090014816); on-board analysers are tech demos (CSA Bio-Analyzer 2019, rHEALTH 2022) |
| Gut microbiome, chromosomes | `research-only` | Twins Study; no in-flight measurement |
| Cabin CO2, temperature, humidity | `live` | OSDR EDA; limits in OCHMO-TB-004 |
| Radiation dose | `live` | ISS-RAD, TEPC, REM (RadLab) |

### 4.4 Crew identities

- Real Inspiration4 baselines (C001–C004) are mapped to **fictional callsigns**. Never show real names.
- C5–C7 use modelled baselines and are labelled `simulated`.

---

## 5. Rules that apply to both sides

1. **No unsourced numbers.** Every threshold comes from `CLAUDE_HAFIZASI.md` §4.1, `NASA_DATA_SOURCES.md` or `PSYCHOLOGY_AND_LONG_DURATION.md`. If it is not there, it is not in the code.
2. **Never use** the values in the "do not use in code" table (`CLAUDE_HAFIZASI.md` §4.1).
3. **Medication** only from the 2023 ISS formulary (Diaz 2024), pending decision #11. Always shown with evidence and source; always needs CMO approval.
4. **Privacy:**
   - Psychological check-in data is **never** sent to the Crew Deck.
   - Crew Deck shows medication details only to CMOs, or when the astronaut opts in, or in an emergency (allergies and current medication only).
   - Crew climate appears only as an anonymous aggregate with a minimum-response threshold (our design choice).
5. **Disclaimer** always visible: "Simulation — not medical advice."
6. **English only** in the UI, code, comments and commits.
7. **NASA data only** (team rule). Partner instruments marked ⚠️ in `NASA_DATA_SOURCES.md` §4.2 (e.g. DosTel) are not used until #13 is decided.

---

## 6. Scenarios (both sides implement their half)

| ID | Title | Priority | Key sources |
|---|---|---|---|
| `chest-pain` | Crewmate chest pain and collapse | **MVP** (challenge video; Crew-11, Jan 2026) | Hinkelbein 2020 CPR guideline; Diaz 2024 |
| `co2` | Cabin CO2 build-up | MVP | OCHMO-TB-004 |
| `spe-iss` | Solar particle event on the ISS, Sep 10, 2017 | Target | RadLab `RAD-*-pre` |
| `bad-news` | Bad news from home | Target | Picano 2024; OCHMO-TB-016 |
| others | Dehydration/hypoglycemia, sleep disruption, landing orthostatic intolerance | Extra | `ARASTIRMA_RAPORU.md` |

Details: [`BACKEND.md`](BACKEND.md) §5 (logic) and [`FRONTEND.md`](FRONTEND.md) §6 (screens).

---

## 7. Open decisions

| # | Decision | Affects |
|---|---|---|
| #24 | Backend: **Rust** ingest CLI + **Java 21 / Spring Boot 4.1** API, joined only by `data/cache/*.json` (**DECIDED**, see `BACKEND.md` §1) | backend |
| #35 | Frontend: **Tauri v2** desktop app with **React 19 + TypeScript + Vite**, Tailwind + shadcn/ui + Motion, Recharts (**DECIDED**, see `FRONTEND.md` §1) | frontend |
| — | Exact endpoint paths and JSON shapes | both: agree before wiring |
| #11 | ISS medication list (Diaz 2024) | recommendation cards |
| #12 | NOAA-derived DONKI fields | space-weather panel |
| #13 | Partner and commercial data | radiation, baselines |

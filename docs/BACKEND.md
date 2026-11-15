# ASTRA-TWIN — Backend

Owner: backend developer. Shared rules and data model: [`TECH_OVERVIEW.md`](TECH_OVERVIEW.md).
The backend turns NASA data into clean JSON, runs the twin and rules engines, drives scenarios and enforces privacy.

---

## 1. Stack (DECIDED, #24)

Two parts in two languages. They never call each other: the only contract between them is the JSON files in `data/cache/`.

| Part | Language | Job | Libraries |
|---|---|---|---|
| `backend/ingest/` | **Rust** (stable, edition 2024) | Command-line tool. Reads the OSDR CSVs and study metadata, fetches the ISS dose (CDAWeb) and EDA, transcribes the IMM table, writes `data/cache/*.json` | `csv`, `serde`, `serde_json`, `reqwest` (blocking, rustls), `clap`, `anyhow` |
| `backend/api/` | **Java 21, JDK only** (#33): the JDK HTTP server and an in-project JSON reader and writer. Spring Boot 4.1 (#24) is deferred until the API moves to a server | HTTP API. Loads `data/cache/*.json` at start-up; runs the twin, rules and scenario engines and the privacy filter | none (`javac`, `jar`; tests with a small in-project runner) |

Both parts:

- **Only `ingest` goes online.** The API never calls the internet, so the demo always runs offline.
- CORS: the API sends `Access-Control-Allow-Origin: *`, so the Vite dev server and the Tauri webview can call it; with `--ui frontend/dist` the API serves the built frontend itself (same origin).
- Routes and JSON shapes: `backend/README.md` (the agreed contract, #56).
- Pin dependency versions: commit `Cargo.lock`; fix the Spring Boot and plugin versions in the build file.

```
backend/
  ingest/                       ← Rust (cargo)
    src/osdr.rs                 ← OSD-575, OSD-569 + study metadata → baselines.json
    src/iss_dose.rs             ← ISS TEPC dose (CDAWeb) → iss_dose.json
    src/eda.rs                  ← cabin CO2, temperature, humidity → eda_iss.json
    src/imm.rs                  ← IMM table → imm_leo.json
  api/                          ← Java (Spring Boot)
    src/main/java/…/cache/      ← loads data/cache/*.json
    src/main/java/…/twin/       ← baselines, z-scores, forecasts
    src/main/java/…/rules/      ← thresholds → status levels and alerts
    src/main/java/…/scenario/   ← scripted scenario timelines
    src/main/java/…/privacy/    ← filters what each role/screen may see
    src/main/java/…/web/        ← controllers
```

### 1.1 Cache file contract

Every file in `data/cache/` uses the same envelope. `items` use the entity shapes and field names in TECH_OVERVIEW §4.2, so the API can read them without renaming.

```json
{ "source": "<URL or repo path>", "fetchedAt": "<ISO-8601 UTC>", "items": [ ] }
```

| File | `items` | Source |
|---|---|---|
| `baselines.json` | `Baseline` (one item per crew × analyte × `timepoint`, with `mission`) | `data/osdr/OSD-575_*`, `data/osdr/OSD-569_*`; `mission` from the OSDR study metadata |
| `iss_dose.json` | `Environment` (`doseRate_uGy_h`, `doseEqRate_uSv_h`, `instrument`) | CDAWeb `ISS_DOSANL_TEPC` (NASA SRAG), Sep 4–19, 2017, 1-minute |
| `eda_iss.json` | `Environment` (`co2_mmHg`, `temp_C`, `rh_pct`) | OSDR EDA |
| `imm_leo.json` | condition probabilities | `NASA_DATA_SOURCES.md` §10.1 |

If one side needs a field the other does not write, change this table and TECH_OVERVIEW §4 in the same PR.

**Units (#30):** absorbed dose is μGy/h, dose equivalent is μSv/h (ICRP-60). The source gives μGy/min and μSv/min; `ingest` multiplies by 60. Never derive Sv from Gy with an assumed quality factor: if a source has no dose equivalent, leave `doseEqRate_uSv_h` empty.

### 1.2 Object-oriented design (DECIDED, #30)

Every behaviour that has variants is an interface with one class per variant. Engines hold a list of these objects and do not know the concrete classes, so a new rule or scenario is a new class, not an edit to the engine.

| Package (`api`) | Interface | Implementations (MVP first) | Used by |
|---|---|---|---|
| `domain` | — | Java records for the TECH_OVERVIEW §4.2 entities (`CrewMember`, `Baseline`, `MissionTag`, `Reading`, `Alert`, `Forecast`, `Environment`, `CheckIn`) | everything |
| `rules` | `Rule` → `List<Alert> evaluate(RuleContext)` | `GlucoseRule`, `Co2Rule`, `SpO2Rule`, `DoseRateRule`, `ZScoreRule`, `PsychTrendRule` | `RulesEngine` |
| `twin` | `ForecastModel` → `Forecast forecast(CrewMember, int horizonHours)` | `SleepDebtModel`, `HydrationModel`, `CumulativeDoseModel`, `TemperatureDriftModel`, `EnergyIntakeModel` | `TwinEngine` (with `BaselineStatistics`) |
| `scenario` | `Scenario` → `id()`, `title()`, `steps()` | `ChestPainScenario`, `Co2Scenario`, `SpeIssScenario`, `BadNewsScenario` | `ScenarioEngine` (start, step, reset) |
| `privacy` | `ViewPolicy` → filters one entity type for a `Viewer` (`SELF`, `CMO`, `CREW_DECK`, `EMERGENCY`) | one policy per row of §6 | `PrivacyFilter` |
| `store` | `ReadingRepository`, `AlertRepository`, `CheckInRepository` | `InMemory…` now; a database implementation when the API moves to a server | engines, controllers |
| `cache` | `CacheLoader` | reads `data/cache/*.json` once at start-up | `store`, `twin` |
| `web` | — | REST controllers; demo console (§1.3) | frontend, demo |

In `ingest` (Rust), each source is a type that implements a `Source` trait (`name()`, `build(offline) -> CacheFile`): `OsdrBaselines`, `IssDose`, `Eda`, `Imm`.

### 1.3 Runtime and demo console (DECIDED, #30)

- **Now:** local on Ubuntu 24.04. `ingest` runs once and its output is committed, so other machines need only Java 21 to run the API.
- **Later:** the same API on a server; only the `store` implementations change.
- **Demo console:** the API serves a plain page at `http://localhost:8080/demo/` (`src/main/resources/static/demo/`). It lists the sample scenarios, starts, steps and resets them, shows crew levels and alerts, and switches the viewer (self / CMO / Crew Deck / emergency) to show the privacy filter. It is a backend test and demo tool, not the product UI; it does not touch `frontend/`.

---

## 2. Data ingestion

| Source | What | How | Reference |
|---|---|---|---|
| **OSDR OSD-575** (CMP, cardio, immune) | Real crew baselines: glucose, Na, K, BUN, creatinine, IL-6, CRP | Read `data/osdr/OSD-575_*_TRANSFORMED.csv` | `NASA_DATA_SOURCES.md` §2.1 |
| **OSDR OSD-569** (CBC) | Haematology baselines, 4 crew × 7 time points (L-92 … R+194) | Read `data/osdr/OSD-569_*` | same |
| **CDAWeb ISS TEPC** (NASA SRAG) | ISS absorbed dose and dose equivalent, 1-minute | `https://cdaweb.gsfc.nasa.gov/hapi/data?id=ISS_DOSANL_TEPC&time.min=2017-09-04T00:00:00Z&time.max=2017-09-20T00:00:00Z&parameters=DOSE,DOSE_EQ60,TIME_INTERVAL&format=csv` | §4.3 |
| **OSDR RadLab** (cross-check only) | ISS absorbed dose only (μGy/h, daily for NASA instruments); no dose equivalent for any ISS instrument | `https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Earth&timestamp%3E=2017-09-04&timestamp%3C2017-09-20&absorbed_dose_rate&format=csv` | §4.2, §11.3 |
| **OSDR study metadata** | Mission name, start, end, link for the `mission` tag | `https://osdr.nasa.gov/osdr/data/osd/meta/575` → `studies[0].comments` ("Mission Name", "Mission Start", "Mission End", "Mission Link") and `description` | §2.1 |
| **OSDR EDA** | ISS cabin CO2, temperature, humidity | https://visualization.osdr.nasa.gov/eda (app was unreachable on 2026-11-14; retry and cache) | §4.1 |
| **NASA IMM** | LEO 6-month condition probabilities | Transcribe the table in `NASA_DATA_SOURCES.md` §10.1 into `data/cache/imm_leo.json` | §10.1 |

**CSV gotchas:**

- Files start with a UTF-8 BOM. In `ingest`, check that the first header name does not start with `\u{feff}` after parsing, and strip it if it does.
- OSD-569/575 are **pre- and post-flight only**. They give baselines, not in-flight values.
- Use only crews C001–C004 and map them to fictional callsigns (TECH_OVERVIEW §4.4).
- Every Inspiration4 baseline carries the `mission` tag (#30). OSDR metadata for OSD-575: "SpaceX Inspiration4", 09/16/2021–09/18/2021, "a 3-day mission with four private astronauts to low Earth orbit".

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
| Cumulative dose | Sum of the TEPC dose equivalent (normal days 0.77–0.94 mSv/day in Sep 2017) against the 600 mSv career limit. Label it "instrument dose equivalent (ICRP-60), not effective dose"; show NASA's ISS reference 0.2–0.5 mSv/day effective next to it | CDAWeb `ISS_DOSANL_TEPC` (#30); Simonsen 2025 (NTRS 20250006168) |
| Temperature drift | +~1 °C over ~2.5 months | Stahn 2017 |
| Energy intake | ISS crews eat ~74–80% of estimated need | NTRS 20060049121; Smith 2005 |

When a new reading arrives, compare it with the previous forecast and store the error (for the "forecast vs actual" view).

---

## 4. Rules engine

Input: readings, check-ins, environment. Output: `Alert` objects with level L0–L4 (TECH_OVERVIEW §4.1).

| Indicator | Rule | Source |
|---|---|---|
| Glucose | <70 mg/dL → L1; <54 → L3; confusion reported → L4. Action: 15 g fast carbs, re-check in 15 min. `evidence` must say: "Team assumption: in-flight glucose measurement is assumed; not verified for the ISS." | ADA 2025 (doi:10.2337/dc25-S006); assumption #30 |
| Cabin CO2 | 1-h average >3 mmHg → L1; ≥4.5 mmHg or headache → L2; ≥6.8 mmHg → L3. Also flag 7-day average >2.5 mmHg (headache risk) | 3 mmHg and 2.5 mmHg: OCHMO-TB-004 (NASA-STD-3001 V2 6004); headache at 2.8–4.5 mmHg: ARASTIRMA_RAPORU §4; 6.8 mmHg: NASA contingency proposal "6.8–10 mmHg for ≤4 h", not yet a standard ([ICES-2025-402](https://ntrs.nasa.gov/api/citations/20250006273/downloads/ICES-2025-402.pdf)) |
| SpO2 | <90% → L4 | <90% = hypoxaemia (ARASTIRMA_RAPORU §4, SpO2 row). NASA's own limit is PIO2 ≥122 mmHg (OCHMO-TB-003) |
| Dose rate | 24-h rolling mean of `doseEqRate_uSv_h` > 1.5× the preceding 7-day median → L2 (shelter advice, ground informed). Never alert on single hours: South Atlantic Anomaly passes reach 183 μSv/h on normal days | our design (#30). Calibration from Sep 2017 TEPC data: normal days 0.86–1.04×, Sep 11 1.90× |
| Lab analytes, HR, BP | |z| ≥ 2 vs personal baseline → L1; persists or combined with symptoms → L2 | our design (no universal in-flight threshold) |
| Psych check-in trend | 3 check-ins in a row with mood ≤ 40/100 or stress ≥ 70/100 → private L1 prompt "discuss it at your next PPC, or contact your flight surgeon now" (`PsychTrendRule`) | our design; SELF only; never changes the crew level |

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
Replay the CDAWeb ISS TEPC 1-minute data, Sep 4–19, 2017 (`iss_dose.json`), at demo speed. The dose rule fires on Sep 11 (1.71 mSv/day, 1.90× the 7-day median) → shelter advice.

### `bad-news` (Target)
Inject a family-event flag; personal support plan; Earth Twin 24–72 h plan for ground approval; Crew Deck sees only "reduced tasking requested".

---

## 6. Privacy filter (server-side)

Enforce here, not only in the UI:

| Data | Self | CMO | Ground (flight surgeon)* | Other crew (Crew Deck) | Emergency |
|---|---|---|---|---|---|
| Status level, module | ✔ | ✔ | ✔ | ✔ | ✔ |
| Vitals detail | ✔ | ✔ | ✔ | — | ✔ |
| Medication | ✔ | ✔ | ✔ | only if opted in | allergies + current meds |
| Psych check-in, psych alerts | ✔ | — (shared to BHP by the astronaut) | — | **never** | **never** |
| Support companion conversation (§10) | ✔ | **never** | **never** | **never** | **never** |
| Crew climate | own answers | aggregate | aggregate | aggregate, min. response threshold | — |

\* **Provisional (#56):** the frontend has a "Ground (flight surgeon)" role; the backend gives it the CMO tier without psychological data until the team decides. Psych alerts never change the crew level, because the level is visible on the Crew Deck. CMO and ground may approve or reject alerts.

Basis: NASA-STD-3001 Vol 1 V1 6002/6003/6010/6011 (private medical and psychological communications); AMIS 2024 (NTRS 20240004505); IOM *Safe Passage*.

---

## 7. Cache and offline

- `ingest` (Rust) writes everything to `data/cache/*.json` in the envelope from §1.1.
- To refresh data, run `ingest` again. On any network error it keeps the existing cache file and reports which source failed.
- The API (Java) only reads the cache at start-up and never goes online.
- Commit cache files that the demo needs (small JSON only).

---

## 8. Definition of done (backend)

- Starts with no internet and serves all MVP data from `data/cache/`.
- JSON field names match TECH_OVERVIEW §4 exactly.
- Every alert has `evidence` and `source`.
- Privacy filter has a test for "psych data never reaches the Crew Deck".

---

## 9. Live telemetry stream (CSV, #46)

Flowing data in CSV, in and out. Wearable values are **simulated** in the style of the CSA Bio-Monitor (TECH_OVERVIEW §4.3).

| Part | Class | Job |
|---|---|---|
| Source (interface) | `TelemetrySource` | One call = one tick of rows |
| Simulator | `SimulatedTelemetrySource` | Seeded, mean-reverting random walk of `hr`, `spo2`, `temp_c` around each member's latest reading; scenario and manual readings become the new target, so the stream never overwrites them |
| Replay | `CsvReplaySource` | Replays any CSV in `data/stream/` with the header below; each distinct timestamp is one tick |
| Streamer | `TelemetryStreamer` | Pulls a tick at a fixed rate, feeds the station (rules re-run), appends to `data/stream/out/telemetry-<UTC>.csv`, pushes rows to live subscribers |

- **CSV columns** (the `Reading` entity): `timestamp,crewId,metric,value,unit,provenance,measuredHow`.
- **Routes:**
  - `GET /api/stream/telemetry.csv` is a live, chunked CSV until the stream stops (`&limit=N` to stop earlier).
  - `GET /api/stream/recent.csv?n=100` is a snapshot.
  - `GET /api/stream/status`.
  - `POST /api/stream/start?source=sim|replay&file=<name>.csv&intervalMs=1000` and `POST /api/stream/stop`.
- **Privacy:** every row passes the vitals policy (§6). CMO and the emergency view see everyone, SELF only itself, and the Crew Deck gets 403.
- **Memory:** the reading store keeps the last 20,000 readings plus the latest value of every crew × metric, so slow metrics such as glucose never drop out.
- **Sample:** `data/stream/sample_telemetry.csv` has 20 minutes × 7 crew. C5's simulated SpO2 drop to 88% fires the SpO2 rule during a replay.

---

## 10. Support companion (local LLM, #47)

A private peer-support chat for one astronaut. It is not a therapist, gives no diagnosis and names no medication. It runs on a **local** model through Ollama (`phi3.5` by default), so it works without internet and the conversation never leaves the station.

Every message goes through these steps in order:

| Step | Class | What it does |
|---|---|---|
| 1. Safety gate | `SafetyGate` | Crisis wording (EN/TR, accent-insensitive) gets a fixed reply: "contact your flight surgeon or behavioral health provider now, do not stay alone". Questions about medication, doses or diagnosis are redirected to the CMO. **The model is not called** in either case. |
| 2. Responder | `SupportResponder` → `OllamaResponder` | Calls `http://127.0.0.1:11434/api/chat` with the conditioning prompt, a worked example and the last 4 messages. |
| 3. Output filter | `OutputFilter` | Rejects replies that name a drug or a dose, give a diagnosis, or claim to be human. Removes bracketed asides and cuts an unfinished last sentence. |
| Fallback | `GuidedResponder` | Rule-based reply, used when the model is off, unreachable or failing, or its reply was filtered. |

- **Conditioning prompt** (`SupportPrompt`):
  - Always English, 2–4 sentences, under 70 words.
  - Reflect what the crew member said, then offer one or two options, then at most one question.
  - Use only the support options sourced in `PSYCHOLOGY_AND_LONG_DURATION.md` §2–§3: PPC with the behavioral health provider every two weeks; family conference weekly and special conference monthly; Cupola, Earth photography and journaling; exercise; sleep window and light; workload with the CMO and mission control; one-to-one talks.
  - The model may never schedule, remind or contact anyone.
- **Privacy:** conversations are psychological data. Only `viewer=SELF&crewId=<own id>` may read or write them; CMO, Crew Deck and emergency get 403. They are kept in memory, never logged, and the astronaut can clear them.
  - The check-in is not sent to the model: in testing, phi3.5 repeated it back in the third person.
- **Routes:**
  - `GET /api/support/status` (public: engine and availability).
  - `GET /api/support/conversation`, `POST /api/support/message` with body `{"text"}`, and `POST /api/support/clear` (all SELF only).
- **Options:** `--llm-url http://127.0.0.1:11434`, `--llm-model phi3.5`, `--llm off` (guided only).
- **Guards added after evaluation (#59):**
  - **Prompt requests:** "system prompt", "ignore previous instructions" and similar get a fixed reply.
  - **Turkish input** goes to the guided responder; phi3.5 misread it.
  - **Retry:** a rejected reply is retried once with another seed (seed 42 + attempt, so results are reproducible).
  - **Output filter** also rejects:
    - offers to schedule, remind or contact anyone (rule 7);
    - non-English text and garbled tokens;
    - mix-ups of the sourced facts, e.g. a "weekly special conference" or "outdoor" activities;
    - prompt leaks: copied rule 6-grams or paraphrases.
  - **Invariant:** every guided reply passes the output filter (unit test).
- **Evaluation:** `backend/api/eval.sh` runs `eval/support_cases.json` against the real model and writes `eval/REPORT.md`.
  - 34 cases: crisis in English and Turkish, false-positive traps, medication and diagnosis, adversarial, everyday, Turkish input.
  - Last run: **34/34**, reproducible across runs.
  - 18 fixed safety replies (the model is not called), 13 model replies (mean 2.9 s), 3 guided replies.
- **Open decision (needs-human):** a crisis message currently gets the fixed reply and nothing else. Whether it should also alert the CMO or the flight surgeon, overriding the privacy rule for safety, is a team decision.

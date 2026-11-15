# ASTRA-TWIN backend

Two parts that never call each other; the JSON files in `data/cache/` are the only contract (`docs/BACKEND.md` §1).

| Part | What | Needs |
|---|---|---|
| `ingest/` | Rust tool: raw NASA files → `data/cache/*.json` | Rust (only to rebuild the cache) |
| `api/` | Java API, rules, scenarios, privacy filter, demo console | Java 21 only — no Maven, no downloads |

The cache files are committed, so **to run the demo you only need Java 21**.

**Full app (frontend + backend):** `cd frontend && npm ci && npm run build` once (needs Node.js), then `backend/api/run.sh` serves the app at **http://127.0.0.1:8080/** and the demo console at `/demo/`.

## Run the demo

```sh
backend/api/run.sh            # builds if needed, then serves http://127.0.0.1:8080
```

Open **http://127.0.0.1:8080/demo/**:

- **Scenarios:** *Start* a scenario, then *Next step ▶*; *Stop and reset* returns to the nominal station.
  - `chest-pain` — a crewmate collapses: L4 broadcast, roles, microgravity CPR checklist, emergency card.
  - `co2` — synthetic CO2 ramp through the NASA limits: L1 → L2 (headache) → L3 → recovered.
  - `spe-iss` — real NASA SRAG ISS TEPC data, Sep 2017: the dose-rate rule fires on Sep 11.
- **Viewer** (top right): switch between CMO, Crew Deck, Emergency and Self to see the privacy filter. The Crew Deck never sees vitals or psychological check-ins.
- **Add a reading:** e.g. glucose 52 for C3 → L3 alert; then approve or reject it as the CMO.
- **Live telemetry (CSV stream):** *Start simulated* streams wearable values (heart rate, SpO2, temperature) for the whole crew every second; *Replay sample CSV* plays `data/stream/sample_telemetry.csv`, where C5's SpO2 drops and the rule fires. The panel reads the live CSV line by line; each run is also logged to `data/stream/out/`.
- **Support companion (Self view only):** switch the viewer to *Self*, pick a crew member and write how you feel. Replies come from a local model through Ollama (`phi3.5`); crisis wording and medication questions get a fixed reply without the model, and crisis wording also alerts the CMO, without the message (#51); if Ollama is not running, a rule-based fallback answers. Setup: install Ollama, then `ollama pull phi3.5` (about 2.2 GB, once).
- **Personal baseline & twin:** real pre-flight baselines with the commercial-mission tag (SpaceX Inspiration4, NASA OSDR), and the T+6/T+12 dose and sleep-debt forecasts (sleep debt is built from check-ins, so only the astronaut sees it).

Every value is labelled `real` or `simulated`. *Simulation — not medical advice.*

## Tests

```sh
backend/api/test.sh           # 13 tests, including "psych data never reaches the Crew Deck"
```

## Rebuild the cache (optional, needs Rust)

```sh
cargo run --release --manifest-path backend/ingest/Cargo.toml -- all
```

It reads the raw files listed in `data/raw/README.md` and does not use the network.

## API

All routes take `?viewer=CMO|GROUND|CREW_DECK|EMERGENCY|SELF` (and `&crewId=C1` for `SELF`). Without a viewer the most restricted view, `CREW_DECK`, applies. `GROUND` (flight surgeon) is provisionally the CMO tier without psychological data.

| Method | Path | Returns |
|---|---|---|
| GET | `/api/health` | status and disclaimer |
| GET | `/api/station` | crew tiles, alerts, cabin and dose, scenario state (filtered for the viewer) |
| GET | `/api/scenarios` | scenario list |
| POST | `/api/scenarios/{id}/start` | scenario state after step 1 |
| POST | `/api/scenario/step` · `/api/scenario/stop` | next step · reset |
| POST | `/api/readings` | body `{"crewId","metric","value","unit"}`; returns the station view |
| POST | `/api/alerts/{id}/approve` · `/reject` | CMO or GROUND view only |
| GET · POST | `/api/checkins` (body `{"mood","fatigue","stress","conflict","sleepHours"}`) | **SELF only**; low trend → private PPC prompt |
| GET | `/api/crew/{id}/baselines` · `/forecasts` | personal baseline stats · T+6/T+12 forecasts (vitals policy; each forecast also uses its own data kind's policy, so sleep debt is SELF only) |
| GET | `/api/environment/dose` | hourly ISS dose series currently loaded |
| GET | `/api/imm` | NASA IMM LEO 6-month probabilities |
| GET | `/api/stream/telemetry.csv` | live CSV stream (chunked; `&limit=N`); vitals policy per row, Crew Deck gets 403 |
| GET | `/api/stream/recent.csv?n=100` · `/api/stream/status` | recent rows as CSV · stream status |
| POST | `/api/stream/start?source=sim\|replay&file=<name>.csv&intervalMs=1000` · `/api/stream/stop` | start or stop the stream |
| GET | `/api/support/status` | companion engine and availability (public) |
| GET · POST | `/api/support/conversation` · `/api/support/message` (body `{"text"}`) · `/api/support/clear` | **SELF only** (`viewer=SELF&crewId=Cx`); others get 403 |

Options: `java -jar backend/api/out/astra-twin-api.jar [--port 8080] [--host 127.0.0.1] [--data data/cache] [--stream sim]` (`--stream sim` starts the simulated stream at launch) [--llm-url http://127.0.0.1:11434] [--llm-model phi3.5] [--llm off] [--ui frontend/dist] (`--ui` also serves the built frontend at `/`).

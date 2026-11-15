# ASTRA-TWIN — Frontend

Owner: frontend developer. Shared rules and data model: [`TECH_OVERVIEW.md`](TECH_OVERVIEW.md).
The demo video (≤30 s) and slides (≤7) are recorded from this UI, so polish here is the presentation.

---

## 1. Stack (DECIDED 2026-11-14, #35)

Decided question by question with the frontend owner.

| # | Need | Decision | Why |
|---|---|---|---|
| 1 | Platform | **Tauri v2 desktop app** (Ubuntu 24, Windows, macOS) | Runs locally in its own window, no browser and no internet; Tauri's shell is Rust, which the team already uses in the backend |
| 1 | UI framework | **React 19 + TypeScript + Vite** inside Tauri | Same code runs in Tauri and, as plan B, in a browser |
| 2 | Visual language | **Hybrid "calm mission control"** | Dark base and cyan accents (reference image, deck); data areas plain with large numbers; glow and motion only on the body figure and on alerts |
| 3 | Components and styling | **Tailwind CSS 4 + shadcn/ui (Radix) + Motion** | shadcn copies component code into the repo, so every detail is ours; Radix gives keyboard and screen-reader support; Motion for alert and transition animation |
| 4 | Body figure | **2.5D SVG** | Layered lines, soft glow, light parallax, front/back flip; clickable and keyboard-focusable regions; no 3D model licence or NASA-logo risk |
| 5 | Charts | **Recharts** via shadcn chart components | Same theme as the rest of the UI; baseline bands, dashed forecasts, real vs simulated line styles |
| 6 | Navigation | **Personal screen first** | Matches the challenge goal ("assess their own health"); see §1.2 |
| — | State | React context (Zustand if it grows) | Few global objects: crew, alerts, environment, role |
| — | Sound | Web Audio API | No audio files; mute switch |

**Backend link:** the Java API runs on `localhost` (see `BACKEND.md`); the UI calls it over HTTP. Until it is ready, mock JSON (§7).

**Plan B (demo safety):** if a Tauri build fails on a machine, the Java API serves the same `vite build` output on `localhost` and the UI runs in a browser tab. No code change.

**Versions:** pin exact versions in `frontend/package.json` when the project is created (no `^`/`latest`). Latest on 2026-11-14 for reference: `@tauri-apps/cli` 2.12.4, `react` 19.3.1, `vite` 8.3.8, `tailwindcss` 4.3.5, `shadcn` 4.23.0, `motion` 14.14.0, `recharts` 3.10.1. Rust stable 1.100.0.

### 1.1 Build prerequisites per OS

| OS | Install once | Source |
|---|---|---|
| **Ubuntu 24** | `sudo apt update && sudo apt install libwebkit2gtk-4.1-dev build-essential curl wget file libxdo-dev libssl-dev libayatana-appindicator3-dev librsvg2-dev`, then Rust (rustup) and Node.js | https://v2.tauri.app/start/prerequisites/ |
| **macOS** | Xcode Command Line Tools, Rust (rustup), Node.js | same |
| **Windows** | Microsoft C++ Build Tools and WebView2 (preinstalled on Windows 10 1803+ and 11), Rust (rustup), Node.js | same |

Each OS builds its own installer (`npm run tauri build` on that OS). Commit no build output.

### 1.2 App shell and navigation

- **Top status bar:** station time · Earth link status · open alert count · **role switcher** (Astronaut C1–C7 / CMO / Ground).
  - The role switcher is a **simulation** (no real login); the UI says so.
  - Switching role changes what is visible, enforcing `TECH_OVERVIEW.md` §5 rule 4 on screen: the astronaut sees their own psych check-in; the CMO sees medication and detailed values; psych data never appears on the Crew Deck for any role.
- **Left rail:** Personal · Crew Deck · Environment · Twin · Check-in.
- **Start screen:** the selected astronaut's Personal screen (body figure, indicators, next measurement reminder).
- **L4 emergency:** takes over the full screen from any screen.

```
frontend/
  src-tauri/     ← Tauri (Rust) shell and config
  src/
    api/         ← client; switches between mock and backend (one flag)
    mock/        ← JSON matching TECH_OVERVIEW §4
    components/  ← BodyFigure, IndicatorCard, StatusBadge, AlertCard, RoleSwitcher, …
    components/ui/ ← shadcn/ui components (copied in, owned by us)
    screens/     ← Personal, CrewDeck, Emergency, Environment, Twin, CheckIn, ReactionTest
    theme/       ← Tailwind theme tokens (§3)
```

---

## 2. Screens

| Screen | Content | Priority |
|---|---|---|
| **Personal** | Body figure; indicator cards; "What does this mean?"; next measurement reminder; disclaimer | MVP |
| **Crew Deck** | 7 crew tiles: callsign, role, module, status level; station module map; open alerts | MVP |
| **Emergency view** | Who, where, role assignment, step checklist, emergency card (allergies, current meds) | MVP |
| **Measurement entry** | HR, BP, SpO2, temperature, glucose, fluids, symptoms | MVP |
| **Environment** | CO2, temperature, humidity, dose rate; real-data badges | MVP |
| **Twin** | T+0 / T+6 / T+12 slider; forecast curves labelled "modelled" | Target |
| **Daily check-in** | ~2-minute form (§5) | Target |
| **Reaction test** | 3-minute PVT-style test, personal-best and streaks | Target |
| **PPC prep** | 2-week trend summary for the next private conference | Extra |

### 2.1 Personal screen (DECIDED 2026-11-14, #40)

Decided question by question with the frontend owner.

| # | Topic | Decision |
|---|---|---|
| 1 | Layout | **Today strip** on top; **body figure** left; **detail panel** right. With no region selected, the panel shows the day summary. An alert lights its region and opens the panel on it. |
| 2 | Body figure | **3D anatomy** (revised 2026-11-14 after review of the 2.5D SVG): NIH **HuBMAP 3D Reference Organ Set, United Male v1.7** (CC BY 4.0), slimmed to one mesh per region (`frontend/public/models/body.glb`, ~0.7 MB) by `frontend/scripts/slim_body_model.mjs`; shown with three.js / react-three-fiber. Drag to rotate, scroll to zoom, click an organ; selecting an organ glides the camera to it. Translucent skin = whole-body region. Region chips under the viewer give the same selection for keyboard users. Credit line under the viewer; attribution in `frontend/public/models/ATTRIBUTION.md`. |
| 3 | Detail panel | **Explanation first:** status sentence (colour + icon + words) → values with personal baseline and `real`/`simulated` + "how measured on ISS" badges → trend (baseline band; dashed line if simulated) → "What this means" → "What to do" (a CMO approval card from L2) → collapsible "How it’s measured and sources" (links open in the system browser). |
| 4 | Gamification | **Daily task ring + streak + personal best.** No points or badges: we reward the behaviour (measuring, checking in), not the health value. |

Risk: WebGL performance in Tauri on Ubuntu (WebKitGTK) is not tested yet; if the 3D view fails, the app shows a message and the region chips still work.

Data: blood values are real NASA OSDR OSD-575 baselines (C1–C4), generated by `frontend/scripts/make_baselines.py` into `frontend/src/mock/baselines.json`. Every other value comes from one simulated profile per crew member with one health story each (`src/data/crewProfiles.ts`, #71); the backend seeds the same values. All are labelled simulated. Thresholds not taken from a source are marked "our design" in the text.

### 2.2 Crew Deck and emergency mode (DECIDED 2026-11-14, #42)

| # | Topic | Decision |
|---|---|---|
| 1 | Layout | **Station map first**, 7 crew tiles below (status, role, module only). |
| 2 | Map | **2D schematic** of the main pressurized modules (layout simplified from the NASA ISS Reference Guide, NP-2015-05-022-JSC) with crew pins, plus a small rotating **NASA 3D Resources ISS model** (free and without copyright; converted to meshopt by `frontend/scripts/convert_iss_model.mjs`). Label: crew location is a simulated concept (wearable tag). |
| 3 | Emergency | **Full screen from any screen.** Alert (who, where, elapsed time) → Coordinate (system proposes roles: two CMOs lead and assist per NASA-STD-3001; nearest crew by hatch count fetches the life-support pack and defibrillator; next nearest runs comms; CMO confirms or changes) → Act (microgravity CPR checklist from Hinkelbein 2020, timestamped steps; emergency card with allergies and current medication only; "ground informed" band). The map zooms to the patient and responders and draws their routes. Started by "Run scenario: crewmate collapse (simulated)". **Alarm sound** (Web Audio, 800/1000 Hz alternating every 0.4 s) plays until the CMO confirms the roles; a rising 440 → 880 Hz tone confirms; a mute button is always in the header. |

### 2.3 Check-in and reaction test (DECIDED 2026-11-14, #48)

| # | Topic | Decision |
|---|---|---|
| 1 | Daily check-in | **8 short steps**, one topic each, progress bar, Back / Skip / Next (items from `PSYCHOLOGY_AND_LONG_DURATION.md` §4.1; each step names its basis). Summary: "Check-in done", the Today ring and streak update, supportive notes (our design, never diagnostic), and "Only you see this". |
| 2 | Reaction test | **Added now.** Modelled on NASA's Reaction Self Test (3-minute PVT-B on the ISS): standard 3 min, optional 30 s demo length clearly labelled; Space or click; median, responses, false starts, slow responses; compared with a simulated personal baseline; a better median updates the personal best. Labelled "Measured in this app". |

Privacy: CMO and ground roles see only a notice that check-in data is private to the astronaut (NASA-STD-3001 V1 6010/6011).

### 2.4 Twin screen (DECIDED 2026-11-14, #54)

**Slider + forecast charts + Earth Twin plan.** A "look ahead" slider (T+0 → T+12, quick T+0/T+6/T+12 buttons) moves a cursor across four charts: fluid deficit vs 2.5 L/day, sleep debt vs the 8.5 h schedule (Barger 2014), cumulative mission dose (ISS 0.2–0.5 mSv/day, Simonsen 2025), core temperature (Stahn 2017). Actual = solid, forecast = dashed with a widening band; a forecast that crosses a threshold (our design) warns early ("reaches … at T+9 h"). Right: the Earth Twin plan from the flight surgeon with Accept / Discuss at PPC (the astronaut or CMO decides), and "Forecast vs actual" (forecast made 6 h ago vs now) as the visible test of forecast accuracy. Everything is labelled "Modelled".

**With the backend (#55 + #62):** the dose chart is the real ISS TEPC series (actual = the last 12 hourly samples; forecast = the backend's last-24-h-mean projection; band = lowest to highest daily mean of the last 7 days), and its forecast-vs-actual row is computed from the same data. Sleep debt is the backend's forecast from the astronaut's own check-ins. Fluids, temperature and the plan stay simulated and labelled, built from the crew member's profile (#71). **Privacy:** sleep debt and the sleep-related plan items are psych data, so they appear only in the astronaut's own view, never for the CMO or ground (`BACKEND.md` §6). The real pre-flight baseline table sits below the charts.

---

## 3. Design tokens

Aesthetic reference: `57110f2f9bef1ff11b47b3e897d3eee2.jpg` (cyan wireframe astronaut on dark navy).

```css
:root {
  --bg: #0a0a0c;
  --info: #00d2ff;       /* lines, info, links */
  --ok: #00ff66;         /* L0 */
  --self: #ffd400;       /* L1 */
  --cmo: #ff8a00;        /* L2 */
  --ground: #ff0055;     /* L3 */
  --emergency: #ff0055;  /* L4, blinking */
}
```

Every status uses **colour + icon + text** (red/green may be confused by colour-blind users).

---

## 4. Body figure regions

| Region | Indicators | "What does this mean?" topic |
|---|---|---|
| Head / brain | sleep, mood, cognition (reaction test), CO2 symptoms | sleep loss and CO2 effects on decisions |
| Eyes | SANS reminder (periodic exam) | fluid shift and vision |
| Heart | HR, BP | why BP is lower in flight; landing orthostatic risk |
| Lungs | SpO2, breathing rate | oxygen and cabin atmosphere |
| Blood / metabolism | glucose, Na, K, BUN (real OSDR baselines) | 15-15 rule (ADA); hydration |
| Bones and muscles | exercise log; bone loss 1–1.5%/month (NASA) | why daily exercise is mandatory |
| Whole body | dose, temperature (+~1 °C drift, Stahn 2017) | ISS dose and career limit |

Each card shows: value, unit, personal baseline, trend, `real`/`simulated` badge, `measuredHow` badge, and a "?" with *how to measure / normal range / source link*.

---

## 5. Daily check-in form

From `PSYCHOLOGY_AND_LONG_DURATION.md` §4.1:

1. Mood, fatigue, stress, conflict: sliders 0–100
2. Sleep hours + quality 1–5
3. Workload, "I control my timeline": 1–5
4. Crew climate: 2 items + 1 team item
5. Frustration with mission control: 1–5
6. Family: days since last family conference; "important family event" toggle
7. Restorative activity: cupola / photography / exercise / journal
8. Food intake and variety: quick log

Rules: never show these answers on the Crew Deck; no diagnostic wording; escalation text "Discuss at your next PPC, or contact your flight surgeon now."

---

## 6. Scenario screens

### `chest-pain` (MVP, hero scenario)

1. Wrist alert animation on every crew tile → Crew Deck switches to Emergency view.
2. Module map highlights the patient's location (concept: wearable tag).
3. Role cards: CMO-1 leads, CMO-2 assists, one crew fetches the ALSP and defibrillator, one handles comms and log.
4. Checklist from Hinkelbein 2020 (doi:10.1186/s13049-020-00793-y): Evetts-Russomano CPR first, Reverse Bear Hug as fallback, CMRS restraint for ALS and defibrillation.
5. Emergency card opens (allergies, current medication).
6. "Ground informed" banner (ISS: seconds), but the crew does not wait.

### `co2` (MVP)
Environment panel curve from real EDA data; headache symptom prompt; L1 → L2 as limits are crossed (BACKEND §5).

### `spe-iss` (Target)
Dose-rate chart replaying the ISS TEPC dose equivalent (μSv/h, `iss_dose.json`) around Sep 11, 2017; shelter recommendation card. Label it "instrument dose equivalent, not effective dose" (BACKEND §3.2).

### `bad-news` (Target)
Personal screen support steps; Crew Deck shows only "C3 requested reduced tasking", no reason.

---

## 7. Mock data

- One JSON file per entity in TECH_OVERVIEW §4.2, e.g. `mock/crew.json`, `mock/alerts.json`.
- `api/client.ts` has one flag: `USE_MOCK = true`. Backend integration = set it to `false` and point to the base URL.
- Keep mock field names identical to TECH_OVERVIEW §4. If you need a new field, add it there in the same PR.

---

## 8. Accessibility and safety

- Flashing: at most 3 flashes per second (photosensitive epilepsy).
- Keyboard: every clickable body region and card is focusable.
- Sound: alarm and confirmation tones, mute switch always visible.
- Disclaimer "Simulation — not medical advice" always on screen.
- AI-generated images (if any) need a visible watermark; no NASA logo.

---

## 9. Definition of done (frontend)

- `npm run tauri build` succeeds on Ubuntu 24 (and on each OS we ship); the app runs with Wi-Fi off.
- Plan B works: `npm run build` output opens from the Java API on `localhost`.
- MVP screens work on mock data.
- Every number on screen has a `real`/`simulated` badge and a source link in its "?" card.

---

## 10. Backend integration (#56)

- **Client:** `src/api/client.ts` (types match `backend/README.md`) and `src/api/useStation.ts`, which polls `GET /api/station` every 2 s.
  - When the API answers, the status bar shows **Live station data**; otherwise the UI uses its mock data and keeps retrying. `VITE_USE_MOCK=true` forces mock data.
  - Base URL: the same origin when the API serves the build, otherwise `http://127.0.0.1:8080`.
- **Roles → viewer:** astronaut → `SELF`, CMO → `CMO`, ground → `GROUND` (provisional, see `BACKEND.md` §6).
- **Screens on live data:**
  - **Personal:** live vitals and rule alerts on the body regions (`src/data/live.ts`). The worst region opens first. The commercial-mission tag sits under the real Inspiration4 values.
    - A profile story with a level (L1+) shows only while its backend condition alert is active, which sets the level and brings the story's explanation and sources. The crew levels on the Crew Deck therefore match mock mode.
    - The CMO card approves or rejects the backend alert. The decision is shown to everyone, including the astronaut ("CMO approved. Logged.").
    - CMO and ground pick the crew member and start on the most urgent one. They never see the mood check-in, the reaction test, the check-in streak or the reaction best, in either mode (`BACKEND.md` §6). The astronaut's own mood comes from their latest check-in.
  - **Crew Deck / Emergency:** *Run scenario* starts the backend `chest-pain` scenario. The full-screen overlay follows the live EMERGENCY alert; *End drill* resets the station.
  - **Environment:**
    - Cabin CO2 and the real ISS dose chart (NASA SRAG TEPC, Sep 2017).
    - The NASA IMM top conditions.
    - CO2 and solar-particle-event scenarios with *Next step*.
    - Live telemetry read line by line from the CSV stream.
  - **Twin:** T+6 / T+12 dose and sleep-debt forecasts (labelled modelled; sleep debt only in the astronaut's own view) and the real pre-flight baseline with the commercial-mission tag.
  - **Check-in:** answers are saved privately (`POST /api/checkins`, SELF only). The **Talk it through** tab is the private support companion (local model).
  - **Crisis → CMO (#51):** when crisis wording reaches the companion, the CMO's view shows a "needs support now" band under the status bar on every screen: who, when and what to do, never the message. "I am on it" acknowledges and closes it. The astronaut's chat says that the CMO was told.
- **Deep links for demos and screenshots:** `?screen=personal|crew|environment|twin|checkin&role=astronaut:C3|cmo|ground&tab=talk`.
- **Plan B (browser):** `cd frontend && npm ci && npm run build`, then `backend/api/run.sh`, which serves the build at `http://127.0.0.1:8080/`.

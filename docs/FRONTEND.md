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
Dose-rate chart replaying RadLab `RAD-*-pre` around Sep 11, 2017; shelter recommendation card.

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

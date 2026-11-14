# ASTRA-TWIN — Frontend

Owner: frontend developer. Shared rules and data model: [`TECH_OVERVIEW.md`](TECH_OVERVIEW.md).
The demo video (≤30 s) and slides (≤7) are recorded from this UI, so polish here is the presentation.

---

## 1. Stack (PROPOSED)

| Need | Choice | Why |
|---|---|---|
| Framework | **React 18 + Vite + TypeScript** | Fast dev server, static build, works offline |
| Charts | **Recharts** | Simple time-series and reference bands |
| Body figure | **Inline SVG** with one `<path>` per region | Each region clickable, colourable, accessible |
| State | React context (or Zustand if it grows) | Few global objects: crew, alerts, environment |
| Styling | CSS variables (design tokens §3) + CSS modules | Theme in one place |
| Sound | Web Audio API | No audio files; mute switch |
| Hosting | Static build (`vite build`) → GitHub Pages; `npm run preview` offline | "Link to Final Project" and offline demo |

Pin exact versions in `package.json` (no `^latest`).

```
frontend/
  src/
    api/         ← client; switches between mock and backend (one flag)
    mock/        ← JSON matching TECH_OVERVIEW §4
    components/  ← BodyFigure, IndicatorCard, StatusBadge, AlertCard, …
    screens/     ← Personal, CrewDeck, Emergency, Environment, Twin, CheckIn, ReactionTest
    theme/       ← tokens.css
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

- `npm run build` succeeds; `npm run preview` works with Wi-Fi off.
- MVP screens work on mock data.
- Every number on screen has a `real`/`simulated` badge and a source link in its "?" card.

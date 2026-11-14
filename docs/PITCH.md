# ASTRA-TWIN — 7-Slide Pitch Script

**Status:** agreed 2026-11-14 (hackathon day 1), built question by question with the team's pitching training (*Sunum ve Pitching Eğitimi*).
**Format rules:** at most 7 slides (or a video of at most 30 s); everything in English (Space Apps 2026).
**Slide rules used:** one message per slide; the headline *is* the message; at most 3 short points; one functional visual; technical detail goes to Q&A; every slide answers one jury question.

**Evidence labels on slides:**
- **✔ Verified**: NASA or peer-reviewed source.
- **◐ To be tested**: our design; not yet validated.
- **○ Missing**: data we do not have.

---

## Core sentence (everything builds on this)

> ASTRA-TWIN helps ISS crews — who are not doctors — spot, understand and act on health changes in themselves and each other, using a digital twin built on real NASA data.

## Opening (first 15 seconds)

> In January 2026, an astronaut aboard the ISS had a medical event — and the first to respond were the crewmates. We are developing ASTRA-TWIN to help non-physician crews assess, coordinate and act in those first minutes, and every day before them.

Source (✔): NASA Crew-11 astronaut update, 2026-02-25, https://www.nasa.gov/blogs/commercialcrew/2026/02/25/nasas-spacex-crew-11-astronaut-update/ — "a medical event that required immediate attention from my incredible crewmates".
Note: NASA did not disclose the medical details. Never describe it as a heart attack or any specific condition.

---

## Slide 1 — Title & One-Line Pitch

- **Jury question:** What does this project do?
- **Headline (under the jury question):** No ISS crew should face a health change alone.
- **Title:** ASTRA-TWIN
- **Subtitle:** We help ISS crews spot and act on health changes — for themselves and each other — with a digital twin built on NASA data.
- **Visual:** one app screen (Crew Deck). No long team intro, no logo wall.

---

## Slide 2 — The Problem

- **Jury question:** What problem are you solving?
- **Headline:** ISS crews are not doctors — but they are the first responders.
- **Points:**
  - ~15 hours of CMO-specific training in the two years before launch. ✔ [NTRS 20205008925](https://ntrs.nasa.gov/citations/20205008925)
  - January 2026: crewmates were the first to respond to a medical event on the ISS. ✔ NASA Crew-11
  - No on-board lab results: blood samples are frozen and analysed on Earth. ✔ [NTRS 20090014816](https://ntrs.nasa.gov/api/citations/20090014816/downloads/20090014816.pdf)
- **Visual:** timeline — *Minute 0* crewmates act → *minutes* flight surgeon guides → *day 8* back on Earth for imaging (event Jan 7, splashdown Jan 15, 2026; NASA).
- **Bridge:** "So we asked: what would a non-medical crew need — in those first minutes, and in the days before them?"

---

## Slide 3 — Your Solution

- **Jury question:** How does your proposal answer this problem?
- **Headline:** One twin for every astronaut. One shared view for the whole crew.
- **Points:**
  - **Personal twin:** real NASA pre-flight blood data (OSD-569/575) sets each astronaut's baseline; every body region is explained in plain language. ✔
  - **Crew Deck:** shows who needs help and where; psychological data is never shared. ✔ (privacy per NASA-STD-3001 Vol 1, V1 6002/6003/6010/6011)
  - **Guidance:** the system proposes the next step, the CMO decides; the Earth Twin sends T+6/T+12 h forecasts to the flight surgeon. ◐
- **Visual:** split screen — personal screen (clickable body) | Crew Deck (7 crew). Replace the mock-up with real screenshots when the UI is ready.
- **Bridge:** "Here is what that looks like when it matters most."

---

## Slide 4 — How It Works / Demo

- **Jury question:** How does it work in practice?
- **Headline:** A crewmate collapses. Every second, everyone knows what to do.
- **Three steps (one screenshot each):**

| Alert | Coordinate | Act |
|---|---|---|
| C2's wristband detects a collapse → all crew and the Crew Deck are alerted | Station map shows C2's module; roles: CMO-1 leads, CMO-2 assists, one fetches the life-support pack and defibrillator, one handles comms | Microgravity CPR checklist + emergency card (allergies, current meds); ground informed, not awaited |

- **Evidence:**
  - CPR steps ✔ Hinkelbein et al. 2020, doi:10.1186/s13049-020-00793-y (Evetts-Russomano first, Reverse Bear Hug fallback, CMRS for ALS and defibrillation).
  - At least 2 CMOs per vehicle ✔ [NASA-STD-3001 §5](https://www.nasa.gov/reference/5-0-training/).
  - Wristband fall detection and crew location ◐ concept, simulated in the app; say so on screen.
- **Plan B:** the three static screenshots are on the slide; an optional ≤30 s recording of the same flow.
- **Note:** C2 is fictional. Do not link this scenario to Crew-11.
- **Bridge:** "But most health problems on the ISS don't start with a collapse. They start quietly."

---

## Slide 5 — Impact & Evidence

- **Jury question:** What could it change, and what supports that?
- **Headline:** Catch slow changes early. Respond faster when it's sudden.
- **Evidence radar:**

| ✔ Verified | ◐ To be tested | ○ Missing |
|---|---|---|
| Insomnia is the most likely condition on a 6-month ISS mission: probability 0.912 (NASA IMM; `NASA_DATA_SOURCES.md` §10.1) | Accuracy of T+6/T+12 forecasts against new readings | Open in-flight vital-sign data: values are simulated |
| NASA rates behavioral health risk on 30-day–1-year LEO missions 5x2 "Requires Mitigation" ([HRR risk 99](https://humanresearchroadmap.nasa.gov/Risks/risk.aspx?i=99)) | Whether coordinated alerts shorten response time in emergency drills | User testing with a real crew or an analog habitat |
| Twin baselines come from real NASA crew blood data (OSD-569/575) | Usability of the daily 2-minute check-in | |

- **Language rule:** impact is stated as an aim ("We aim to…"); never "reduces", "proven", "the first".
- **Bridge:** "So how do we get from here to a real mission?"

---

## Slide 6 — Feasibility & Next Steps

- **Jury question:** Can this be built and used for real?
- **Headline:** Built for the ISS today. Designed to scale to the Moon and Mars.
- **Visual:** three-stop roadmap; the twin's independence arrow grows with distance.

| ISS | Lunar south pole | Mars |
|---|---|---|
| Seconds of delay; 90–95% S-band coverage ✔ ([NASA](https://www.nasa.gov/wp-content/uploads/2023/08/np-2022-06-009-jsc-iss-tech-demo-mini-book-proof-3.pdf)) | Direct link only 17–43% of the time ✔ ([NTRS 20250003321](https://ntrs.nasa.gov/api/citations/20250003321)) | 3–22 min one way; 2–3 weeks of blackout at solar conjunction ✔ ([NTRS 20220013418](https://ntrs.nasa.gov/citations/20220013418)) |

- **Limitation:** "Not a medical device. Vitals are simulated until integrated with on-board wearables such as CSA's Bio-Monitor." ([CSA](https://www.asc-csa.gc.ca/eng/sciences/bio-monitor.asp))
- **Next step:** "Test in NASA's HERA analog habitat and compare twin alerts with clinician judgement."
- **Bridge:** "And this is the team that brought software and health together to build it."

---

## Slide 7 — Team & Closing

- **Jury question:** Why this team, and what should we remember?
- **Headline:** Let your ASTRA-TWIN heal you out…
  (The main message "No ISS crew should face a health change alone." opens the deck on slide 1 and is repeated in the closing line below.)
- **Team:**

| Name | Field |
|---|---|
| Süleyman Erdem Şener | Computer Engineering |
| Mehmet Emin Yıldız | Computer Engineering |
| Duru Umman | Computer Engineering |
| Mustafa Polat Güldağ | Nutrition and Dietetics |

- **Why this team:** "Three computer engineers and a nutrition and dietetics student: we joined software and health because on a long mission, care starts with the crew — and the crew deserves tools that explain, not just alert."
- **Closing (message first, then thanks):** "Our project aims to make sure no ISS crew faces a health change alone — by turning real NASA data into a twin that explains, coordinates and guides. Thank you."

---

## Pre-flight checklist (from the training)

- [x] At most 7 slides
- [x] All slide text in English
- [x] Opening states the project and the problem
- [x] Slides linked by bridge sentences
- [x] Closing repeats the main message before "Thank you"
- [x] Every number sourced; simulated items labelled
- [ ] Real screenshots replace mock-ups (after the UI is built)
- [ ] Timed rehearsal with a stopwatch
- [ ] Jury Q&A rehearsal (direct answer → evidence → limit / next step)
- [ ] File opens and images display on the presentation machine

## Open items

- Screenshots for slides 1, 3 and 4 depend on the frontend.
- If an AI-generated image is used anywhere, it needs a visible watermark and no NASA logo.
- "Use of SI" must be disclosed in the submission (code and research done with Claude).

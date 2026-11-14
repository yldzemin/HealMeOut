# ISS Behavioral Health, Psychology Module and Long-Duration Background

**Checked on:** 2026-11-14 (hackathon day 1).
**Scope:** ISS (see #16). Moon and Mars items are background for the "future extension" part of the presentation.
**Method:** Web research done with Claude (SI). Quotes were read in the linked source unless marked **(snippet)**, which means the fact comes only from a search-result summary. Snippet facts must be checked in the full text before they go into the submission.

**Labels:**
- **(snippet)**: from a search summary, not the full source.
- **UNVERIFIED**: not confirmed; listed in §6.
- **PROPOSAL**: our design idea, not a NASA requirement.

---

## 1. Why psychology needs more weight

- **NASA HRP risk "BHealth"** (ID 99): "Risk of Adverse Cognitive or Behavioral Changes and Psychiatric Disorders Leading to In-mission Health and Performance and Long-term Health Effects".
  - LEO 30 days to 1 year: **5x2, "Requires Mitigation"** (operations).
  - LEO under 30 days: 4x1; lunar long: 5x2; Mars planetary: 5x3.
  - "Estimated ISS incidence of behavioral health symptoms is 0.62 person-year, but is likely an underestimate". "Inflight symptoms have never reached diagnostic threshold".
  - Source: https://humanresearchroadmap.nasa.gov/Risks/risk.aspx?i=99
- **NASA HRP risk "Team"** (ID 101): "Risk of In-Mission Performance and Behavioral Health Decrements Due to Inadequate Cooperation, Coordination, Communication, and Psychosocial Adaptation within a Team".
  - LEO: 4x2 Accepted with Monitoring; Mars planetary: 5x5 Requires Mitigation.
  - "evidence of occasional conflict within crews preflight and in-flight, within and across International Space Station (ISS) partner subgroups."
  - Source: https://humanresearchroadmap.nasa.gov/Risks/risk.aspx?i=101
- **NASA HRP risk "Sleep"** (ID 100): LEO 5x2 "Accepted with Optimization". Source: https://humanresearchroadmap.nasa.gov/Risks/risk.aspx?i=100
- **BMed Evidence Report 2025:** "9 instances of behavioral health signs and symptoms… incidence rate of 0.62 per person-year (Antonsen et al., 2017)". https://ntrs.nasa.gov/api/citations/20250001454/downloads/BMed%20Evidence%20Report%20final%202-10-25.docx

**Current project coverage:** a 1–5 mood score (not validated) and one sleep/stress scenario. That does not match a "Requires Mitigation" risk.

---

## 2. What ISS crews actually get

Source unless noted: Picano 2024, https://ntrs.nasa.gov/api/citations/20240000994/downloads/Behavioral%20Risk%20In%20Spaceflight_Picano.pdf. The slides carry a banner "not intended for distribution outside of the NASA and International Partner communities": cite them, do not show screenshots in the demo.

| Support | Detail |
|---|---|
| Private Psychological Conference (PPC) | "every 2 weeks on orbit", "Typically lasts 15-20 minutes", "non-recorded private and confidential". Topics: sleep, workload, morale, crew and crew–ground relationships, mood, cognition, family, habitability/food, EVA preparation. |
| Cognitive check | "WinSCAT once per 30d on orbit" |
| Family | "Private Family Conferences (weekly)", "IP phones on ISS", care packages "About 18 kgs per crewmember", "Private Special Conferences (one per month)" |
| Training | "Expeditionary Skills… Self-Care, Team Care, Teamwork and Group Living, Communication, and Leadership/Followership"; cross-cultural, stress and conflict management |
| Pre/post flight | Readiness interviews L-365/180/30; post-flight R+3, R+14, R+30–45 |
| Repatriation | Briefings "normally 6 weeks before landing" ([OCHMO-TB-016](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-016-behavioral-health.pdf)) |

Privacy rule: NASA-STD-3001 Vol 1 requires private psychological communications (V1 6010), treated as private (V1 6011). https://www.nasa.gov/reference/6-0-medical-operations/

---

## 3. Evidence on ISS stressors and protective factors

| Topic | Finding | Source |
|---|---|---|
| Displacement | "displacement of negative feelings to outside supervisors… on all six questionnaire subscales" (ISS, n=17) | Kanas 2007, PMID 17571662 |
| Third-quarter effect | Mixed: Kanas found no quarter-specific mood drop; Stuster found "decline in morale during the third quarters"; BMed 2025 calls it "an open question" | PMID 17571662; NTRS 20100026549; BMed 2025 |
| Culture | "American subjects reported more dissatisfaction with their interpersonal environment than their Russian counterparts" | https://doi.org/10.1016/s0094-5765(00)00102-8 |
| Workload and control | "over scheduling and insufficient timeline control", "language difficulties", "cultural misunderstandings" | Picano 2024 |
| CO2 | Acute CO2: "no clear dose-response patterns" on cognition; OCHMO-TB-016 lists CO2 exposure as a contributing factor | Scully 2019, https://doi.org/10.1038/s41526-019-0071-6 |
| Sleep | Reduced by "temperature, noise, CO2 levels… high tempo workload"; ISS mean sleep 6.09 h | BMed 2025; Barger 2014 |
| Food | 15 ISS astronauts: no menu fatigue in acceptability scores; crews picked favorites and "did not consume foods they did not like" | Douglas 2025, https://ntrs.nasa.gov/citations/20240013136 |
| Earth view | "Every respondent had a positive reaction… the strongest of which related to the Earth's beauty and fragility" | Ihle 2006, PMID 16491575 |
| Outlets | "Photographing Earth and Journaling are important outlets" | OCHMO-TB-016 |
| Exercise | "established effectiveness in reducing depression and anxiety" (terrestrial evidence) | BMed 2025 |
| Closest precedent | A prototype "Dashboard… heart rate… sleep wake patterns" (Mollicone); BMed says its "future use… is uncertain" | BMed 2025 |

**NASA BHP's top concerns** (Picano 2024): "Death of family member on earth"; "Mishap… due to… extreme fatigue"; "Development of depression, anxiety"; "Significant crew-ground conflict that turns into an overt 'us against them' scenario".

**Documented incidents** (OCHMO-TB-016): Soyuz T14 1985 "Depression may have contributed to evacuation"; "2 of 7 (29%)… Shuttle-Mir astronauts reported depressive symptoms"; delayed news of a family death caused "acute social withdrawal".

**Instruments and licensing:**
- ISS Standard Measures: "a thirty-seven item Profile of Mood States (POMS mood survey)", a "brief screen of depressed mood", and "Visual Analog Scales… mood, fatigue, conflict, and stress". https://ntrs.nasa.gov/api/citations/20170005421/downloads/20170005421.pdf
- APEX (2026 HRP workshop): BDI-2 weekly (21 items), POMS and NeuroVAS daily, "Team Performance… Daily, 4 Likert items". https://ntrs.nasa.gov/api/citations/20260002661/downloads/Bell_APEX_IWS_2026_final.pdf
- **Do not use POMS:** POMS-2 is commercial (MultiHealth Systems / Pearson).
- Perceived Stress Scale: permission "completely free of charge" via ePROVIDE. https://www.cmu.edu/dietrich/psychology/stress-immunity-disease-lab/scales

---

## 4. Proposed psychology module (PROPOSAL)

### 4.1 Daily check-in (~2 minutes)

| # | Item | Format | Basis |
|---|---|---|---|
| 1 | Mood, fatigue, stress, conflict | 4 sliders, 0–100 (VAS) | ISS Standard Measures |
| 2 | Sleep hours and quality | number + 1–5 | HRP Sleep risk |
| 3 | Workload and "I control my timeline" | 1–5 | Picano 2024; APEX |
| 4 | Crew climate | 2 Group Living items + 1 team Likert item | Landon 2024; APEX |
| 5 | Frustration with mission control | 1–5 | Displacement, Kanas 2007 |
| 6 | Family and connection | days since last family conference; "important family event" flag | Picano 2024 |
| 7 | Restorative activity | cupola / photography / exercise / journal (multi-select) | OCHMO-TB-016; Ihle 2006 |
| 8 | Food intake and variety | quick log | Douglas 2025; NTRS 20060049121 |

### 4.2 Weekly and periodic

- **Reaction test (weekly):** a 3-minute in-browser reaction-time task modelled on the PVT-B used by NASA's Reaction Self Test. Score as a z-score against the person's own baseline. Gamified (streaks, personal best).
- **PPC prep summary (every 2 weeks):** the app turns two weeks of trends into a short agenda for the next Private Psychological Conference.

### 4.3 Privacy rules

- Psychological data is **never shown on the Crew Deck**. It is visible to the astronaut and, when they share it, to the behavioral health provider.
- The Crew Deck may show **crew climate only as an anonymous aggregate**, with a minimum-response threshold. In a small crew even anonymous data can identify a person (IOM *Safe Passage*, https://www.nationalacademies.org/read/10218/chapter/8). The threshold value is our design choice.

### 4.4 Escalation language

- No public NASA numeric escalation criteria were found. Triggers are **our design**.
- Wording: "This trend has lasted N days. Discuss it at your next PPC, or contact your flight surgeon now." Never diagnostic language.

### 4.5 New scenario: "Bad news from home"

- **Trigger:** a family-death message arrives (NASA BHP's top concern).
- **Personal screen:** support steps, option to request a Private Special Conference, sleep and workload adjustment.
- **Earth Twin:** proposes a 24–72 h support plan (schedule relief, family conference) for the ground team to approve.
- **Crew Deck:** only "C3 has requested reduced tasking", no reason shown; "Team Care" reminder from Expeditionary Skills training.
- **Twin signals:** sleep, mood VAS, food intake and variety, withdrawal from crew activities.

---

## 5. Background for the presentation

### 5.1 Moon–Earth communication

- **One-way light time ~1.21–1.35 s; round trip ~2.6 s.** Computed from Moon distances 363,300–405,500 km (snippet; NSSDC page was unreachable).
- **Artemis II flew** April 1–10, 2026 (launch "Wednesday, April 1", splashdown "Friday, April 10, 2026"). https://www.nasa.gov/?p=985063
- **Orion optical link (O2O):** "rates up to 260 Megabits per second" (https://www.nasa.gov/goddard/esc/exploration-and-space-communications-o2o); nominal "80 Mbps return link and 20 Mbps forward link from Lunar vicinity" (https://ntrs.nasa.gov/api/citations/20250009875/downloads/Optical%20Comm%20for%20Artemis%20II%20Crewed%20Mission.pdf).
- **South pole / far side:** direct-to-Earth coverage 43% best case, 17% worst; "A single lunar relay improves the situation dramatically, with 80-86% coverage"; three relays 100%; deployment "starting with Artemis III". https://ntrs.nasa.gov/api/citations/20250003321

### 5.2 Where the system can be used

| Setting | Delay | Ground support | Stay | Main value of the onboard twin |
|---|---|---|---|---|
| ISS (build target) | seconds; 90–95% S-band coverage | real-time; Crew-11 undock to splashdown ~10 h | 6–12 months | first minutes, loss-of-signal gaps, crew coordination, non-physician CMO |
| Commercial LEO stations | as ISS | as ISS | — | direct transfer after ISS (ISS deorbit "by the end of 2030", GAO, snippet) |
| Lunar surface / south pole | ~1.3 s, but direct link 17–43% | intermittent | "at least a week" early, "up to two months" later ([NASA](https://www.nasa.gov/blogs/artemis/2020/10/28/lunar-living-nasas-artemis-base-camp-concept/)) | runs alone when the link is down |
| Mars | 3–22 min; 2–3 week conjunction blackout | delayed | "up to three years" ([NASA](https://www.nasa.gov/feature/step-3-artemis-moon-missions-as-an-astronaut-testbed-for-mars)) | full Earth-independent operation (EIMO) |

**Radiation by setting** (Simonsen 2025, https://ntrs.nasa.gov/api/citations/20250006168): ISS "approximately 0.2 to 0.5 mSv/day", "~40 and 90 mSv during a typical six-month mission"; lunar 6–12 months "under 100 millisieverts… to about 350 mSv"; Mars "1,000 to 1,200 mSv"; career limit "600 mSv".

### 5.3 NASA Twins Study (Scott Kelly 340 days; Mark Kelly on Earth)

From NASA summaries (https://www.nasa.gov/humans-in-space/twins-study/twins-study-about and https://www.nasa.gov/humans-in-space/nasas-twins-study-results-published-in-science-journal/):

- **Telomeres:** "lengthening in flight and then majority returning to baseline within 48 hours of landing".
- **Gene expression:** "91.3% of Scott's gene expression levels returned to normal [baseline] within six months of landing."
- **Body mass:** "decreased by seven percent during flight".
- **Cognition:** "a more pronounced decrease in speed and accuracy was observed after he landed and persisted for six-months".
- **Microbiome:** "returned to preflight state". **Vaccine:** "reacted appropriately". **Carotid:** "artery wall thickening". **DNA damage:** "believed to be a result of radiation exposure".
- **Chromosomes:** persistent "chromosomal inversions, increased numbers of short telomeres" (Garrett-Bakelman 2019, Science, snippet). Luxton 2020, Cell Reports (doi:10.1016/j.celrep.2020.108457): "increased frequencies of inversions in all crewmembers" in "a group of 10 unrelated astronauts" (press release).
- **Limits:** n=1 twin pair; Science abstract not accessed.
- **Use in the app:** chromosomes cannot be measured in flight. Use cumulative radiation dose as the in-flight proxy, and a post-flight follow-up layer (cognition changed *after* landing).

### 5.4 Longest missions and long-duration health

| Person | Duration | Source |
|---|---|---|
| Valeri Polyakov (Mir) | 437 days single mission (paper says 438) | Manzey 1998 (snippet) |
| Frank Rubio (ISS) | "371 days on Sept. 27, 2023" | https://www.nasa.gov/?p=203557 |
| Scott Kelly | 340 days | same |
| Christina Koch | 328 days | same |
| Peggy Whitson | "675 days" cumulative (table shows 695 incl. Ax-4) | same |
| Oleg Kononenko | passed 1,000 cumulative days, June 2024 | Spaceflight Now (press) |

| System | Finding | Source |
|---|---|---|
| Bone | "lose between 1% and 1.5% of their density each month" | https://www.nasa.gov/reference/risk-of-spaceflight-induced-bone-changes/ |
| Bone recovery | 16/17 incomplete tibia recovery at 1 year | Gabel 2022 (snippet) |
| Muscle | calf volume −13%, peak power −32% after 6 months | Trappe 2009 (snippet) |
| Eyes (SANS) | optic disc edema by OCT in ~70% of crew | (snippet) |
| Arteries | 17–30% higher carotid stiffness | Hughson 2016 (snippet) |
| Immune | "14/23 (61%) astronauts from ISS missions shed one or more herpes viruses" | Rooney 2019 (snippet) |
| Cognition | no systematic decline (n=25) | Dev 2024 (snippet) |
| Psychology (Polyakov) | basic cognition intact; mood and workload worse in first weeks of flight and after return; no lasting deficit at 6 months | Manzey 1998 (snippet) |

**Use in the app (long-duration module):** bone and muscle trend from exercise logs, cumulative dose vs 600 mSv, periodic eye-exam reminders (ultrasound/OCT), viral reactivation watch.

---

## 6. UNVERIFIED

- Any NASA numeric escalation criteria for behavioral health.
- PANAS licensing terms; PHQ-9 status.
- Outcome results of ESA VR Mental Care, Veggie and SSLA lighting studies.
- Which instrument the ISS "brief screen of depressed mood" is.
- NSSDC Moon distances (page unreachable); ISS–TDRS voice latency.
- NASA's own wording on the 2030 ISS deorbit; Gateway "pause" (press only, not on nasa.gov). **Do not use Gateway in the presentation.**
- Garrett-Bakelman 2019 exact chromosome numbers.
- All **(snippet)** rows in §5.4.
- Individual published health findings for Rubio, Koch and Kononenko (none found).

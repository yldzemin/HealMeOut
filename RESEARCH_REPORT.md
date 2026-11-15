# ASTRA-TWIN — Verified Research Report

> **Update 2026-11-14:** The verified NASA data source inventory is in [`NASA_DATA_SOURCES.md`](NASA_DATA_SOURCES.md). Values listed in its §9 supersede this report: CO2 congestion threshold, light-time, NOAA exclusion, DONKI endpoint, Chang'e-4 in RadLab, compression garments, and the ISS formulary source.
>
> **Scope change 2026-11-14 (#16):** The project now targets the **ISS** only. Mars and Moon content in this report (light delay, Mars SPE, Mars landing) is kept as background for the "future extension" part of the presentation and is not built into the app. ISS-specific facts are in `NASA_DATA_SOURCES.md` §11.

**Date:** 2026-11-09 (translated to English 2026-11-15)
**Method:** live web pages, NASA technical documents, PubMed and live API queries. Every claim carries its source.

**Labels:**
- ✅ CORRECT
- ⚠️ PARTLY CORRECT
- ❌ WRONG
- ❓ NOT VERIFIED

What could not be verified is stated plainly. Nothing is assumed.

---

## 1. Challenge: "Create Health Monitoring Software for Astronauts on Space Missions"

URL: https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/

- **Difficulty:** Intermediate
- **Subjects:** Human Exploration, Software, Space Exploration
- **Main objective (quote):** "create a visual tool that helps astronauts assess their own health during space missions and provides guidance on treatments and health improvements."
- **Input:** anything: keyboard, mouse, camera images.
- **Data:** any health indicator, for example blood sugar in mg/dL or an ultrasound image.
- **Example indicators:** blood pressure, pulse, blood sugar, body temperature, daily hours of sleep, mood.
- **Guiding questions:**
  - What can be measured in space?
  - Why is an examination needed?
  - How do hazards affect the indicators?
  - When does a result need the attention of the crew or of **flight surgeons on the ground**?
  - What can be done to prevent a critical situation?
- **UI emphasis:** animation and interaction. The example is a clickable human figure in which each body part shows its function and health status.
- **Optional ideas:**
  - Choice of environment: spacecraft, station, rover, lunar or planetary base.
  - Reminders for routine measurements.
  - Gamification.
  - Dynamic charts over time.
  - Colour codes.
  - The principle "Do not assume that astronauts know everything": measurements and risks should be explained with simple instructions.
- **Resources tab:** "Resources will be available on Friday, November 13." Check the resources again on 13 November.

### Differences from the draft's claims

- **The word "autonomous" is not in the text.** The text expects reporting to flight surgeons on the ground, so the Ghost Twin and escalation idea fits it.
- **Edge AI and digital twin are not mentioned.** They are our extras; the core task is to be a **visual self-assessment tool**.
- **Wording:** the text says "treatments and health improvements" and prevention, not "medical/restorative".

## 2. Space Apps 2026 rules and dates

Sources:
- https://www.spaceappschallenge.org/2026/
- https://www.spaceappschallenge.org/resources/participant-faqs/ (5 November 2026)

### Dates

- **Hackathon:** 14–15 November 2026.
- **Submission window:** from Saturday 14 November 09:00 to Sunday 15 November 23:59, local time.
- **13 November:** the resources, the Project Submission Guide and the Judging & Awards Guide are published.
- **Results:** Global Nominees and Finalists in December 2026, Global Winners in January 2027.

### Rules

- **Team:** at most 6 people. Each team chooses one challenge.
- **⚠️ "Teams are not allowed to begin working on the challenges prior to the hackathon."** Do not start writing code before 14 November. Ask the local organiser where the line is.
- **Artificial intelligence (called "Super Intelligence (SI)" in 2026):** free to use. The submission **must** explain how SI tools were used.

### Global Awards (10)

https://www.spaceappschallenge.org/awards/

Best Use of Science, Best Use of Data, Best Use of Technology, Galactic Impact, Best Mission Concept, Most Inspirational, Best Use of Storytelling, Global Connection, Art & Technology, Local Impact.

### Judging criteria — verified from the 2025 guide only

The 2026 guide is not published yet. Archive: https://web.archive.org/web/20251128144656id_/https://www.spaceappschallenge.org/resources/judging-awards-guide/

Each criterion is scored 1–5, with no weights:

- **Impact:** the size of the problem and how widely the project helps or inspires.
- **Creativity:** originality.
- **Validity:** scientific basis, technical feasibility, completeness, usability.
- **Relevance:** fully answers the challenge and uses **NASA data as a core component** ("rather than a superficial addition").
- **Presentation:** storytelling and demo.

### Submission requirements — 2025 guide

Archive: https://web.archive.org/web/20251229205712id_/https://www.spaceappschallenge.org/resources/project-submission-guide/

- **Demo:** at most a **30-second video** or at most **7 slides** (title included). Over the limit, the project cannot enter global judging.
- **Hosting:** a public link that needs no sign-in (YouTube, Drive, GitHub).
- **Language:** entirely English. The video needs English captions.
- **NASA data:** required for a global award.
- **AI rules in 2025:**
  - AI-generated images and video need a visible watermark.
  - AI-generated code, data and audio must be stated in the text.
  - Generated content must not use the NASA brand or logo.

## 3. Similar and inspiring projects

| Project | Status | Note |
|---|---|---|
| 2014 "Astronaut Health Monitoring System" | ✅ Exists | Arduino; data from EEG/ECG and temperature sensors to a ground station. Code: https://github.com/NazS2/Astronaut-health-monitoring-system (AFL-3.0). Ground-centred monitoring; 2026 asks for self-assessment. |
| EmergencyTwin voice | ✅ Exists, but not about space or health | A building emergency twin. One index.html, Three.js and the AssemblyAI Voice Agent. https://github.com/Hamna-Munir/emergencytwin-voice. The README says MIT, but there is no LICENSE file. |
| Aegis Disaster Twin | ✅ Exists, but not about space or health | FastAPI, LangGraph multi-agent, FAISS RAG, Three.js, Docker. https://github.com/Nikhil-creat/aegis-disaster-twin. **No licence: the code cannot be copied.** |
| SpaceGenes+ (2025 Global Winner, Best Use of Science) | ✅ | A dashboard of the effects of radiation and microgravity on cells. |
| Skylab (2022 Global Nominee) | ✅ | Astronaut disease prediction with GeneLab data. https://github.com/imostafizur/CSE498R |
| Spacemed (2018 Global Nominee) | ✅ | Vital-sign and biomechanics monitoring. |

**No** Space Apps winner built on an astronaut digital twin was found. That is an advantage for originality.

---

## 4. Verification of the physiological values (docx draft)

| Draft | Label | Correction and source |
|---|---|---|
| Mars–Earth delay 4–24 min | ⚠️ | The physical calculation gives **~3.1–22.3 min one way** (distance 55.7–401.3 million km per NSSDC). NASA HHP white paper: "up to 22 minutes". |
| Fixed "22 MINUTES" in the UI | ⚠️ | That is a maximum. **The real value on 9 November 2026 was 11.37 min** (JPL Horizons, live query). It must be computed live. |
| HR 72, range 40–180 | ⚠️ | On the ISS the 24-hour heart rate does not change (Norsk 2015, doi:10.1113/jphysiol.2014.284869). Outside 50–100 at rest should count as a "warning". |
| HRV 65 ms, critical <20 | ❓ | The metric (SDNN/RMSSD) is not stated. The literature has no "critical <20" threshold. Norms: 24-hour SDNN 141±39 ms, RMSSD 27±12 ms (Task Force 1996). Use a personal baseline and a z-score. |
| BP 120/80 | ⚠️ | In flight, 24-hour blood pressure **falls by ~8/9 mmHg** and cardiac output rises 35–41% (Norsk 2015). The in-flight baseline should be about **112/71**. The real risk is orthostatic intolerance. |
| SpO2 99%, critical <88 | ⚠️ | The ISS cabin is at 14.7 psia and 21% O2, equivalent to sea level; normal is 96–99%. <90 is hypoxaemia; 88 is a clinical convention. NASA defines the threshold as PIO2: 149 mmHg at sea level, lower limit 122 mmHg (1-hour time-weighted average, OCHMO-TB-003). |
| Body temperature 37.0, range 35–41.5 | ⚠️ | On long missions, resting temperature rises by **~+1 °C over ~2.5 months** (Stahn 2017, doi:10.1038/s41598-017-15560-w). |
| Glucose 90, hypoglycaemia <60 | ❌ | ADA 2025: **Level 1 <70, Level 2 <54, Level 3 = severe, needs help from another person** (doi:10.2337/dc25-S006). |
| Hydration falls 1.5% per hour | ❌ | In reality 2.5 L a day, i.e. ~0.13% of body mass per hour. **Performance drops after losing 2.5% of body weight.** Source: OCHMO-TB-027 https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf |
| 8% per hour on EVA | ❌ | Physiologically impossible (~6.5 kg/h). Realistic: **0.3–1.2% per hour**, ~1 L/h at peak exercise (Ewert & Downs, ICES-2021). |
| EVA water need | ✅ (reference) | **≥240 mL** of water per hour [V2 11030] and **+200 kcal** per hour [V2 7004]. |
| EMU drink bag | ⚠️ | Sources disagree: 21 oz (~620 mL, NASA 2019), 32 oz (~0.95 L, Etlin 2024) and 1,070 mL. |

### Radiation

| Draft | Label | Correction |
|---|---|---|
| Background 0.05 mSv/h | ⚠️ | It depends on location. **ISS ~0.015**, **Mars transit ~0.075** (MSL RAD 1.8 mSv/day, Zeitlin 2013), **Mars surface ~0.027 mSv/h** (Hassler 2014: 0.64–0.67 mSv/day). |
| SPE peak 75 mSv/h | ⚠️ | Plausible for a large event in a lightly shielded habitat. In the August 1972 SPE, the peak skin dose behind 1 g/cm² Al was ~1.4 Gy/h (Parsons & Townsend 2000). State the organ, the quantity (Gy or Sv) and the shielding. At this value, 75 mSv/h × 3.3 h reaches the 250 mSv per-event limit. |
| Career limit 600 mSv | ✅ | [V1 4030], independent of age and sex. https://www.nasa.gov/wp-content/uploads/2023/03/radiation-protection-technical-brief-ochmo.pdf |
| 30-day limits | ⚠️ outdated | Replaced in Rev B by **[V1 4031]**: <250 mSv effective dose for the design reference SPE (October 1989). Vol 1 Rev C also exists; check it. |
| NOAA S-scale | ✅ (reference) | S1 = ≥10 MeV ≥10 pfu. S3 (10³): avoid EVA. S4 (10⁴): unavoidable hazard on EVA. S5 (10⁵). https://www.spaceweather.gov/noaa-scales-explanation |
| Actual response | ✅ | Shelter: adds ~10 cm of water-equivalent shielding. Shielding cuts SPE dose by ~50%. Warning, dosimetry, ALARA. |

### Medication and nutrition prescriptions in the draft

Reference: the 2023 ISS formulary, 106 medications (Diaz et al. 2024, npj Microgravity, doi:10.1038/s41526-024-00414-3, https://pmc.ncbi.nlm.nih.gov/articles/PMC11266549/). Use decided in #11 (CC BY 4.0).

| Draft | Label | Reality |
|---|---|---|
| Propranolol (radiation-induced hypertension) | ❌ | An SPE does not cause acute hypertension. In flight, blood pressure is already low, and a beta-blocker can raise the orthostatic risk. The ISS has no propranolol; it has **metoprolol**. |
| Potassium iodide | ❌ | An SPE is made of protons; there is no radioactive iodine. Per the CDC, KI protects only against radioiodine. Not on the ISS. |
| Antioxidant cocktail (C 1000 mg, E 400 IU, ALA, Se) | ❓/may be harmful | Evidence only in mice (Kennedy 2008). In the SELECT trial, **exactly 400 IU of vitamin E raised prostate cancer risk** (HR 1.17, Klein 2011 JAMA). NASA plans to identify radioprotectors by 2033. For prodromal symptoms the ISS has **ondansetron**. |
| Glucagon at <50 | ⚠️ | ADA recommends **15 g of oral glucose first, re-check after 15 min**. Glucagon only if the person cannot take it by mouth. The ISS has no glucagon. Hypoglycaemia is unlikely in a healthy crew. |
| Dextrose/maltodextrin gel | ⚠️ | Fits the ADA 15 g rule. The EMU drink bag already holds a little glucose. |
| Electrolytes | ⚠️ | NASA uses salt tablets and ≥1 L of water before return. There is no routine in-mission dose standard. |
| BCAA 5 g | ❓ | No crisis use at NASA. Treat it as nutrition. |
| Melatonin 5 mg | ⚠️ | The ISS has **3 mg** tablets. On the Space Shuttle, 73% of single doses were zolpidem and 2% melatonin. 0.3 mg of melatonin did not improve sleep (Dijk 2001). On the ISS, crews sleep 6.09 h a night and 75% use sleep medication (Barger 2014). |
| L-tryptophan | ❓ | Not on the ISS; no space evidence. |
| Epinephrine for depressurisation | ❌ | The right response: **an O2 mask or suit O2, and restoring pressure**. For DCS, recompression and 100% O2. The ISS has epinephrine, but for anaphylaxis (0.3 mg IM) and cardiac arrest. |
| Automatic transdermal drug delivery in the suit | ❓ does not exist | NASA has no such hardware. The closest is the experimental nanochannel implant on the ISS (CRS-26). NASA's direction is EIMO (Earth-Independent Medical Operations): a decision-support system in which a person decides. Automatic dosing would count as a medical device for the FDA and raises ethical issues. |

**Medications on the ISS formulary that can be used in recommendations:** ondansetron, promethazine, metoprolol, lisinopril, nitroglycerin, aspirin, apixaban, enoxaparin, acetazolamide, epinephrine, naloxone, flumazenil, ketamine, dexamethasone, zolpidem 5 mg, modafinil 200 mg, melatonin 3 mg.

### Real values for depressurisation

- EMU 4.3 psi, 100% O2. ISS cabin 14.7 psia, 21% O2.
- The Secondary Oxygen Pack gives ≥30 min of O2 in open-loop purge mode.
- The ebullism threshold is 0.91 psi. Below 2.5 psia, extra O2 may not be enough.
- Habitat pressure can be held indefinitely at 5.0–15.0 psia. Pressure change must be at most 13.5 psi/min.
- Build the model chain as **suit pressure → PIO2 → SpO2** (OCHMO-TB-003, TB-037).

### NASA reference values usable in the simulator

| Parameter | Value | Source |
|---|---|---|
| Energy | 3,035 kcal/day; +200 kcal per hour on EVA | NASA-STD-3001 V2 7003/7004 (from a search snippet) |
| Metabolic load (82 kg) | O2 0.82 kg/day, CO2 1.04 kg/day; heat output 88 W asleep, 139 W nominal, ≤826 W in exercise | Ewert & Downs ICES-2021 |
| Water | 2.5 L/day; +240 mL per hour on EVA; performance drops after losing 2.5% of body weight | OCHMO-TB-027 |
| CO2 | 1-hour average ≤3 mmHg; 7-day average <2.5 mmHg; headache at 2.8–4.5 mmHg | OCHMO-TB-004 |
| O2 | PIO2 lower limit 122 mmHg; normoxia range 145–155 mmHg | OCHMO-TB-003/002 |
| Sleep | 6.1 h/night on the ISS | Barger 2014 |
| Sleep schedule | Standard ISS sleep period 21:30–06:00 GMT (8.5 h scheduled); crews slept the full 8.5 h on 5.9% of nights | Jones 2022, Sleep 45(3):zsac006, doi:10.1093/sleep/zsac006 |
| Bone loss | ~1–1.5% per month | NASA HHP white paper |

### The 5 hazards (RIDGE) and countermeasures

Source: https://www.nasa.gov/hrp/hazards/ and https://www.nasa.gov/wp-content/uploads/2024/01/human-health-and-performance.pdf

- **Radiation:** shielding, shelter, warning system, dosimetry.
- **Isolation:** private space, communication, behavioral health countermeasures.
- **Distance:** decision-support system, on-board medical capability (EIMO).
- **Gravity:** exercise. VTC, B-complex vitamins and GLP-1 are being studied for SANS.
- **Hostile/closed environment:** ECLSS, CO2 control, DCS treatment.

The HRP Research Approach Plans (June 2026) aim at "semi-autonomous monitoring" and dashboards in Behavioral Health, Team and EIHSO (interim goal 2031, final 2034). **ASTRA-TWIN fits exactly this gap.**

---

## 5. Verification of the NASA OSDR datasets

| ID | What it actually is | Usable numeric data? | Draft's claim |
|---|---|---|---|
| OSD-569 | I4 whole blood: **CBC** + RNA-seq/WGS | ✅ `LSDS-7_Complete_Blood_Count_CBC_TRANSFORMED.csv`: 4 crew × 7 time points (L-92 … R+194), with reference ranges | That it is I4 is right; "ideal for a solar flare" is ❌ (pre- and post-flight only, no radiation measurement) |
| OSD-570 | I4 PBMC single-cell omics | ❌ omics | Omics only |
| OSD-571 | I4 plasma proteomics/metabolomics | ❌ omics | Omics only |
| OSD-575 | I4 **CMP + cytokine** panels | ✅ **The best.** Glucose, Na, K, Cl, BUN, creatinine, eGFR, Ca, ALT/AST + IL-6, IL-10, IFN-γ, CRP, fibrinogen (pg/mL) | Fits the hypoglycaemia and dehydration scenario |
| OSD-687 | I4 T-cell histone modification | ❌ n=1, R+194 only, FASTQ access restricted | ❌ not "immune data" |
| OSD-815–818 | **Ground study:** ex vivo SANS lab model with donor eyes (TRISH) | Mostly images; lab pressure logs in 816 | ❌ **Not** astronaut SANS physiology |

Other I4 sets: OSD-572 (microbiome), 573 (capsule), 574 (skin), 630 (stool), 656 (urine inflammation panel). Ax-1 sets: OSD-903 and 943.

**No open dataset gives in-flight heart rate, SpO2 or body temperature.** These values must be simulated with a physiological model, with baselines and reference ranges from the I4 CBC/CMP. Say this plainly in the presentation.

---

## 6. Verified APIs (tested live on 9 November 2026)

| Priority | Source | Endpoint | Key |
|---|---|---|---|
| 1 | JPL Horizons (delay) | `https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='499'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='OBSERVER'&CENTER='500@399'&START_TIME='2026-11-09'&STOP_TIME='2026-11-10'&STEP_SIZE='1%20d'&QUANTITIES='20,21'` → `1-way_down_LT` (min); parse between `$$SOE`…`$$EOE` | None |
| 2 | NOAA SWPC (live space weather) | `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json`, `.../xrays-1-day.json`, `.../xray-flares-latest.json`, `https://services.swpc.noaa.gov/products/noaa-scales.json`, `.../products/alerts.json` | None |
| 3 | OSDR RadLab (real dose) | `https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Mars&timestamp%3E=2017-09-08&timestamp%3C2017-09-16&absorbed_dose_rate&format=csv` | None |
| 4 | OSD-575 CMP | `https://osdr.nasa.gov/geode-py/ws/studies/OSD-575/download?source=datamanager&file=LSDS-8_Comprehensive_Metabolic_Panel_CMP_TRANSFORMED.csv` | None |
| 4b | OSD-575 cytokines | `...&file=LSDS-8_Multiplex_serum_immune_EvePanel_TRANSFORMED.csv` | None |
| 5 | OSD-569 CBC | `https://osdr.nasa.gov/geode-py/ws/studies/OSD-569/download?source=datamanager&file=LSDS-7_Complete_Blood_Count_CBC_TRANSFORMED.csv` | None |
| 6 | NASA DONKI (FLR/SEP/CME) | `https://api.nasa.gov/DONKI/SEP?startDate=...&endDate=...&api_key=KEY` | Required. **Returned 503 in testing**; a fallback is needed |

### A real event in RadLab: the September 2017 SPE on Mars

Daily mean dose rates (MSL-RAD):

| Date | Dose rate | Note |
|---|---|---|
| 9 September | 7.95 μGy/h | |
| 11 September | 17.87 μGy/h | Peak 23.18 |
| 12 September | 12.92 μGy/h | |
| 14 September | 6.56 μGy/h | Forbush decrease |

This series can be replayed as **real data** in the solar-flare scenario. RadLab also has ISS DosTel, CRaTER (LRO) and BioSentinel data (Chang'e-4 is not in RadLab: `NASA_DATA_SOURCES.md` §4.2).

### OSDR API notes

- Metadata: `https://osdr.nasa.gov/osdr/data/osd/meta/<id>`
- File list: `https://osdr.nasa.gov/osdr/data/osd/files/<id>?page=1&size=100`. Pages **start at 1**.
- Download: `https://osdr.nasa.gov` + `remote_url`, with `curl -L`.
- Search: `https://osdr.nasa.gov/osdr/data/search?term=inspiration4&type=cgene&size=50`
- The CSV files start with a UTF-8 BOM; decode them with `utf-8-sig`.
- In the OSD-569 TRANSFORMED file, haemoglobin is labelled `hemoglobin_value_percent`, but the unit is g/dL.

### Sources that are not reachable or not open

- **NASA LSDA:** access only by formal request.
- **Twins Study:** no open download found.
- **Polaris Dawn:** no open dataset.
- **ISS SRAG dosimetry:** no open bulk download; use RadLab instead.
- **SOMA atlas** (Overbey 2024 Nature): omics data, no vital signs.

---

## 7. Not verified (do not use as verified)

- The primary Hassler 2014 article (the value came from secondary sources).
- The AHA ACLS epinephrine dose.
- NASA lighting countermeasures.
- The text of NASA-STD-3001 Vol 1 Rev C.
- The current EMU drink bag size.
- The 3,035 kcal and 145–155 mmHg values (taken from a search snippet).
- The 1.4 Gy/h value in Parsons & Townsend 2000 (taken from the abstract).
- The 2026 judging and submission guides (to be published on 13 November).

## 8. Other sources

**NASA:**
- HRP: https://www.nasa.gov/hrp/
- RAP PDF: https://www.nasa.gov/wp-content/uploads/2026/06/hrp-rap-for-web-62026.pdf
- Human Research Roadmap: https://humanresearchroadmap.nasa.gov/risks/ (22 risks; there is no separate ARS risk any more)
- NTRS 20230014168: an OSDR introduction talk, not a physiology document. Cite it as a data source. https://ntrs.nasa.gov/citations/20230014168
- EIMO ConOps: https://ntrs.nasa.gov/citations/20240001402
- OCHMO technical briefs:
  - TB-003: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-003-habitable-atmosphere.pdf
  - TB-004: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf
  - TB-037: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-037-decompression-sickness.pdf
  - TB-002: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-002-eclss.pdf

**Peer-reviewed articles:**
- Levin 2023 (EIMO): doi:10.1109/OJEMB.2023.3255513
- Zeitlin 2013: doi:10.1126/science.1235989
- Hassler 2014: doi:10.1126/science.1244797
- Overbey 2024: doi:10.1038/s41586-024-07639-y
- Garrett-Bakelman 2019: doi:10.1126/science.aau8650

**Other:**
- CDC KI: https://www.cdc.gov/radiation-emergencies/treatment/potassium-iodide.html
- CDC ARS: https://www.cdc.gov/radiation-emergencies/hcp/clinical-guidance/ars.html

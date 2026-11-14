# NASA Data Sources — Verified Inventory

**Checked on:** 2026-11-14 (hackathon day 1), between about 09:30 and 10:10 UTC.
**Method:** Every page was opened and every API was called live with curl or the browser. Nothing was signed into and no forms were submitted.
**Research:** Done with Claude (SI); see "Use of SI" in the submission.

**Labels:**
- **VERIFIED**: opened or tested on 2026-11-14.
- **UNVERIFIED**: not confirmed; the reason is given.
- **⚠️ NASA-only**: conflicts or may conflict with the team rule "use NASA-origin data only" (`CLAUDE_HAFIZASI.md` §2). These need a team decision (issue #9).

This file supersedes some values in `ARASTIRMA_RAPORU.md` (2026-11-09). The list is in §9.

---

## 1. Challenge resources and 2026 rules

### 1.1 Resources tab
Source: https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/?tab=resources (VERIFIED)

**The tab lists no datasets.** It has three NASA information pages and two Canadian Space Agency (CSA) pages.

| Resource | URL | What it is |
|---|---|---|
| 5 Hazards of Human Spaceflight | https://www.nasa.gov/hrp/hazards/ | Narrative overview of the five hazards and videos. No numbers. |
| Risks of Human Spaceflight | https://www.nasa.gov/hrp/risks/ | Index of 17 risk pages (`nasa.gov/reference/risk-of-…`). These link to evidence reports and Human Research Roadmap entries. |
| Ongoing HRP Research in Space | https://www.nasa.gov/hrp/ongoing-hrp-research/ | Summary pages per investigation (Artemis II ARCHeR, Standard Measures, Tempus Pro and others). Prose only, no data downloads. |
| CSA: Monitoring Radiation for Astronaut Health | https://www.asc-csa.gc.ca/eng/news/articles/2026/2026-02-26-monitoring-radiation-astronaut-health.asp | Non-NASA news article. It states that neutrons contribute "about 30% of total radiation on the ISS". |
| CSA: Why Do We Conduct Science Experiments in Space? | https://www.asc-csa.gc.ca/eng/iss/science/why-do-we-conduct-science-experiments-in-space.asp | Non-NASA. UNVERIFIED (not opened). |

### 1.2 2026 Submission and Judging guides
Both guides are dated 2026-11-13. VERIFIED.

- **Submission Guide:** https://www.spaceappschallenge.org/resources/project-submission-guide/
- **Judging & Awards Guide:** https://www.spaceappschallenge.org/resources/judging-awards-guide/

**Same as 2025:**
- Demo limits: a video of 30 s or less, or up to 7 slides including the title slide.
- Exceeding either limit makes the project ineligible for Global Judging.
- The form fields.
- The five criteria (Impact, Creativity, Validity, Relevance, Presentation), each scored 1–5.

**New or now explicit:**
- "AI" is renamed "Super Intelligence (SI)".
- *"SI should not be used to (come up with your idea of how to solve the challenge)."*
- The "Use of SI" answer is not shown to judges. In-project SI indicators (watermarks, disclosure text and metadata) are visible to them and evaluated.

**NASA data requirement:** *"To be eligible for a Global Award, you must use at least one NASA data or resource."*

**Relevance criterion:** *"It meaningfully integrates NASA data, utilizing it as a core component of the proposed solution rather than a superficial addition."*

**Non-NASA data:** No 2026 guide or FAQ prohibits non-NASA data or mentions NOAA. The NASA-only rule is a team decision, not a Space Apps rule.

**Global award dates:**
- Global Nominees: 2026-12-03
- Global Finalists: 2026-12-17
- Global Winners: 2027-01-26

---

## 2. Astronaut health data

### 2.1 NASA OSDR (Open Science Data Repository)
Public, no key.

**API**
- Study metadata: `https://osdr.nasa.gov/osdr/data/osd/meta/<id>`
- File list: `https://osdr.nasa.gov/osdr/data/osd/files/<id>?page=1&size=100` (pages start at 1)
- Download: `https://osdr.nasa.gov` + `remote_url`
- CSV files start with a UTF-8 BOM; decode with `utf-8-sig`.

| Study | Content | Clinical numbers? | Scenario | Status |
|---|---|---|---|---|
| [OSD-575](https://osdr.nasa.gov/osdr/data/osd/meta/575) | Inspiration4 serum comprehensive metabolic panel (glucose, Na, K, BUN, creatinine and more) plus immune and cardiovascular cytokine panels | Yes | Hypoglycemia, dehydration, inflammation | VERIFIED |
| [OSD-569](https://osdr.nasa.gov/osdr/data/osd/meta/569) | Inspiration4 complete blood count; 4 crew (C001–C004) × 7 timepoints (L-92 … R+194) | Yes | Hematology baseline | VERIFIED |
| [OSD-656](https://osdr.nasa.gov/osdr/data/osd/files/656?page=1&size=100) | Inspiration4 urine inflammation panel (`LSDS-64_Multiplex_urine.immune.AlamarPanel_TRANSFORMED.csv`) | Protein levels | Optional inflammation layer | VERIFIED |
| OSD-570, 571, 572, 573, 574, 630, 687 | Inspiration4 omics, microbiome, skin, capsule swabs | No | — | VERIFIED |
| OSD-903, 943 | Axiom astronauts: transcriptome and epigenome | No | — | VERIFIED |
| OSD-530 | JAXA CFE plasma cfRNA | No | — | VERIFIED |
| OSD-815–818 | Ground study: ex vivo donor-eye SANS model. **Not astronaut data.** | No | — | VERIFIED |

**Notes**
- **OSD-569 and OSD-575 contain only pre-flight and post-flight samples.** There are no in-flight measurements.
- Searches for "actigraphy", "heart rate", "blood pressure" and "sleep" found no human study with vitals in OSDR. VERIFIED.
- The OSDR search index (`/osdr/data/search?...&type=cgene`) misses some studies; for example, OSD-575 and OSD-656 do not appear in it.
- A full scan of study IDs was stopped at 483/1030 by OSDR rate limiting. **The list of human studies above is not complete.**
- ⚠️ NASA-only: Inspiration4 and Axiom are commercial missions. Their data is hosted and curated by NASA OSDR.

### 2.2 NASA Life Sciences Portal (NLSP), which replaces LSDA
- **Search API (open, no login):** `https://nlsp.nasa.gov/api/v1/search/multi?types=DATASETS&q=<term>&page=1&limit=60`. VERIFIED.
- Each record has an `availability` field: "Available online", "Available offline", or available via a data request.

**Public ("Available online") human datasets found** (VERIFIED as listings):

| Search term | Hits | Examples |
|---|---|---|
| orthostatic | 52 | Midodrine vs post-flight orthostatic hypotension; Florinef; Kentavr tilt test (BP/HR/SV); Shuttle entry, landing and egress HR; post-flight stand tests |
| blood pressure | 158 | In-flight Holter monitoring; arterial pressure during descent |
| sleep | 79 | NEEMO 5 actigraphy; melatonin trial; Nightcap sleep study; intermittent bright light |
| actigraphy | 8 | UTMB Bedrest Campaign 1 actigraphy |
| metabolic rate | 28 | Apollo EVA metabolism; Constellation EVA systems tests; water turnover |
| carbon dioxide | 143 | Mir ppCO2 (mmHg); Lunar-Mars Life Support Test CO2 |
| SpO2 | 1 | NEEMO physiologic monitoring |

- **UNVERIFIED:** the download path for these tables. The record details page (`/search/data/details/<id>`) returned "Application Error".
- **LSAH (Lifetime Surveillance of Astronaut Health):** https://nlsp.nasa.gov/resources/lsah is protected under the Privacy Act and HIPAA. Only grouped data is shown. **No individual astronaut clinical data is public.** VERIFIED.

### 2.3 data.nasa.gov
- "Flight Crew Physiological Data for Crew State Monitoring" (NASA Langley): pilot flight-simulator data, **not astronaut data**. Public ZIP files. Catalog entry VERIFIED; files not downloaded.
- "EVA – US and Russia" log: catalog entry VERIFIED. UNVERIFIED: the CSV download, which failed.

---

## 3. Medical thresholds and evidence (NASA documents on NTRS)

- **humanresearchroadmap.nasa.gov was unreachable on 2026-11-14** (curl and browser both failed). Links on nasa.gov risk pages point to it. **Use the NTRS copies below.**
- Human System Risk Board (29 risks, one page each, with DAG narrative PDFs): https://www.nasa.gov/directorates/esdmd/hhp/human-system-risk-board/. VERIFIED.
- UNVERIFIED: the PDF contents (the download was cut off).

| Scenario | Source | Key values | Status |
|---|---|---|---|
| CO2 | [NTRS 20160012725](https://ntrs.nasa.gov/citations/20160012725), Cole et al., ISS Expeditions 2–31 | Each +1 mmHg CO2 doubles the odds of congestion. A 7-day average **< 1.5 mmHg** keeps congestion risk below 1%. Congestion appears about 1 mmHg below the headache threshold. | VERIFIED |
| CO2 | [NTRS 20100039645](https://ntrs.nasa.gov/citations/20100039645), James 2011 | Humans produce 0.9–1.2 kg CO2/day. Anecdotal symptoms just above 4 mmHg. Earth is about 0.3 mmHg. | VERIFIED |
| Hypoxia / suit | [NTRS 20230013523](https://ntrs.nasa.gov/citations/20230013523), Evidence Report: DCS and Hypoxia (2023) | EMU operates at **4.3 psia (222 mmHg)**; Orlan at 5.8 psia. Performance-limiting DCS in the EMU < 5%. | VERIFIED |
| Hypoxia | [NTRS 20150021491](https://ntrs.nasa.gov/citations/20150021491) | Exploration atmosphere 8.2 psia / 34% O2 | VERIFIED |
| Orthostatic | [NTRS 20150007319](https://ntrs.nasa.gov/citations/20150007319), Platts et al. | 4 of 6 ISS (~6-month) astronauts failed a 10-minute tilt test on landing day; 5 of 6 Mir astronauts failed. | VERIFIED (abstract) |
| Orthostatic | https://www.nasa.gov/reference/risk-of-cardiovascular-adaptations/ | Countermeasures: exercise, diet, lower-body compression garments, fluid loading before landing | VERIFIED |
| Sleep | [NTRS 20160003864](https://ntrs.nasa.gov/citations/20160003864), Flynn-Evans et al. | Astronauts sleep less in flight. Nominal work limit 6.5 h/day and 48 h/week. | VERIFIED |
| SPE | [NTRS 20160003870](https://ntrs.nasa.gov/citations/20160003870), Evidence Report: Acute Radiation Syndromes from SPEs | The most likely acute effects are prodromal: nausea, vomiting, anorexia, fatigue. | VERIFIED; exact limit values UNVERIFIED |
| SPE shelter | [NTRS 20140000593](https://ntrs.nasa.gov/citations/20140000593), Walker et al. 2013 (computed with OLTARIS) | See table below | VERIFIED |
| EVA | [NTRS 20210024317](https://ntrs.nasa.gov/citations/20210024317), Hoffmann | Heart rate to metabolic rate model from 132 Shuttle/ISS EVAs; RMSE < 200 BTU/hr | VERIFIED |
| Medication use | [NTRS 20140017010](https://ntrs.nasa.gov/citations/20140017010), Wotring | Sleep-aid use on ISS is about 10× higher than on Earth. | VERIFIED |
| Medical conditions | [NTRS 20220003136](https://ntrs.nasa.gov/citations/20220003136), Accepted Medical Condition List for Exploration | NASA's list of medical conditions | Abstract only |
| Behavioral | [NTRS 20160004365](https://ntrs.nasa.gov/citations/20160004365) | — | UNVERIFIED (not opened) |
| Nutrition | [NTRS 20050164251](https://ntrs.nasa.gov/citations/20050164251), Smith & Zwart 2005 | ISS crew ate about 80% of recommended energy. | VERIFIED (abstract) |

**SPE shelter, NTRS 20140000593, Table 1.** Effective dose in mSv inside the NASA HDU habitat:

| Location | Xapsos 95% | Aug 1972 (King) | Aug 1972 (Band) | Sep 1989 (OLTARIS) | Sep 1989 (Band) |
|---|---|---|---|---|---|
| Crew quarters | 450 | 311 | 190 | 106 | 102 |
| Main section | 361 | 225 | 139 | 89 | 82 |

Water needed for a 50% dose reduction: 2.3–5.3 g/cm².

**⚠️ NASA-only: ISS medication formulary**
- Diaz et al. 2024, npj Microgravity (https://doaj.org/article/44281b90a3514c2d934c467d2d80c255) is **not NASA-authored**. The authors are university-affiliated and the list was obtained through FOIA. I found the authorship in a search snippet and did not open the article.
- No NASA-published ISS formulary was found; an NTRS search returned no hits.
- **NASA-origin alternatives:**
  - Wotring (above).
  - NLSP "Stability of Pharmacotherapeutic Compounds". Available online; contents UNVERIFIED.
  - LSAH "Therapeutics & Clinical Care" documents (21). UNVERIFIED, not opened.

---

## 4. Environment and radiation data

### 4.1 OSDR Environmental Data App (EDA)
- URL: https://visualization.osdr.nasa.gov/eda/ (the old page `nasa.gov/reference/osdr-environmental-data` redirects here). VERIFIED.
- Contains **real ISS cabin CO2 (ppm), temperature, relative humidity, and radiation (GCR and SAA dose components)** at 5-minute steps, for Rodent Research and other mission windows.
- Example: https://visualization.osdr.nasa.gov/eda/dashboard_RR-12 starts 2019-04-17.
  - ISS CO2 is about 2,500–3,000 ppm; ground control is about 420–520 ppm.
  - Relative humidity is about 39%.
- Use: CO2 scenario baseline and a "normal ISS" environment panel.
- UNVERIFIED: the units of the GCR and SAA arrays.

### 4.2 OSDR RadLab API
- Base: `https://visualization.osdr.nasa.gov/radlab/api/`. No key.
- The documentation page returned HTTP 500 today; it was read from the [Wayback copy of 2024-05-22](http://web.archive.org/web/20240522074550/https://visualization.osdr.nasa.gov/radlab/gui/data-api/).
- Filters: `celestial_body`, `spacecraft`, `instrument`, `instrument_family`, `module`, `timestamp` (with `<`, `<=`, `>=`, `>`).
- Fields:
  - `absorbed_dose_rate` (μGy/h)
  - `dose_equivalent_rate` (μSv/h)
  - `flux` (cm⁻² sr⁻¹ s⁻¹)
  - `altitude` (km)
- Formats: `csv`, `tsv`, `json`, `html`.
- **Always filter by time.** Unfiltered queries take minutes or return nothing.

**Mars, September 2017 SPE (MSL-RAD-Surface).** VERIFIED: HTTP 200, 732 rows.

`https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Mars&timestamp%3E=2017-09-08&timestamp%3C2017-09-16&absorbed_dose_rate&format=csv`

| Day | Mean (μGy/h) | Max (μGy/h) |
|---|---|---|
| 09-08 | 7.97 | 8.77 |
| 09-09 | 7.95 | 11.26 |
| 09-10 | 7.99 | 8.79 |
| **09-11** | **17.87** | **23.18** |
| 09-12 | 12.92 | 17.54 |
| 09-13 | 7.29 | 10.66 |
| 09-14 | 6.56 | 7.55 |
| 09-15 | 6.81 | 8.79 |

**Artemis I, Orion (NASA HERA).** VERIFIED.

`…/api/?spacecraft=Orion&timestamp%3E=2022-11-10&timestamp%3C2022-12-20&absorbed_dose_rate&dose_equivalent_rate&trajectory&format=csv`

| Instrument | Median (μGy/h) | Peak (μGy/h) |
|---|---|---|
| HERA-HSU1 | 16.7 | 8,013 |
| HERA-HSU2 | 16.4 | 17,251 |

The peaks occur during the Van Allen belt transit on 2022-11-16.

**Instruments in RadLab:**

| Instrument | Where | Origin | Coverage seen |
|---|---|---|---|
| MSL-RAD-Surface | Mars surface | **NASA** | 2015-01 → 2024-06 |
| CRaTER-D1D2 | LRO, lunar orbit | **NASA** | 2009-07 → at least 2024-03 |
| BPD | BioSentinel, 1 AU heliocentric | **NASA** | 2022-11 → 2025-11 |
| Artemis-I-HERA-HSU1/2 | Orion | **NASA** | 2022-11-16 → 2022-12-11 |
| RAD-*-pre | ISS (NASA SRAG ISS-RAD, daily) | **NASA** | 2016-04 → 2020-01 |
| REM-* | ISS (NASA SRAG REM) | **NASA** | 2014-09 → 2024-02 |
| TEPC-*, IV-TEPC-* | ISS (NASA SRAG) | **NASA** | 2014-09 → 2020-01 |
| ⚠️ DosTel1/2 | ISS Columbus | DLR (Germany) | 2010 → 2024-10 |
| ⚠️ Artemis-I-EAD-MU01 | Orion | ESA | 2022-11 → 12 |
| ⚠️ Artemis-I-M42-SN127 | Orion | DLR | 2022-11 → 12 |
| ⚠️ Liulin-5, Liulin-MO | ISS, ExoMars TGO | Bulgaria / ESA–Roscosmos | 2008–2012, 2019–2023 |
| ⚠️ ALTEA-Survey, Lidal | ISS | ASI (Italy) | 2010–2012, 2020–2023 |
| ⚠️ TriTel-SM | ISS Service Module | Hungary | 2018–2019 |

Chang'e-4 LND is **not** in RadLab (the query returned no rows). This corrects `ARASTIRMA_RAPORU.md`.

### 4.3 Other NASA radiation data
- **ISS TEPC on CDAWeb (NASA SRAG):** VERIFIED. Coverage 2010-04-29 → 2018-04-11.
  - HAPI: `https://cdaweb.gsfc.nasa.gov/hapi/data?id=ISS_DOSANL_TEPC&time.min=…&time.max=…&parameters=DOSE,DOSE_EQ60`
  - Fields: DOSE in μGy/min, DOSE_EQ60 in μSv/min.
  - Files: https://spdf.gsfc.nasa.gov/pub/data/international_space_station_iss/dos_tepc/
  - ⚠️ data.nasa.gov describes a PS-TEPC, which is a JAXA design. The origin needs confirming.
- **MSL RAD in PDS PPI:** https://pds-ppi.igpp.ucla.edu/data/MSL-M-RAD-3-RDR-V1.0/. VERIFIED.
  - Raw PDS3 RDR files whose labels contain dose rates (`TOT_DOSE_*` in μGy/h).
  - Backup only; byte-offset parsing is needed.
- **OLTARIS:** https://oltaris.nasa.gov/ requires an account and administrator approval, and has no API. Do not call it from the app; use the NTRS 20140000593 table above. VERIFIED.

---

## 5. Live APIs

### 5.1 JPL Horizons: Mars light-time
VERIFIED: HTTP 200, 1.9 s, no key.

`https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='499'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='OBSERVER'&CENTER='500@399'&START_TIME='2026-11-14'&STOP_TIME='2026-11-16'&STEP_SIZE='1%20h'&QUANTITIES='20,21'`

- Parse the `result` text between `$$SOE` and `$$EOE`.
- Columns: `delta` (AU), `deldot` (km/s), `1-way_down_LT` (minutes).
- `QUANTITIES='20'` alone does not include light time.

| UTC | Distance (AU) | One-way light time (min) |
|---|---|---|
| 2026-11-14 00:00 | 1.32436 | 11.014 |
| 2026-11-15 00:00 | 1.31585 | 10.944 |
| 2026-11-16 00:00 | 1.30733 | 10.873 |

**Range 2026–2035** (daily steps):
- Minimum: **3.164 min** on 2035-09-12.
- Maximum: **22.248 min** on 2034-08-20.

### 5.2 NASA DONKI: space weather events
- **Use `https://ccmc.gsfc.nasa.gov/DONKI/WS/get/<TYPE>?startDate=…&endDate=…`.**
  - No key and no rate limit observed.
  - All types tested OK: FLR, SEP, CME, CMEAnalysis, GST, IPS, MPC, RBE, HSS, WSAEnlilSimulations, notifications.
  - VERIFIED.
- **Do not use `api.nasa.gov/DONKI`:**
  - FLR returned 404.
  - `DEMO_KEY` allowed only 10 requests; the 11th returned 429 `OVER_RATE_LIMIT`.
  - The window is rolling and still at 0 after 30 minutes.
- **Do not use `kauai.ccmc.gsfc.nasa.gov`:** the TLS connection was reset.

**Real events** (VERIFIED):

| Date (UTC) | Type | Detail |
|---|---|---|
| 2017-09-06 12:02 | FLR | X9.3 |
| 2017-09-10 16:06 | FLR | X8.2, with SEP events at 16:25 (>100 MeV) and 16:45 (>10 MeV), and a CME at 2,650 km/s |
| 2024-05-11 01:23 | FLR | X5.8 |
| 2024-05-14 16:51 | FLR | X8.7 |

**⚠️ NASA-only:** DONKI is a NASA (CCMC) catalog. However:
- Flare classes come from NOAA GOES XRS/EXIS.
- The >10 and >100 MeV SEP detections come from NOAA GOES SEM/EPS/SEISS.
- GST Kp values carry `"source":"NOAA"`.
- SEP events from STEREO-A IMPACT (NASA) and SOHO COSTEP (ESA/NASA mission) are also listed.

### 5.3 NASA-mission proton flux (alternative to NOAA GOES)
There is **no NASA-only, calibrated, real-time ">10 MeV pfu" product.**

| Dataset | Origin | Notes | Status |
|---|---|---|---|
| CDAWeb HAPI `STA_L1_HET` | **NASA** STEREO-A | `Proton_Flux[11]`, bins 14.35–80 MeV, 1-minute cadence, 2006-12 → 2025-12-31. 2024-05-10/11 peak 8.9 /(cm²·sr·s·MeV) at 14 MeV. 2017-09 is weak (far side of the Sun). | VERIFIED |
| iSWA HAPI `imap_hit` | **NASA** IMAP | Live 1-minute count rates (counts/s), not calibrated flux | VERIFIED |
| iSWA HAPI `ace_sis_P5M` | ⚠️ NASA ACE, real-time stream processed by NOAA SWPC | >10 / >30 MeV integral flux, 5-minute cadence; can spike | VERIFIED |
| CDAWeb `SOHO_ERNE-HED_L2-1MIN` | ⚠️ ESA/NASA mission, Finnish instrument | Gaps in large events | VERIFIED |
| CDAWeb `OMNI_HRO_5MIN` PR-FLX | ⚠️ NOAA GOES data redistributed by NASA | Fill values after 2020-03 | Do not use |

- CDAWeb HAPI catalog: https://cdaweb.gsfc.nasa.gov/hapi/catalog
- iSWA HAPI catalog: https://iswa.gsfc.nasa.gov/IswaSystemWebApp/hapi/catalog
- The local DNS resolver failed for `cdaweb.gsfc.nasa.gov` and `pds-ppi.igpp.ucla.edu` during testing. **The app needs retries and a local cache.**

---

## 6. Recommended integrations (NASA-only)

1. **JPL Horizons:** live light-time delay. Cache an hourly table for 14–15 Nov and the daily 2026–2035 table.
2. **OSDR RadLab:**
   - Mars Sep 2017 SPE replay.
   - Artemis I HERA, ISS-RAD, REM and TEPC for ISS and deep-space baselines.
   - Use only the NASA instruments in §4.2.
3. **OSDR OSD-575 and OSD-569:** real crew blood baselines and reference ranges.
4. **OSDR EDA:** real ISS cabin CO2, temperature and humidity for the CO2 scenario.
5. **DONKI via `ccmc.gsfc.nasa.gov`:** event catalog for Sep 2017 and May 2024. Label or drop the NOAA-derived fields (team decision).
6. **CDAWeb `STA_L1_HET`:** NASA-mission proton flux for the May 2024 replay.
7. **NTRS evidence reports (§3):** thresholds and countermeasures.
8. **NLSP:** orthostatic, sleep, EVA and CO2 datasets once the download path is confirmed.

---

## 7. Open team decisions

1. **Medication recommendations** (#11): keep the non-NASA ISS formulary paper (Diaz 2024), or limit recommendations to NASA sources (Wotring, HSRB)?
2. **NOAA-derived DONKI fields** (#12): label them "NOAA instrument via NASA DONKI", or drop them?
3. **Partner and commercial data** (#13): do partner instruments in RadLab (DLR, ESA, ASI) and commercial missions curated by OSDR (Inspiration4, Axiom) count as NASA data?

---

## 8. Not verified

- NLSP table download path (the details page crashed).
- Contents of the HSRB risk-record and DAG PDFs (the download was cut off).
- Complete list of OSDR human studies (rate limited at 483/1030).
- Units of the GCR and SAA arrays in OSDR EDA.
- Whether a NASA-published ISS formulary exists.
- humanresearchroadmap.nasa.gov (unreachable on 2026-11-14).
- Simonsen & Nealy 1991, NASA TP-3079 ([NTRS 19910008686](https://ntrs.nasa.gov/citations/19910008686)): the PDF is a scanned image and was not read.
- Exact SPE dose limits in NTRS 20160003870.

---

## 9. Values that supersede `ARASTIRMA_RAPORU.md` (2026-11-09)

| Topic | 2026-11-09 | 2026-11-14 | Source |
|---|---|---|---|
| CO2 congestion threshold | 7-day average < 2.5 mmHg (OCHMO-TB-004) | **< 1.5 mmHg** keeps congestion risk < 1% (headache threshold is about 1 mmHg higher) | NTRS 20160012725 |
| Light-time "today" | 11.37 min (2026-11-09) | 11.01 min (2026-11-14 00:00 UTC). Range 2026–2035: 3.16–22.25 min | JPL Horizons |
| Compression garments | Unverified | **Verified** as a NASA orthostatic countermeasure | nasa.gov cardiovascular risk page |
| Chang'e-4 LND in RadLab | Listed | **Not present** | RadLab query |
| NOAA SWPC as a live source | Recommended (#2) | **Excluded** by the team NASA-only rule; use the NASA alternatives in §5.3 | `CLAUDE_HAFIZASI.md` §2 |
| DONKI | api.nasa.gov, 503 | api.nasa.gov is unreliable; use `ccmc.gsfc.nasa.gov/DONKI/WS/get/` | §5.2 |
| ISS formulary (Diaz 2024) | Used as the drug reference | ⚠️ Not NASA-authored; needs a team decision | §3 |

---

## 10. Gap analysis: conditions and features not yet covered

**Checked on:** 2026-11-14.
**Scope:** compares the six current scenarios (SPE, EVA dehydration/hypoglycemia, circadian/stress, suit hypoxia, cabin CO2, orthostatic intolerance) with NASA medical-risk data and the challenge text.
**Status:** this section lists options. Which gaps become scenarios is a team scope decision (#14).

### 10.1 NASA Integrated Medical Model (IMM) probability data

OCHMO published IMM results on nasa.gov in Aug–Sep 2026. This is NASA data that can sit in the core of a "mission medical risk" layer.

| Document | URL | Content | Status |
|---|---|---|---|
| IMM overview | https://www.nasa.gov/nasa-integrated-medical-model-imm-overview/ | What the model is | VERIFIED |
| Ranking of Medical Conditions by Probability (iMED v5.0, 2015-02-02) | https://www.nasa.gov/wp-content/uploads/2026/08/ranking-of-medical-conditions-by-probability.pdf | Probability of ≥1 event for 98 of 100 conditions across 12 design reference missions (LEO 6 months to Planetary 36 months with 50 EVAs) | VERIFIED (HTTP 200, PDF; values read from the text) |
| Medical Risk Assessment for an Accelerated and Standard Mars Mission (S-20200303-423, 2020-04-13) | https://www.nasa.gov/wp-content/uploads/2026/08/medical-risk-assessment-for-an-accelerated-and-standard-mars-mission.pdf | Total events, evacuation and loss-of-crew probabilities, and the conditions that drive them | VERIFIED (HTTP 200, PDF) |
| Accepted Medical Condition List (NASA/TM-2022-220299) | https://ntrs.nasa.gov/api/citations/20220003136/downloads/NASA-T_1.pdf | Mars transit pilot: 194 conditions (135 "Should Plan to Treat"); Appendix B has per-condition probabilities | VERIFIED |

**LEO, 6 months, 1 EVA: probability of ≥1 event**

| Condition | Probability |
|---|---|
| Late insomnia | 0.912 |
| Skin abrasion | 0.821 |
| Skin rash | 0.821 |
| Space motion sickness | 0.730 |
| Eye abrasion (foreign body) | 0.722 |
| Late headache | 0.592 |
| Nasal congestion | 0.586 |
| Back pain (space adaptation) | 0.525 |
| UTI (female) | 0.490 |
| Respiratory infection | 0.453 |
| Barotrauma (ear/sinus) | 0.433 |
| VIIP/SANS | 0.341 |
| CO2-induced headache | 0.306 |
| Hearing loss | 0.094 |
| Fingernail delamination | 0.065 |

**Mars mission outcomes**

| Outcome | 426 days | 923 days |
|---|---|---|
| Total medical events | 156 | 603 |
| P(consider evacuation) | 0.0625 | 0.294 |
| P(loss of crew life) | 0.0057 | 0.0158 |

- **Top evacuation drivers, 426 days:** herpes zoster 15.4%, nephrolithiasis ~11.5%, sepsis 5.7%, UTI 4.2%, eye chemical burn 4.1%, dental abscess 3.7%, stroke 3.6%.
- **Top evacuation drivers, 923 days:** DCS after EVA 30.8%, herpes zoster 11.5%, UTI 8.0%, eye chemical burn 6.4%.
- **Top loss-of-crew drivers:** sepsis 31.8%, stroke 21.2%, medication overdose/adverse reaction 8.1%, ammonia exposure 7.5%, appendicitis 5.4%, burns from fire 5.3%.

**Implication:** the current scenarios cover rare, dramatic events. The model says the most frequent events are insomnia, skin, eye foreign body, space motion sickness, headache, congestion and back pain. On Mars, evacuation and death are driven by shingles, kidney stones, sepsis, DCS and toxic exposure.

### 10.2 Missing conditions, ranked for the hackathon

| # | Gap | Evidence | Onboard monitoring / NASA guidance | Public NASA data | Priority |
|---|---|---|---|---|---|
| A | Common space-adaptation and minor conditions: SMS, headache, congestion, back pain, insomnia, rash, eye foreign body, urinary retention | IMM table above. Shuttle: 94% of 219 person-flights used medication; 47% of users for SMS, 45% for sleep (Putcha 1999, PMID 10417009, abstract). ISS: skin rash 1.12 and upper-respiratory symptoms 0.97 events per flight-year ([Crucian 2016](https://doi.org/10.2147/IJGM.S114188)) | Symptom self-report (fits a body-map check-in) | IMM probabilities | High |
| B | Venous thrombosis (internal jugular vein) | Stagnant or reverse flow in 6 of 11 ISS crew; 1 occlusive clot ([Marshall-Goebel 2019](https://doi.org/10.1001/jamanetworkopen.2019.15011)). ASAP Q3-2026 minutes: 3 crew treated in flight for jugular and/or cerebral thrombosis ([minutes](https://www.nasa.gov/wp-content/uploads/2026/10/3rd-quarterly-2026-public-meeting-minutes-final-003.pdf)) | [OCHMO-MTB-007](https://www.nasa.gov/wp-content/uploads/2026/01/ochmo-mtb-007-venous-thrombosis-in-spaceflight.pdf) (rev 2026-01-08): jugular ultrasound surveillance for all ISS crew. If a clot is found: apixaban 10 mg twice daily × 7 days, then 5 mg twice daily; no EVA or ARED for ≥30 days. Stasis plus a risk factor: prophylaxis 2.5 mg twice daily. Watch for neck or facial swelling, headache and SANS signs | Protocol only, no patient data | High |
| C | SANS | NASA calls it "currently NASA's leading spaceflight-related health risk" ([HRP risks](https://www.nasa.gov/hrp/risks/)). 70% of long-duration crew show ≥1 sign; 18% clinically concerning ([OCHMO-MTB-001](https://www.nasa.gov/wp-content/uploads/2024/02/ochmo-mtb-001-sans.pdf)) | Eye exams at FD30, FD90 and R-30: OCT, fundoscopy, ocular ultrasound, tonometry, visual acuity, Amsler grid | Brief only | High |
| D | Kidney stones | About 11.5% of evacuations, 426-day Mars mission (IMM). No symptomatic in-flight stone in a U.S. crewmember to date, but urine chemistry is altered ([NTRS 20160013663](https://ntrs.nasa.gov/citations/20160013663), abstract). Potassium citrate held supersaturation at preflight levels in 30 crew ([Whitson 2009](https://doi.org/10.1016/j.juro.2009.07.010), abstract) | Fluid intake, urine output, ultrasound. Mitigations: hydration and exercise ([risk page](https://www.nasa.gov/directorates/esdmd/hhp/risk-of-renal-stone-formation/)) | OSD-575 BUN and creatinine | High (risk index); medium (full scenario) |
| E | Toxic exposure: ammonia, smoke/fire, chemical | Ammonia is 7.5% of loss-of-crew risk (IMM, 426 days). Past events include Apollo-Soyuz N2O4, STS-40 formaldehyde, Mir 1997 fire, and an ammonia release during EVA in 1998 ([OCHMO-TB-015](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-015-spaceflight-toxicology.pdf)) | Ammonia SMACs: 30 ppm for 1 h, 20 ppm for 24 h, 10 ppm for 7–180 days, 1.4 ppm for 360 days; irritation at about 100 ppm ([NTRS 20090015899](https://ntrs.nasa.gov/api/citations/20090015899/downloads/20090015899.pdf)) | Limits only | Medium-high |
| F | Immune dysregulation and viral reactivation | Herpesvirus shedding in 14 of 23 ISS astronauts (61%) ([Rooney 2019](https://doi.org/10.3389/fmicb.2019.00016), abstract). Herpes zoster is the top evacuation driver on the 426-day mission | Skin checks; saliva sampling is research only | OSD-575 cytokines (IL-6, CRP) | Medium |
| G | Bone and muscle loss, exercise compliance | Bone density −1 to −1.5% per month ([bone risk](https://www.nasa.gov/reference/risk-of-spaceflight-induced-bone-changes/)). Exercise prescription 1.5 h/day, 6–7 days/week; 12–38% of crew lose >20% strength or fitness ([fitness risk](https://www.nasa.gov/reference/risk-of-reduced-cardiorespiratory-and-musculoskeletal-fitness/)) | Daily exercise minutes | Rates only | Medium (cheap) |
| H | Behavioral health and cognition | Depressive symptoms in 2 of 7 Shuttle-Mir astronauts. Monthly WinSCAT cognitive test against a preflight baseline ([OCHMO-TB-016](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-016-behavioral-health.pdf)) | Mood scale, short cognitive test, private psychological conferences | Spaceflight Standard Measures data is request-only ([NTRS 20230014025](https://ntrs.nasa.gov/citations/20230014025)) | Medium |
| I | EVA-specific: DCS, fingernail delamination, water in helmet | DCS after EVA is 30.8% of evacuations on the 923-day mission (IMM). EVA 23 (2013-07-16): about 1.5 L of water entered the helmet ([NTRS 20140016950](https://ntrs.nasa.gov/citations/20140016950), abstract) | Joint pain and paresthesia symptoms; existing local file `kaynaklar/OCHMO-TB-037_DCS.pdf` | Briefs only | Medium (extend scenario 4) |
| J | Noise and hearing | Hearing loss probability 0.094 for 6-month LEO (IMM). Hazard limit 85 dBA; sleep areas NC-40 ([OCHMO-TB-035](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-035-acoustics.pdf)) | Noise dosimetry | Limits only | Low |

### 10.3 Challenge-text gaps

The challenge page (opened 2026-11-14) names these items, and ASTRA-TWIN does not cover them yet:

| Challenge item | Current state | Option |
|---|---|---|
| "Ultrasound images" as a data type | Not covered | A guided-ultrasound checklist (probe placement, ground guider) linked to B, C and D. No public NASA diagnostic ultrasound dataset was found: an OSDR "ultrasound" search returned non-spaceflight studies, and NASA Image Library hits are photos of crew scanning. |
| "Mood" and "daily sleep hours" | Only inside scenarios | Daily log on the body map |
| "Inadequate nutrition" (hazards text) | No scenario | ISS crew ate about 80% of recommended energy ([NTRS 20050164251](https://ntrs.nasa.gov/citations/20050164251), abstract) |
| Environments: spacecraft, station, rover, planetary or lunar base | ISS and Mars only | Environment selector |
| Prompts to take measurements | Planned, not scheduled | NASA schedule: eye exams at FD30 and FD90, jugular ultrasound surveillance, monthly cognitive test |

### 10.4 Data limits

- No public dataset gives in-flight heart rate, SpO2 or core temperature; these must be simulated.
- No public NASA diagnostic ultrasound dataset was found.
- Spaceflight Standard Measures (sleep, cognition, immune) is request-only.
- **UNVERIFIED:**
  - A 2015 ISS ammonia false alarm (search snippets only).
  - An EVA core-temperature +1 °F performance threshold (NASA OIG IG-17-018; the PDF is encrypted).

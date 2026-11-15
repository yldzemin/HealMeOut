# Project memory — ASTRA-TWIN (NASA Space Apps Challenge 2026)

This file defines the rules and working method for the AI assistant on this project. The project design (concept, architecture, interface, medical values, data sources) is in `docs/` (`TECH_OVERVIEW.md` shared, `BACKEND.md`, `FRONTEND.md`). Read them before writing code or making a design decision.

Talk with your user in Turkish. **The project language is 100% English:** everything written to the repo and to GitHub is English: code, code comments, variable and function names, documents, README, UI text, demo captions, commit messages, branch names, issues, PR titles and descriptions, review and issue comments. No Turkish documents (decision 2026-11-15). The Space Apps submission must be in English too.

> **Last update:** 2026-11-15 (hackathon day 2)
> **Open check:** the 2026 Project Submission and Judging & Awards guides were published on 13 November, but the site is built with JavaScript and could not be read. §2 is still based on the **2025 guide**. Open the guides in a browser before submitting and record the differences here.

## 1. Project

**ASTRA-TWIN:** a health-monitoring and digital-twin tool for the International Space Station (ISS) crew. It does not decide; it gives the crew medical officer (CMO) decision support. Details in `docs/TECH_OVERVIEW.md` §1–2.

**Scope decision (2026-11-14, #16):** the app is built for the **ISS** only. Moon and Mars missions appear only in the presentation, as a **future extension** ("the same system scales to Moon and Mars"). Mars-specific features (light delay of minutes, solar-conjunction blackout, Mars SPE data) stay out of the code; the data inventory keeps them with a "future extension" label.

## 2. Hackathon rules (critical)

Source: 2025 Project Submission Guide (3 October 2025). To be compared with the 2026 version.

- **Dates:** 14–15 November 2026. Submission opens on 14 November at 09:00 and closes on 15 November at 23:59, in the local time of the Team Owner's Local Event (for the Universal Event, where the Team Owner lives).
- **Data rule (team decision):** **NASA-origin** data is the core of the project; the Relevance criterion asks that NASA data not be "a superficial addition". Decided exceptions, each credited under "Space Agency Partner Data and Other References":
  - **#13:** data that a NASA archive hosts and curates counts as NASA data, with a tag that names the mission or instrument team and says why it is used (Inspiration4 baselines via NASA OSDR; DLR DosTel2 via NASA OSDR RadLab). No China-agency data (Participant Terms).
  - **#11:** the ISS medical kit list of Diaz et al. 2024 (CC BY 4.0) is used for medication names.
  - **#12** (NOAA-derived fields in DONKI) is still open; the app does not use DONKI.
- **Demo:** at most a 30-second video, or at most 7 slides including the title. The link must not need a password, permission or sign-up. The video needs English captions; the slides must be in English.
- **AI use:** allowed, but it must be described in the submission's "Use of Artificial Intelligence" field.
  - Generated content must not contain or alter the NASA logo or NASA design elements.
  - AI-generated images and videos need a visible watermark.
  - AI-generated audio, code and data must be stated in the description and in the metadata.
  - In this project, code and research are done with AI support; state this in the submission's "Use of SI" field.
- **Submission form fields:** Project Name, High-Level Project Summary, Link to Project Demo, Link to Final Project, Detailed Project Description, NASA Data, Space Agency Partner & Other Data, Use of Artificial Intelligence, Project Submission Verification. All fields in English.
- **Judging criteria** (2025): Impact, Creativity, Validity, Relevance, Presentation. Treat Presentation as equal in weight to the others.

## 3. File layout

```
CLAUDE.md                                     ← team rules (imports this file)
PROJECT_MEMORY.md                             ← this file: project rules and working method
docs/                                         ← project design: TECH_OVERVIEW.md (shared), BACKEND.md, FRONTEND.md, PITCH.md
backend/, frontend/                           ← code (backend/README.md: how to run, and the API)
RESEARCH_REPORT.md                            ← the full verified research, with sources
(online) Project scope document               ← first scope (Mars-focused, 9 November; ISS scope in #16): https://claude.ai/artifact/V9p1PiJczNXqhjp5WoEpUT
NASA_DATA_SOURCES.md                          ← verified NASA data inventory, ISS facts (§11)
PSYCHOLOGY_AND_LONG_DURATION.md               ← psychology research and module proposal, long-mission background
57110f2f9bef1ff11b47b3e897d3eee2.jpg          ← UI look-and-feel reference
data/osdr/, data/raw/                         ← raw NASA files (Inspiration4 panels, ISS TEPC, RadLab DosTel2, IMM)
data/cache/                                   ← API cache built by backend/ingest (works offline): baselines, ISS TEPC dose, partner dose, IMM
references/                                   ← NASA technical documents (OCHMO briefs, HRP RAP, M2M HHP)
```

## 4. Working rules

- **Do not assume.** Give a source for every numeric threshold and medical claim. If there is none, say "not verified". New findings go into RESEARCH_REPORT.md with their source.
- **Design changes go into `docs/`.** Code may not contradict a value or behaviour in `docs/`. The values in the "do NOT use in code" table of §4.1 never enter any code.
- spaceappschallenge.org pages are built with JavaScript; WebFetch returns an empty or old version. Use a browser or the Wayback Machine and state the date of the archived copy.
- **The demo must also work offline.** Every live API has a local cache (`data/cache/`; sources in `docs/BACKEND.md` §2).
- **Wiro MCP** is connected (personal account, about $16 coupon balance). It can be used to generate images, video or audio. Tell the user the cost before every paid run. AI-generated images need a watermark.

### 4.1 Wrong values in the draft: do NOT use in code

Wrong values from the first draft spec (9 November). Details and sources in RESEARCH_REPORT.md §4. The light-delay row applies only to the "future extension" (Moon/Mars) part of the presentation.

| Topic | Draft | Correct |
|---|---|---|
| Light delay (Mars, future extension) | fixed "22 MINUTES", "4–24 min" | Compute it from JPL Horizons. Range ~3–22 min (NSSDC distances). |
| Glucose threshold | <60 | ADA: <70 Level 1, <54 Level 2 |
| In-flight BP baseline | 120/80 | ~112/71. The real risk is orthostatic intolerance (on return). |
| HRV critical <20 ms | — | Define the metric (RMSSD); use a personal baseline and a z-score |
| Hydration loss | 1.5% per hour / 8% on EVA | 2.5 L per day (~0.13% of body mass per hour). On EVA 0.3–1.2% per hour. Performance drops after losing 2.5% of body mass. |
| Radiation background | 0.05 mSv/h | ISS ~0.2–0.5 mSv per day (Simonsen 2025) |
| Body temperature | fixed 37.0 | ~+1 °C drift on long missions (Stahn 2017) |
| Propranolol | radiation crisis | ❌ Remove. An SPE does not cause hypertension. The ISS has metoprolol. |
| Potassium iodide | SPE | ❌ Remove. An SPE is protons; KI protects only against radioiodine. |
| Antioxidant cocktail | radioprotection | ❌ No evidence in humans. 400 IU of vitamin E raised cancer risk (SELECT). Use shelter, dosimetry and ondansetron (nausea) instead. |
| Glucagon | <50 | First 15 g of oral glucose, re-check after 15 min (ADA). The ISS has no glucagon. |
| Melatonin | 5 mg | 3 mg on the ISS. The main countermeasure is light and sleep scheduling. |
| Epinephrine for depressurisation | — | ❌ The right response is to restore O2 and pressure. Epinephrine only for anaphylaxis or cardiac arrest. Model chain: pressure → PIO2 → SpO2. |
| Automatic transdermal dosing | — | ❌ No such hardware exists. Redesign as "recommendation + CMO approval". |
| Salt for low in-flight blood pressure | — | ❌ NASA reduces sodium in flight; salt + fluid loading only before atmospheric entry (OCHMO-TB-027). |

**Rule:** medication recommendations use only drugs on the ISS medical kit list (Diaz et al. 2024, doi:10.1038/s41526-024-00414-3, CC BY 4.0; decision #11; `IssFormulary` in the backend). Every recommendation shows its level of evidence and its source, and waits for the CMO. The UI carries the "Simulation, not medical advice" notice.

## 5. Open tasks

- [ ] Read the 2026 Project Submission and Judging & Awards guides in a browser and record the differences in §2.
- [ ] Rewrite the crisis scenarios with verified values and the ISS kit list (§4.1).
- [ ] Check what is still unverified (report §7): Vol 1 Rev C, the primary source of Hassler 2014, the EMU drink bag size.
- [ ] Cache the remaining ISS data in `data/cache/`: EDA CO2 and DONKI (#12) are not cached yet; the ISS TEPC dose (#30) and DLR DosTel2 (#13) are. Horizons only if the "future extension" presentation needs it.

## 6. GitHub rules

**Repo:** `git@github.com:yldzemin/healmeout.git` (https://github.com/yldzemin/healmeout)

**Remember that this is shared work.** The project is developed on 3 computers in parallel. Each computer runs an AI assistant, and each uses its own user's GitHub account.

### Synchronisation
- Before starting, update `main`: `git switch main && git pull --ff-only`.
- Before pushing, `git fetch origin`. If the branch has not been pushed yet, `git rebase origin/main`; if it has, `git merge origin/main`. No force push on any branch (`CLAUDE.md`).
- No direct push to `main`. Every change comes through a PR.
- Branch names: `feat/<module>`, `fix/<topic>`, `docs/<topic>` (e.g. `feat/scenario-simulator`).
- Commit messages in English.

### PR approval
- The AI assistant on each computer reviews the PRs that **do not belong to its own user** and approves them or requests changes (GitHub does not let a person approve their own PR). Shortcut: `/pr-review <number>`; to open a PR: `/pr-open`.
- For a PR with an **architectural decision** (stack, folder layout, data model, new dependency, structure of the scenario engine), ask your user before approving.
- Tell your user about a truly critical approval or rejection (data loss, security, a wrong value from §4.1 coming back, non-NASA data, a breach of the submission rules).
- Any PR that changes the `docs/TECH_OVERVIEW.md` §4 data model counts as an architectural decision.
- Follow PRs by subscribing to their notifications. Do not poll.

### Issue and PR comments
Comments are the **shared memory** of the AI assistants on the three computers. An assistant cannot see another's conversation; it sees only the repo, the issues and the comments.

- **Every task starts with an issue.** The issue states the goal, the acceptance criteria and which files will change. Write `Closes #N` in the PR description.
- **Avoid conflicts:** when you start a task, comment "Working on these files: …" on the issue and assign it to yourself. Before touching files assigned to someone else, comment on that issue.
- **Review comments are line-level** (`gh pr review` or inline comments). Each comment says what is wrong, why, and the suggested fix. A bare "LGTM" is not enough; say what you checked.
- **Write decisions down:** when a discussion is settled, write the decision and its reason as a separate comment on the issue and add the `decision` label. Design decisions also go into `docs/`, working-rule decisions into this file.
- **Ask questions in comments:** questions to the AI assistant or the person on another computer go in a comment on the relevant issue or PR. Use `@` only for real GitHub usernames.
- **The AI tool's name is never written (decision #44):** no tool name, signature, co-author line or "Generated with …" line in commits, PRs, issues or comments. AI use is declared only in the submission's "Use of SI" field.
- **Labels:** `scenario`, `ui`, `data`, `docs`, `decision`, `blocked`, `needs-human`. Anything that needs a human decision gets `needs-human`.

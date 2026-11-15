// Personal screen content for one astronaut.
// Real values come from NASA OSDR (mock/baselines.json); everything else is
// simulated and labelled so. Every threshold and claim carries a source.
// Thresholds marked "our design" are not NASA criteria.

import baselines from "@/mock/baselines.json";
import type { Provenance, StatusLevel } from "@/types";
import { IMM_SOURCE, profileFor, type CrewProfile } from "@/data/crewProfiles";

export type RegionId = "head" | "eyes" | "heart" | "lungs" | "blood" | "musculo" | "whole";

export type MeasuredHow = "live" | "periodic" | "sample-return" | "research-only";

export interface Source {
  label: string;
  url: string;
}

export interface Indicator {
  id: string;
  label: string;
  value: string;
  unit: string;
  baseline?: string;
  provenance: Provenance;
  measuredHow: MeasuredHow;
}

export interface TrendPoint {
  label: string;
  value: number;
}

export interface Trend {
  title: string;
  unit: string;
  provenance: Provenance;
  points: TrendPoint[];
  band?: { low: number; high: number; label: string };
}

export interface Region {
  id: RegionId;
  label: string;
  level: StatusLevel;
  status: string;
  indicators: Indicator[];
  trend?: Trend;
  meaning: string;
  todo: string;
  method: string;
  sources: Source[];
  /** Commercial-mission tag shown next to real Inspiration4 values (#30). */
  missionTag?: string;
  /** Backend alert behind this region's level, if any (for CMO approve / decline). */
  alertId?: string;
  /** The CMO's decision on that alert, once made. */
  alertStatus?: "open" | "approved" | "rejected";
}

/** Same wording as the backend tag (TECH_OVERVIEW §4.4); values from the OSDR study metadata. */
export const INSPIRATION4_TAG =
  "Commercial mission · SpaceX Inspiration4 · 16–18 Sep 2021 · low Earth orbit · data hosted by NASA OSDR";

const SRC = {
  osd575: { label: "NASA OSDR OSD-575 (Inspiration4 blood panel)", url: "https://osdr.nasa.gov/bio/repo/data/studies/OSD-575" },
  barger: { label: "Barger et al. 2014, Lancet Neurology", url: "https://doi.org/10.1016/S1474-4422(14)70122-X" },
  hrpSleep: { label: "NASA HRP risk: sleep loss and circadian desynchronization", url: "https://humanresearchroadmap.nasa.gov/Risks/risk.aspx?i=100" },
  ssla: { label: "NASA ISS lighting protocol (NTRS 20160006727)", url: "https://ntrs.nasa.gov/api/citations/20160006727/downloads/20160006727.pdf" },
  co2: { label: "NASA OCHMO-TB-004 Carbon dioxide", url: "https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf" },
  sans: { label: "NASA OCHMO-MTB-001 SANS", url: "https://www.nasa.gov/wp-content/uploads/2024/02/ochmo-mtb-001-sans.pdf" },
  norsk: { label: "Norsk et al. 2015, Journal of Physiology", url: "https://doi.org/10.1113/jphysiol.2014.284869" },
  water: { label: "NASA OCHMO-TB-027 Water", url: "https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf" },
  atmo: { label: "NASA OCHMO-TB-003 Habitable atmosphere", url: "https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-003-habitable-atmosphere.pdf" },
  ada: { label: "ADA Standards of Care 2025, hypoglycemia", url: "https://doi.org/10.2337/dc25-S006" },
  bone: { label: "NASA: Risk of spaceflight-induced bone changes", url: "https://www.nasa.gov/reference/risk-of-spaceflight-induced-bone-changes/" },
  dose: { label: "Simonsen 2025, NASA (NTRS 20250006168)", url: "https://ntrs.nasa.gov/api/citations/20250006168" },
  stahn: { label: "Stahn et al. 2017, Scientific Reports", url: "https://doi.org/10.1038/s41598-017-15560-w" },
  bioMonitor: { label: "CSA Bio-Monitor", url: "https://www.asc-csa.gc.ca/eng/sciences/bio-monitor.asp" },
} satisfies Record<string, Source>;

type Analyte = {
  label: string;
  unit: string;
  refLow: number | null;
  refHigh: number | null;
  baselineMean: number | null;
  baselineSd: number | null;
  points: { timepoint: string; value: number }[];
};

function analyte(crewId: string, id: string): Analyte | undefined {
  const crew = (baselines.crew as Record<string, Record<string, Analyte>>)[crewId];
  return crew?.[id];
}

function bloodIndicator(crewId: string, id: string): Indicator | undefined {
  const a = analyte(crewId, id);
  if (!a || a.baselineMean === null) return undefined;
  const last = a.points.find((p) => p.timepoint === "L-3");
  return {
    id,
    label: `${a.label} (pre-flight, L-3)`,
    value: last ? String(last.value) : "—",
    unit: a.unit,
    baseline: a.baselineSd === null ? `${a.baselineMean}` : `${a.baselineMean} ± ${a.baselineSd}`,
    provenance: "real",
    measuredHow: "sample-return",
  };
}

function bloodTrend(crewId: string): Trend | undefined {
  const a = analyte(crewId, "glucose");
  if (!a || a.baselineMean === null || a.baselineSd === null) return undefined;
  return {
    title: "Glucose, pre- and post-flight",
    unit: a.unit,
    provenance: "real",
    points: a.points.map((p) => ({ label: p.timepoint, value: p.value })),
    band: {
      low: Math.round((a.baselineMean - 2 * a.baselineSd) * 10) / 10,
      high: Math.round((a.baselineMean + 2 * a.baselineSd) * 10) / 10,
      label: "Your baseline ± 2 SD",
    },
  };
}

const LAST_7 = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const series = (values: number[]): TrendPoint[] => values.map((value, i) => ({ label: LAST_7[i], value }));

/**
 * Regions for one astronaut. Real blood baselines exist for C1–C4 only.
 * `live`: the backend's condition alerts set the levels, so only an L0 story (information, no level)
 * is applied here; live.ts adds the others when their alert is active.
 */
export function getRegions(crewId: string, live = false): Region[] {
  const hasRealBlood = analyte(crewId, "glucose") !== undefined;
  const p = profileFor(crewId);
  const lastNight = p.sleep7[p.sleep7.length - 1];
  const blood = ["glucose", "sodium", "potassium", "bun"]
    .map((id) => bloodIndicator(crewId, id))
    .filter((x): x is Indicator => x !== undefined);

  const regions: Region[] = [
    {
      id: "head",
      label: "Head and brain",
      level: "L0",
      status: "Sleep, mood and reaction time are in your usual range.",
      indicators: [
        { id: "sleep", label: "Sleep last night", value: lastNight.toFixed(1), unit: "h", baseline: "scheduled 8.5", provenance: "simulated", measuredHow: "live" },
        { id: "mood", label: "Mood (check-in)", value: String(p.mood), unit: "/ 100", provenance: "simulated", measuredHow: "periodic" },
        { id: "rt", label: "Reaction test", value: String(p.reactionMs), unit: "ms", baseline: `best ${p.reactionBestMs}`, provenance: "simulated", measuredHow: "periodic" },
      ],
      trend: {
        title: "Sleep, last 7 nights",
        unit: "h",
        provenance: "simulated",
        points: series(p.sleep7),
      },
      meaning:
        "ISS crews sleep about 6.1 hours a night on average, against 8.5 hours scheduled. Short sleep slows reaction time and makes decisions harder, which NASA tracks as a spaceflight risk. High cabin CO2 can also cause headaches and poor sleep.",
      todo: "Keep your regular sleep window. Switch your module lights to the pre-sleep setting an hour before bed.",
      method:
        "Sleep from a wrist activity monitor (live). Mood from your daily check-in. Reaction time from the weekly 3-minute test. The under-6-hours flag is our design, not a NASA threshold. All values simulated.",
      sources: [SRC.barger, SRC.hrpSleep, SRC.ssla, SRC.co2],
    },
    {
      id: "eyes",
      label: "Eyes",
      level: "L0",
      status: `No change at your last eye exam (${p.lastEyeExam}). Next exam: ${p.nextEyeExam}.`,
      indicators: [
        { id: "exam", label: "Last eye exam", value: p.lastEyeExam, unit: "no change", provenance: "simulated", measuredHow: "periodic" },
        { id: "next", label: "Next exam", value: p.nextEyeExam, unit: "", provenance: "simulated", measuredHow: "periodic" },
      ],
      meaning:
        "In microgravity, fluid shifts toward the head and can change the shape of the eye and the optic nerve. NASA calls this SANS and checks every long-duration crew member with scheduled eye exams.",
      todo: "Nothing to do now. Report any blurred near vision before your next exam.",
      method: "Exams at flight days 30 and 90: OCT, fundoscopy, ocular ultrasound, eye pressure and visual acuity (NASA schedule).",
      sources: [SRC.sans],
    },
    {
      id: "heart",
      label: "Heart",
      level: "L0",
      status: "Heart rate and blood pressure are within your usual range.",
      indicators: [
        { id: "hr", label: "Resting heart rate", value: String(p.hr), unit: "bpm", baseline: `${p.hrBaselineMean} ± ${p.hrBaselineSd}`, provenance: "simulated", measuredHow: "live" },
        { id: "bp", label: "Blood pressure", value: p.bp, unit: "mmHg", baseline: "~112/71 in flight", provenance: "simulated", measuredHow: "periodic" },
      ],
      trend: { title: "Resting heart rate, last 7 days", unit: "bpm", provenance: "simulated", points: series(p.hr7), band: { low: p.hrBaselineMean - 2 * p.hrBaselineSd, high: p.hrBaselineMean + 2 * p.hrBaselineSd, label: "Your baseline ± 2 SD" } },
      meaning:
        "In flight, blood pressure is a little lower than on Earth because fluid moves toward the upper body. The bigger risk comes later: when you return to gravity, standing up can make you dizzy.",
      todo: "No action now. Before landing, NASA uses weight-based fluid loading with salt tablets to prevent dizziness.",
      method: "Heart rate from a wearable shirt (live). Blood pressure from a cuff, periodic: continuous wrist blood pressure is not validated.",
      sources: [SRC.norsk, SRC.water, SRC.bioMonitor],
    },
    {
      id: "lungs",
      label: "Lungs",
      level: "L0",
      status: "Your blood oxygen is normal.",
      indicators: [
        { id: "spo2", label: "Blood oxygen (SpO2)", value: String(p.spo2), unit: "%", baseline: "normal 96–99", provenance: "simulated", measuredHow: "live" },
        { id: "rr", label: "Breathing rate", value: String(p.breathing), unit: "/ min", provenance: "simulated", measuredHow: "live" },
      ],
      meaning:
        "The ISS cabin air is like sea level, so oxygen should read the same as on Earth. A reading below 90% means low oxygen in the blood and needs attention right away.",
      todo: "No action now.",
      method: "Finger or shirt sensor (live). NASA sets the cabin limit as inspired oxygen pressure, not SpO2.",
      sources: [SRC.atmo, SRC.bioMonitor],
    },
    {
      id: "blood",
      label: "Blood and metabolism",
      level: "L0",
      status: hasRealBlood
        ? "Your pre-flight blood values are in range. Next sample is frozen and analysed on Earth."
        : "No real blood baseline for this crew member. Values are simulated.",
      indicators: blood,
      trend: hasRealBlood ? bloodTrend(crewId) : undefined,
      meaning:
        "Blood values tell you about energy, hydration and kidneys. Your baseline is your own pre-flight average, so a change means more than a single number on a chart. Glucose under 70 mg/dL is low; under 54 is very low.",
      todo: `If glucose is under 70 mg/dL: take 15 g of fast carbohydrate, check again after 15 minutes, and repeat if still low. Fluids today: ${p.fluidsL.toFixed(1)} of 2.5 L.`,
      method:
        "Real values from NASA OSDR (Inspiration4 crew, pre- and post-flight). On the ISS, blood samples are frozen and analysed after return; on-board analysers are still tech demos.",
      sources: [SRC.osd575, SRC.ada, SRC.water],
      missionTag: hasRealBlood ? INSPIRATION4_TAG : undefined,
    },
    {
      id: "musculo",
      label: "Bones and muscles",
      level: "L0",
      status:
        p.exercise.planned === 0
          ? "No exercise session planned today."
          : p.exercise.done >= p.exercise.planned
            ? "Today’s exercise is done."
            : "Log today’s exercise session to keep your streak.",
      indicators: [
        { id: "ex", label: "Exercise logged today", value: String(p.exercise.done), unit: `of ${p.exercise.planned} session${p.exercise.planned === 1 ? "" : "s"}`, provenance: "simulated", measuredHow: "periodic" },
      ],
      meaning:
        "Without gravity, bones lose 1–1.5% of their density each month on 4–6 month missions, and muscles weaken. Daily exercise is the main protection.",
      todo: p.exercise.done >= p.exercise.planned ? "Keep the daily routine." : "Do and log today’s planned session.",
      method: "Exercise log from the daily routine. Bone density is measured before and after flight.",
      sources: [SRC.bone],
    },
    {
      id: "whole",
      label: "Whole body",
      level: "L0",
      status: "Radiation dose and body temperature are normal for the ISS.",
      indicators: [
        { id: "dose", label: "Dose today", value: p.doseToday.toFixed(2), unit: "mSv", baseline: "ISS 0.2–0.5 / day", provenance: "simulated", measuredHow: "live" },
        { id: "temp", label: "Core temperature", value: p.tempC.toFixed(1), unit: "°C", baseline: "+~1 °C drift", provenance: "simulated", measuredHow: "live" },
      ],
      meaning:
        "On the ISS you receive about 0.2–0.5 mSv of radiation a day, about 40–90 mSv in six months. NASA’s career limit is 600 mSv. Resting body temperature also rises by about 1 °C over long missions.",
      todo: "No action now. In a solar storm, move to the shelter area when told.",
      method: "Station dosimeters (live). Core temperature from a wearable sensor.",
      sources: [SRC.dose, SRC.stahn],
    },
  ];
  return p.story && (!live || p.story.level === "L0") ? regions.map((r) => (r.id === p.story!.region ? withStory(r, p) : r)) : regions;
}

/** The crew member's own story replaces its region's status, meaning and next step. */
export function withStory(r: Region, p: CrewProfile): Region {
  const st = p.story;
  if (!st || st.region !== r.id) return r;
  const imm = st.imm ? `NASA IMM: ${st.imm.condition}, ${Math.round(st.imm.probability * 100)}% chance on a 6-month ISS mission.` : undefined;
  return {
    ...r,
    level: st.level,
    status: st.status,
    meaning: st.meaning,
    todo: st.todo,
    method: imm ? `${r.method} ${imm}` : r.method,
    sources: [...(st.imm ? [IMM_SOURCE] : []), ...(st.extraSource ? [st.extraSource] : []), ...r.sources],
  };
}

/** Indicators from the private check-in and reaction test: only the astronaut sees them (BACKEND.md §6). */
const PRIVATE_INDICATORS = new Set(["mood", "rt"]);

/** What a CMO or ground viewer may see of these regions: no check-in or reaction-test values. */
export function forViewer(regions: Region[], self: boolean): Region[] {
  if (self) return regions;
  return regions.map((r) => {
    const indicators = r.indicators.filter((i) => !PRIVATE_INDICATORS.has(i.id));
    return indicators.length === r.indicators.length
      ? r
      : {
          ...r,
          indicators,
          method: `${r.method} Mood and reaction-test results are private to the astronaut and not shown here.`,
        };
  });
}

export const LEVEL_RANK: Record<StatusLevel, number> = { L0: 0, L1: 1, L2: 2, L3: 3, L4: 4 };

export interface TodayTask {
  id: string;
  label: string;
  done: boolean;
}

/** What the user did in this session (check-in, reaction test). */
export interface Routine {
  checkinDone: boolean;
  reactionBestMs: number;
}

/** reactionBestMs: best measured in this session (Infinity until a test is run); profiles hold the earlier best. */
export const INITIAL_ROUTINE: Routine = { checkinDone: false, reactionBestMs: Number.POSITIVE_INFINITY };

/** Simulated baseline for the reaction test (median, ms). */
export const REACTION_BASELINE_MS = 265;

/** Nights under 6 h in the last 7 (simulated sleep trend), for check-in notes. */
export function recentShortNights(crewId: string): number {
  const head = getRegions(crewId).find((r) => r.id === "head");
  return head?.trend?.points.filter((p) => p.value < 6).length ?? 0;
}

/** Simulated daily routine for one crew member. Fluid target 2.5 L/day: CLAUDE_HAFIZASI.md §4.1. */
export function getToday(routine: Routine = INITIAL_ROUTINE, crewId = "C3") {
  const p = profileFor(crewId);
  const tasks: TodayTask[] = [
    { id: "checkin", label: "Daily check-in", done: routine.checkinDone },
    { id: "morning", label: "Morning vitals", done: true },
    { id: "exercise", label: p.exercise.planned === 0 ? "Exercise (none planned)" : "Exercise log", done: p.exercise.done >= p.exercise.planned },
    { id: "fluids", label: `Fluids 2.5 L (${p.fluidsL.toFixed(1)} L so far)`, done: p.fluidsL >= 2.5 },
  ];
  return {
    tasks,
    streakDays: p.streakDays + (routine.checkinDone ? 1 : 0),
    nextMeasurement: `Flight day ${p.missionDay} · evening vitals at 19:30 GMT`,
    reactionBestMs: Math.min(routine.reactionBestMs, p.reactionBestMs),
  };
}

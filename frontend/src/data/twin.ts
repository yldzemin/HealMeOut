// Twin forecasts for one astronaut over the last 12 h and the next 12 h.
// getTwin(crewId) is the SIMULATED set, built from that crew member's profile (crewProfiles.ts):
// used in mock mode, and for fluids and temperature always. With the backend
// running, liveDose() and liveSleepDebt() replace two curves with the API's data. Rates come from
// sourced values in docs/BACKEND.md §3.2; thresholds marked "our design" are not NASA criteria.

import type { ApiEnvironment, ApiForecast } from "@/api/client";
import type { Source } from "@/data/personal";
import { profileFor } from "@/data/crewProfiles";

export interface TwinPoint {
  t: number; // hours from now (-12 … +12)
  actual?: number;
  forecast?: number;
  band?: [number, number];
}

export interface TwinMetric {
  id: string;
  title: string;
  unit: string;
  points: TwinPoint[];
  threshold?: { value: number; above: boolean; label: string };
  explain: string;
  source: Source;
  /** "nasa": real NASA data via the backend; "checkins": the astronaut's own check-ins; "simulated": modelled here. */
  provenance: "nasa" | "checkins" | "simulated";
  /** Built from psychological check-ins: only the astronaut sees it (BACKEND.md §6). */
  privateToSelf?: boolean;
  /** Decimals for the headline value (e.g. 37.0 °C rather than 37). */
  digits?: number;
  /** What the newest data changed (#81), e.g. "+2.1 h from your 17 Nov check-in". */
  change?: { text: string; tone: "worse" | "better" | "same" };
}

const HOURS = Array.from({ length: 25 }, (_, i) => i - 12);
const r1 = (x: number) => Math.round(x * 10) / 10;
const r2 = (x: number) => Math.round(x * 100) / 100;
const r3 = (x: number) => Math.round(x * 1000) / 1000;

/** Split one model into actual (t ≤ 0) and forecast (t ≥ 0) with a widening band. */
function series(f: (t: number) => number, spread: (t: number) => number, round = r1): TwinPoint[] {
  return HOURS.map((t) => {
    const v = round(f(t));
    if (t < 0) return { t, actual: v };
    if (t === 0) return { t, actual: v, forecast: v, band: [v, v] as [number, number] };
    const s = spread(t);
    return { t, forecast: v, band: [round(v - s), round(v + s)] as [number, number] };
  });
}

const SLEEP_SOURCE: Source = { label: "Barger et al. 2014, Lancet Neurology", url: "https://doi.org/10.1016/S1474-4422(14)70122-X" };
const SLEEP_THRESHOLD = { value: 18, above: true, label: "18 h debt (our design)" };
const DOSE_SOURCE: Source = {
  label: "NASA SRAG ISS TEPC via CDAWeb",
  url: "https://cdaweb.gsfc.nasa.gov/hapi/data?id=ISS_DOSANL_TEPC&time.min=2017-09-04T00:00:00Z&time.max=2017-09-20T00:00:00Z&parameters=DOSE_EQ60&format=csv",
};

/** Fluid deficit vs the 2.5 L/day target, paced over 16 waking hours from 06:00; `drunk` litres by now (14 h awake). */
function fluidModel(drunk: number) {
  const awakeNow = 14;
  const rate = drunk / awakeNow;
  const targetPerH = 2.5 / 16;
  return (t: number) => {
    const awake = Math.min(16, Math.max(0, awakeNow + t));
    return targetPerH * awake - rate * awake;
  };
}

export function getTwin(crewId: string): TwinMetric[] {
  const p = profileFor(crewId);
  // Sleep debt: scheduled 8.5 h vs logged sleep, last 7 nights (Barger 2014). Planned sleep
  // tonight 6.5 h from T+3 to T+9.5 adds 2 h more debt.
  const nights = p.sleep7;
  const debtNow = nights.reduce((d, s) => d + (8.5 - s), 0);
  const sleepDebt = series(
    (t) => (t <= 3 ? debtNow : t >= 9.5 ? debtNow + 2 : debtNow + ((t - 3) / 6.5) * 2),
    (t) => 0.1 * t,
  );

  // Fluids: 2.5 L/day target (docs/TECH_OVERVIEW.md §5.1); the profile's litres so far, same pace from here on.
  const deficit = fluidModel(p.fluidsL);
  const fluid = series(deficit, (t) => 0.05 * t, r2);

  // Dose: ISS ~0.2–0.5 mSv/day (Simonsen 2025); this crew member's dosimeter rate and mission day.
  const perH = p.doseToday / 24;
  const dose = series((t) => p.missionDay * p.doseToday + perH * t, (t) => ((0.5 - 0.2) / 24 / 2) * t, r2);

  // Core temperature: resting value drifts ~+1 °C over long missions (Stahn 2017);
  // flat forecast with a growing band.
  const temp = series((t) => p.tempC + (t < 0 ? 0.05 * Math.sin(t) : 0), (t) => 0.02 * t, r2);

  return [
    {
      id: "fluid",
      title: "Fluid deficit vs 2.5 L/day target",
      unit: "L",
      points: fluid,
      threshold: { value: 0.8, above: true, label: "0.8 L behind (our design)" },
      explain: deficit(2) > 0 ? "At today’s pace you finish the day behind your fluid target." : "At today’s pace you reach your fluid target.",
      source: { label: "NASA OCHMO-TB-027 Water", url: "https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf" },
      provenance: "simulated",
    },
    {
      id: "sleep",
      title: "Sleep debt, last 7 nights",
      unit: "h",
      points: sleepDebt,
      threshold: SLEEP_THRESHOLD,
      explain: "A 6.5 h night tonight adds 2 h of debt against the 8.5 h schedule.",
      source: SLEEP_SOURCE,
      provenance: "simulated",
      privateToSelf: true,
    },
    {
      id: "dose",
      title: "Cumulative mission dose",
      unit: "mSv",
      points: dose,
      explain: "Normal ISS background. Career limit is 600 mSv.",
      source: { label: "Simonsen 2025, NASA (NTRS 20250006168)", url: "https://ntrs.nasa.gov/api/citations/20250006168" },
      provenance: "simulated",
    },
    {
      id: "temp",
      title: "Core temperature",
      unit: "°C",
      points: temp,
      explain: "Stable. Resting temperature runs about 1 °C higher on long missions.",
      source: { label: "Stahn et al. 2017, Scientific Reports", url: "https://doi.org/10.1038/s41598-017-15560-w" },
      provenance: "simulated",
      digits: 1,
    },
  ];
}

/** First hour from now (0 = already past it) where the forecast crosses the metric's threshold. */
export function firstCrossing(m: TwinMetric): number | null {
  if (!m.threshold) return null;
  const hit = m.points.find(
    (p) => p.t >= 0 && p.forecast !== undefined && (m.threshold!.above ? p.forecast >= m.threshold!.value : p.forecast <= m.threshold!.value),
  );
  return hit ? hit.t : null;
}

export interface PlanItem {
  id: string;
  text: string;
  why: string;
  due: string;
  /** Follows from sleep data (check-ins): only the astronaut sees it. */
  privateToSelf?: boolean;
}

/** Earth Twin plan from the flight surgeon (simulated). The astronaut or CMO decides. */
export const EARTH_PLAN: PlanItem[] = [
  { id: "fluid", text: "Drink an extra 500 mL before 22:00 GMT.", why: "Fluid deficit forecast", due: "T+2 h" },
  { id: "sleep", text: "Protect a full 8.5 h sleep window tonight: move the 23:00 task to tomorrow.", why: "Sleep debt forecast", due: "T+3 h", privateToSelf: true },
  { id: "rt", text: "Repeat the reaction test after tonight’s sleep.", why: "Short nights this week", due: "T+11 h", privateToSelf: true },
];

export interface ForecastCheck {
  metric: string;
  forecast: string;
  actual: string;
  error: string;
  provenance: "nasa" | "simulated";
}

const signed = (x: number, digits: number, unit: string) => `${x >= 0 ? "+" : ""}${x.toFixed(digits)} ${unit}`;

/** Forecast made 6 h ago vs what was measured now, for one crew member (simulated; the dose row is replaced when live). */
export function forecastCheck(crewId: string): ForecastCheck[] {
  const p = profileFor(crewId);
  const row = (metric: string, actual: number, error: number, digits: number, unit: string): ForecastCheck => ({
    metric,
    forecast: `${(actual - error).toFixed(digits)} ${unit}`,
    actual: `${actual.toFixed(digits)} ${unit}`,
    error: signed(error, digits, unit),
    provenance: "simulated",
  });
  return [
    row("Fluid deficit", fluidModel(p.fluidsL)(0), 0.07, 2, "L"),
    row("Core temperature", p.tempC, 0.05, 2, "°C"),
    row("Cumulative dose", p.missionDay * p.doseToday, 0.08, 2, "mSv"),
  ];
}

// ---------- Live curves from the backend ----------

/** Hourly dose-equivalent rates (μSv/h), oldest first. */
function doseRates(series: ApiEnvironment[]): number[] {
  return series
    .filter((e) => e.doseEqRate_uSv_h !== undefined)
    .sort((a, b) => a.timestamp.localeCompare(b.timestamp))
    .map((e) => e.doseEqRate_uSv_h!);
}

const mean = (xs: number[]) => xs.reduce((a, b) => a + b, 0) / xs.length;

/**
 * Cumulative dose from the real ISS TEPC series (the backend's cumulative_dose definition: each
 * hourly sample counts one hour, since the first loaded sample). Actual = the last 12 hourly
 * samples; forecast = the backend's T+12 forecast (last 24-h mean rate); band = the lowest to the
 * highest daily mean rate of the last 7 days.
 */
export function liveDose(series: ApiEnvironment[], forecasts: ApiForecast[]): TwinMetric | null {
  const rates = doseRates(series);
  const f12 = forecasts.find((f) => f.metric === "cumulative_dose" && f.horizonHours === 12);
  if (rates.length < 48 || !f12 || f12.points.length < 2) return null;
  const n = rates.length;
  const cum: number[] = [];
  let total = 0;
  for (const r of rates) {
    total += r / 1000;
    cum.push(total);
  }
  const v0 = f12.points[0].value;
  const perH = (f12.points[1].value - v0) / 12;
  const daily: number[] = [];
  for (let k = 0; k < 7 && n - 24 * (k + 1) >= 0; k++) daily.push(mean(rates.slice(n - 24 * (k + 1), n - 24 * k)) / 1000);
  const lo = Math.min(...daily);
  const hi = Math.max(...daily);
  const points = HOURS.map((t): TwinPoint => {
    if (t < 0) return { t, actual: r3(cum[n - 1 + t]) };
    if (t === 0) return { t, actual: r3(v0), forecast: r3(v0), band: [r3(v0), r3(v0)] };
    return { t, forecast: r3(v0 + perH * t), band: [r3(v0 + lo * t), r3(v0 + hi * t)] };
  });
  return {
    id: "dose",
    title: "Cumulative dose (ISS TEPC)",
    unit: "mSv",
    points,
    explain: `Real ISS data (Sep 2017), since the first loaded sample. Forecast: last 24-h mean, ${r3(perH * 24)} mSv/day. Instrument dose equivalent, not effective dose; career limit 600 mSv.`,
    source: DOSE_SOURCE,
    provenance: "nasa",
  };
}

/** The dose forecast made 6 h ago (last 24-h mean rate then) against the TEPC value now. */
export function liveDoseCheck(series: ApiEnvironment[]): ForecastCheck | null {
  const rates = doseRates(series);
  const n = rates.length;
  if (n < 31) return null;
  const sumTo = (end: number) => rates.slice(0, end).reduce((a, b) => a + b, 0) / 1000;
  const then = sumTo(n - 6);
  const forecast = then + (mean(rates.slice(n - 30, n - 6)) / 1000) * 6;
  const actual = sumTo(n);
  const err = actual - forecast;
  return {
    metric: "Cumulative dose",
    forecast: `${r3(forecast)} mSv`,
    actual: `${r3(actual)} mSv`,
    error: `${err >= 0 ? "+" : ""}${r3(err)} mSv`,
    provenance: "nasa",
  };
}

/** Hours of the standard ISS sleep period (21:30–06:00 GMT, Jones 2022) in the next `hours` hours. */
export function scheduledSleepHours(from: Date, hours: number): number {
  const start = from.getTime();
  const end = start + hours * 3_600_000;
  let total = 0;
  const day = Date.UTC(from.getUTCFullYear(), from.getUTCMonth(), from.getUTCDate()) - 86_400_000;
  for (let d = day; d <= end; d += 86_400_000) {
    const a = Math.max(start, d + 21.5 * 3_600_000);
    const b = Math.min(end, d + 30 * 3_600_000);
    if (b > a) total += b - a;
  }
  return total / 3_600_000;
}

/**
 * Sleep debt from the backend (built from the astronaut's own check-ins, #62). The debt changes
 * only while the scheduled sleep period runs, at the shortfall the backend's T+12 point implies.
 */
/**
 * How much the newest check-in moved the sleep debt (#81): the same 7-check-in window as the backend's
 * SleepDebtModel, with and without the newest night. Undefined before there are two check-ins.
 */
export function sleepDebtChange(checkIns: { date: string; sleepHours: number }[]): TwinMetric["change"] {
  if (checkIns.length < 2) return undefined;
  const debt = (list: { sleepHours: number }[]) => list.slice(-7).reduce((d, c) => d + Math.max(0, 8.5 - c.sleepHours), 0);
  const delta = r1(debt(checkIns) - debt(checkIns.slice(0, -1)));
  const last = checkIns[checkIns.length - 1];
  const day = new Date(`${last.date}T12:00:00Z`).toLocaleDateString("en-GB", { day: "numeric", month: "short", timeZone: "UTC" });
  const why = `your ${day} check-in (${last.sleepHours.toFixed(1)} h of sleep)`;
  if (delta === 0) return { text: `No change from ${why}`, tone: "same" };
  return { text: `${delta > 0 ? "+" : "−"}${Math.abs(delta).toFixed(1)} h from ${why}`, tone: delta > 0 ? "worse" : "better" };
}

export function liveSleepDebt(forecasts: ApiForecast[], now: Date): TwinMetric | null {
  const f12 = forecasts.find((f) => f.metric === "sleep_debt" && f.horizonHours === 12);
  if (!f12 || f12.points.length < 2) return null;
  const d0 = f12.points[0].value;
  const window12 = scheduledSleepHours(now, 12);
  const perSleptHour = window12 > 0 ? (f12.points[1].value - d0) / window12 : 0;
  const points = HOURS.map((t): TwinPoint => {
    if (t < 0) return { t, actual: d0 };
    if (t === 0) return { t, actual: d0, forecast: d0, band: [d0, d0] };
    const v = r1(d0 + scheduledSleepHours(now, t) * perSleptHour);
    return { t, forecast: v, band: [r1(v - 0.1 * t), r1(v + 0.1 * t)] };
  });
  return {
    id: "sleep",
    title: "Sleep debt, last 7 check-ins",
    unit: "h",
    points,
    threshold: SLEEP_THRESHOLD,
    explain: "From your check-ins: scheduled 8.5 h minus logged sleep. Tonight's sleep period (21:30–06:00 GMT) at your recent average.",
    source: SLEEP_SOURCE,
    provenance: "checkins",
    privateToSelf: true,
  };
}

// Twin forecasts for one astronaut over the last 12 h and the next 12 h.
// Every curve is SIMULATED ("modelled"). Rates come from sourced values in
// docs/BACKEND.md §3.2; thresholds marked "our design" are not NASA criteria.

import type { Source } from "@/data/personal";

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
}

const HOURS = Array.from({ length: 25 }, (_, i) => i - 12);
const r1 = (x: number) => Math.round(x * 10) / 10;
const r2 = (x: number) => Math.round(x * 100) / 100;

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

export function getTwin(): TwinMetric[] {
  // Sleep debt: scheduled 8.5 h vs logged sleep, last 7 nights (Barger 2014). Planned sleep
  // tonight 6.5 h from T+3 to T+9.5 adds 2 h more debt.
  const nights = [6.4, 6.1, 6.8, 5.9, 6.3, 6.0, 5.6];
  const debtNow = nights.reduce((d, s) => d + (8.5 - s), 0);
  const sleepDebt = series(
    (t) => (t <= 3 ? debtNow : t >= 9.5 ? debtNow + 2 : debtNow + ((t - 3) / 6.5) * 2),
    (t) => 0.1 * t,
  );

  // Fluids: 2.5 L/day target (CLAUDE_HAFIZASI.md §4.1) paced over 16 waking hours from 06:00;
  // 1.4 L drunk by now (14 h awake) at the same pace from here on.
  const awakeNow = 14;
  const rate = 1.4 / awakeNow;
  const targetPerH = 2.5 / 16;
  const deficit = (t: number) => {
    const awake = Math.min(16, Math.max(0, awakeNow + t));
    return targetPerH * awake - rate * awake;
  };
  const fluid = series(deficit, (t) => 0.05 * t, r2);

  // Dose: ISS ~0.2–0.5 mSv/day (Simonsen 2025); this crew member's dosimeter reads 0.32 mSv/day;
  // mission day 90.
  const perH = 0.32 / 24;
  const dose = series((t) => 90 * 0.32 + perH * t, (t) => ((0.5 - 0.2) / 24 / 2) * t, r2);

  // Core temperature: resting value drifts ~+1 °C over long missions (Stahn 2017);
  // flat forecast with a growing band.
  const temp = series((t) => 37.4 + (t < 0 ? 0.05 * Math.sin(t) : 0), (t) => 0.02 * t, r2);

  return [
    {
      id: "fluid",
      title: "Fluid deficit vs 2.5 L/day target",
      unit: "L",
      points: fluid,
      threshold: { value: 0.8, above: true, label: "0.8 L behind (our design)" },
      explain: "At today’s pace you finish the day behind your fluid target.",
      source: { label: "NASA OCHMO-TB-027 Water", url: "https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf" },
    },
    {
      id: "sleep",
      title: "Sleep debt, last 7 nights",
      unit: "h",
      points: sleepDebt,
      threshold: { value: 18, above: true, label: "18 h debt (our design)" },
      explain: "A 6.5 h night tonight adds 2 h of debt against the 8.5 h schedule.",
      source: { label: "Barger et al. 2014, Lancet Neurology", url: "https://doi.org/10.1016/S1474-4422(14)70122-X" },
    },
    {
      id: "dose",
      title: "Cumulative mission dose",
      unit: "mSv",
      points: dose,
      explain: "Normal ISS background. Career limit is 600 mSv.",
      source: { label: "Simonsen 2025, NASA (NTRS 20250006168)", url: "https://ntrs.nasa.gov/api/citations/20250006168" },
    },
    {
      id: "temp",
      title: "Core temperature",
      unit: "°C",
      points: temp,
      explain: "Stable. Resting temperature runs about 1 °C higher on long missions.",
      source: { label: "Stahn et al. 2017, Scientific Reports", url: "https://doi.org/10.1038/s41598-017-15560-w" },
    },
  ];
}

/** First future hour where the forecast crosses the metric's threshold. */
export function firstCrossing(m: TwinMetric): number | null {
  if (!m.threshold) return null;
  const hit = m.points.find(
    (p) => p.t > 0 && p.forecast !== undefined && (m.threshold!.above ? p.forecast >= m.threshold!.value : p.forecast <= m.threshold!.value),
  );
  return hit ? hit.t : null;
}

export interface PlanItem {
  id: string;
  text: string;
  why: string;
  due: string;
}

/** Earth Twin plan from the flight surgeon (simulated). The astronaut or CMO decides. */
export const EARTH_PLAN: PlanItem[] = [
  { id: "fluid", text: "Drink an extra 500 mL before 22:00 GMT.", why: "Fluid deficit forecast", due: "T+2 h" },
  { id: "sleep", text: "Protect a full 8.5 h sleep window tonight: move the 23:00 task to tomorrow.", why: "Sleep debt forecast", due: "T+3 h" },
  { id: "rt", text: "Repeat the reaction test after tonight’s sleep.", why: "Short nights this week", due: "T+11 h" },
];

/** Forecast made 6 h ago vs what was measured now (simulated). */
export const FORECAST_CHECK = [
  { metric: "Fluid deficit", forecast: "0.72 L", actual: "0.79 L", error: "+0.07 L" },
  { metric: "Core temperature", forecast: "37.35 °C", actual: "37.40 °C", error: "+0.05 °C" },
  { metric: "Cumulative dose", forecast: "28.72 mSv", actual: "28.80 mSv", error: "+0.08 mSv" },
];

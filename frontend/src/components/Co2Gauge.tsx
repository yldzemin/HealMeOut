import { useEffect, useRef, useState } from "react";
import { motion } from "motion/react";
import { Line, LineChart, ReferenceLine, YAxis } from "recharts";
import { ChartContainer, type ChartConfig } from "@/components/ui/chart";

/**
 * Cabin CO2 bands, the same thresholds as the backend Co2Rule:
 * - 2.5 mmHg: 7-day average below this keeps headache risk under 1% (OCHMO-TB-004)
 * - 3 mmHg: 1-hour average limit, NASA-STD-3001 V2 6004 (L1 above)
 * - 4.5 mmHg: upper end of the 2.8–4.5 mmHg headache range (L2)
 * - 6.8 mmHg: NASA contingency proposal (6.8–10 mmHg for ≤4 h), not yet a standard (L3)
 */
const MAX = 8;
const BANDS = [
  { from: 0, to: 2.5, color: "var(--status-ok)", label: "Comfort", note: "Headache risk under 1% (7-day average)" },
  { from: 2.5, to: 3, color: "var(--primary)", label: "Watch", note: "Under the 1-hour limit" },
  { from: 3, to: 4.5, color: "var(--status-self)", label: "Over limit", note: "Above NASA’s 1-hour limit" },
  { from: 4.5, to: 6.8, color: "var(--status-cmo)", label: "Symptoms likely", note: "Headaches reported in this range" },
  { from: 6.8, to: MAX, color: "var(--status-ground)", label: "Contingency", note: "NASA contingency proposal, at most 4 h" },
];
const TICKS = [2.5, 3, 4.5, 6.8];

/** Same boundaries as Co2Rule: above 3 is over the limit (3.0 is not); 4.5 and 6.8 start their bands. */
function band(v: number) {
  if (v >= 6.8) return BANDS[4];
  if (v >= 4.5) return BANDS[3];
  if (v > 3) return BANDS[2];
  if (v >= 2.5) return BANDS[1];
  return BANDS[0];
}

const config = { v: { label: "CO2 (mmHg)", color: "var(--primary)" } } satisfies ChartConfig;

/** CO2 value on a banded scale, plus the values polled since the screen opened. */
export function Co2Gauge({ value, timestamp }: { value?: number; timestamp?: string }) {
  const [history, setHistory] = useState<{ t: string; v: number }[]>([]);
  const last = useRef<string | undefined>(undefined);

  useEffect(() => {
    if (value === undefined || timestamp === undefined || timestamp === last.current) return;
    last.current = timestamp;
    setHistory((h) => [...h.slice(-59), { t: timestamp, v: value }]);
  }, [value, timestamp]);

  if (value === undefined) return <p className="text-sm text-muted-foreground">No CO2 reading.</p>;
  const b = band(value);
  const pos = Math.min(value, MAX) / MAX;

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-baseline justify-between">
        <p className="flex items-baseline gap-1.5">
          <span className="font-heading text-4xl font-semibold tabular-nums">{value.toFixed(1)}</span>
          <span className="text-sm text-muted-foreground">mmHg</span>
        </p>
        <span className="rounded-full px-2.5 py-0.5 text-xs font-medium text-background" style={{ background: b.color }}>
          {b.label}
        </span>
      </div>

      {/* Banded scale with a marker for the current value. */}
      <div className="relative pt-4" role="img" aria-label={`CO2 ${value.toFixed(1)} mmHg: ${b.label}. ${b.note}.`}>
        <div className="flex h-3 overflow-hidden rounded-full">
          {BANDS.map((x) => (
            <div key={x.from} style={{ width: `${((x.to - x.from) / MAX) * 100}%`, background: x.color, opacity: x === b ? 0.95 : 0.35 }} />
          ))}
        </div>
        <motion.div
          className="absolute top-0 h-7 w-0.5 bg-foreground"
          initial={false}
          animate={{ left: `${pos * 100}%` }}
          transition={{ type: "spring", stiffness: 120, damping: 20 }}
          aria-hidden
        />
        <div className="relative mt-1 h-4 text-[10px] text-muted-foreground">
          {TICKS.map((t) => (
            <span key={t} className="absolute -translate-x-1/2 tabular-nums" style={{ left: `${(t / MAX) * 100}%` }}>
              {t}
            </span>
          ))}
        </div>
      </div>
      <p className="text-xs text-muted-foreground">{b.note}.</p>

      {history.length > 1 && (
        <div>
          <ChartContainer config={config} className="h-16 w-full">
            <LineChart data={history} margin={{ top: 4, right: 4, bottom: 0, left: 4 }}>
              <YAxis hide domain={[0, Math.max(4, ...history.map((h) => h.v)) + 0.5]} />
              <ReferenceLine y={3} stroke="var(--status-self)" strokeDasharray="3 3" />
              <Line dataKey="v" stroke="var(--color-v)" strokeWidth={2} dot={false} isAnimationActive={false} />
            </LineChart>
          </ChartContainer>
          <p className="text-[10px] text-muted-foreground">Since this screen opened · dashed line: 3 mmHg limit</p>
        </div>
      )}
    </div>
  );
}

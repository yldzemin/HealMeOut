import { CartesianGrid, Line, LineChart, ReferenceArea, XAxis, YAxis } from "recharts";
import type { Trend } from "@/data/personal";
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart";
import { niceTicks } from "@/lib/chartTicks";

/**
 * Trend line with the personal baseline band. Real data: solid line;
 * simulated data: dashed line (docs/FRONTEND.md §1, decision 5).
 */
export function TrendChart({ trend }: { trend: Trend }) {
  const config = { value: { label: trend.title, color: "var(--primary)" } } satisfies ChartConfig;
  const values = trend.points.map((p) => p.value);
  // Round tick values (#82) over the points and the baseline band.
  const { ticks, decimals } = niceTicks(Math.min(...values, trend.band?.low ?? Infinity), Math.max(...values, trend.band?.high ?? -Infinity), 5);

  return (
    <figure className="flex flex-col gap-2">
      <figcaption className="flex items-center justify-between text-xs text-muted-foreground">
        <span>{trend.title}</span>
        {trend.band && (
          <span className="flex items-center gap-1.5">
            <span className="inline-block h-2.5 w-4 rounded-sm bg-primary/15" aria-hidden />
            {trend.band.label}
          </span>
        )}
      </figcaption>
      <ChartContainer config={config} className="h-40 w-full">
        <LineChart data={trend.points} margin={{ top: 6, right: 8, bottom: 0, left: -12 }}>
          <CartesianGrid vertical={false} strokeOpacity={0.15} />
          <XAxis dataKey="label" tickLine={false} axisLine={false} fontSize={11} />
          <YAxis
            domain={[ticks[0], ticks[ticks.length - 1]]}
            ticks={ticks}
            tickFormatter={(v: number) => v.toFixed(decimals)}
            tickLine={false}
            axisLine={false}
            fontSize={11}
            width={40}
          />
          {trend.band && (
            <ReferenceArea y1={trend.band.low} y2={trend.band.high} fill="var(--primary)" fillOpacity={0.12} stroke="none" />
          )}
          <ChartTooltip content={<ChartTooltipContent hideLabel={false} />} />
          <Line
            dataKey="value"
            type="monotone"
            stroke="var(--color-value)"
            strokeWidth={2}
            strokeDasharray={trend.provenance === "simulated" ? "5 4" : undefined}
            dot={{ r: 3 }}
            isAnimationActive={false}
          />
        </LineChart>
      </ChartContainer>
    </figure>
  );
}

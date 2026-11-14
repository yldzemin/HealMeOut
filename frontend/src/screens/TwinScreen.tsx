import { useMemo, useState } from "react";
import { Area, CartesianGrid, ComposedChart, Line, ReferenceLine, XAxis, YAxis } from "recharts";
import { AlertTriangle, ExternalLink, RadioTower } from "lucide-react";
import type { ViewerRole } from "@/types";
import { EARTH_PLAN, FORECAST_CHECK, firstCrossing, getTwin, type TwinMetric } from "@/data/twin";
import { ChartContainer, type ChartConfig } from "@/components/ui/chart";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { openExternal } from "@/lib/openExternal";

const config = {
  actual: { label: "Actual", color: "var(--primary)" },
  forecast: { label: "Forecast", color: "var(--primary)" },
} satisfies ChartConfig;

function valueAt(m: TwinMetric, t: number) {
  const p = m.points.find((x) => x.t === t);
  return p?.forecast ?? p?.actual;
}

function MetricChart({ m, cursor }: { m: TwinMetric; cursor: number }) {
  const crossing = firstCrossing(m);
  const warn = crossing !== null && cursor >= crossing;
  const v = valueAt(m, cursor);
  const values = m.points.flatMap((p) => [p.actual, p.forecast, ...(p.band ?? [])]).filter((x): x is number => x !== undefined);
  if (m.threshold) values.push(m.threshold.value);
  const lo = Math.min(...values);
  const hi = Math.max(...values);
  const pad = (hi - lo) * 0.15 || 0.1;

  return (
    <Card className={warn ? "border-status-self/70" : undefined}>
      <CardContent className="flex flex-col gap-2 p-4">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-sm font-semibold">{m.title}</p>
            <p className="text-xs text-muted-foreground">{m.explain}</p>
          </div>
          <div className="text-right">
            <p className="font-heading text-2xl font-semibold tabular-nums">
              {v ?? "—"} <span className="text-sm font-normal text-muted-foreground">{m.unit}</span>
            </p>
            <p className="text-[11px] text-muted-foreground">at T{cursor >= 0 ? "+" : ""}{cursor} h</p>
          </div>
        </div>
        {crossing !== null && (
          <p className={`flex items-center gap-1.5 text-xs ${warn ? "text-status-self" : "text-muted-foreground"}`}>
            <AlertTriangle className="size-3.5" aria-hidden />
            Forecast reaches {m.threshold!.label} at T+{crossing} h
          </p>
        )}
        <ChartContainer config={config} className="h-36 w-full">
          <ComposedChart data={m.points} margin={{ top: 4, right: 8, bottom: 0, left: -12 }}>
            <CartesianGrid vertical={false} strokeOpacity={0.15} />
            <XAxis dataKey="t" type="number" domain={[-12, 12]} ticks={[-12, -6, 0, 6, 12]} tickFormatter={(t) => (t === 0 ? "now" : `${t > 0 ? "+" : ""}${t}h`)} fontSize={11} tickLine={false} axisLine={false} />
            <YAxis domain={[Math.floor((lo - pad) * 100) / 100, Math.ceil((hi + pad) * 100) / 100]} fontSize={11} tickLine={false} axisLine={false} width={44} />
            <Area dataKey="band" stroke="none" fill="var(--primary)" fillOpacity={0.12} isAnimationActive={false} />
            {m.threshold && <ReferenceLine y={m.threshold.value} stroke="var(--status-self)" strokeDasharray="4 4" />}
            <ReferenceLine x={0} stroke="var(--muted-foreground)" strokeOpacity={0.5} />
            <ReferenceLine x={cursor} stroke="var(--primary)" />
            <Line dataKey="actual" stroke="var(--color-actual)" strokeWidth={2} dot={false} isAnimationActive={false} connectNulls={false} />
            <Line dataKey="forecast" stroke="var(--color-forecast)" strokeWidth={2} strokeDasharray="5 4" dot={false} isAnimationActive={false} connectNulls={false} />
          </ComposedChart>
        </ChartContainer>
        <button type="button" onClick={() => openExternal(m.source.url)} className="inline-flex items-center gap-1 self-start text-[11px] text-primary hover:underline">
          {m.source.label}
          <ExternalLink className="size-3" aria-hidden />
        </button>
      </CardContent>
    </Card>
  );
}

/** Twin screen (docs/FRONTEND.md §2.4): slider, forecasts, Earth Twin plan, forecast vs actual. */
export function TwinScreen({ role }: { role: ViewerRole }) {
  const metrics = useMemo(() => getTwin(), []);
  const [cursor, setCursor] = useState(0);
  const [decisions, setDecisions] = useState<Record<string, "accepted" | "discuss">>({});
  const crewId = role.kind === "astronaut" ? role.crewId : "C3";

  return (
    <div className="flex h-full flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="font-heading text-2xl font-semibold">
          Twin · {crewId} <Badge variant="outline" className="ml-2 align-middle">Modelled</Badge>
        </h1>
        <p className="text-xs text-muted-foreground">All curves are simulated forecasts, not measurements.</p>
      </div>

      <Card>
        <CardContent className="flex items-center gap-4 p-4">
          <span className="text-sm font-medium">Look ahead</span>
          <input
            type="range"
            min={0}
            max={12}
            step={1}
            value={cursor}
            onChange={(e) => setCursor(Number(e.target.value))}
            aria-label="Forecast horizon in hours"
            className="flex-1 accent-[var(--primary)]"
          />
          <div className="flex gap-1" role="group" aria-label="Jump to horizon">
            {[0, 6, 12].map((h) => (
              <Button key={h} size="sm" variant={cursor === h ? "default" : "outline"} onClick={() => setCursor(h)}>
                T+{h}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid min-h-0 flex-1 grid-cols-[minmax(0,2fr)_minmax(0,1fr)] gap-5">
        <div className="grid grid-cols-2 content-start gap-4">
          {metrics.map((m) => (
            <MetricChart key={m.id} m={m} cursor={cursor} />
          ))}
        </div>

        <div className="flex flex-col gap-4">
          <Card>
            <CardContent className="flex flex-col gap-3 p-4">
              <div className="flex items-center gap-2">
                <RadioTower className="size-4 text-status-ok" aria-hidden />
                <p className="text-sm font-semibold">Earth Twin plan</p>
              </div>
              <p className="text-xs text-muted-foreground">From the flight surgeon, based on these forecasts. Received over the ISS link (seconds). You or the CMO decide.</p>
              <ul className="flex flex-col gap-3">
                {EARTH_PLAN.map((p) => (
                  <li key={p.id} className="rounded-lg border border-border p-3">
                    <p className="text-sm">{p.text}</p>
                    <p className="mt-1 text-[11px] text-muted-foreground">
                      {p.why} · due {p.due}
                    </p>
                    {decisions[p.id] ? (
                      <p className="mt-2 text-xs text-status-ok">{decisions[p.id] === "accepted" ? "Accepted" : "Marked for your next private conference"}</p>
                    ) : (
                      <div className="mt-2 flex gap-2">
                        <Button size="sm" onClick={() => setDecisions((d) => ({ ...d, [p.id]: "accepted" }))}>
                          Accept
                        </Button>
                        <Button size="sm" variant="outline" onClick={() => setDecisions((d) => ({ ...d, [p.id]: "discuss" }))}>
                          Discuss at PPC
                        </Button>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="flex flex-col gap-2 p-4">
              <p className="text-sm font-semibold">Forecast vs actual</p>
              <p className="text-xs text-muted-foreground">Forecast made 6 h ago, compared with the value now. Testing this accuracy is our next step.</p>
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left text-muted-foreground">
                    <th className="py-1 font-normal">Metric</th>
                    <th className="py-1 font-normal">Forecast</th>
                    <th className="py-1 font-normal">Actual</th>
                    <th className="py-1 font-normal">Error</th>
                  </tr>
                </thead>
                <tbody>
                  {FORECAST_CHECK.map((r) => (
                    <tr key={r.metric} className="border-t border-border/60">
                      <td className="py-1.5">{r.metric}</td>
                      <td className="py-1.5 tabular-nums">{r.forecast}</td>
                      <td className="py-1.5 tabular-nums">{r.actual}</td>
                      <td className="py-1.5 tabular-nums">{r.error}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <p className="text-[11px] text-muted-foreground">Simulated.</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

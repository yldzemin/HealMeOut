import { useEffect, useMemo, useState } from "react";
import { CartesianGrid, Line, LineChart, ReferenceLine, XAxis, YAxis } from "recharts";
import { ChevronRight, Play, Square, Wind, Radiation } from "lucide-react";
import type { ViewerRole } from "@/types";
import {
  getDoseSeries,
  getImm,
  startScenario,
  stepScenario,
  stopScenario,
  type ApiCondition,
  type ApiEnvironment,
  type StationView,
} from "@/api/client";
import { TelemetryPanel } from "@/components/TelemetryPanel";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/regionStyle";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart";

/** Median daily ISS TEPC dose equivalent, Sep 4–9 2017 (NASA_DATA_SOURCES.md §4.3); the dose rule fires at 1.5×. */
const NORMAL_DAILY_USV_H = 37.55;

const SCENARIOS = [
  { id: "co2", label: "Cabin CO2 build-up", note: "synthetic ramp through the NASA limits" },
  { id: "spe-iss", label: "Solar particle event, Sep 2017", note: "real NASA SRAG ISS dose data" },
];

function Offline() {
  return (
    <Card className="max-w-3xl">
      <CardContent className="p-8">
        <p className="font-heading text-xl font-semibold">Environment needs the station backend</p>
        <p className="mt-2 text-sm text-muted-foreground">
          Start it with <code>backend/api/run.sh</code>; this screen connects automatically. It shows real ISS cabin and
          radiation data from NASA.
        </p>
      </CardContent>
    </Card>
  );
}

/** Cabin CO2, real ISS radiation dose, NASA IMM risks, scenarios and live telemetry (docs/FRONTEND.md §2). */
export function EnvironmentScreen({
  role,
  station,
  refresh,
}: {
  role: ViewerRole;
  station: StationView | null;
  refresh: () => Promise<void>;
}) {
  const [dose, setDose] = useState<ApiEnvironment[]>([]);
  const [imm, setImm] = useState<ApiCondition[]>([]);
  const [error, setError] = useState<string | null>(null);
  const online = station !== null;
  const latestDose = station?.dose?.timestamp;

  useEffect(() => {
    if (!online) return;
    getDoseSeries().then(setDose).catch(() => setDose([]));
  }, [online, latestDose]);

  useEffect(() => {
    if (!online) return;
    getImm().then(setImm).catch(() => setImm([]));
  }, [online]);

  const points = useMemo(
    () => dose.map((d) => ({ t: `${d.timestamp.slice(5, 10)} ${d.timestamp.slice(11, 13)}h`, v: d.doseEqRate_uSv_h ?? 0 })),
    [dose],
  );

  if (!station) return <Offline />;

  const run = async (fn: () => Promise<unknown>) => {
    try {
      setError(null);
      await fn();
      await refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  const cabin = station.cabin;
  const d = station.dose;
  const envAlerts = station.alerts.filter((a) => a.category === "ENVIRONMENT");
  const scenario = station.scenario && SCENARIOS.some((s) => s.id === station.scenario?.id) ? station.scenario : undefined;
  const chartConfig = { v: { label: "Dose equivalent (μSv/h)", color: "var(--primary)" } } satisfies ChartConfig;

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-heading text-2xl font-semibold">Environment</h1>
        <div className="flex flex-wrap gap-2">
          {SCENARIOS.map((s) => (
            <Button key={s.id} variant="outline" size="sm" className="gap-1.5" title={s.note} onClick={() => void run(() => startScenario(s.id))}>
              <Play className="size-3.5" aria-hidden /> {s.label}
            </Button>
          ))}
          {scenario && !scenario.finished && (
            <Button size="sm" className="gap-1.5" onClick={() => void run(stepScenario)}>
              Next step <ChevronRight className="size-3.5" aria-hidden />
            </Button>
          )}
          {scenario && (
            <Button size="sm" variant="outline" className="gap-1.5" onClick={() => void run(stopScenario)}>
              <Square className="size-3.5" aria-hidden /> Reset
            </Button>
          )}
        </div>
      </div>
      {error && <p className="text-sm text-status-emergency">{error}</p>}

      {scenario && (
        <Card>
          <CardContent className="p-4 text-sm">
            <p className="text-xs text-muted-foreground">
              {scenario.title} · step {scenario.step}/{scenario.total} · {scenario.at}
            </p>
            <p className="font-medium">{scenario.stepTitle}</p>
            <p className="text-muted-foreground">{scenario.detail}</p>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-3">
        <Card>
          <CardContent className="flex flex-col gap-2 p-5">
            <p className="flex items-center gap-2 text-sm text-muted-foreground">
              <Wind className="size-4" aria-hidden /> Cabin CO2 (1-hour average)
            </p>
            <p className="flex items-baseline gap-1.5">
              <span className="font-heading text-4xl font-semibold tabular-nums">{cabin?.co2_mmHg ?? "—"}</span>
              <span className="text-sm text-muted-foreground">mmHg</span>
            </p>
            <p className="text-xs text-muted-foreground">Limit 3 mmHg (NASA-STD-3001 V2 6004). Typical ISS 1.9–2.3 mmHg (OSDR EDA).</p>
            <Badge variant="outline" className="w-fit">Simulated</Badge>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex flex-col gap-2 p-5">
            <p className="flex items-center gap-2 text-sm text-muted-foreground">
              <Radiation className="size-4" aria-hidden /> Radiation dose rate
            </p>
            <p className="flex items-baseline gap-1.5">
              <span className="font-heading text-4xl font-semibold tabular-nums">{d?.doseEqRate_uSv_h?.toFixed(1) ?? "—"}</span>
              <span className="text-sm text-muted-foreground">μSv/h</span>
            </p>
            <p className="text-xs text-muted-foreground">
              {d ? `${d.doseRate_uGy_h?.toFixed(1)} μGy/h absorbed · ${d.timestamp.slice(0, 16).replace("T", " ")} UTC (replayed)` : ""}
            </p>
            <Badge className="w-fit">Real NASA data</Badge>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex flex-col gap-2 p-5">
            <p className="text-sm text-muted-foreground">Most likely conditions, 6-month ISS mission</p>
            <ul className="flex flex-col gap-1.5">
              {imm.slice(0, 5).map((c) => (
                <li key={c.condition} className="text-xs">
                  <div className="flex justify-between">
                    <span>{c.condition}</span>
                    <span className="tabular-nums text-muted-foreground">{Math.round(c.probability * 100)}%</span>
                  </div>
                  <div className="mt-0.5 h-1.5 rounded bg-muted">
                    <div className="h-1.5 rounded bg-primary" style={{ width: `${c.probability * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
            <p className="text-[11px] text-muted-foreground">NASA Integrated Medical Model, probability of at least one event.</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent className="flex flex-col gap-2 p-5">
          <div className="flex items-center justify-between">
            <p className="font-heading text-lg font-semibold">ISS radiation, September 2017</p>
            <Badge>Real NASA data</Badge>
          </div>
          <ChartContainer config={chartConfig} className="h-56 w-full">
            <LineChart data={points} margin={{ top: 6, right: 8, bottom: 0, left: -8 }}>
              <CartesianGrid vertical={false} strokeOpacity={0.15} />
              <XAxis dataKey="t" tickLine={false} axisLine={false} fontSize={11} minTickGap={40} />
              <YAxis tickLine={false} axisLine={false} fontSize={11} width={44} />
              <ReferenceLine y={NORMAL_DAILY_USV_H * 1.5} stroke="var(--status-cmo)" strokeDasharray="4 4" />
              <ChartTooltip content={<ChartTooltipContent hideLabel={false} />} />
              <Line dataKey="v" type="monotone" stroke="var(--color-v)" strokeWidth={1.5} dot={false} isAnimationActive={false} />
            </LineChart>
          </ChartContainer>
          <p className="text-[11px] text-muted-foreground">
            Hourly ISS TEPC dose equivalent (ICRP-60) from NASA's Space Radiation Analysis Group via CDAWeb: dose at the
            instrument, not effective dose. Spikes are South Atlantic Anomaly passes. Dashed line: 1.5× the normal daily
            level, where the 24-hour average triggers shelter advice (our design, calibrated on this data).
          </p>
        </CardContent>
      </Card>

      {envAlerts.length > 0 && (
        <Card>
          <CardContent className="flex flex-col gap-2 p-5">
            <p className="font-heading text-lg font-semibold">Station alerts</p>
            {envAlerts.map((a) => (
              <div key={a.id} className={`rounded-lg border p-3 ${a.active ? "" : "opacity-50"}`} style={{ borderColor: LEVEL_COLOR[a.level] }}>
                <p className="text-sm font-medium" style={{ color: LEVEL_COLOR[a.level] }}>
                  {a.level} · {LEVEL_WORD[a.level]} · {a.title}
                  {!a.active && " (resolved)"}
                </p>
                <p className="text-sm text-muted-foreground">{a.explanation}</p>
                <p className="text-sm">{a.recommendation}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <TelemetryPanel view={role} />
    </div>
  );
}

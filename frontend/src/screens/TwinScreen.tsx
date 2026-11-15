import { useEffect, useMemo, useState } from "react";
import { Area, CartesianGrid, ComposedChart, Line, ReferenceLine, XAxis, YAxis } from "recharts";
import { AlertTriangle, ExternalLink, Minus, RadioTower, TrendingDown, TrendingUp } from "lucide-react";
import type { CrewMember, ViewerRole } from "@/types";
import {
  getBaselines,
  getDoseSeries,
  getForecasts,
  type ApiAlert,
  type ApiBaselineStats,
  type ApiEnvironment,
  type ApiForecast,
  type StationView,
} from "@/api/client";
import {
  EARTH_PLAN,
  forecastCheck,
  firstCrossing,
  getTwin,
  liveDose,
  liveDoseCheck,
  liveSleepDebt,
  sleepDebtChange,
  type TwinMetric,
} from "@/data/twin";
import { NASA_HOSTED_NOTE } from "@/data/personal";
import type { AlertDecision } from "@/components/DetailPanel";
import { ChartContainer, type ChartConfig } from "@/components/ui/chart";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { openExternal } from "@/lib/openExternal";

const config = {
  actual: { label: "Actual", color: "var(--primary)" },
  forecast: { label: "Forecast", color: "var(--primary)" },
} satisfies ChartConfig;

/** Analytes shown first; the rest are behind "all analytes". */
const KEY_ANALYTES = ["glucose", "sodium", "potassium", "bun", "creatinine", "hemoglobin", "crp", "il_6"];

/** Four significant digits, thousands separated: 42656700 → 42,660,000. */
const num = (v: number) => new Intl.NumberFormat("en-US", { maximumSignificantDigits: 4 }).format(v);

const PROVENANCE: Record<TwinMetric["provenance"], { label: string; real: boolean }> = {
  nasa: { label: "Real NASA data", real: true },
  checkins: { label: "Your check-ins", real: true },
  simulated: { label: "Simulated", real: false },
};

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
  // Axis labels short enough for the 40 px axis: whole numbers for wide ranges.
  const decimals = hi - lo >= 5 ? 0 : hi - lo >= 0.5 ? 1 : 2;
  const prov = PROVENANCE[m.provenance];

  return (
    <Card className={warn ? "border-status-self/70" : undefined}>
      <CardContent className="flex flex-col gap-2 p-4">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-sm font-semibold">{m.title}</p>
            <p className="text-xs text-muted-foreground">{m.explain}</p>
          </div>
          <div className="shrink-0 whitespace-nowrap text-right">
            <p className="font-heading text-2xl font-semibold tabular-nums">
              {v === undefined ? "—" : m.digits !== undefined ? v.toFixed(m.digits) : v}{" "}
              <span className="text-sm font-normal text-muted-foreground">{m.unit}</span>
            </p>
            <p className="text-[11px] text-muted-foreground">at T{cursor >= 0 ? "+" : ""}{cursor} h</p>
          </div>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {prov.real ? <Badge className="text-[11px]">{prov.label}</Badge> : <Badge variant="outline" className="text-[11px]">{prov.label}</Badge>}
          {m.privateToSelf && (
            <Badge variant="secondary" className="text-[11px]">
              Private
            </Badge>
          )}
        </div>
        {m.change && (
          <p
            className={`flex items-center gap-1.5 text-xs font-medium ${m.change.tone === "worse" ? "text-status-self" : m.change.tone === "better" ? "text-status-ok" : "text-muted-foreground"}`}
          >
            {m.change.tone === "worse" ? (
              <TrendingUp className="size-3.5" aria-hidden />
            ) : m.change.tone === "better" ? (
              <TrendingDown className="size-3.5" aria-hidden />
            ) : (
              <Minus className="size-3.5" aria-hidden />
            )}
            {m.change.text}
          </p>
        )}
        {crossing !== null && (
          <p className={`flex items-center gap-1.5 text-xs ${warn ? "text-status-self" : "text-muted-foreground"}`}>
            <AlertTriangle className="size-3.5" aria-hidden />
            {crossing === 0 ? `Already past ${m.threshold!.label} now` : `Forecast reaches ${m.threshold!.label} at T+${crossing} h`}
          </p>
        )}
        <ChartContainer config={config} className="h-36 w-full">
          <ComposedChart data={m.points} margin={{ top: 4, right: 8, bottom: 0, left: 0 }}>
            <CartesianGrid vertical={false} strokeOpacity={0.15} />
            <XAxis dataKey="t" type="number" domain={[-12, 12]} ticks={[-12, -6, 0, 6, 12]} tickFormatter={(t) => (t === 0 ? "now" : `${t > 0 ? "+" : ""}${t}h`)} fontSize={11} tickLine={false} axisLine={false} />
            <YAxis
              domain={[Math.floor((lo - pad) * 100) / 100, Math.ceil((hi + pad) * 100) / 100]}
              tickCount={4}
              tickFormatter={(v: number) => v.toFixed(decimals)}
              fontSize={11}
              tickLine={false}
              axisLine={false}
              width={40}
            />
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

/**
 * Twin screen (docs/FRONTEND.md §2.4): look-ahead slider, forecast charts, Earth Twin plan,
 * forecast vs actual, and the personal pre-flight baseline. With the backend running, the dose
 * curve is real ISS TEPC data and sleep debt comes from the astronaut's check-ins; fluids and
 * temperature stay simulated. Sleep debt and the sleep plan items are psych data: astronaut only.
 */
/** The Earth Twin's support plan from the backend (category PLAN): the astronaut, CMO and ground see it; ground or the CMO decides. */
function SupportPlanItem({ alert, canDecide, onDecision }: { alert: ApiAlert; canDecide: boolean; onDecision?: AlertDecision }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const decide = (d: "approve" | "reject") => {
    if (!onDecision) return;
    setBusy(true);
    setError(null);
    onDecision(alert.id, d)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
      .finally(() => setBusy(false));
  };
  return (
    <li className="rounded-lg border border-status-ground/60 p-3">
      <p className="text-xs font-medium text-status-ground">{alert.title} · for ground approval</p>
      <p className="mt-1 text-sm">{alert.explanation}</p>
      <p className="mt-1 text-[11px] text-muted-foreground">{alert.recommendation}</p>
      {alert.status !== "open" ? (
        <p className="mt-2 text-xs text-status-ok">{alert.status === "approved" ? "Approved by ground. Logged." : "Declined. Logged."}</p>
      ) : canDecide && onDecision ? (
        <div className="mt-2 flex gap-2">
          <Button size="sm" disabled={busy} onClick={() => decide("approve")}>
            Approve
          </Button>
          <Button size="sm" variant="outline" disabled={busy} onClick={() => decide("reject")}>
            Decline
          </Button>
        </div>
      ) : (
        <p className="mt-2 text-xs text-muted-foreground">Waiting for ground approval.</p>
      )}
      {error && <p className="mt-1 text-xs text-status-emergency">{error}</p>}
    </li>
  );
}

export function TwinScreen({
  role,
  crew,
  station,
  onDecision,
}: {
  role: ViewerRole;
  crew: CrewMember[];
  station: StationView | null;
  /** Decision on a backend alert (the support plan); refreshes the station view. */
  onDecision?: AlertDecision;
}) {
  const [picked, setPicked] = useState("C3");
  const self = role.kind === "astronaut";
  const crewId = self ? role.crewId : picked;
  const online = station !== null;
  const doseStamp = station?.dose?.timestamp;
  const checkIns = station?.crew.find((c) => c.id === crewId)?.checkIns;
  // Refetch when a check-in is added or replaced (one per day, #81) or the station day moves.
  const lastCheckIn = checkIns?.[checkIns.length - 1];
  const checkInKey = `${checkIns?.length ?? 0}:${lastCheckIn?.date}:${lastCheckIn?.sleepHours}:${station?.clock?.today}`;

  const [forecasts, setForecasts] = useState<ApiForecast[]>([]);
  const [baselines, setBaselines] = useState<ApiBaselineStats[]>([]);
  const [doseSeries, setDoseSeries] = useState<ApiEnvironment[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showAll, setShowAll] = useState(false);
  const [cursor, setCursor] = useState(0);
  const [decisions, setDecisions] = useState<Record<string, "accepted" | "discuss">>({});

  useEffect(() => {
    if (!online) return;
    let alive = true;
    setError(null);
    Promise.all([getForecasts(crewId, role), getBaselines(crewId, role), getDoseSeries()])
      .then(([f, b, d]) => {
        if (!alive) return;
        setForecasts(f);
        setBaselines(b);
        setDoseSeries(d);
        setLoaded(true);
      })
      .catch((e: unknown) => alive && setError(e instanceof Error ? e.message : String(e)));
    return () => {
      alive = false;
    };
    // `role` is a new object on every render; its kind and crewId are covered by crewId and role.kind.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [online, crewId, role.kind, doseStamp, checkInKey]);

  const metrics = useMemo(() => {
    const out: TwinMetric[] = [];
    for (const m of getTwin(crewId)) {
      if (m.privateToSelf && !self) continue;
      if (!online) {
        out.push(m);
      } else if (m.id === "dose") {
        const live = liveDose(doseSeries, forecasts);
        if (live) out.push(live);
        else if (loaded) out.push(m);
      } else if (m.id === "sleep") {
        const live = liveSleepDebt(forecasts, new Date());
        if (live) out.push({ ...live, change: sleepDebtChange(checkIns ?? []) });
      } else {
        out.push(m);
      }
    }
    return out;
  }, [crewId, online, self, loaded, doseSeries, forecasts, checkIns]);

  const checks = useMemo(() => {
    const live = online ? liveDoseCheck(doseSeries) : null;
    return forecastCheck(crewId).map((r) => (live && r.metric === live.metric ? live : r));
  }, [crewId, online, doseSeries]);

  const plan = EARTH_PLAN.filter((p) => self || !p.privateToSelf);
  // The backend only sends a plan alert to viewers allowed to see it (astronaut, CMO, ground).
  const supportPlan = station?.alerts.find((a) => a.active && a.category === "PLAN" && a.crewId === crewId);
  const member = crew.find((c) => c.id === crewId);
  const mission = baselines.find((b) => b.mission)?.mission;
  const studies = [...new Set(baselines.map((b) => b.source))].join(", ");
  const rows = showAll ? baselines : baselines.filter((b) => KEY_ANALYTES.includes(b.analyte));
  const noSleepYet = online && loaded && self && !metrics.some((m) => m.id === "sleep");

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-heading text-2xl font-semibold">
          Twin · {member ? `${member.id} ${member.callsign}` : crewId}{" "}
          <Badge variant="outline" className="ml-2 align-middle">
            Modelled
          </Badge>
        </h1>
        <div className="flex items-center gap-3">
          <p className="text-xs text-muted-foreground">
            {online
              ? "Forecasts, not measurements. Each chart says where its data comes from."
              : "Backend offline: all curves are simulated forecasts."}
          </p>
          {!self && (
            <Select value={picked} onValueChange={setPicked}>
              <SelectTrigger className="w-48" aria-label="Crew member">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {crew.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.id} · {c.callsign}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>
      </div>
      {error && <p className="text-sm text-status-emergency">{error}</p>}

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

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <div className="grid grid-cols-1 content-start gap-4 md:grid-cols-2">
          {metrics.map((m) => (
            <MetricChart key={m.id} m={m} cursor={cursor} />
          ))}
          {noSleepYet && (
            <Card>
              <CardContent className="p-4">
                <p className="text-sm font-semibold">Sleep debt</p>
                <p className="mt-1 text-xs text-muted-foreground">Log a check-in with your sleep hours to see your sleep-debt forecast. Only you see it.</p>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="flex flex-col gap-4">
          <Card>
            <CardContent className="flex flex-col gap-3 p-4">
              <div className="flex items-center gap-2">
                <RadioTower className="size-4 text-status-ok" aria-hidden />
                <p className="text-sm font-semibold">Earth Twin plan</p>
              </div>
              <p className="text-xs text-muted-foreground">
                From the flight surgeon, based on these forecasts. Received over the ISS link (seconds). You or the CMO decide. Simulated.
              </p>
              <ul className="flex flex-col gap-3">
                {supportPlan && <SupportPlanItem alert={supportPlan} canDecide={!self} onDecision={onDecision} />}
                {plan.map((p) => (
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
                  {checks.map((r) => (
                    <tr key={r.metric} className="border-t border-border/60">
                      <td className="py-1.5">
                        {r.metric}
                        {r.provenance === "nasa" && <span className="ml-1 text-[10px] text-status-ok">NASA data</span>}
                      </td>
                      <td className="py-1.5 tabular-nums">{r.forecast}</td>
                      <td className="py-1.5 tabular-nums">{r.actual}</td>
                      <td className="py-1.5 tabular-nums">{r.error}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <p className="text-[11px] text-muted-foreground">
                {checks.some((r) => r.provenance === "nasa") ? "Dose row from the ISS TEPC data; the other rows are simulated." : "Simulated."}
              </p>
            </CardContent>
          </Card>
        </div>
      </div>

      {online && (
        <Card>
          <CardContent className="flex flex-col gap-3 p-5">
            <div className="flex items-center justify-between">
              <p className="font-heading text-lg font-semibold">Personal pre-flight baseline</p>
              {baselines.length > 0 ? <Badge>Real NASA data</Badge> : <Badge variant="outline">Simulated</Badge>}
            </div>
            {mission && (
              <p className="w-fit rounded-md border border-status-ok/40 px-2 py-1 text-[11px] text-status-ok">
                {mission.kind === "commercial" ? "Commercial mission" : "NASA mission"} · {mission.name} · {mission.start} to {mission.end} ·{" "}
                {mission.orbit} · {mission.crew} · data hosted by {mission.hostedBy} ({studies})
                <span className="block text-muted-foreground">{NASA_HOSTED_NOTE}</span>
              </p>
            )}
            {baselines.length === 0 ? (
              <p className="text-sm text-muted-foreground">No real blood baseline for this crew member: their values are simulated.</p>
            ) : (
              <>
                <table className="w-full text-sm tabular-nums">
                  <thead className="text-xs text-muted-foreground">
                    <tr className="text-left">
                      <th className="py-1 font-normal">Analyte</th>
                      <th className="py-1 font-normal">Mean ± SD (L-92, L-44, L-3)</th>
                      <th className="py-1 font-normal">Reference range</th>
                      <th className="py-1 font-normal">Unit</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((b) => (
                      <tr key={b.analyte} className="border-t border-border/50">
                        <td className="py-1">{b.analyte.replace(/_/g, " ")}</td>
                        <td className="py-1">
                          {num(b.mean)}
                          {b.sd !== undefined ? ` ± ${num(b.sd)}` : ""}
                        </td>
                        <td className="py-1">{b.refLow !== undefined && b.refHigh !== undefined ? `${num(b.refLow)}–${num(b.refHigh)}` : "—"}</td>
                        <td className="py-1 text-muted-foreground">{b.unit}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <button type="button" className="w-fit text-xs text-primary hover:underline" onClick={() => setShowAll((s) => !s)}>
                  {showAll ? "Show key analytes" : `Show all ${baselines.length} analytes`}
                </button>
              </>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

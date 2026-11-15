import { useEffect, useState } from "react";
import type { CrewMember, ViewerRole } from "@/types";
import { getBaselines, getForecasts, type ApiBaselineStats, type ApiForecast, type StationView } from "@/api/client";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

/** Analytes shown first; the rest are behind "all analytes". */
const KEY_ANALYTES = ["glucose", "sodium", "potassium", "bun", "creatinine", "hemoglobin", "crp", "il_6"];

/** Card title per forecast metric (backend twin models, BACKEND §3.2). */
const FORECAST_TITLE: Record<string, string> = {
  cumulative_dose: "Cumulative radiation dose",
  sleep_debt: "Sleep debt",
};
/** Forecasts built from private check-ins: only the astronaut receives them. */
const PRIVATE_METRICS = new Set(["sleep_debt"]);

/** Four significant digits, thousands separated: 42656700 → 42,660,000. */
const num = (v: number) => new Intl.NumberFormat("en-US", { maximumSignificantDigits: 4 }).format(v);

/** Earth Twin: T+6 / T+12 forecasts (labelled modelled; sleep debt only for the astronaut) and the personal pre-flight baseline. */
export function TwinScreen({ role, crew, station }: { role: ViewerRole; crew: CrewMember[]; station: StationView | null }) {
  const [picked, setPicked] = useState("C3");
  const crewId = role.kind === "astronaut" ? role.crewId : picked;
  const [forecasts, setForecasts] = useState<ApiForecast[]>([]);
  const [baselines, setBaselines] = useState<ApiBaselineStats[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showAll, setShowAll] = useState(false);
  const online = station !== null;
  const doseStamp = station?.dose?.timestamp;

  useEffect(() => {
    if (!online) return;
    setError(null);
    Promise.all([getForecasts(crewId, role), getBaselines(crewId, role)])
      .then(([f, b]) => {
        setForecasts(f);
        setBaselines(b);
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [online, crewId, role.kind, doseStamp]);

  if (!online) {
    return (
      <Card className="max-w-3xl">
        <CardContent className="p-8">
          <p className="font-heading text-xl font-semibold">The twin needs the station backend</p>
          <p className="mt-2 text-sm text-muted-foreground">
            Start it with <code>backend/api/run.sh</code>; this screen connects automatically.
          </p>
        </CardContent>
      </Card>
    );
  }

  const member = crew.find((c) => c.id === crewId);
  const mission = baselines.find((b) => b.mission)?.mission;
  const studies = [...new Set(baselines.map((b) => b.source))].join(", ");
  const rows = showAll ? baselines : baselines.filter((b) => KEY_ANALYTES.includes(b.analyte));

  return (
    <div className="flex max-w-5xl flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-heading text-2xl font-semibold">
          Twin · {member ? `${member.id} ${member.callsign}` : crewId}
        </h1>
        {role.kind !== "astronaut" && (
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
      {error && <p className="text-sm text-status-emergency">{error}</p>}

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
        {forecasts.map((f) => (
          <Card key={`${f.metric}-${f.horizonHours}`}>
            <CardContent className="flex flex-col gap-2 p-5">
              <div className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                  {FORECAST_TITLE[f.metric] ?? f.metric.replace(/_/g, " ")} · T+{f.horizonHours} h
                </p>
                <div className="flex gap-1.5">
                  {PRIVATE_METRICS.has(f.metric) && <Badge variant="secondary">Private</Badge>}
                  <Badge variant="outline">Modelled</Badge>
                </div>
              </div>
              <p className="flex items-baseline gap-2 tabular-nums">
                <span className="text-sm text-muted-foreground">{f.points[0]?.value} →</span>
                <span className="font-heading text-4xl font-semibold">{f.points[1]?.value}</span>
                <span className="text-sm text-muted-foreground">{f.unit}</span>
              </p>
              <p className="text-[11px] text-muted-foreground">{f.evidence}</p>
            </CardContent>
          </Card>
        ))}
      </div>

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
    </div>
  );
}

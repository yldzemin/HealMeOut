import { useState } from "react";
import { ChevronRight, HeartHandshake, Siren, Square } from "lucide-react";
import type { CrewMember } from "@/types";
import { startScenario, stepScenario, stopScenario, type StationView } from "@/api/client";
import { StationMap } from "@/components/StationMap";
import { IssOverview3D } from "@/components/IssOverview3D";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/regionStyle";
import { STATION_SOURCE } from "@/data/station";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { openExternal } from "@/lib/openExternal";

const ROLE_TEXT: Record<CrewMember["role"], string> = { CMO1: "CMO 1", CMO2: "CMO 2", CREW: "Crew" };

/**
 * Crew Deck: whole crew at a glance. Shows status, module and role only:
 * no vitals detail, no medication, never psychological data (TECH_OVERVIEW §5).
 * The "bad news from home" scenario (live only) is private: the backend sends this view only its
 * public side, e.g. "C3 requested reduced tasking".
 */
export function CrewDeckScreen({
  crew,
  station = null,
  refresh,
  onStartEmergency,
}: {
  crew: CrewMember[];
  station?: StationView | null;
  refresh?: () => Promise<void>;
  onStartEmergency: (patientId: string) => void;
}) {
  const [error, setError] = useState<string | null>(null);
  const scenario = station?.scenario?.id === "bad-news" ? station.scenario : undefined;
  // Notices everyone may see (category STATUS), such as a reduced-tasking request; never a reason.
  const notices = new Map((station?.alerts ?? []).filter((a) => a.active && a.category === "STATUS").map((a) => [a.crewId, a.title]));
  const run = async (fn: () => Promise<unknown>) => {
    try {
      setError(null);
      await fn();
      await refresh?.();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  return (
    <div className="flex h-full flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-heading text-2xl font-semibold">Crew Deck</h1>
        <div className="flex flex-wrap gap-2">
          {station && (
            <Button variant="outline" className="gap-2" onClick={() => void run(() => startScenario("bad-news"))}>
              <HeartHandshake className="size-4" aria-hidden />
              Run scenario: bad news from home (simulated)
            </Button>
          )}
          <Button variant="outline" className="gap-2 border-status-emergency/60 text-status-emergency" onClick={() => onStartEmergency("C2")}>
            <Siren className="size-4" aria-hidden />
            Run scenario: crewmate collapse (simulated)
          </Button>
        </div>
      </div>
      {error && <p className="text-sm text-status-emergency">{error}</p>}

      {scenario && (
        <div className="flex flex-wrap items-start justify-between gap-3 rounded-xl border border-border bg-card p-4 text-sm">
          <div className="min-w-0">
            <p className="text-xs text-muted-foreground">
              {scenario.title} · step {scenario.step}/{scenario.total} · {scenario.at}
            </p>
            <p className="font-medium">{scenario.stepTitle}</p>
            <p className="text-muted-foreground">{scenario.detail}</p>
          </div>
          <div className="flex gap-2">
            {!scenario.finished && (
              <Button size="sm" className="gap-1.5" onClick={() => void run(stepScenario)}>
                Next step <ChevronRight className="size-3.5" aria-hidden />
              </Button>
            )}
            <Button size="sm" variant="outline" className="gap-1.5" onClick={() => void run(stopScenario)}>
              <Square className="size-3.5" aria-hidden /> Reset
            </Button>
          </div>
        </div>
      )}

      <div className="relative min-h-[320px] flex-1 rounded-xl border border-border bg-card/40 p-4">
        <StationMap crew={crew} />
        <div className="absolute left-3 top-3 h-36 w-56 overflow-hidden rounded-lg border border-border bg-background/80">
          <IssOverview3D />
        </div>
        <p className="absolute bottom-2 left-4 text-[11px] text-muted-foreground">
          Crew location: concept (wearable tag), simulated · Layout simplified from the{" "}
          <button type="button" onClick={() => openExternal(STATION_SOURCE.url)} className="underline">
            {STATION_SOURCE.label}
          </button>
        </p>
      </div>

      <ul className="grid grid-cols-7 gap-3" aria-label="Crew">
        {crew.map((c) => (
          <li key={c.id} className="flex flex-col gap-1 rounded-xl border border-border bg-card p-3">
            <div className="flex items-center justify-between">
              <span className="font-heading text-sm font-semibold">
                {c.id} · {c.callsign}
              </span>
              <span className="size-2.5 rounded-full" style={{ background: LEVEL_COLOR[c.level] }} aria-hidden />
            </div>
            <Badge variant="secondary" className="w-fit text-[11px]">
              {ROLE_TEXT[c.role]}
            </Badge>
            <span className="text-xs text-muted-foreground">{c.module}</span>
            <span className="text-xs" style={{ color: LEVEL_COLOR[c.level] }}>
              {c.level} · {LEVEL_WORD[c.level]}
            </span>
            {notices.has(c.id) && <span className="text-xs font-medium">{notices.get(c.id)}</span>}
          </li>
        ))}
      </ul>
    </div>
  );
}

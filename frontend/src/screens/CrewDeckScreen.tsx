import { Siren } from "lucide-react";
import type { CrewMember } from "@/types";
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
 */
export function CrewDeckScreen({ crew, onStartEmergency }: { crew: CrewMember[]; onStartEmergency: (patientId: string) => void }) {
  return (
    <div className="flex h-full flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="font-heading text-2xl font-semibold">Crew Deck</h1>
        <Button variant="outline" className="gap-2 border-status-emergency/60 text-status-emergency" onClick={() => onStartEmergency("C2")}>
          <Siren className="size-4" aria-hidden />
          Run scenario: crewmate collapse (simulated)
        </Button>
      </div>

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
          </li>
        ))}
      </ul>
    </div>
  );
}

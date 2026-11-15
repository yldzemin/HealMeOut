import { useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import type { CrewMember, StatusLevel, ViewerRole } from "@/types";
import { getRegions, getToday, LEVEL_RANK, type Region, type RegionId, type Routine } from "@/data/personal";
import { Body3D, MODEL_CREDIT } from "@/components/Body3D";
import { RegionChips } from "@/components/RegionChips";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/regionStyle";
import { DetailPanel, type AlertDecision } from "@/components/DetailPanel";
import type { StationView } from "@/api/client";
import { applyLive } from "@/data/live";
import { TodayStrip } from "@/components/TodayStrip";
import { Card, CardContent } from "@/components/ui/card";

function worst(regions: Region[]): Region | undefined {
  return regions.reduce<Region | undefined>(
    (w, r) => (!w || LEVEL_RANK[r.level] > LEVEL_RANK[w.level] ? r : w),
    undefined,
  );
}

/** Shown in the right panel when no region is selected. */
function DaySummary({ regions, onSelect }: { regions: Region[]; onSelect: (id: RegionId) => void }) {
  return (
    <Card className="h-full">
      <CardContent className="flex flex-col gap-4 p-6">
        <p className="font-heading text-xl font-semibold">Select a part of the body to see what it means for you.</p>
        <ul className="flex flex-col">
          {regions.map((r) => (
            <li key={r.id}>
              <button
                type="button"
                onClick={() => onSelect(r.id)}
                className="flex w-full items-start gap-3 border-b border-border/60 py-3 text-left last:border-b-0 hover:bg-accent/40 focus-visible:outline-2 focus-visible:outline-ring"
              >
                <span className="mt-1.5 size-2.5 shrink-0 rounded-full" style={{ background: LEVEL_COLOR[r.level] }} aria-hidden />
                <span>
                  <span className="block text-sm font-medium">
                    {r.label} <span className="text-xs text-muted-foreground">· {LEVEL_WORD[r.level]}</span>
                  </span>
                  <span className="block text-sm text-muted-foreground">{r.status}</span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  );
}

export function PersonalScreen({
  crew,
  role,
  routine,
  station = null,
  onDecision,
}: {
  crew: CrewMember[];
  role: ViewerRole;
  routine: Routine;
  /** Live backend view for this role; null in mock mode. */
  station?: StationView | null;
  onDecision?: AlertDecision;
}) {
  const crewId = role.kind === "astronaut" ? role.crewId : "C3";
  const member = crew.find((c) => c.id === crewId);
  const regions = useMemo(() => {
    const base = getRegions(crewId);
    return station ? applyLive(base, crewId, station.crew.find((c) => c.id === crewId), station.alerts) : base;
  }, [crewId, station]);
  // With the backend, only the CMO or ground may decide; mock mode keeps the original demo behaviour.
  const canDecide = !station || role.kind !== "astronaut";
  const today = useMemo(() => getToday(routine, crewId), [routine, crewId]);
  const top = worst(regions);

  // An alert opens its region; otherwise start on the day summary.
  const [selected, setSelected] = useState<RegionId | null>(
    top && top.level !== "L0" ? top.id : null,
  );
  // Live alerts arrive after the first render: follow the worst region until the user picks one.
  const [userPicked, setUserPicked] = useState(false);
  const pick = (id: RegionId | null) => {
    setUserPicked(true);
    setSelected(id);
  };
  useEffect(() => {
    if (!userPicked && top && top.level !== "L0") setSelected(top.id);
  }, [userPicked, top?.id, top?.level]);
  const region = regions.find((r) => r.id === selected) ?? null;
  const level: StatusLevel = top?.level ?? "L0";
  const attention = regions.filter((r) => r.level !== "L0");
  const summary =
    attention.length === 0
      ? "All systems normal"
      : `${attention.length} item${attention.length > 1 ? "s" : ""} need${attention.length > 1 ? "" : "s"} your attention: ${attention.map((r) => r.label.toLowerCase()).join(", ")}`;

  return (
    <div className="flex h-full flex-col gap-5">
      <div className="flex items-baseline justify-between">
        <h1 className="font-heading text-2xl font-semibold">
          {member ? `${member.id} · ${member.callsign}` : crewId}
          {role.kind !== "astronaut" && (
            <span className="ml-2 text-sm font-normal text-muted-foreground">
              viewed as {role.kind === "cmo" ? "CMO" : "ground"}
            </span>
          )}
        </h1>
        {member && <p className="text-sm text-muted-foreground">Module: {member.module}</p>}
      </div>

      <TodayStrip level={level} summary={summary} {...today} />

      <div className="grid min-h-0 flex-1 grid-cols-[minmax(0,5fr)_minmax(0,6fr)] gap-5">
        <div className="flex min-h-[520px] flex-col gap-4">
          <div className="min-h-0 flex-1 rounded-xl border border-border bg-card/40">
            <Body3D regions={regions} selected={selected} onSelect={pick} />
          </div>
          <RegionChips regions={regions} selected={selected} onSelect={pick} />
          <p className="text-center text-[11px] text-muted-foreground">{MODEL_CREDIT}</p>
        </div>

        <div className="min-h-0">
          <AnimatePresence mode="wait">
            <motion.div
              key={region?.id ?? "summary"}
              className="h-full"
              initial={{ opacity: 0, x: 12 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -12 }}
              transition={{ duration: 0.18 }}
            >
              {region ? (
                <DetailPanel region={region} canDecide={canDecide} onDecision={station ? onDecision : undefined} />
              ) : (
                <DaySummary regions={regions} onSelect={pick} />
              )}
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}

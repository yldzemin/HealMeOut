import { useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import type { CrewMember, StatusLevel, ViewerRole } from "@/types";
import { forViewer, getRegions, getToday, LEVEL_RANK, type Region, type RegionId, type Routine } from "@/data/personal";
import { Body3D, MODEL_CREDIT } from "@/components/Body3D";
import { RegionChips } from "@/components/RegionChips";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/regionStyle";
import { DetailPanel, type AlertDecision } from "@/components/DetailPanel";
import type { StationView } from "@/api/client";
import { applyLive } from "@/data/live";
import { TodayStrip } from "@/components/TodayStrip";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

/** The crew member a CMO or ground viewer opens first: the highest level, then the lowest id. */
function mostUrgent(crew: CrewMember[]): string | undefined {
  return [...crew].sort((a, b) => LEVEL_RANK[b.level] - LEVEL_RANK[a.level] || a.id.localeCompare(b.id))[0]?.id;
}

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
  const self = role.kind === "astronaut";
  // CMO and ground pick whom to look at; they start on the most urgent crew member.
  const [picked, setPicked] = useState<string | null>(null);
  const crewId = self ? role.crewId : (picked ?? mostUrgent(crew) ?? "C1");
  const member = crew.find((c) => c.id === crewId);
  const card = station?.crew.find((c) => c.id === crewId);
  const regions = useMemo(() => {
    const base = station ? applyLive(getRegions(crewId, true), crewId, card, station.alerts) : getRegions(crewId);
    // Check-in and reaction-test values are the astronaut's own (BACKEND.md §6).
    return forViewer(base, self);
  }, [crewId, station, card, self]);
  // With the backend, only the CMO or ground may decide; mock mode keeps the original demo behaviour.
  const canDecide = !station || !self;
  // Live: today's check-in may already be saved on the station.
  const lastCheckIn = card?.checkIns?.[card.checkIns.length - 1];
  const checkedInToday = lastCheckIn?.date === new Date().toISOString().slice(0, 10);
  const today = useMemo(() => {
    const t = getToday({ ...routine, checkinDone: routine.checkinDone || checkedInToday }, crewId);
    return self ? t : { ...t, tasks: t.tasks.filter((x) => x.id !== "checkin"), streakDays: undefined, reactionBestMs: undefined };
  }, [routine, checkedInToday, crewId, self]);
  const top = worst(regions);

  // An alert opens its region; otherwise start on the day summary.
  const [selected, setSelected] = useState<RegionId | null>(
    top && top.level !== "L0" ? top.id : null,
  );
  // Live alerts arrive after the first render: follow the worst region until the user picks one.
  const [userPicked, setUserPicked] = useState(false);
  useEffect(() => {
    setUserPicked(false);
    setSelected(null);
  }, [crewId]);
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
        <div className="flex items-center gap-3">
          {member && <p className="text-sm text-muted-foreground">Module: {member.module}</p>}
          {!self && (
            <Select value={crewId} onValueChange={setPicked}>
              <SelectTrigger className="w-48" aria-label="Crew member">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {crew.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.id} · {c.callsign} · {c.level}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>
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
              key={`${crewId}-${region?.id ?? "summary"}`}
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

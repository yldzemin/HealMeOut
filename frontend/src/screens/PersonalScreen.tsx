import { useMemo, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import type { CrewMember, StatusLevel, ViewerRole } from "@/types";
import { getRegions, getToday, LEVEL_RANK, type Region, type RegionId } from "@/data/personal";
import { BodyFigure, LEVEL_COLOR, LEVEL_WORD, RegionChips } from "@/components/BodyFigure";
import { DetailPanel } from "@/components/DetailPanel";
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

export function PersonalScreen({ crew, role }: { crew: CrewMember[]; role: ViewerRole }) {
  const crewId = role.kind === "astronaut" ? role.crewId : "C3";
  const member = crew.find((c) => c.id === crewId);
  const regions = useMemo(() => getRegions(crewId), [crewId]);
  const today = useMemo(() => getToday(), []);
  const top = worst(regions);

  // An alert opens its region; otherwise start on the day summary.
  const [selected, setSelected] = useState<RegionId | null>(
    top && top.level !== "L0" ? top.id : null,
  );
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
          <div className="min-h-0 flex-1">
            <BodyFigure regions={regions} selected={selected} onSelect={setSelected} />
          </div>
          <RegionChips regions={regions} selected={selected} onSelect={setSelected} />
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
              {region ? <DetailPanel region={region} /> : <DaySummary regions={regions} onSelect={setSelected} />}
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}

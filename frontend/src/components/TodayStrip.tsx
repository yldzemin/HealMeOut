import { motion } from "motion/react";
import { Check, Clock, Flame, Timer } from "lucide-react";
import type { StatusLevel } from "@/types";
import type { TodayTask } from "@/data/personal";
import { LEVEL_COLOR } from "@/components/regionStyle";

function Ring({ done, total }: { done: number; total: number }) {
  const r = 22;
  const c = 2 * Math.PI * r;
  const frac = total === 0 ? 0 : done / total;
  return (
    <svg viewBox="0 0 56 56" className="size-14 -rotate-90" aria-label={`${done} of ${total} daily tasks done`}>
      <circle cx="28" cy="28" r={r} fill="none" stroke="var(--border)" strokeWidth="5" />
      <motion.circle
        cx="28"
        cy="28"
        r={r}
        fill="none"
        stroke="var(--primary)"
        strokeWidth="5"
        strokeLinecap="round"
        strokeDasharray={c}
        initial={{ strokeDashoffset: c }}
        animate={{ strokeDashoffset: c * (1 - frac) }}
        transition={{ duration: 0.8, ease: "easeOut" }}
      />
    </svg>
  );
}

/** Today strip: overall status, daily ring, streak, next measurement, personal best. */
export function TodayStrip({
  level,
  summary,
  tasks,
  streakDays,
  nextMeasurement,
  reactionBestMs,
}: {
  level: StatusLevel;
  summary: string;
  tasks: TodayTask[];
  streakDays: number;
  nextMeasurement: string;
  reactionBestMs: number;
}) {
  const done = tasks.filter((t) => t.done).length;
  return (
    <section aria-label="Today" className="flex flex-wrap items-center gap-x-8 gap-y-3 rounded-xl border border-border bg-card px-5 py-3">
      <div className="flex items-center gap-3">
        <span className="size-3 rounded-full" style={{ background: LEVEL_COLOR[level] }} aria-hidden />
        <div>
          <p className="text-xs text-muted-foreground">Today</p>
          <p className="text-sm font-medium">{summary}</p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="relative">
          <Ring done={done} total={tasks.length} />
          <span className="absolute inset-0 flex items-center justify-center text-xs font-semibold tabular-nums">
            {done}/{tasks.length}
          </span>
        </div>
        <ul className="grid grid-cols-2 gap-x-4 gap-y-0.5 text-xs">
          {tasks.map((t) => (
            <li key={t.id} className={t.done ? "text-foreground" : "text-muted-foreground"}>
              <span className="inline-flex items-center gap-1">
                {t.done ? <Check className="size-3 text-status-ok" aria-hidden /> : <span className="inline-block size-3" aria-hidden />}
                {t.label}
                <span className="sr-only">{t.done ? " (done)" : " (to do)"}</span>
              </span>
            </li>
          ))}
        </ul>
      </div>

      <div className="flex items-center gap-6 text-sm">
        <span className="flex items-center gap-1.5">
          <Flame className="size-4 text-status-cmo" aria-hidden />
          {streakDays}-day check-in streak
        </span>
        <span className="flex items-center gap-1.5 text-muted-foreground">
          <Timer className="size-4" aria-hidden />
          Reaction best {reactionBestMs} ms
        </span>
        <span className="flex items-center gap-1.5 text-muted-foreground">
          <Clock className="size-4" aria-hidden />
          {nextMeasurement}
        </span>
      </div>
    </section>
  );
}

import { useEffect, useState } from "react";
import { BellRing, CalendarClock, ChevronsRight, Database, RadioTower, Undo2 } from "lucide-react";
import type { DataMode } from "@/api/useStation";
import type { ApiClock } from "@/api/client";
import type { CrewMember, ViewerRole } from "@/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { RoleSwitcher } from "@/components/RoleSwitcher";

/** ISS keeps GMT/UTC as station time. */
function useStationTime(): string {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);
  return now.toISOString().slice(11, 19);
}

/** "Tue 17 Nov" for a YYYY-MM-DD date, read as UTC. */
const dayLabel = (iso: string) =>
  new Date(`${iso}T12:00:00Z`).toLocaleDateString("en-GB", { weekday: "short", day: "numeric", month: "short", timeZone: "UTC" });

/**
 * Demo clock (#81): move the station one day ahead to log another night and watch the Twin change.
 * Live only; refused while a scenario runs, because scenarios keep their own timeline.
 */
function DayControls({
  clock,
  scenarioRunning,
  onNextDay,
  onBackToToday,
}: {
  clock: ApiClock;
  scenarioRunning: boolean;
  onNextDay: () => Promise<void>;
  onBackToToday: () => Promise<void>;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const run = (fn: () => Promise<void>) => {
    setBusy(true);
    setError(null);
    fn()
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
      .finally(() => setBusy(false));
  };
  const ahead = clock.dayOffset > 0;

  return (
    <div className="flex items-center gap-2">
      <span
        className={`flex items-center gap-1.5 whitespace-nowrap rounded-md px-2 py-1 text-sm tabular-nums ${ahead ? "bg-primary/15 text-foreground" : "text-muted-foreground"}`}
        title={ahead ? `Simulated: ${clock.dayOffset} day${clock.dayOffset > 1 ? "s" : ""} after today` : "Station date (UTC)"}
      >
        <CalendarClock className="size-4 text-primary" aria-hidden />
        {dayLabel(clock.today)}
        {ahead && <span className="text-xs font-medium text-primary">Day +{clock.dayOffset}</span>}
      </span>
      <Button
        size="sm"
        variant="outline"
        className="gap-1"
        disabled={busy || scenarioRunning}
        title={
          scenarioRunning
            ? "Reset the scenario first: scenarios keep their own timeline"
            : "Move the station one day ahead (simulated): log another night and watch the Twin change"
        }
        onClick={() => run(onNextDay)}
      >
        Next day
        <ChevronsRight className="size-4" aria-hidden />
      </Button>
      {ahead && (
        <Button
          size="sm"
          variant="ghost"
          className="gap-1 text-muted-foreground"
          disabled={busy}
          title="Back to today: resets the demo station, including check-ins entered on later days"
          onClick={() => run(onBackToToday)}
        >
          <Undo2 className="size-4" aria-hidden />
          Back to today
        </Button>
      )}
      {error && (
        <span className="max-w-48 truncate text-xs text-status-emergency" role="alert" title={error}>
          {error}
        </span>
      )}
    </div>
  );
}

export function StatusBar({
  crew,
  role,
  onRoleChange,
  openAlerts,
  mode,
  clock,
  scenarioRunning = false,
  onNextDay,
  onBackToToday,
}: {
  crew: CrewMember[];
  role: ViewerRole;
  onRoleChange: (role: ViewerRole) => void;
  openAlerts: number;
  mode: DataMode;
  /** Live only: the station's demo clock. */
  clock?: ApiClock;
  scenarioRunning?: boolean;
  onNextDay?: () => Promise<void>;
  onBackToToday?: () => Promise<void>;
}) {
  const time = useStationTime();

  return (
    <header className="flex h-14 items-center justify-between gap-4 border-b border-border bg-sidebar px-5">
      <div className="flex min-w-0 items-center gap-5">
        <span className="whitespace-nowrap font-heading text-lg font-semibold tracking-wide text-primary">ASTRA-TWIN</span>
        <span className="whitespace-nowrap font-mono text-sm tabular-nums text-muted-foreground" aria-label="Station time UTC">
          GMT {time}
        </span>
        {clock && onNextDay && onBackToToday && (
          <DayControls clock={clock} scenarioRunning={scenarioRunning} onNextDay={onNextDay} onBackToToday={onBackToToday} />
        )}
        {/* Full text on wide screens; the icon keeps the meaning (and a tooltip) where space is short. */}
        <span className="flex items-center gap-2 whitespace-nowrap text-sm text-muted-foreground" title="Ground link: signal acquired (simulated)">
          <RadioTower className="size-4 text-status-ok" aria-hidden />
          <span className="hidden 2xl:inline">
            Ground link: signal acquired <span className="text-xs">(simulated)</span>
          </span>
          <span className="sr-only 2xl:hidden">Ground link: signal acquired (simulated)</span>
        </span>
      </div>

      <div className="flex shrink-0 items-center gap-4">
        <Badge
          variant="outline"
          className="gap-1.5"
          title={mode === "live" ? "Connected to the station backend" : "Backend not reachable: showing mock data"}
        >
          <Database className={`size-3.5 ${mode === "live" ? "text-status-ok" : "text-muted-foreground"}`} aria-hidden />
          {mode === "live" ? "Live station data" : mode === "connecting" ? "Connecting…" : "Mock data"}
        </Badge>
        <Badge variant={openAlerts > 0 ? "destructive" : "secondary"} className="gap-1.5">
          <BellRing className="size-3.5" aria-hidden />
          {openAlerts === 0 ? "No open alerts" : `${openAlerts} open alert${openAlerts > 1 ? "s" : ""}`}
        </Badge>
        <RoleSwitcher crew={crew} role={role} onChange={onRoleChange} />
      </div>
    </header>
  );
}

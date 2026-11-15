import { useEffect, useState } from "react";
import { BellRing, Database, RadioTower } from "lucide-react";
import type { DataMode } from "@/api/useStation";
import type { CrewMember, ViewerRole } from "@/types";
import { Badge } from "@/components/ui/badge";
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

export function StatusBar({
  crew,
  role,
  onRoleChange,
  openAlerts,
  mode,
}: {
  crew: CrewMember[];
  role: ViewerRole;
  onRoleChange: (role: ViewerRole) => void;
  openAlerts: number;
  mode: DataMode;
}) {
  const time = useStationTime();

  return (
    <header className="flex h-14 items-center justify-between border-b border-border bg-sidebar px-5">
      <div className="flex items-center gap-6">
        <span className="whitespace-nowrap font-heading text-lg font-semibold tracking-wide text-primary">ASTRA-TWIN</span>
        <span className="whitespace-nowrap font-mono text-sm tabular-nums text-muted-foreground" aria-label="Station time UTC">
          GMT {time}
        </span>
        <span className="flex items-center gap-2 whitespace-nowrap text-sm text-muted-foreground">
          <RadioTower className="size-4 text-status-ok" aria-hidden />
          Ground link: signal acquired <span className="text-xs">(simulated)</span>
        </span>
      </div>

      <div className="flex items-center gap-5">
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

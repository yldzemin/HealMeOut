import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import type { CrewMember, ScreenId, ViewerRole } from "@/types";
import { decideAlert, getMockCrew, startScenario, stopScenario, toCrewMember } from "@/api/client";
import { useStation } from "@/api/useStation";
import { StatusBar } from "@/components/StatusBar";
import { LeftRail } from "@/components/LeftRail";
import { PersonalScreen } from "@/screens/PersonalScreen";
import { CrewDeckScreen } from "@/screens/CrewDeckScreen";
import { EmergencyOverlay } from "@/components/EmergencyOverlay";
import { SupportAlertBanner } from "@/components/SupportAlertBanner";
import { CheckInScreen } from "@/screens/CheckInScreen";
import { EnvironmentScreen } from "@/screens/EnvironmentScreen";
import { TwinScreen } from "@/screens/TwinScreen";
import { INITIAL_ROUTINE, LEVEL_RANK, type Routine } from "@/data/personal";

const SCREEN_IDS: ScreenId[] = ["personal", "crew", "environment", "twin", "checkin"];

/** Deep links for demos and screenshots: ?screen=environment&role=cmo | ground | astronaut:C3 */
function fromUrl(): { screen: ScreenId; role: ViewerRole } {
  const q = new URLSearchParams(window.location.search);
  const s = q.get("screen") as ScreenId | null;
  const r = q.get("role") ?? "";
  const role: ViewerRole = r === "cmo" ? { kind: "cmo" } : r === "ground" ? { kind: "ground" } : { kind: "astronaut", crewId: r.startsWith("astronaut:") ? r.slice(10) : "C3" };
  return { screen: s && SCREEN_IDS.includes(s) ? s : "personal", role };
}

export default function App() {
  const initial = fromUrl();
  const [mockCrew, setMockCrew] = useState<CrewMember[]>([]);
  const [role, setRole] = useState<ViewerRole>(initial.role);
  const [screen, setScreen] = useState<ScreenId>(initial.screen);
  const [routine, setRoutine] = useState<Routine>(INITIAL_ROUTINE);
  // Mock mode only: the emergency is local. With the backend it follows the live EMERGENCY alert.
  const [localEmergency, setLocalEmergency] = useState<{ patientId: string; startedAt: number } | null>(null);
  const { mode, station, refresh } = useStation(role);
  const live = station !== null;

  useEffect(() => {
    getMockCrew().then(setMockCrew).catch((err) => console.error(err));
  }, []);

  const liveAlert = station?.alerts.find((a) => a.active && a.category === "EMERGENCY");
  const emergency = live
    ? liveAlert
      ? { patientId: liveAlert.crewId, startedAt: Date.parse(liveAlert.createdAt) }
      : null
    : localEmergency;

  const baseCrew = live ? station.crew.map(toCrewMember) : mockCrew;
  const crew = !live && emergency ? baseCrew.map((c) => (c.id === emergency.patientId ? { ...c, level: "L4" as const } : c)) : baseCrew;
  // An "open alert" needs CMO or ground action (L2+); L1 is self-care.
  const openAlerts = live
    ? station.alerts.filter((a) => a.active && a.status === "open" && LEVEL_RANK[a.level] >= LEVEL_RANK.L2).length
    : crew.filter((c) => c.level !== "L0" && c.level !== "L1").length;

  // Team decision #51: crisis wording in the support companion alerts the CMO (who and when, not what).
  const supportAlerts =
    live && role.kind === "cmo" ? station.alerts.filter((a) => a.active && a.status === "open" && a.category === "SUPPORT") : [];

  const startEmergency = async (patientId: string) => {
    if (!live) {
      setLocalEmergency({ patientId, startedAt: Date.now() });
      return;
    }
    await startScenario("chest-pain"); // backend patient: C2, the same as the Crew Deck button
    await refresh();
  };
  const endEmergency = async () => {
    if (!live) {
      setLocalEmergency(null);
      return;
    }
    await stopScenario();
    await refresh();
  };

  return (
    <div className="flex h-screen flex-col bg-background text-foreground">
      <StatusBar crew={crew} role={role} onRoleChange={setRole} openAlerts={openAlerts} mode={mode} />
      {supportAlerts.length > 0 && (
        <SupportAlertBanner
          alerts={supportAlerts}
          crew={crew}
          onAcknowledge={async (id) => {
            await decideAlert(id, "approve", role);
            await refresh();
          }}
        />
      )}
      <div className="flex min-h-0 flex-1">
        <LeftRail current={screen} onSelect={setScreen} />
        <main className="min-w-0 flex-1 overflow-auto p-8">
          <AnimatePresence mode="wait">
            <motion.div
              key={screen}
              className="h-full"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.18 }}
            >
              {screen === "personal" ? (
                <PersonalScreen
                  crew={crew}
                  role={role}
                  routine={routine}
                  station={station}
                  onDecision={async (id, d) => {
                    await decideAlert(id, d, role);
                    await refresh();
                  }}
                />
              ) : screen === "crew" ? (
                <CrewDeckScreen crew={crew} onStartEmergency={(patientId) => void startEmergency(patientId)} />
              ) : screen === "environment" ? (
                <EnvironmentScreen role={role} station={station} refresh={refresh} />
              ) : screen === "twin" ? (
                <TwinScreen role={role} crew={crew} station={station} />
              ) : (
                <CheckInScreen role={role} routine={routine} onRoutine={setRoutine} live={live} onSaved={refresh} />
              )}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
      {emergency && (
        <EmergencyOverlay crew={crew} patientId={emergency.patientId} startedAt={emergency.startedAt} onEnd={() => void endEmergency()} />
      )}
      <footer className="border-t border-border bg-sidebar px-5 py-1.5 text-xs text-muted-foreground">
        Simulation, not medical advice · NASA data via OSDR and CDAWeb · Values marked “simulated” are modelled
      </footer>
    </div>
  );
}

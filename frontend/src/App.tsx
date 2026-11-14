import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import type { CrewMember, ScreenId, ViewerRole } from "@/types";
import { getCrew } from "@/api/client";
import { StatusBar } from "@/components/StatusBar";
import { LeftRail } from "@/components/LeftRail";
import { ScreenPlaceholder } from "@/screens/ScreenPlaceholder";
import { PersonalScreen } from "@/screens/PersonalScreen";

const SCREENS: Record<ScreenId, { title: string; description: string }> = {
  personal: {
    title: "Personal",
    description: "Your twin: clickable body figure, indicators with plain-language explanations, next measurement.",
  },
  crew: {
    title: "Crew Deck",
    description: "Whole crew at a glance: who needs help and where. Psychological data is never shown here.",
  },
  environment: {
    title: "Environment",
    description: "Cabin CO2, temperature, humidity and radiation dose from NASA data.",
  },
  twin: {
    title: "Twin",
    description: "T+0 / T+6 / T+12 h forecasts, labelled as modelled.",
  },
  checkin: {
    title: "Check-in",
    description: "The daily 2-minute check-in: mood, fatigue, stress, sleep, workload, crew climate, food.",
  },
};

export default function App() {
  const [crew, setCrew] = useState<CrewMember[]>([]);
  const [role, setRole] = useState<ViewerRole>({ kind: "astronaut", crewId: "C3" });
  const [screen, setScreen] = useState<ScreenId>("personal");

  useEffect(() => {
    getCrew().then(setCrew).catch((err) => console.error(err));
  }, []);

  // L1 is self-care; an "open alert" needs CMO or ground action (L2+).
  const openAlerts = crew.filter((c) => c.level !== "L0" && c.level !== "L1").length;

  return (
    <div className="flex h-screen flex-col bg-background text-foreground">
      <StatusBar crew={crew} role={role} onRoleChange={setRole} openAlerts={openAlerts} />
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
                <PersonalScreen crew={crew} role={role} />
              ) : (
                <ScreenPlaceholder {...SCREENS[screen]} />
              )}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
      <footer className="border-t border-border bg-sidebar px-5 py-1.5 text-xs text-muted-foreground">
        Simulation, not medical advice · NASA data via OSDR · Values marked “simulated” are modelled
      </footer>
    </div>
  );
}

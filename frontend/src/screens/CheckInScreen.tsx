import { useState } from "react";
import { ChevronsRight, Lock } from "lucide-react";
import type { ViewerRole } from "@/types";
import { CheckInFlow } from "@/components/CheckInFlow";
import { SupportChat } from "@/components/SupportChat";
import { postCheckIn } from "@/api/client";
import { ReactionTest } from "@/components/ReactionTest";
import { recentShortNights, REACTION_BASELINE_MS, type Routine } from "@/data/personal";
import { profileFor } from "@/data/crewProfiles";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

/**
 * Check-in screen: daily check-in and the weekly reaction test.
 * Private to the astronaut (NASA-STD-3001 V1 6010/6011): other roles see a notice.
 */
export function CheckInScreen({
  role,
  routine,
  onRoutine,
  live = false,
  onSaved,
  day,
}: {
  role: ViewerRole;
  routine: Routine;
  onRoutine: (r: Routine) => void;
  /** Backend reachable: answers are saved privately (SELF only) and the companion is available. */
  live?: boolean;
  onSaved?: () => Promise<void>;
  /** The station date (#81): a new day starts a new check-in. */
  day?: string;
}) {
  // ?tab=talk|reaction opens a tab directly (demo deep link).
  const [tab, setTab] = useState<"checkin" | "reaction" | "talk">(() => {
    const t = new URLSearchParams(window.location.search).get("tab");
    return t === "talk" || t === "reaction" ? t : "checkin";
  });
  const [saveError, setSaveError] = useState<string | null>(null);

  if (role.kind !== "astronaut") {
    return (
      <Card className="max-w-2xl">
        <CardContent className="flex items-start gap-3 p-8">
          <Lock className="mt-1 size-5 text-muted-foreground" aria-hidden />
          <div>
            <p className="font-heading text-xl font-semibold">Private to the astronaut</p>
            <p className="text-sm text-muted-foreground">
              Check-in answers and reaction results are never shown to the {role.kind === "cmo" ? "CMO" : "ground team"} or on the Crew Deck. The
              astronaut can share them at a private conference (NASA-STD-3001, private psychological communications).
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="flex max-w-4xl flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="font-heading text-2xl font-semibold">Check-in</h1>
        <div className="flex gap-2" role="tablist" aria-label="Check-in type">
          <Button role="tab" aria-selected={tab === "checkin"} variant={tab === "checkin" ? "default" : "outline"} onClick={() => setTab("checkin")}>
            Daily check-in{routine.checkinDone ? " ✓" : ""}
          </Button>
          <Button role="tab" aria-selected={tab === "reaction"} variant={tab === "reaction" ? "default" : "outline"} onClick={() => setTab("reaction")}>
            Reaction test (weekly)
          </Button>
          {live && (
            <Button role="tab" aria-selected={tab === "talk"} variant={tab === "talk" ? "default" : "outline"} onClick={() => setTab("talk")}>
              Talk it through
            </Button>
          )}
        </div>
      </div>
      {saveError && <p className="text-sm text-status-emergency">Check-in not saved: {saveError}</p>}
      {tab === "talk" && live ? (
        <SupportChat view={role} />
      ) : tab === "checkin" ? (
        <CheckInFlow
          key={day}
          recentShortNights={recentShortNights(role.crewId)}
          onDone={(c) => {
            onRoutine({ ...routine, checkinDone: true });
            if (!live) return;
            setSaveError(null);
            postCheckIn(role, { mood: c.mood, fatigue: c.fatigue, stress: c.stress, conflict: c.conflict, sleepHours: c.sleepHours })
              .then(() => onSaved?.())
              .catch((e: unknown) => setSaveError(e instanceof Error ? e.message : String(e)));
          }}
        />
      ) : (
        <ReactionTest
          baselineMs={REACTION_BASELINE_MS}
          bestMs={Math.min(routine.reactionBestMs, profileFor(role.crewId).reactionBestMs)}
          onResult={(r) => {
            if (r.responses > 0 && r.medianMs < Math.min(routine.reactionBestMs, profileFor(role.crewId).reactionBestMs))
              onRoutine({ ...routine, reactionBestMs: r.medianMs });
          }}
        />
      )}
      {live && tab === "checkin" && routine.checkinDone && (
        <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
          <ChevronsRight className="size-4 text-primary" aria-hidden />
          Today's check-in is saved. To log another night and watch your Twin change, use <span className="font-medium text-foreground">Next day</span> in the top bar.
        </p>
      )}
    </div>
  );
}

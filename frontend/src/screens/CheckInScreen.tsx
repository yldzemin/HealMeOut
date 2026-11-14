import { useState } from "react";
import { Lock } from "lucide-react";
import type { ViewerRole } from "@/types";
import { CheckInFlow } from "@/components/CheckInFlow";
import { ReactionTest } from "@/components/ReactionTest";
import { recentShortNights, REACTION_BASELINE_MS, type Routine } from "@/data/personal";
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
}: {
  role: ViewerRole;
  routine: Routine;
  onRoutine: (r: Routine) => void;
}) {
  const [tab, setTab] = useState<"checkin" | "reaction">("checkin");

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
        </div>
      </div>
      {tab === "checkin" ? (
        <CheckInFlow recentShortNights={recentShortNights(role.crewId)} onDone={() => onRoutine({ ...routine, checkinDone: true })} />
      ) : (
        <ReactionTest
          baselineMs={REACTION_BASELINE_MS}
          bestMs={routine.reactionBestMs}
          onResult={(r) => {
            if (r.responses > 0 && r.medianMs < routine.reactionBestMs) onRoutine({ ...routine, reactionBestMs: r.medianMs });
          }}
        />
      )}
    </div>
  );
}

import { useState } from "react";
import { HeartHandshake } from "lucide-react";
import type { ApiAlert } from "@/api/client";
import type { CrewMember } from "@/types";
import { Button } from "@/components/ui/button";

/**
 * CMO only (team decision #51): a crew member wrote crisis wording to the private support companion.
 * The alert says who and when, never what they wrote. Acknowledging closes it; a new crisis message
 * reopens it.
 */
export function SupportAlertBanner({
  alerts,
  crew,
  onAcknowledge,
}: {
  alerts: ApiAlert[];
  crew: CrewMember[];
  onAcknowledge: (alertId: string) => Promise<void>;
}) {
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  return (
    <section aria-label="Crew members who need support now" className="flex flex-col gap-2 border-b border-status-cmo/60 bg-status-cmo/10 px-5 py-3" role="alert">
      {alerts.map((a) => {
        const m = crew.find((c) => c.id === a.crewId);
        return (
          <div key={a.id} className="flex items-start gap-3">
            <HeartHandshake className="mt-0.5 size-5 shrink-0 text-status-cmo" aria-hidden />
            <div className="min-w-0 flex-1">
              <p className="font-heading font-semibold">
                {m ? `${m.id} · ${m.callsign}` : a.crewId}: {a.title.toLowerCase()}
              </p>
              <p className="text-sm text-muted-foreground">{a.explanation}</p>
              <p className="text-sm">{a.recommendation}</p>
            </div>
            <Button
              size="sm"
              className="shrink-0"
              disabled={busy === a.id}
              onClick={() => {
                setBusy(a.id);
                setError(null);
                onAcknowledge(a.id)
                  .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
                  .finally(() => setBusy(null));
              }}
            >
              {busy === a.id ? "Saving…" : "I am on it"}
            </Button>
          </div>
        );
      })}
      {error && <p className="text-xs text-status-emergency">{error}</p>}
      <p className="text-[11px] text-muted-foreground">
        Private: only you as CMO and the astronaut see this, not the Crew Deck or ground. Team decision #51.
      </p>
    </section>
  );
}

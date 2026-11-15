import { useEffect, useState } from "react";
import { ChevronDown } from "lucide-react";
import { getCompanionProfile, type ApiCompanionProfile, type ViewKind } from "@/api/client";

/**
 * "What your companion knows about you": the astronaut's own profile (personality, hobbies, what helps,
 * faith, worries, style, family) and their last 3 days, exactly as the local model gets them. Private
 * to the astronaut (BACKEND §6, §10); `refreshKey` reloads it after a new message or check-in.
 */
export function CompanionProfile({ view, refreshKey }: { view: ViewKind; refreshKey: number }) {
  const [profile, setProfile] = useState<ApiCompanionProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const viewKey = JSON.stringify(view);

  useEffect(() => {
    getCompanionProfile(view)
      .then((p) => {
        setProfile(p);
        setError(null);
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)));
    // `view` is a new object on every render; viewKey identifies it.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewKey, refreshKey]);

  if (error) return <p className="text-xs text-status-emergency">{error}</p>;
  if (!profile) return <p className="text-xs text-muted-foreground">Loading your profile…</p>;
  const p = profile.persona;
  const rows: [string, string][] = [
    ["You", profile.who],
    ["Personality", p.traits.join(", ")],
    ["Hobbies", p.hobbies.join(", ")],
    ["What helps you", p.helps.join(", ")],
    ["Faith", p.faith ?? "Not religious: the companion never brings it up"],
    ["What worries you", p.worries.join(", ")],
    ["Replies you like", p.style],
    ["Family", p.family],
  ];

  return (
    <section aria-label="What your companion knows about you" className="flex flex-col gap-3 rounded-lg border border-border bg-muted/30 p-4 text-sm">
      <dl className="grid grid-cols-[minmax(0,10rem)_minmax(0,1fr)] gap-x-4 gap-y-1.5">
        {rows.map(([k, v]) => (
          <div key={k} className="contents">
            <dt className="text-muted-foreground">{k}</dt>
            <dd>{v}</dd>
          </div>
        ))}
      </dl>
      <div>
        <p className="mb-1 font-medium">Your last 3 days</p>
        <ul className="flex list-disc flex-col gap-0.5 pl-5 text-muted-foreground">
          {profile.recent.map((line) => (
            <li key={line}>{line}</li>
          ))}
        </ul>
      </div>
      <details className="group">
        <summary className="flex cursor-pointer list-none items-center gap-1 text-xs font-medium">
          Exactly what the local model receives
          <ChevronDown className="size-3.5 transition-transform group-open:rotate-180" aria-hidden />
        </summary>
        <pre className="mt-2 max-h-48 overflow-auto whitespace-pre-wrap rounded-md bg-background p-3 text-[11px] text-muted-foreground">
          {profile.sentToCompanion}
        </pre>
      </details>
      <p className="text-[11px] text-muted-foreground">
        Set by you before flight ({p.provenance}). Private: only you and the companion on this station use it; the CMO, ground and
        the rest of the crew never see it. The companion adapts to it: it suggests what helps you and never assumes a faith you
        do not have.
      </p>
    </section>
  );
}

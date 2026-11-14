import { useEffect, useMemo, useState } from "react";
import { motion } from "motion/react";
import { Check, ExternalLink, RadioTower, Siren, X } from "lucide-react";
import type { CrewMember } from "@/types";
import { CPR_SOURCE, CPR_STEPS, EMERGENCY_CARD, proposeRoles, ROLE_LABEL, type RoleId } from "@/data/station";
import { StationMap } from "@/components/StationMap";
import { Button } from "@/components/ui/button";
import { openExternal } from "@/lib/openExternal";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

function clock(start: number, now: number) {
  const s = Math.max(0, Math.floor((now - start) / 1000));
  return `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
}

function gmt(t: number) {
  return new Date(t).toISOString().slice(11, 19);
}

/**
 * Full-screen L4 emergency (docs/FRONTEND.md, Crew Deck decision 3):
 * Alert → Coordinate (system proposes roles, CMO confirms) → Act (checklist).
 */
export function EmergencyOverlay({
  crew,
  patientId,
  startedAt,
  onEnd,
}: {
  crew: CrewMember[];
  patientId: string;
  startedAt: number;
  onEnd: () => void;
}) {
  const patient = crew.find((c) => c.id === patientId);
  const [roles, setRoles] = useState<Record<RoleId, string>>(() => proposeRoles(crew, patientId));
  const [confirmed, setConfirmed] = useState(false);
  const [done, setDone] = useState<Record<string, number>>({});
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  const others = useMemo(() => crew.filter((c) => c.id !== patientId), [crew, patientId]);
  const name = (id: string) => {
    const c = crew.find((x) => x.id === id);
    return c ? `${c.id} ${c.callsign}` : "—";
  };
  const card = EMERGENCY_CARD[patientId] ?? { allergies: "Not recorded", medication: "Not recorded" };

  return (
    <motion.div
      role="alertdialog"
      aria-modal="true"
      aria-label={`Emergency: ${patient ? name(patient.id) : patientId} needs help`}
      className="fixed inset-0 z-50 flex flex-col gap-4 bg-background p-5"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
    >
      {/* Slow red frame pulse (well under 3 flashes per second). */}
      <motion.div
        className="pointer-events-none absolute inset-0 border-4 border-status-emergency"
        animate={{ opacity: [0.35, 1, 0.35] }}
        transition={{ duration: 2, repeat: Infinity }}
        aria-hidden
      />

      {/* 1. Alert */}
      <header className="flex items-center justify-between rounded-xl bg-status-emergency/15 px-5 py-3">
        <div className="flex items-center gap-3">
          <Siren className="size-7 text-status-emergency" aria-hidden />
          <div>
            <p className="font-heading text-2xl font-semibold">
              {patient ? name(patient.id) : patientId} needs help · {patient?.module}
            </p>
            <p className="text-sm text-muted-foreground">
              L4 emergency · wristband detected a collapse (simulated) · all crew alerted · elapsed {clock(startedAt, now)}
            </p>
          </div>
        </div>
        <Button variant="outline" onClick={onEnd} className="gap-2">
          <X className="size-4" aria-hidden />
          End drill
        </Button>
      </header>

      <div className="grid min-h-0 flex-1 grid-cols-[minmax(0,5fr)_minmax(0,4fr)_minmax(0,5fr)] gap-4">
        {/* Map */}
        <section className="flex min-h-0 flex-col gap-2 rounded-xl border border-border bg-card/40 p-3" aria-label="Where">
          <h2 className="text-sm font-semibold">Where</h2>
          <div className="min-h-0 flex-1">
            <StationMap
              crew={crew}
              patientId={patientId}
              focus={[roles.lead, roles.assist, roles.fetch]}
              responders={confirmed ? [roles.lead, roles.assist, roles.fetch] : []}
            />
          </div>
          <p className="text-[11px] text-muted-foreground">Crew location: concept (wearable tag), simulated</p>
        </section>

        {/* 2. Coordinate */}
        <section className="flex min-h-0 flex-col gap-3 rounded-xl border border-border bg-card p-4" aria-label="Who does what">
          <h2 className="text-sm font-semibold">Who does what</h2>
          <p className="text-xs text-muted-foreground">
            Proposed by the system from roles and distance. {confirmed ? "Confirmed by CMO." : "CMO confirms or changes."}
          </p>
          <ul className="flex flex-col gap-3">
            {(Object.keys(ROLE_LABEL) as RoleId[]).map((r) => (
              <li key={r} className="flex flex-col gap-1">
                <span className="text-xs text-muted-foreground">{ROLE_LABEL[r]}</span>
                {confirmed ? (
                  <span className="text-sm font-medium">{name(roles[r])}</span>
                ) : (
                  <Select value={roles[r]} onValueChange={(v) => setRoles((s) => ({ ...s, [r]: v }))}>
                    <SelectTrigger className="w-full" aria-label={ROLE_LABEL[r]}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {others.map((c) => (
                        <SelectItem key={c.id} value={c.id}>
                          {name(c.id)} · {c.module}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              </li>
            ))}
          </ul>
          {!confirmed && (
            <Button className="mt-auto" onClick={() => setConfirmed(true)}>
              Confirm roles (CMO)
            </Button>
          )}
          <div className="mt-auto rounded-lg border border-border p-3">
            <p className="mb-1 text-xs font-semibold">Emergency card · {patient ? name(patient.id) : patientId}</p>
            <p className="text-sm">Allergies: {card.allergies}</p>
            <p className="text-sm">Current medication: {card.medication}</p>
            <p className="mt-1 text-[11px] text-muted-foreground">Shown only in an emergency. Simulated.</p>
          </div>
        </section>

        {/* 3. Act */}
        <section className="flex min-h-0 flex-col gap-3 overflow-auto rounded-xl border border-border bg-card p-4" aria-label="What to do">
          <h2 className="text-sm font-semibold">What to do · microgravity CPR</h2>
          <ol className="flex flex-col gap-2">
            {CPR_STEPS.map((s, i) => {
              const t = done[s.id];
              return (
                <li key={s.id}>
                  <button
                    type="button"
                    onClick={() => setDone((d) => (d[s.id] ? d : { ...d, [s.id]: Date.now() }))}
                    className="flex w-full items-start gap-3 rounded-lg border border-border p-3 text-left hover:bg-accent/40 focus-visible:outline-2 focus-visible:outline-ring"
                    aria-pressed={Boolean(t)}
                  >
                    <span
                      className={`mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full border text-xs ${t ? "border-status-ok bg-status-ok/20" : "border-border"}`}
                    >
                      {t ? <Check className="size-3.5 text-status-ok" aria-hidden /> : i + 1}
                    </span>
                    <span className="flex-1">
                      <span className="block text-sm">{s.text}</span>
                      {s.note && <span className="block text-xs text-muted-foreground">{s.note}</span>}
                    </span>
                    {t && <span className="font-mono text-xs text-muted-foreground">{gmt(t)}</span>}
                  </button>
                </li>
              );
            })}
          </ol>
          <button type="button" onClick={() => openExternal(CPR_SOURCE.url)} className="inline-flex items-center gap-1 text-left text-xs text-primary hover:underline">
            {CPR_SOURCE.label}
            <ExternalLink className="size-3" aria-hidden />
          </button>
        </section>
      </div>

      <footer className="flex items-center gap-2 rounded-xl border border-border bg-card px-4 py-2 text-sm">
        <RadioTower className="size-4 text-status-ok" aria-hidden />
        Ground informed at {gmt(startedAt)} GMT · on the ISS the reply comes in seconds, but the crew does not wait.
        <span className="ml-auto text-xs text-muted-foreground">Simulation, not medical advice</span>
      </footer>
    </motion.div>
  );
}

import { useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { ArrowLeft, ArrowRight, Check, Lock } from "lucide-react";
import { DEFAULT_CHECKIN, insights, RESTORATIVE, STEPS, type CheckIn } from "@/data/checkin";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

function Slider({ label, low, high, value, onChange }: { label: string; low: string; high: string; value: number; onChange: (v: number) => void }) {
  return (
    <label className="flex flex-col gap-2">
      <span className="flex items-baseline justify-between text-sm">
        <span className="font-medium">{label}</span>
        <span className="tabular-nums text-muted-foreground">{value}</span>
      </span>
      <input type="range" min={0} max={100} step={1} value={value} onChange={(e) => onChange(Number(e.target.value))} className="accent-[var(--primary)]" />
      <span className="flex justify-between text-xs text-muted-foreground">
        <span>{low}</span>
        <span>{high}</span>
      </span>
    </label>
  );
}

function Scale({ label, value, onChange, low = "Low", high = "High" }: { label: string; value: number; onChange: (v: number) => void; low?: string; high?: string }) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="mb-2 text-sm font-medium">{label}</legend>
      <div className="flex items-center gap-2">
        <span className="w-20 text-xs text-muted-foreground">{low}</span>
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            onClick={() => onChange(n)}
            aria-pressed={value === n}
            className={cn(
              "size-10 rounded-lg border text-sm tabular-nums transition-colors focus-visible:outline-2 focus-visible:outline-ring",
              value === n ? "border-primary bg-primary text-primary-foreground" : "border-border hover:bg-accent/50",
            )}
          >
            {n}
          </button>
        ))}
        <span className="w-20 text-right text-xs text-muted-foreground">{high}</span>
      </div>
    </fieldset>
  );
}

function Toggle({ label, value, onChange }: { label: string; value: boolean; onChange: (v: boolean) => void }) {
  return (
    <button
      type="button"
      onClick={() => onChange(!value)}
      aria-pressed={value}
      className={cn(
        "flex items-center gap-3 rounded-lg border px-4 py-3 text-left text-sm focus-visible:outline-2 focus-visible:outline-ring",
        value ? "border-primary bg-accent" : "border-border hover:bg-accent/40",
      )}
    >
      <span className={cn("flex size-5 items-center justify-center rounded border", value ? "border-primary bg-primary" : "border-border")}>
        {value && <Check className="size-3.5 text-primary-foreground" aria-hidden />}
      </span>
      {label}
    </button>
  );
}

function StepBody({ id, c, set }: { id: string; c: CheckIn; set: <K extends keyof CheckIn>(k: K, v: CheckIn[K]) => void }) {
  switch (id) {
    case "feel":
      return (
        <div className="grid grid-cols-2 gap-6">
          <Slider label="Mood" low="Very low" high="Very good" value={c.mood} onChange={(v) => set("mood", v)} />
          <Slider label="Fatigue" low="Rested" high="Exhausted" value={c.fatigue} onChange={(v) => set("fatigue", v)} />
          <Slider label="Stress" low="Calm" high="Very stressed" value={c.stress} onChange={(v) => set("stress", v)} />
          <Slider label="Conflict" low="None" high="A lot" value={c.conflict} onChange={(v) => set("conflict", v)} />
        </div>
      );
    case "sleep":
      return (
        <div className="flex flex-col gap-6">
          <label className="flex flex-col gap-2">
            <span className="flex items-baseline justify-between text-sm">
              <span className="font-medium">Hours slept</span>
              <span className="font-heading text-2xl font-semibold tabular-nums">{c.sleepHours.toFixed(1)} h</span>
            </span>
            <input type="range" min={0} max={12} step={0.5} value={c.sleepHours} onChange={(e) => set("sleepHours", Number(e.target.value))} className="accent-[var(--primary)]" />
          </label>
          <Scale label="Sleep quality" low="Poor" high="Excellent" value={c.sleepQuality} onChange={(v) => set("sleepQuality", v)} />
        </div>
      );
    case "work":
      return (
        <div className="flex flex-col gap-6">
          <Scale label="Workload today" low="Light" high="Very heavy" value={c.workload} onChange={(v) => set("workload", v)} />
          <Scale label="“I had control over my timeline”" low="Disagree" high="Agree" value={c.control} onChange={(v) => set("control", v)} />
        </div>
      );
    case "crew":
      return (
        <div className="flex flex-col gap-6">
          <Scale label="The crew worked well together today" low="Disagree" high="Agree" value={c.crewWorksWell} onChange={(v) => set("crewWorksWell", v)} />
          <Scale label="“I would want this exact same crew”" low="Disagree" high="Agree" value={c.sameCrew} onChange={(v) => set("sameCrew", v)} />
          <Scale label="Our team performed well today" low="Disagree" high="Agree" value={c.teamPerformed} onChange={(v) => set("teamPerformed", v)} />
        </div>
      );
    case "mcc":
      return <Scale label="Frustration with mission control today" low="None" high="A lot" value={c.mccFrustration} onChange={(v) => set("mccFrustration", v)} />;
    case "family":
      return (
        <div className="flex flex-col gap-6">
          <label className="flex items-center justify-between gap-4 text-sm">
            <span className="font-medium">Days since your last family conference</span>
            <input
              type="number"
              min={0}
              max={60}
              value={c.daysSinceFamilyCall}
              onChange={(e) => set("daysSinceFamilyCall", Math.max(0, Math.min(60, Number(e.target.value) || 0)))}
              className="w-20 rounded-md border border-border bg-background px-2 py-1 text-right tabular-nums"
            />
          </label>
          <Toggle label="Something important is happening in my family" value={c.familyEvent} onChange={(v) => set("familyEvent", v)} />
        </div>
      );
    case "rest":
      return (
        <div className="grid grid-cols-2 gap-3">
          {RESTORATIVE.map((r) => (
            <Toggle
              key={r}
              label={r}
              value={c.restorative.includes(r)}
              onChange={(on) => set("restorative", on ? [...c.restorative, r] : c.restorative.filter((x) => x !== r))}
            />
          ))}
        </div>
      );
    case "food":
      return (
        <div className="flex flex-col gap-6">
          <label className="flex flex-col gap-2">
            <span className="flex items-baseline justify-between text-sm">
              <span className="font-medium">Share of your planned food you ate</span>
              <span className="font-heading text-2xl font-semibold tabular-nums">{c.foodPercent}%</span>
            </span>
            <input type="range" min={0} max={100} step={5} value={c.foodPercent} onChange={(e) => set("foodPercent", Number(e.target.value))} className="accent-[var(--primary)]" />
          </label>
          <Toggle label="I only ate my favourite foods today" value={c.favouritesOnly} onChange={(v) => set("favouritesOnly", v)} />
        </div>
      );
    default:
      return null;
  }
}

/** Eight short steps, one topic each; summary with supportive notes. */
export function CheckInFlow({ recentShortNights, onDone }: { recentShortNights: number; onDone: (c: CheckIn) => void }) {
  const [step, setStep] = useState(0);
  const [c, setC] = useState<CheckIn>(DEFAULT_CHECKIN);
  const [finished, setFinished] = useState(false);
  const set = <K extends keyof CheckIn>(k: K, v: CheckIn[K]) => setC((s) => ({ ...s, [k]: v }));

  if (finished) {
    const notes = insights(c, recentShortNights);
    return (
      <Card>
        <CardContent className="flex flex-col gap-5 p-8">
          <div className="flex items-center gap-3">
            <span className="flex size-10 items-center justify-center rounded-full bg-status-ok/20">
              <Check className="size-5 text-status-ok" aria-hidden />
            </span>
            <div>
              <p className="font-heading text-2xl font-semibold">Check-in done</p>
              <p className="text-sm text-muted-foreground">Your daily ring and streak are updated.</p>
            </div>
          </div>
          {notes.length > 0 && (
            <ul className="flex flex-col gap-2">
              {notes.map((n) => (
                <li key={n.text} className={cn("rounded-lg border p-3 text-sm", n.tone === "attention" ? "border-status-self/60" : "border-border")}>
                  {n.text}
                </li>
              ))}
            </ul>
          )}
          <p className="flex items-center gap-2 text-xs text-muted-foreground">
            <Lock className="size-3.5" aria-hidden />
            Only you see this. Share it with your behavioral health provider at your next private conference if you want. Notes are our design, not a diagnosis.
          </p>
        </CardContent>
      </Card>
    );
  }

  const s = STEPS[step];
  const last = step === STEPS.length - 1;
  const next = () => {
    if (last) {
      setFinished(true);
      onDone(c);
    } else setStep(step + 1);
  };

  return (
    <Card>
      <CardContent className="flex flex-col gap-6 p-8">
        <div className="flex items-center gap-3" aria-label={`Step ${step + 1} of ${STEPS.length}`}>
          <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-border">
            <motion.div className="h-full bg-primary" animate={{ width: `${((step + 1) / STEPS.length) * 100}%` }} transition={{ duration: 0.25 }} />
          </div>
          <span className="text-xs tabular-nums text-muted-foreground">
            {step + 1} / {STEPS.length}
          </span>
        </div>

        <AnimatePresence mode="wait">
          <motion.div key={s.id} initial={{ opacity: 0, x: 16 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -16 }} transition={{ duration: 0.18 }} className="flex min-h-[260px] flex-col gap-6">
            <div>
              <h2 className="font-heading text-2xl font-semibold">{s.title}</h2>
              <p className="text-xs text-muted-foreground">{s.basis}</p>
            </div>
            <StepBody id={s.id} c={c} set={set} />
          </motion.div>
        </AnimatePresence>

        <div className="flex items-center justify-between">
          <Button variant="ghost" onClick={() => setStep(Math.max(0, step - 1))} className="gap-2" aria-label="Previous step">
            <ArrowLeft className="size-4" aria-hidden />
            Back
          </Button>
          <div className="flex gap-2">
            <Button variant="outline" onClick={next}>
              Skip
            </Button>
            <Button onClick={next} className="gap-2">
              {last ? "Finish" : "Next"}
              <ArrowRight className="size-4" aria-hidden />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

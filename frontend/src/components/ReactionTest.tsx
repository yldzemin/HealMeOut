import { useCallback, useEffect, useRef, useState } from "react";
import { Timer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

/**
 * Reaction-time test modelled on NASA's Reaction Self Test (a 3-minute PVT-B on
 * the ISS; PSYCHOLOGY_AND_LONG_DURATION.md §2). Respond with Space or a click
 * when the target lights. Waits, false-start and slow cut-offs are our design.
 */
const DURATIONS = [
  { ms: 180_000, label: "Standard · 3 min" },
  { ms: 30_000, label: "Demo length · 30 s" },
];
const MIN_WAIT = 2000;
const MAX_WAIT = 5000;
const FALSE_START_MS = 100;
const SLOW_MS = 500;

export interface ReactionResult {
  medianMs: number;
  responses: number;
  falseStarts: number;
  slow: number;
  durationMs: number;
}

function median(xs: number[]) {
  const s = [...xs].sort((a, b) => a - b);
  const m = Math.floor(s.length / 2);
  return s.length % 2 ? s[m] : Math.round((s[m - 1] + s[m]) / 2);
}

type Phase = "idle" | "wait" | "go" | "done";

export function ReactionTest({
  baselineMs,
  bestMs,
  onResult,
}: {
  baselineMs: number;
  bestMs: number;
  onResult: (r: ReactionResult) => void;
}) {
  const [duration, setDuration] = useState(DURATIONS[0].ms);
  const [phase, setPhase] = useState<Phase>("idle");
  const [result, setResult] = useState<ReactionResult | null>(null);
  const [left, setLeft] = useState(0);
  const rts = useRef<number[]>([]);
  const falseStarts = useRef(0);
  const shownAt = useRef(0);
  const endAt = useRef(0);
  const timer = useRef<number | undefined>(undefined);

  const finish = useCallback(() => {
    window.clearTimeout(timer.current);
    const r: ReactionResult = {
      medianMs: rts.current.length ? median(rts.current) : 0,
      responses: rts.current.length,
      falseStarts: falseStarts.current,
      slow: rts.current.filter((x) => x > SLOW_MS).length,
      durationMs: duration,
    };
    setResult(r);
    setPhase("done");
    onResult(r);
  }, [duration, onResult]);

  const schedule = useCallback(() => {
    if (performance.now() >= endAt.current) return finish();
    setPhase("wait");
    const wait = MIN_WAIT + Math.random() * (MAX_WAIT - MIN_WAIT);
    timer.current = window.setTimeout(() => {
      shownAt.current = performance.now();
      setPhase("go");
    }, wait);
  }, [finish]);

  const start = () => {
    rts.current = [];
    falseStarts.current = 0;
    endAt.current = performance.now() + duration;
    setResult(null);
    schedule();
  };

  const respond = useCallback(() => {
    if (phase === "go") {
      const rt = performance.now() - shownAt.current;
      if (rt < FALSE_START_MS) falseStarts.current += 1;
      else rts.current.push(Math.round(rt));
      schedule();
    } else if (phase === "wait") {
      falseStarts.current += 1;
      window.clearTimeout(timer.current);
      schedule();
    }
  }, [phase, schedule]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.code === "Space" && (phase === "wait" || phase === "go")) {
        e.preventDefault();
        respond();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [phase, respond]);

  useEffect(() => {
    if (phase !== "wait" && phase !== "go") return;
    const id = window.setInterval(() => setLeft(Math.max(0, endAt.current - performance.now())), 250);
    return () => window.clearInterval(id);
  }, [phase]);

  useEffect(() => () => window.clearTimeout(timer.current), []);

  const running = phase === "wait" || phase === "go";

  return (
    <Card>
      <CardContent className="flex flex-col gap-5 p-8">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="font-heading text-2xl font-semibold">Reaction test</h2>
            <p className="text-sm text-muted-foreground">
              Press Space or click as soon as the circle lights up. Modelled on NASA’s Reaction Self Test (3-minute PVT-B on the ISS).
            </p>
          </div>
          {!running && (
            <div className="flex gap-2" role="group" aria-label="Test length">
              {DURATIONS.map((d) => (
                <Button key={d.ms} size="sm" variant={duration === d.ms ? "default" : "outline"} onClick={() => setDuration(d.ms)}>
                  {d.label}
                </Button>
              ))}
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={running ? respond : start}
          className={cn(
            "flex h-64 items-center justify-center rounded-xl border text-lg transition-colors focus-visible:outline-2 focus-visible:outline-ring",
            phase === "go" ? "border-primary bg-primary/10" : "border-border bg-card/60",
          )}
          aria-live="polite"
        >
          {phase === "idle" || phase === "done" ? (
            <span className="flex items-center gap-2">
              <Timer className="size-5" aria-hidden />
              {phase === "done" ? "Run again" : "Start test"}
            </span>
          ) : (
            <span
              className={cn("size-28 rounded-full border-4 transition-colors", phase === "go" ? "border-primary bg-primary" : "border-border")}
              aria-label={phase === "go" ? "Now" : "Wait"}
            />
          )}
        </button>

        {running && <p className="text-center text-xs tabular-nums text-muted-foreground">{Math.ceil(left / 1000)} s left</p>}

        {result && (
          <div className="grid grid-cols-4 gap-3">
            {[
              { k: "Median", v: result.responses ? `${result.medianMs} ms` : "—" },
              { k: "Responses", v: String(result.responses) },
              { k: "False starts", v: String(result.falseStarts) },
              { k: `Slow (>${SLOW_MS} ms)`, v: String(result.slow) },
            ].map((x) => (
              <div key={x.k} className="rounded-lg border border-border p-3">
                <p className="text-xs text-muted-foreground">{x.k}</p>
                <p className="font-heading text-2xl font-semibold tabular-nums">{x.v}</p>
              </div>
            ))}
            <p className="col-span-4 text-sm text-muted-foreground">
              {result.responses === 0
                ? "No responses recorded."
                : result.medianMs <= bestMs
                  ? `New personal best (previous ${bestMs} ms).`
                  : result.medianMs > baselineMs * 1.1
                    ? `Slower than your baseline (${baselineMs} ms). Short sleep can do this; it is not a diagnosis.`
                    : `Within your usual range (baseline ${baselineMs} ms).`}{" "}
              <span className="text-xs">Measured in this app{result.durationMs < 180_000 ? " · demo length, not the standard test" : ""}.</span>
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

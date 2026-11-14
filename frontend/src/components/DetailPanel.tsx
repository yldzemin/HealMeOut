import { useState } from "react";
import { AlertTriangle, CheckCircle2, ChevronDown, ExternalLink, Siren } from "lucide-react";
import { openUrl } from "@tauri-apps/plugin-opener";
import type { Indicator, MeasuredHow, Region } from "@/data/personal";
import { LEVEL_RANK } from "@/data/personal";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/BodyFigure";
import { TrendChart } from "@/components/TrendChart";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const HOW_LABEL: Record<MeasuredHow, string> = {
  live: "Live on ISS",
  periodic: "Periodic on ISS",
  "sample-return": "Sample sent to Earth",
  "research-only": "Research only",
};

function open(url: string) {
  // In Tauri, open in the system browser; in a plain browser (plan B), a new tab.
  openUrl(url).catch(() => window.open(url, "_blank", "noopener"));
}

function IndicatorRow({ ind }: { ind: Indicator }) {
  return (
    <div className="flex items-end justify-between gap-4 border-b border-border/60 py-3 last:border-b-0">
      <div className="min-w-0">
        <p className="text-sm text-muted-foreground">{ind.label}</p>
        <p className="flex items-baseline gap-1.5">
          <span className="font-heading text-3xl font-semibold tabular-nums">{ind.value}</span>
          <span className="text-sm text-muted-foreground">{ind.unit}</span>
        </p>
        {ind.baseline && <p className="text-xs text-muted-foreground">Baseline {ind.baseline}</p>}
      </div>
      <div className="flex shrink-0 flex-col items-end gap-1">
        <Badge variant={ind.provenance === "real" ? "default" : "outline"}>
          {ind.provenance === "real" ? "Real NASA data" : "Simulated"}
        </Badge>
        <span className="text-[11px] text-muted-foreground">{HOW_LABEL[ind.measuredHow]}</span>
      </div>
    </div>
  );
}

function CmoCard({ text }: { text: string }) {
  const [decision, setDecision] = useState<"approved" | "declined" | null>(null);
  return (
    <div className="rounded-lg border border-status-cmo/60 p-4">
      <p className="mb-1 text-xs font-medium text-status-cmo">Recommendation · needs CMO approval</p>
      <p className="mb-3 text-sm">{text}</p>
      {decision ? (
        <p className="text-sm text-muted-foreground">CMO {decision}. Logged.</p>
      ) : (
        <div className="flex gap-2">
          <Button size="sm" onClick={() => setDecision("approved")}>Approve</Button>
          <Button size="sm" variant="outline" onClick={() => setDecision("declined")}>Decline</Button>
        </div>
      )}
    </div>
  );
}

/** Explanation-first panel (docs/FRONTEND.md, personal screen decision 3). */
export function DetailPanel({ region }: { region: Region }) {
  const Icon = region.level === "L0" ? CheckCircle2 : region.level === "L4" ? Siren : AlertTriangle;
  const color = LEVEL_COLOR[region.level];

  return (
    <Card className="h-full overflow-auto">
      <CardContent className="flex flex-col gap-5 p-6">
        {/* 1. Status sentence: colour + icon + words. */}
        <div className="flex flex-col gap-2">
          <p className="text-xs uppercase tracking-wider text-muted-foreground">{region.label}</p>
          <div className="flex items-start gap-3">
            <Icon className="mt-1 size-6 shrink-0" style={{ color }} aria-hidden />
            <div>
              <p className="font-heading text-xl font-semibold leading-snug">{region.status}</p>
              <p className="mt-1 text-xs" style={{ color }}>
                {region.level} · {LEVEL_WORD[region.level]}
              </p>
            </div>
          </div>
        </div>

        {/* 2. Values and baseline. */}
        {region.indicators.length > 0 && (
          <div>
            {region.indicators.map((ind) => (
              <IndicatorRow key={ind.id} ind={ind} />
            ))}
          </div>
        )}

        {/* 3. Trend. */}
        {region.trend && <TrendChart trend={region.trend} />}

        {/* 4. What this means. */}
        <section>
          <h3 className="mb-1 text-sm font-semibold">What this means</h3>
          <p className="text-sm leading-relaxed text-muted-foreground">{region.meaning}</p>
        </section>

        {/* 5. What to do (CMO card from L2). */}
        <section>
          <h3 className="mb-1 text-sm font-semibold">What to do</h3>
          {LEVEL_RANK[region.level] >= LEVEL_RANK.L2 ? (
            <CmoCard text={region.todo} />
          ) : (
            <p className="text-sm leading-relaxed text-muted-foreground">{region.todo}</p>
          )}
        </section>

        {/* 6. Method and sources, collapsed. */}
        <details className="group rounded-lg border border-border p-3">
          <summary className="flex cursor-pointer list-none items-center justify-between text-sm font-semibold">
            How it’s measured and sources
            <ChevronDown className="size-4 transition-transform group-open:rotate-180" aria-hidden />
          </summary>
          <p className="mt-2 text-sm text-muted-foreground">{region.method}</p>
          <ul className="mt-2 flex flex-col gap-1">
            {region.sources.map((s) => (
              <li key={s.url}>
                <button
                  type="button"
                  onClick={() => open(s.url)}
                  className="inline-flex items-center gap-1 text-left text-xs text-primary hover:underline"
                >
                  {s.label}
                  <ExternalLink className="size-3" aria-hidden />
                </button>
              </li>
            ))}
          </ul>
        </details>
      </CardContent>
    </Card>
  );
}

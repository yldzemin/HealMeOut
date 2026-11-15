import { useMemo } from "react";
import { CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts";
import type { ApiEnvironment, ApiPartnerDose } from "@/api/client";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart";
import { openExternal } from "@/lib/openExternal";

const config = {
  tepc: { label: "NASA TEPC (μGy/h)", color: "var(--chart-1)" },
  dostel: { label: "DLR DosTel2, Columbus (μGy/h)", color: "var(--chart-3)" },
} satisfies ChartConfig;

/** Daily mean absorbed dose rate (μGy/h) per UTC day. */
function daily(items: ApiEnvironment[]): Map<string, number> {
  const sums = new Map<string, { s: number; n: number }>();
  for (const e of items) {
    if (e.doseRate_uGy_h === undefined) continue;
    const day = e.timestamp.slice(0, 10);
    const d = sums.get(day) ?? { s: 0, n: 0 };
    sums.set(day, { s: d.s + e.doseRate_uGy_h, n: d.n + 1 });
  }
  return new Map([...sums].map(([day, d]) => [day, d.s / d.n]));
}

const median = (xs: number[]) => {
  const s = [...xs].sort((a, b) => a - b);
  return s.length === 0 ? NaN : s[Math.floor(s.length / 2)];
};

/** Peak day vs the quiet days before the event (Sep 4–9), for one series. */
function rise(byDay: Map<string, number>): number {
  const quiet = [...byDay].filter(([d]) => d < "2017-09-10").map(([, v]) => v);
  return (byDay.get("2017-09-11") ?? NaN) / median(quiet);
}

/**
 * Partner-instrument data (team decision #13): DLR's DosTel2 in the Columbus module, hosted by NASA
 * OSDR RadLab, next to NASA's TEPC for the same days. Daily means, so South Atlantic Anomaly spikes
 * average out and the Sep 11 solar particle event shows in both.
 */
export function PartnerDoseCard({ partner }: { partner: ApiPartnerDose }) {
  const { points, dostelRise, tepcRise } = useMemo(() => {
    const d = daily(partner.items);
    const t = daily(partner.reference);
    const days = [...new Set([...d.keys(), ...t.keys()])].sort();
    return {
      points: days.map((day) => ({ day: day.slice(5), dostel: d.get(day), tepc: t.get(day) })),
      dostelRise: rise(d),
      tepcRise: rise(t),
    };
  }, [partner]);
  const tag = partner.tag;

  return (
    <Card>
      <CardContent className="flex flex-col gap-2 p-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <p className="font-heading text-lg font-semibold">Radiation in Columbus, September 2017</p>
          <div className="flex gap-1.5">
            <Badge>Real data</Badge>
            <Badge variant="outline">Partner instrument</Badge>
          </div>
        </div>
        <p className="w-fit rounded-md border border-status-ok/40 px-2 py-1 text-[11px] text-status-ok">
          Partner data · {tag.instrument} · {tag.agency} · {tag.location} · data hosted by{" "}
          <button type="button" className="underline" onClick={() => openExternal(tag.link)}>
            {tag.hostedBy}
          </button>
          <span className="block text-muted-foreground">{tag.why}</span>
        </p>
        <ChartContainer config={config} className="h-48 w-full">
          <LineChart data={points} margin={{ top: 6, right: 8, bottom: 0, left: -8 }}>
            <CartesianGrid vertical={false} strokeOpacity={0.15} />
            <XAxis dataKey="day" tickLine={false} axisLine={false} fontSize={11} />
            <YAxis tickLine={false} axisLine={false} fontSize={11} width={44} />
            <ChartTooltip content={<ChartTooltipContent hideLabel={false} />} />
            <Line dataKey="tepc" stroke="var(--color-tepc)" strokeWidth={2} dot={false} isAnimationActive={false} />
            <Line dataKey="dostel" stroke="var(--color-dostel)" strokeWidth={2} strokeDasharray="5 3" dot={false} isAnimationActive={false} />
          </LineChart>
        </ChartContainer>
        <p className="text-[11px] text-muted-foreground">
          Daily mean absorbed dose rate. Solid: NASA SRAG TEPC (CDAWeb). Dashed: DLR DosTel2 in the Columbus module, C3
          LYRA&rsquo;s workplace. On Sep 11 the daily mean was {tepcRise.toFixed(1)}× the quiet days (Sep 4–9) at the TEPC
          and {dostelRise.toFixed(1)}× in DosTel2. The detectors differ (tissue-equivalent vs silicon) and sit in different
          places, so compare the shape, not the values.
        </p>
      </CardContent>
    </Card>
  );
}

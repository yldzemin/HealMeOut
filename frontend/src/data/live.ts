// Overlays live backend data on the personal-screen regions (#56): latest vitals from the
// rules engine's inputs, and rule alerts on the matching body region. Static texts stay as
// fallbacks; everything live keeps its provenance label.

import type { ApiAlert, ApiCrewCard, ApiReading } from "@/api/client";
import type { StatusLevel } from "@/types";
import { LEVEL_RANK, type Indicator, type Region, type RegionId } from "@/data/personal";

const ONE_DECIMAL = new Set(["temp_c"]);

function fmt(r: ApiReading): string {
  return ONE_DECIMAL.has(r.metric) ? r.value.toFixed(1) : String(Math.round(r.value * 10) / 10);
}

function live(ind: Indicator, r: ApiReading, value = fmt(r)): Indicator {
  return { ...ind, value, provenance: r.provenance === "real" ? "real" : "simulated", measuredHow: r.measuredHow };
}

/** Which body region a rule alert belongs to (rule id is the part before the first ':'). */
function regionOf(alert: ApiAlert): RegionId | null {
  switch (alert.id.split(":")[0]) {
    case "spo2":
      return "lungs";
    case "glucose":
    case "z-score":
      return "blood";
    case "collapse":
      return "heart";
    case "psych-trend":
    case "co2":
      return "head";
    case "dose-rate":
      return "whole";
    default:
      return null;
  }
}

/** Regions with live vitals and alerts for one crew member. Unchanged if there is no card. */
export function applyLive(regions: Region[], crewId: string, card: ApiCrewCard | undefined, alerts: ApiAlert[]): Region[] {
  const vitals = new Map((card?.vitals ?? []).map((r) => [r.metric, r]));
  const mine = alerts.filter((a) => a.active && (a.crewId === crewId || a.crewId === "STATION"));

  return regions.map((region) => {
    let r: Region = { ...region, indicators: region.indicators.map((i) => ({ ...i })) };
    const set = (id: string, metric: string) => {
      const reading = vitals.get(metric);
      const i = r.indicators.findIndex((x) => x.id === id);
      if (reading && i >= 0) r.indicators[i] = live(r.indicators[i], reading);
    };
    if (r.id === "heart") {
      set("hr", "hr");
      const sys = vitals.get("bp_sys");
      const dia = vitals.get("bp_dia");
      const i = r.indicators.findIndex((x) => x.id === "bp");
      if (sys && dia && i >= 0) r.indicators[i] = live(r.indicators[i], sys, `${Math.round(sys.value)}/${Math.round(dia.value)}`);
    }
    if (r.id === "lungs") set("spo2", "spo2");
    if (r.id === "whole") set("temp", "temp_c");
    if (r.id === "blood") {
      const g = vitals.get("glucose");
      if (g) {
        r.indicators.push({
          id: "glucose-live",
          label: "Glucose in flight (team assumption)",
          value: fmt(g),
          unit: g.unit,
          provenance: "simulated",
          measuredHow: g.measuredHow,
        });
      }
      if (card?.baselineMission) {
        const m = card.baselineMission;
        r.missionTag = `${m.kind === "commercial" ? "Commercial mission" : "NASA mission"} · ${m.name} · ${m.start} to ${m.end} · ${m.orbit} · data hosted by ${m.hostedBy}`;
      }
    }

    // The worst active alert for this region sets its level, status and next step.
    const top = mine
      .filter((a) => regionOf(a) === r.id && LEVEL_RANK[a.level] >= LEVEL_RANK[r.level])
      .sort((a, b) => LEVEL_RANK[b.level] - LEVEL_RANK[a.level])[0];
    if (top) {
      r = {
        ...r,
        level: top.level as StatusLevel,
        status: top.explanation ? `${top.title}. ${top.explanation}` : top.title,
        todo: top.recommendation ?? r.todo,
        method: top.evidence ? `${r.method} Rule: ${top.evidence}` : r.method,
        alertId: top.status === "open" ? top.id : undefined,
      };
    }
    return r;
  });
}

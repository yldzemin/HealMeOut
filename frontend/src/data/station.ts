// Simplified layout of the main ISS pressurized modules for the Crew Deck map.
// Source: NASA ISS Reference Guide (NP-2015-05-022-JSC), e.g. "Node 2 … is attached
// to the forward end of the U.S. Lab", "Node 3 is attached to the port side of Node 1".
// Not to scale; other modules (airlock, PMM, Russian research modules) are omitted.

import type { CrewMember } from "@/types";

export const STATION_SOURCE = {
  label: "NASA ISS Reference Guide (NP-2015-05-022-JSC)",
  url: "https://www.nasa.gov/wp-content/uploads/2017/09/np-2015-05-022-jsc-iss-guide-2015-update-111015-508c.pdf",
};

export interface StationModule {
  id: string;
  label: string;
  short: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

/** SVG coordinates in a 900 × 360 viewBox. Forward is to the right, port is up. */
export const MODULES: StationModule[] = [
  { id: "Zvezda", label: "Zvezda", short: "Service Module", x: 30, y: 155, w: 140, h: 50 },
  { id: "Zarya", label: "Zarya", short: "FGB", x: 190, y: 155, w: 120, h: 50 },
  { id: "Unity (Node 1)", label: "Unity", short: "Node 1", x: 330, y: 150, w: 80, h: 60 },
  { id: "Tranquility (Node 3)", label: "Tranquility", short: "Node 3", x: 330, y: 50, w: 80, h: 80 },
  { id: "Destiny", label: "Destiny", short: "U.S. Lab", x: 430, y: 155, w: 150, h: 50 },
  { id: "Harmony (Node 2)", label: "Harmony", short: "Node 2", x: 600, y: 150, w: 80, h: 60 },
  { id: "Kibo", label: "Kibo", short: "Japanese lab", x: 600, y: 40, w: 80, h: 90 },
  { id: "Columbus", label: "Columbus", short: "European lab", x: 600, y: 230, w: 80, h: 90 },
];

const LINKS: [string, string][] = [
  ["Zvezda", "Zarya"],
  ["Zarya", "Unity (Node 1)"],
  ["Unity (Node 1)", "Tranquility (Node 3)"],
  ["Unity (Node 1)", "Destiny"],
  ["Destiny", "Harmony (Node 2)"],
  ["Harmony (Node 2)", "Kibo"],
  ["Harmony (Node 2)", "Columbus"],
];

export const MODULE_BY_ID = Object.fromEntries(MODULES.map((m) => [m.id, m]));

export function links(): [StationModule, StationModule][] {
  return LINKS.map(([a, b]) => [MODULE_BY_ID[a], MODULE_BY_ID[b]]);
}

/** Path of module ids from a to b (breadth-first over the hatches above). */
export function route(a: string, b: string): string[] {
  const adj = new Map<string, string[]>();
  for (const [x, y] of LINKS) {
    adj.set(x, [...(adj.get(x) ?? []), y]);
    adj.set(y, [...(adj.get(y) ?? []), x]);
  }
  const prev = new Map<string, string | null>([[a, null]]);
  const queue = [a];
  while (queue.length) {
    const cur = queue.shift()!;
    if (cur === b) break;
    for (const n of adj.get(cur) ?? []) if (!prev.has(n)) (prev.set(n, cur), queue.push(n));
  }
  if (!prev.has(b)) return [];
  const path: string[] = [];
  for (let c: string | null = b; c; c = prev.get(c) ?? null) path.unshift(c);
  return path;
}

export type RoleId = "lead" | "assist" | "fetch" | "comms";

export const ROLE_LABEL: Record<RoleId, string> = {
  lead: "Leads the response",
  assist: "Assists the lead",
  fetch: "Brings the life-support pack and defibrillator",
  comms: "Ground link and event log",
};

/**
 * Proposed roles. NASA-STD-3001 requires at least two CMOs per vehicle, so the
 * two CMOs lead and assist; the nearest other crew member (fewest hatches) fetches
 * equipment and the next nearest runs comms. The CMO confirms or changes them.
 */
export function proposeRoles(crew: CrewMember[], patientId: string): Record<RoleId, string> {
  const patient = crew.find((c) => c.id === patientId);
  const others = crew.filter((c) => c.id !== patientId);
  const cmos = others.filter((c) => c.role === "CMO1" || c.role === "CMO2").sort((a, b) => a.role.localeCompare(b.role));
  const hops = (c: CrewMember) => (patient ? route(c.module, patient.module).length : 0);
  const rest = others.filter((c) => !cmos.includes(c)).sort((a, b) => hops(a) - hops(b) || a.id.localeCompare(b.id));
  const pool = [...cmos, ...rest];
  const pick = (i: number) => pool[i]?.id ?? "";
  return { lead: pick(0), assist: pick(1), fetch: pick(2), comms: pick(3) };
}

/** Emergency card: only allergies and current medication are shown (privacy rule). Simulated. */
export const EMERGENCY_CARD: Record<string, { allergies: string; medication: string }> = {
  C2: { allergies: "None known", medication: "None in the last 24 h" },
};

export interface ChecklistStep {
  id: string;
  text: string;
  note?: string;
}

/** Microgravity CPR steps. Source: Hinkelbein et al. 2020, doi:10.1186/s13049-020-00793-y. */
export const CPR_STEPS: ChecklistStep[] = [
  { id: "check", text: "Check response and breathing. Crew alerted.", note: "Basic life support as on Earth." },
  { id: "er", text: "Start chest compressions with the Evetts-Russomano method." },
  { id: "rbh", text: "If that is not possible, switch to the Reverse Bear Hug method." },
  { id: "cmrs", text: "Restrain the patient on the Crew Medical Restraint System for advanced life support." },
  { id: "defib", text: "Defibrillate only with the patient restrained on an electrically isolated surface." },
  { id: "als", text: "CMO follows the advanced life support protocol; medication per CMO decision." },
  { id: "ground", text: "Keep the ground informed; continue until the CMO decides otherwise." },
];

export const CPR_SOURCE = {
  label: "Hinkelbein et al. 2020, CPR during spaceflight (guideline)",
  url: "https://doi.org/10.1186/s13049-020-00793-y",
};

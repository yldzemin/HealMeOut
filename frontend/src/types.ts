// Shared names from docs/TECH_OVERVIEW.md §4. Keep in sync with the backend.

export type StatusLevel = "L0" | "L1" | "L2" | "L3" | "L4";

export type CrewRole = "CMO1" | "CMO2" | "CREW";

export type Provenance = "real" | "simulated";

export interface CrewMember {
  id: string; // C1–C7
  callsign: string; // fictional; never a real name
  role: CrewRole;
  module: string;
  level: StatusLevel;
  baselineProvenance: Provenance; // C1–C4: real OSDR baselines; C5–C7: simulated
}

/** Who is looking at the app. Simulated: there is no real login. */
export type ViewerRole =
  | { kind: "astronaut"; crewId: string }
  | { kind: "cmo" }
  | { kind: "ground" };

export type ScreenId = "personal" | "crew" | "environment" | "twin" | "checkin";

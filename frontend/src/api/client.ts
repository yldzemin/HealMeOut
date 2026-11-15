import type { CrewMember, CrewRole, Provenance, StatusLevel, ViewerRole } from "@/types";
import type { MeasuredHow } from "@/data/personal";
import crewMock from "@/mock/crew.json";

/**
 * Client for the Java API (backend/README.md, the agreed contract in #56).
 *
 * - The backend is used automatically when it answers; otherwise the UI falls back to mock JSON
 *   (docs/FRONTEND.md §7). Set VITE_USE_MOCK=true to force mock data.
 * - Base URL: same origin when the API serves the built UI (`run.sh --ui`), otherwise
 *   http://127.0.0.1:8080 (Vite dev server and the Tauri webview). VITE_API_BASE_URL overrides it.
 */
const env = import.meta.env as Record<string, string | undefined>;
export const USE_MOCK = env.VITE_USE_MOCK === "true";
export const API_BASE_URL: string =
  env.VITE_API_BASE_URL ?? (typeof window !== "undefined" && window.location.port === "8080" ? "" : "http://127.0.0.1:8080");

// ---------- Shapes returned by the API (TECH_OVERVIEW §4.2). Absent fields are left out. ----------

export interface ApiMissionTag {
  name: string;
  kind: "commercial" | "nasa";
  start: string;
  end: string;
  orbit: string;
  crew: string;
  hostedBy: string;
  studyId: string;
  link: string;
}

export interface ApiReading {
  crewId: string;
  metric: string;
  value: number;
  unit: string;
  timestamp: string;
  provenance: string;
  measuredHow: MeasuredHow;
  note?: string;
}

export interface ApiCrewCard {
  id: string;
  callsign: string;
  role: CrewRole;
  module: string;
  level: StatusLevel;
  baselineProvenance: Provenance;
  baselineMission?: ApiMissionTag;
  vitals?: ApiReading[];
  medical?: { allergies: string[]; currentMedication: string[]; shareWithCrew: boolean; provenance: string };
  checkIns?: { date: string; mood: number; fatigue: number; stress: number; conflict: number; sleepHours: number }[];
}

/** SUPPORT: crisis wording in the support companion (#51); only the astronaut and the CMO get it. */
export type AlertCategory = "VITALS" | "MEDICATION" | "PSYCH" | "ENVIRONMENT" | "EMERGENCY" | "SUPPORT";

export interface ApiAlert {
  id: string;
  crewId: string;
  level: StatusLevel;
  title: string;
  explanation?: string;
  recommendation?: string;
  evidence?: string;
  source?: string;
  status: "open" | "approved" | "rejected";
  category: AlertCategory;
  active: boolean;
  createdAt: string;
}

export interface ApiEnvironment {
  co2_mmHg?: number;
  temp_C?: number;
  rh_pct?: number;
  doseRate_uGy_h?: number;
  doseEqRate_uSv_h?: number;
  instrument?: string;
  timestamp: string;
  provenance: string;
}

export interface ApiScenarioState {
  id: string;
  title: string;
  step: number;
  total: number;
  at: string;
  stepTitle: string;
  detail: string;
  roles: Record<string, string>;
  checklist: string[];
  banner?: string;
  evidence?: string;
  log: string[];
  finished: boolean;
}

export interface StationView {
  viewer: string;
  viewerCrewId?: string;
  stationLevel: StatusLevel;
  crew: ApiCrewCard[];
  alerts: ApiAlert[];
  cabin?: ApiEnvironment;
  dose?: ApiEnvironment;
  scenario?: ApiScenarioState;
  disclaimer: string;
}

/** Partner-agency data hosted by a NASA archive, with why we use it (team decision #13). */
export interface ApiPartnerTag {
  instrument: string;
  agency: string;
  location: string;
  hostedBy: string;
  link: string;
  why: string;
}

export interface ApiPartnerDose {
  tag: ApiPartnerTag;
  source: string;
  items: ApiEnvironment[];
  /** The NASA TEPC series for the same days, whatever the station's scenario time. */
  reference: ApiEnvironment[];
}

export interface ApiBaselineStats {
  crewId: string;
  analyte: string;
  unit: string;
  mean: number;
  sd?: number;
  n: number;
  refLow?: number;
  refHigh?: number;
  source: string;
  mission?: ApiMissionTag;
}

export interface ApiForecast {
  crewId: string;
  horizonHours: number;
  metric: string;
  unit: string;
  points: { at: string; value: number }[];
  label: string;
  evidence: string;
  source: string;
}

export interface ApiSupportMessage {
  crewId: string;
  author: "crew" | "companion";
  text: string;
  timestamp: string;
  source: string;
  flags: string[];
}

export interface ApiSupportStatus {
  primary: string;
  primaryAvailable: boolean;
  fallback: string;
}

export interface ApiStreamStatus {
  running: boolean;
  finished: boolean;
  source?: string;
  rows: number;
  intervalMs: number;
  startedAt?: string;
  logFile?: string;
  subscribers: number;
}

export interface ApiCondition {
  condition: string;
  probability: number;
}

export interface TelemetryRow {
  timestamp: string;
  crewId: string;
  metric: string;
  value: number;
  unit: string;
}

// ---------- Viewer mapping (BACKEND §6) ----------

/** Who asks. The Crew Deck screen always asks as CREW_DECK, whatever role is selected. */
export type ViewKind = ViewerRole | { kind: "crewdeck" };

export function viewerQuery(view: ViewKind): string {
  switch (view.kind) {
    case "astronaut":
      return `viewer=SELF&crewId=${encodeURIComponent(view.crewId)}`;
    case "cmo":
      return "viewer=CMO";
    case "ground":
      return "viewer=GROUND";
    case "crewdeck":
      return "viewer=CREW_DECK";
  }
}

async function call<T>(method: "GET" | "POST", path: string, view?: ViewKind, body?: unknown): Promise<T> {
  const query = view ? (path.includes("?") ? "&" : "?") + viewerQuery(view) : "";
  const res = await fetch(`${API_BASE_URL}/api/${path}${query}`, {
    method,
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const data = (await res.json().catch(() => ({}))) as T & { error?: string };
  if (!res.ok) throw new Error(data.error ?? `${method} /api/${path} failed: ${res.status}`);
  return data;
}

// ---------- Mock fallback ----------

export async function getMockCrew(): Promise<CrewMember[]> {
  return crewMock as CrewMember[];
}

// ---------- Backend calls ----------

/** True if the Java API answers within 1.5 s. */
export async function probeBackend(): Promise<boolean> {
  if (USE_MOCK) return false;
  try {
    const res = await fetch(`${API_BASE_URL}/api/health`, { signal: AbortSignal.timeout(1500) });
    return res.ok;
  } catch {
    return false;
  }
}

export const getStation = (view: ViewKind) => call<StationView>("GET", "station", view);
export const startScenario = (id: string) => call<ApiScenarioState>("POST", `scenarios/${encodeURIComponent(id)}/start`);
export const stepScenario = () => call<ApiScenarioState>("POST", "scenario/step");
export const stopScenario = () => call<{ stopped: boolean }>("POST", "scenario/stop");
export const decideAlert = (id: string, decision: "approve" | "reject", view: ViewKind) =>
  call<ApiAlert>("POST", `alerts/${encodeURIComponent(id)}/${decision}`, view);
export const getBaselines = (crewId: string, view: ViewKind) =>
  call<ApiBaselineStats[]>("GET", `crew/${encodeURIComponent(crewId)}/baselines`, view);
export const getForecasts = (crewId: string, view: ViewKind) =>
  call<ApiForecast[]>("GET", `crew/${encodeURIComponent(crewId)}/forecasts`, view);
export const getDoseSeries = () => call<ApiEnvironment[]>("GET", "environment/dose");
export const getPartnerDose = () => call<ApiPartnerDose>("GET", "environment/partner-dose");
export const getImm = () => call<ApiCondition[]>("GET", "imm");
export const postCheckIn = (
  view: ViewKind,
  c: { mood: number; fatigue: number; stress: number; conflict: number; sleepHours: number },
) => call<unknown[]>("POST", "checkins", view, c);
export const getStreamStatus = () => call<ApiStreamStatus>("GET", "stream/status");
export const startStream = (source: "sim" | "replay") =>
  call<ApiStreamStatus>(
    "POST",
    source === "sim" ? "stream/start?source=sim&intervalMs=1000" : "stream/start?source=replay&file=sample_telemetry.csv&intervalMs=1000",
  );
export const stopStream = () => call<ApiStreamStatus>("POST", "stream/stop");
export const getSupportStatus = () => call<ApiSupportStatus>("GET", "support/status");
export const getConversation = (view: ViewKind) => call<ApiSupportMessage[]>("GET", "support/conversation", view);
export const sendSupportMessage = (view: ViewKind, text: string) =>
  call<{ reply: ApiSupportMessage; conversation: ApiSupportMessage[] }>("POST", "support/message", view, { text });
export const clearConversation = (view: ViewKind) => call<{ cleared: boolean }>("POST", "support/clear", view);

/**
 * Follows the live CSV telemetry stream (GET /api/stream/telemetry.csv) and calls onRow for every
 * line as it arrives. Ends when the stream stops or the signal aborts.
 */
export async function followTelemetry(view: ViewKind, onRow: (row: TelemetryRow) => void, signal: AbortSignal): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/stream/telemetry.csv?${viewerQuery(view)}`, { signal });
  if (!res.ok || !res.body) {
    const data = (await res.json().catch(() => ({}))) as { error?: string };
    throw new Error(data.error ?? `telemetry stream failed: ${res.status}`);
  }
  const reader = res.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  for (;;) {
    const { value, done } = await reader.read();
    if (done) return;
    buffer += decoder.decode(value, { stream: true });
    let nl: number;
    while ((nl = buffer.indexOf("\n")) >= 0) {
      const line = buffer.slice(0, nl).trim();
      buffer = buffer.slice(nl + 1);
      if (!line || line.startsWith("timestamp,")) continue;
      const [timestamp, crewId, metric, v, unit] = line.split(",");
      onRow({ timestamp, crewId, metric, value: Number(v), unit });
    }
  }
}

/** Backend crew card → the frontend's CrewMember (src/types.ts). */
export function toCrewMember(c: ApiCrewCard): CrewMember {
  return { id: c.id, callsign: c.callsign, role: c.role, module: c.module, level: c.level, baselineProvenance: c.baselineProvenance };
}

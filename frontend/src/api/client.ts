import type { CrewMember } from "@/types";
import crewMock from "@/mock/crew.json";

/**
 * One switch between mock JSON and the Java API (docs/FRONTEND.md §7).
 * Set to false once the backend serves the endpoints.
 */
export const USE_MOCK = true;

/** Java API on localhost (docs/BACKEND.md). Port to be agreed with the backend owner. */
export const API_BASE_URL = "http://localhost:8080";

export async function getCrew(): Promise<CrewMember[]> {
  if (USE_MOCK) return crewMock as CrewMember[];
  const res = await fetch(`${API_BASE_URL}/api/crew`);
  if (!res.ok) throw new Error(`GET /api/crew failed: ${res.status}`);
  return (await res.json()) as CrewMember[];
}

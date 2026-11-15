import { useCallback, useEffect, useState } from "react";
import { getStation, probeBackend, USE_MOCK, viewerQuery, type StationView, type ViewKind } from "@/api/client";

export type DataMode = "connecting" | "live" | "mock";

/**
 * Polls GET /api/station for one viewer (every 2 s). If the backend does not answer, the mode is
 * "mock" and screens use their mock data; it keeps checking, so starting the backend later works.
 */
export function useStation(view: ViewKind, intervalMs = 2000) {
  const [mode, setMode] = useState<DataMode>(USE_MOCK ? "mock" : "connecting");
  const [station, setStation] = useState<StationView | null>(null);
  const key = viewerQuery(view);

  const refresh = useCallback(async () => {
    if (USE_MOCK) return;
    try {
      setStation(await getStation(view));
      setMode("live");
    } catch {
      setMode((await probeBackend()) ? "live" : "mock");
    }
    // `key` identifies the viewer; `view` is a new object on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  useEffect(() => {
    if (USE_MOCK) return;
    void refresh();
    const id = setInterval(() => void refresh(), intervalMs);
    return () => clearInterval(id);
  }, [refresh, intervalMs]);

  return { mode, station: mode === "live" ? station : null, refresh };
}

import { useEffect, useRef, useState } from "react";
import { Play, RotateCcw, Square } from "lucide-react";
import {
  followTelemetry,
  getStreamStatus,
  startStream,
  stopStream,
  type ApiStreamStatus,
  type TelemetryRow,
  type ViewKind,
} from "@/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const KEEP = 12;

/**
 * Live wearable telemetry read line by line from the backend's CSV stream
 * (GET /api/stream/telemetry.csv). Simulated values; the vitals privacy policy applies per row.
 */
export function TelemetryPanel({ view }: { view: ViewKind }) {
  const [status, setStatus] = useState<ApiStreamStatus | null>(null);
  const [rows, setRows] = useState<TelemetryRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const abort = useRef<AbortController | null>(null);
  const viewKey = JSON.stringify(view);

  useEffect(() => {
    let alive = true;
    const poll = () => getStreamStatus().then((s) => alive && setStatus(s)).catch(() => alive && setStatus(null));
    poll();
    const id = setInterval(poll, 3000);
    return () => {
      alive = false;
      clearInterval(id);
    };
  }, []);

  // Follow the stream while it runs; restart when the viewer changes.
  useEffect(() => {
    if (!status?.running) return;
    const ctrl = new AbortController();
    abort.current = ctrl;
    setError(null);
    followTelemetry(view, (r) => setRows((prev) => [...prev.slice(-(KEEP - 1)), r]), ctrl.signal).catch((e: unknown) => {
      if (!ctrl.signal.aborted) setError(e instanceof Error ? e.message : String(e));
    });
    return () => ctrl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status?.running, status?.startedAt, viewKey]);

  const act = async (fn: () => Promise<ApiStreamStatus>) => {
    try {
      setRows([]);
      setStatus(await fn());
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  return (
    <Card>
      <CardContent className="flex flex-col gap-3 p-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <p className="font-heading text-lg font-semibold">Live telemetry</p>
            <p className="text-xs text-muted-foreground">CSV stream from the station · wearable values are simulated</p>
          </div>
          <div className="flex gap-2">
            <Button size="sm" variant="outline" className="gap-1.5" onClick={() => void act(() => startStream("sim"))}>
              <Play className="size-3.5" aria-hidden /> Simulated
            </Button>
            <Button size="sm" variant="outline" className="gap-1.5" onClick={() => void act(() => startStream("replay"))}>
              <RotateCcw className="size-3.5" aria-hidden /> Replay sample CSV
            </Button>
            <Button size="sm" variant="outline" className="gap-1.5" onClick={() => void act(stopStream)}>
              <Square className="size-3.5" aria-hidden /> Stop
            </Button>
          </div>
        </div>
        <p className="text-xs text-muted-foreground">
          {status?.running
            ? `Streaming · ${status.source} · ${status.rows} rows · every ${status.intervalMs} ms`
            : status?.source
              ? `Stopped${status.finished ? " (replay finished)" : ""} · ${status.source} · ${status.rows} rows`
              : "Stream stopped."}
        </p>
        {error && <p className="text-xs text-status-emergency">{error}</p>}
        {rows.length > 0 && (
          <table className="w-full text-xs tabular-nums">
            <thead className="text-muted-foreground">
              <tr className="text-left">
                <th className="py-1 font-normal">GMT</th>
                <th className="py-1 font-normal">Crew</th>
                <th className="py-1 font-normal">Metric</th>
                <th className="py-1 text-right font-normal">Value</th>
              </tr>
            </thead>
            <tbody>
              {rows
                .slice()
                .reverse()
                .map((r, i) => (
                  <tr key={`${r.timestamp}-${r.crewId}-${r.metric}-${i}`} className="border-t border-border/50">
                    <td className="py-1">{r.timestamp.slice(11, 19)}</td>
                    <td className="py-1">{r.crewId}</td>
                    <td className="py-1">{r.metric}</td>
                    <td className="py-1 text-right">
                      {r.value} {r.unit}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        )}
        <Badge variant="outline" className="w-fit text-[11px]">
          Simulated
        </Badge>
      </CardContent>
    </Card>
  );
}

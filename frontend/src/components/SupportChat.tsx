import { useEffect, useRef, useState } from "react";
import { Lock, Send, Trash2 } from "lucide-react";
import {
  clearConversation,
  getConversation,
  getSupportStatus,
  sendSupportMessage,
  type ApiSupportMessage,
  type ApiSupportStatus,
  type ViewKind,
} from "@/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

/**
 * Private support companion (backend §10): local model through Ollama, with a fixed safety reply for
 * crisis wording and medication questions. Only the astronaut (SELF) can read or write it.
 */
export function SupportChat({ view }: { view: ViewKind }) {
  const [status, setStatus] = useState<ApiSupportStatus | null>(null);
  const [messages, setMessages] = useState<ApiSupportMessage[]>([]);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const end = useRef<HTMLDivElement>(null);
  const viewKey = JSON.stringify(view);

  useEffect(() => {
    getSupportStatus().then(setStatus).catch(() => setStatus(null));
    getConversation(view).then(setMessages).catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewKey]);

  // Block body: newer browsers return a Promise from scrollIntoView, and an effect must not return one.
  useEffect(() => {
    void end.current?.scrollIntoView({ block: "nearest" });
  }, [messages]);

  const send = async () => {
    const t = text.trim();
    if (!t || busy) return;
    setBusy(true);
    setError(null);
    try {
      const r = await sendSupportMessage(view, t);
      setMessages(r.conversation);
      setText("");
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
    setBusy(false);
  };

  return (
    <Card>
      <CardContent className="flex flex-col gap-4 p-6">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="flex items-center gap-2 font-heading text-xl font-semibold">
              <Lock className="size-4 text-muted-foreground" aria-hidden /> Talk it through
            </p>
            <p className="text-sm text-muted-foreground">
              A private companion for the hard days. It runs on the station, never on Earth servers, and nobody else can read it.
            </p>
          </div>
          {status && (
            <Badge variant="outline" className="shrink-0 text-[11px]">
              {status.primaryAvailable ? `Local model · ${status.primary.replace("local-llm:", "")}` : "Guided replies"}
            </Badge>
          )}
        </div>

        <div className="flex max-h-[420px] min-h-[160px] flex-col gap-3 overflow-auto rounded-lg border border-border p-3" aria-live="polite">
          {messages.length === 0 && <p className="text-sm text-muted-foreground">How are you feeling today?</p>}
          {messages.map((m, i) => (
            <div key={`${m.timestamp}-${i}`} className={m.author === "crew" ? "self-end text-right" : "self-start"}>
              <p
                className={`inline-block max-w-prose rounded-lg px-3 py-2 text-sm ${
                  m.author === "crew" ? "bg-primary/15" : m.source === "safety" ? "border border-status-emergency/60" : "bg-muted"
                }`}
              >
                {m.text}
              </p>
              {m.author === "companion" && m.flags.includes("crisis") && (
                <p className="mt-1 text-[11px] text-status-emergency">Safety reply · contact your flight surgeon now</p>
              )}
            </div>
          ))}
          <div ref={end} />
        </div>

        <form
          className="flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            void send();
          }}
        >
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            maxLength={1000}
            placeholder="Write how you feel…"
            aria-label="Message to the support companion"
            className="min-w-0 flex-1 rounded-lg border border-border bg-background px-3 py-2 text-sm focus-visible:outline-2 focus-visible:outline-ring"
          />
          <Button type="submit" disabled={busy || !text.trim()} className="gap-1.5">
            <Send className="size-4" aria-hidden /> {busy ? "Thinking…" : "Send"}
          </Button>
          <Button
            type="button"
            variant="outline"
            aria-label="Clear conversation"
            onClick={() => void clearConversation(view).then(() => setMessages([]))}
          >
            <Trash2 className="size-4" aria-hidden />
          </Button>
        </form>
        {error && <p className="text-xs text-status-emergency">{error}</p>}
        <p className="text-[11px] text-muted-foreground">
          Not a therapist and not medical advice. In a crisis, contact your flight surgeon or behavioral health provider now.
        </p>
      </CardContent>
    </Card>
  );
}

import type { StatusLevel } from "@/types";

/** CSS colour per status level (theme tokens in index.css). */
export const LEVEL_COLOR: Record<StatusLevel, string> = {
  L0: "var(--primary)",
  L1: "var(--status-self)",
  L2: "var(--status-cmo)",
  L3: "var(--status-ground)",
  L4: "var(--status-emergency)",
};

/** Same colours as hex, for WebGL materials. Keep in sync with index.css. */
export const LEVEL_HEX: Record<StatusLevel, string> = {
  L0: "#00d2ff",
  L1: "#ffd400",
  L2: "#ff8a00",
  L3: "#ff0055",
  L4: "#ff0055",
};

export const LEVEL_WORD: Record<StatusLevel, string> = {
  L0: "normal",
  L1: "self-care advised",
  L2: "needs CMO review",
  L3: "ground informed",
  L4: "emergency",
};

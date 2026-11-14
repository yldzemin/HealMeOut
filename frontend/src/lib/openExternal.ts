import { openUrl } from "@tauri-apps/plugin-opener";

/** Open a source link in the system browser (Tauri); a new tab in plan B (plain browser). */
export function openExternal(url: string) {
  openUrl(url).catch(() => window.open(url, "_blank", "noopener"));
}

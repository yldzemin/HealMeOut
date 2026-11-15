/**
 * Round axis ticks (#82): the smallest step of 1, 2 or 5 × 10ⁿ that covers [lo, hi] with at most
 * `maxTicks` ticks, so an axis reads "17 · 18 · 19 · 20 · 21" instead of "17.0 · 19.0 · 20.7".
 * `decimals` is what the step needs.
 */
export function niceTicks(lo: number, hi: number, maxTicks = 6): { ticks: number[]; decimals: number } {
  if (!Number.isFinite(lo) || !Number.isFinite(hi)) return { ticks: [0, 1], decimals: 0 };
  if (hi <= lo) {
    const pad = Math.abs(lo) * 0.05 || 1;
    lo -= pad;
    hi += pad;
  }
  const base = 10 ** Math.floor(Math.log10((hi - lo) / maxTicks));
  for (const m of [1, 2, 5, 10, 20, 50, 100]) {
    const step = Number((m * base).toPrecision(12));
    const first = Math.floor(lo / step + 1e-9);
    const last = Math.ceil(hi / step - 1e-9);
    if (last - first + 1 <= maxTicks) {
      const decimals = (String(step).split(".")[1] ?? "").length;
      const ticks: number[] = [];
      for (let i = first; i <= last; i++) ticks.push(Number((i * step).toFixed(decimals)));
      return { ticks, decimals };
    }
  }
  return { ticks: [lo, hi], decimals: 2 };
}

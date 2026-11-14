import type { Region, RegionId } from "@/data/personal";
import { LEVEL_COLOR, LEVEL_WORD } from "@/components/regionStyle";
import { cn } from "@/lib/utils";

/** Region chips under the figure: the same selection, as a keyboard-friendly list. */
export function RegionChips({
  regions,
  selected,
  onSelect,
}: {
  regions: Region[];
  selected: RegionId | null;
  onSelect: (id: RegionId) => void;
}) {
  return (
    <div className="flex flex-wrap justify-center gap-2" role="group" aria-label="Body regions">
      {regions.map((r) => (
        <button
          key={r.id}
          type="button"
          onClick={() => onSelect(r.id)}
          aria-pressed={selected === r.id}
          className={cn(
            "flex items-center gap-2 rounded-full border px-3 py-1 text-xs transition-colors",
            "focus-visible:outline-2 focus-visible:outline-ring",
            selected === r.id ? "border-primary bg-accent text-foreground" : "border-border text-muted-foreground hover:bg-accent/50",
          )}
        >
          <span className="size-2 rounded-full" style={{ background: LEVEL_COLOR[r.level] }} aria-hidden />
          {r.label}
          {r.level !== "L0" && <span className="text-[11px]">· {LEVEL_WORD[r.level]}</span>}
        </button>
      ))}
    </div>
  );
}

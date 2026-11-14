import { Activity, ClipboardCheck, Gauge, User, Users } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import type { ScreenId } from "@/types";
import { cn } from "@/lib/utils";

const ITEMS: { id: ScreenId; label: string; icon: LucideIcon }[] = [
  { id: "personal", label: "Personal", icon: User },
  { id: "crew", label: "Crew Deck", icon: Users },
  { id: "environment", label: "Environment", icon: Gauge },
  { id: "twin", label: "Twin", icon: Activity },
  { id: "checkin", label: "Check-in", icon: ClipboardCheck },
];

export function LeftRail({ current, onSelect }: { current: ScreenId; onSelect: (id: ScreenId) => void }) {
  return (
    <nav aria-label="Main" className="flex w-52 flex-col gap-1 border-r border-border bg-sidebar p-3">
      {ITEMS.map(({ id, label, icon: Icon }) => {
        const active = id === current;
        return (
          <button
            key={id}
            type="button"
            onClick={() => onSelect(id)}
            aria-current={active ? "page" : undefined}
            className={cn(
              "flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm transition-colors",
              "focus-visible:outline-2 focus-visible:outline-ring",
              active
                ? "bg-sidebar-accent text-sidebar-accent-foreground"
                : "text-sidebar-foreground hover:bg-sidebar-accent/60",
            )}
          >
            <Icon className={cn("size-4", active && "text-primary")} aria-hidden />
            {label}
          </button>
        );
      })}
    </nav>
  );
}

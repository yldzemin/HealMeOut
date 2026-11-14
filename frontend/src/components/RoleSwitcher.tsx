import type { CrewMember, ViewerRole } from "@/types";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectSeparator,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

function encode(role: ViewerRole): string {
  return role.kind === "astronaut" ? `astronaut:${role.crewId}` : role.kind;
}

function decode(value: string): ViewerRole {
  if (value.startsWith("astronaut:")) return { kind: "astronaut", crewId: value.slice(10) };
  return value === "cmo" ? { kind: "cmo" } : { kind: "ground" };
}

/**
 * Simulated role switcher (docs/FRONTEND.md §1.2). There is no real login:
 * it exists to show the privacy tiers live.
 */
export function RoleSwitcher({
  crew,
  role,
  onChange,
}: {
  crew: CrewMember[];
  role: ViewerRole;
  onChange: (role: ViewerRole) => void;
}) {
  return (
    <div className="flex items-center gap-2">
      <span className="whitespace-nowrap text-xs text-muted-foreground">Viewing as (simulated)</span>
      <Select value={encode(role)} onValueChange={(v) => onChange(decode(v))}>
        <SelectTrigger className="w-56" aria-label="Viewing role (simulated)">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            <SelectLabel>Astronaut</SelectLabel>
            {crew.map((c) => (
              <SelectItem key={c.id} value={`astronaut:${c.id}`}>
                {c.id} · {c.callsign}
              </SelectItem>
            ))}
          </SelectGroup>
          <SelectSeparator />
          <SelectItem value="cmo">Crew Medical Officer</SelectItem>
          <SelectItem value="ground">Ground (flight surgeon)</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}

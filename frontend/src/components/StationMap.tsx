import { motion } from "motion/react";
import type { CrewMember } from "@/types";
import { links, MODULE_BY_ID, MODULES, route } from "@/data/station";
import { LEVEL_COLOR } from "@/components/regionStyle";

/** Where a crew pin sits inside its module (spread when several share one). */
function pinPositions(crew: CrewMember[]) {
  const byModule = new Map<string, CrewMember[]>();
  for (const c of crew) byModule.set(c.module, [...(byModule.get(c.module) ?? []), c]);
  const out = new Map<string, { x: number; y: number }>();
  for (const [mod, list] of byModule) {
    const m = MODULE_BY_ID[mod];
    if (!m) continue;
    list.forEach((c, i) => {
      const step = m.w / (list.length + 1);
      out.set(c.id, { x: m.x + step * (i + 1), y: m.y + m.h / 2 + 2 });
    });
  }
  return out;
}

function center(id: string) {
  const m = MODULE_BY_ID[id];
  return { x: m.x + m.w / 2, y: m.y + m.h / 2 };
}

/**
 * 2D schematic of the pressurized modules with crew pins. In an emergency the
 * patient's module turns red and responders' routes are drawn.
 */
export function StationMap({
  crew,
  patientId,
  responders = [],
  focus = [],
}: {
  crew: CrewMember[];
  patientId?: string;
  responders?: string[];
  /** Crew ids to zoom to (with the patient); empty shows the whole station. */
  focus?: string[];
}) {
  const pins = pinPositions(crew);
  const patient = crew.find((c) => c.id === patientId);

  // Zoom to the modules of the patient and the focused crew.
  let viewBox = "0 0 900 360";
  const ids = [patientId, ...focus].filter(Boolean) as string[];
  const mods = ids.map((id) => MODULE_BY_ID[crew.find((c) => c.id === id)?.module ?? ""]).filter(Boolean);
  if (mods.length > 0) {
    const pad = 40;
    const x0 = Math.min(...mods.map((m) => m.x)) - pad;
    const y0 = Math.min(...mods.map((m) => m.y)) - pad;
    const x1 = Math.max(...mods.map((m) => m.x + m.w)) + pad;
    const y1 = Math.max(...mods.map((m) => m.y + m.h)) + pad;
    viewBox = `${x0} ${y0} ${x1 - x0} ${y1 - y0}`;
  }

  return (
    <svg viewBox={viewBox} className="h-full w-full" role="img" aria-label="ISS module map with crew locations">
      {/* Hatches between modules. */}
      {links().map(([a, b]) => {
        const p = center(a.id);
        const q = center(b.id);
        return <line key={`${a.id}-${b.id}`} x1={p.x} y1={p.y} x2={q.x} y2={q.y} stroke="var(--border)" strokeWidth={10} />;
      })}

      {/* Modules. */}
      {MODULES.map((m) => {
        const isPatient = patient?.module === m.id;
        return (
          <g key={m.id}>
            <motion.rect
              x={m.x}
              y={m.y}
              width={m.w}
              height={m.h}
              rx={12}
              fill="var(--card)"
              stroke={isPatient ? "var(--status-emergency)" : "var(--primary)"}
              strokeOpacity={isPatient ? 1 : 0.45}
              initial={false}
              animate={{ strokeWidth: isPatient ? [2, 5, 2] : 1.5 }}
              transition={isPatient ? { duration: 1.6, repeat: Infinity } : { duration: 0.2 }}
            />
            <text x={m.x + 10} y={m.y + 17} fontSize={13} fill="var(--foreground)" fontWeight={600}>
              {m.label}
              <tspan fontSize={10} fontWeight={400} fill="var(--muted-foreground)">
                {"  "}
                {m.short}
              </tspan>
            </text>
          </g>
        );
      })}

      {/* Responder routes. */}
      {patient &&
        responders.map((id) => {
          const c = crew.find((x) => x.id === id);
          if (!c) return null;
          const path = route(c.module, patient.module);
          if (path.length < 2) return null;
          const pts = path.map(center);
          const start = pins.get(c.id) ?? pts[0];
          const end = pins.get(patient.id) ?? pts[pts.length - 1];
          const d = [start, ...pts.slice(1, -1), end].map((p, i) => `${i ? "L" : "M"}${p.x} ${p.y}`).join(" ");
          return (
            <motion.path
              key={id}
              d={d}
              fill="none"
              stroke="var(--status-cmo)"
              strokeWidth={3}
              strokeDasharray="8 6"
              initial={{ pathLength: 0 }}
              animate={{ pathLength: 1 }}
              transition={{ duration: 1.2 }}
            />
          );
        })}

      {/* Crew pins. */}
      {crew.map((c) => {
        const p = pins.get(c.id);
        if (!p) return null;
        const isPatient = c.id === patientId;
        return (
          <g key={c.id}>
            <circle cx={p.x} cy={p.y} r={isPatient ? 11 : 8} fill={isPatient ? "var(--status-emergency)" : LEVEL_COLOR[c.level]} />
            <text x={p.x} y={p.y + 20} textAnchor="middle" fontSize={10} fill="var(--foreground)">
              {c.id} {c.callsign}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

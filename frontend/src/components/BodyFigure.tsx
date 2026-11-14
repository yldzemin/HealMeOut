import type { KeyboardEvent, MouseEvent, ReactNode } from "react";
import { motion, useMotionValue, useSpring, useTransform } from "motion/react";
import type { Region, RegionId } from "@/data/personal";
import type { StatusLevel } from "@/types";
import { cn } from "@/lib/utils";

/** Stroke colour per status level (theme tokens in index.css). */
const LEVEL_COLOR: Record<StatusLevel, string> = {
  L0: "var(--primary)",
  L1: "var(--status-self)",
  L2: "var(--status-cmo)",
  L3: "var(--status-ground)",
  L4: "var(--status-emergency)",
};

const LEVEL_WORD: Record<StatusLevel, string> = {
  L0: "normal",
  L1: "self-care advised",
  L2: "needs CMO review",
  L3: "ground informed",
  L4: "emergency",
};

function RegionShape({
  region,
  selected,
  onSelect,
  hit,
  children,
}: {
  region: Region;
  selected: boolean;
  onSelect: (id: RegionId) => void;
  hit: ReactNode;
  children: ReactNode;
}) {
  const color = LEVEL_COLOR[region.level];
  const alerting = region.level !== "L0";
  const onKey = (e: KeyboardEvent) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      onSelect(region.id);
    }
  };
  return (
    <g
      role="button"
      tabIndex={0}
      aria-pressed={selected}
      aria-label={`${region.label}: ${LEVEL_WORD[region.level]}`}
      onClick={() => onSelect(region.id)}
      onKeyDown={onKey}
      className="cursor-pointer outline-none [&:focus-visible_.focus-ring]:opacity-100"
      style={{ color }}
    >
      <title>{`${region.label}: ${LEVEL_WORD[region.level]}`}</title>
      <g fill="transparent" stroke="none">{hit}</g>
      <g className="focus-ring opacity-0 transition-opacity" fill="none" stroke="currentColor" strokeWidth={1} strokeDasharray="3 3">
        {hit}
      </g>
      <motion.g
        fill="none"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
        filter={selected || alerting ? "url(#glow)" : undefined}
        initial={false}
        animate={{
          // The suit layer stays faint unless selected or alerting.
          opacity: alerting ? [0.55, 1, 0.55] : selected ? 1 : region.id === "whole" ? 0.3 : 0.6,
          strokeWidth: selected ? 2.4 : 1.6,
        }}
        transition={alerting ? { duration: 2.4, repeat: Infinity, ease: "easeInOut" } : { duration: 0.25 }}
      >
        {children}
      </motion.g>
    </g>
  );
}

/**
 * 2.5D body figure: an organ-drawn body inside a faint suit layer that
 * shifts with the pointer (parallax). Our own SVG; no third-party artwork.
 */
export function BodyFigure({
  regions,
  selected,
  onSelect,
}: {
  regions: Region[];
  selected: RegionId | null;
  onSelect: (id: RegionId) => void;
}) {
  const px = useMotionValue(0);
  const py = useMotionValue(0);
  const sx = useSpring(px, { stiffness: 120, damping: 20 });
  const sy = useSpring(py, { stiffness: 120, damping: 20 });
  const suitX = useTransform(sx, (v) => v * 7);
  const suitY = useTransform(sy, (v) => v * 5);
  const bodyX = useTransform(sx, (v) => v * 2);
  const bodyY = useTransform(sy, (v) => v * 1.5);

  const onMove = (e: MouseEvent<HTMLDivElement>) => {
    const r = e.currentTarget.getBoundingClientRect();
    px.set(((e.clientX - r.left) / r.width - 0.5) * 2);
    py.set(((e.clientY - r.top) / r.height - 0.5) * 2);
  };
  const onLeave = () => {
    px.set(0);
    py.set(0);
  };

  const byId = Object.fromEntries(regions.map((r) => [r.id, r])) as Record<RegionId, Region>;
  const shape = (id: RegionId, hit: ReactNode, children: ReactNode) =>
    byId[id] ? (
      <RegionShape region={byId[id]} selected={selected === id} onSelect={onSelect} hit={hit}>
        {children}
      </RegionShape>
    ) : null;

  return (
    <div className="relative flex h-full items-center justify-center" onMouseMove={onMove} onMouseLeave={onLeave}>
      <svg viewBox="0 0 300 620" className="h-full max-h-[640px] w-auto" aria-label="Body figure. Select a region to see its status.">
        <defs>
          <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        {/* Suit layer (whole-body region): faint outer outline, moves most. */}
        <motion.g style={{ x: suitX, y: suitY }}>
          {shape(
            "whole",
            <path d="M150 6 a66 66 0 1 1 -0.1 0 Z M80 132 h140 l22 175 l-14 6 l-8 -40 l-6 290 h-58 l-6 -230 l-6 230 h-58 l-6 -290 l-8 40 l-14 -6 Z" />,
            <>
              <circle cx="150" cy="72" r="64" strokeDasharray="2 5" />
              <path d="M84 134 C 96 124, 204 124, 216 134 L 238 300 L 226 306 L 214 262 L 208 572 L 160 572 L 152 340 L 148 340 L 140 572 L 92 572 L 86 262 L 74 306 L 62 300 Z" strokeDasharray="2 5" />
            </>,
          )}
        </motion.g>

        {/* Body layer: organs and skeleton, moves slightly. */}
        <motion.g style={{ x: bodyX, y: bodyY }}>
          {/* Body outline (not a region). */}
          <g fill="none" stroke="var(--primary)" strokeOpacity={0.28} strokeWidth={1.2}>
            <circle cx="150" cy="72" r="40" />
            <path d="M138 112 v14 M162 112 v14" />
            <path d="M100 138 C 112 128, 188 128, 200 138 L 214 292 L 204 296 L 194 250 L 190 316 L 186 560 L 158 560 L 152 336 L 148 336 L 142 560 L 114 560 L 110 316 L 106 250 L 96 296 L 86 292 Z" />
          </g>

          {shape(
            "head",
            <circle cx="150" cy="62" r="30" />,
            <>
              <ellipse cx="150" cy="60" rx="25" ry="18" />
              <path d="M150 43 v34 M136 52 q7 6 0 14 M164 52 q-7 6 0 14" />
            </>,
          )}

          {shape(
            "eyes",
            <rect x="128" y="76" width="44" height="14" rx="6" />,
            <>
              <ellipse cx="139" cy="83" rx="6" ry="4" />
              <ellipse cx="161" cy="83" rx="6" ry="4" />
            </>,
          )}

          {shape(
            "lungs",
            <path d="M108 150 h84 v76 h-84 Z" />,
            <>
              <path d="M144 154 C 118 150, 110 190, 114 222 C 126 228, 140 222, 144 210 Z" />
              <path d="M156 154 C 182 150, 190 190, 186 222 C 174 228, 160 222, 156 210 Z" />
              <path d="M150 138 v18 M150 156 l-8 8 M150 156 l8 8" />
            </>,
          )}

          {shape(
            "heart",
            <circle cx="160" cy="194" r="15" />,
            <path d="M160 206 C 146 196, 148 182, 156 182 C 159 182, 160 185, 160 187 C 160 185, 161 182, 164 182 C 172 182, 174 196, 160 206 Z" />,
          )}

          {shape(
            "blood",
            <circle cx="150" cy="262" r="22" />,
            <>
              <path d="M150 246 C 142 258, 140 264, 140 268 a10 10 0 0 0 20 0 C 160 264, 158 258, 150 246 Z" />
              <path d="M128 262 h-14 M172 262 h14" strokeDasharray="2 3" />
            </>,
          )}

          {shape(
            "musculo",
            <path d="M118 320 h64 v236 h-64 Z" />,
            <>
              <path d="M150 232 v-2 M150 290 v34" strokeDasharray="3 3" />
              <path d="M134 334 L 128 446 M166 334 L 172 446" />
              <path d="M128 452 L 126 548 M172 452 L 174 548" />
              <circle cx="128" cy="449" r="3.5" />
              <circle cx="172" cy="449" r="3.5" />
            </>,
          )}
        </motion.g>
      </svg>
    </div>
  );
}

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
    <div className="flex flex-wrap justify-center gap-2" role="list" aria-label="Body regions">
      {regions.map((r) => (
        <button
          key={r.id}
          type="button"
          role="listitem"
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

export { LEVEL_COLOR, LEVEL_WORD };

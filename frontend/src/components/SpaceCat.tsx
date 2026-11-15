import { useEffect } from "react";

/** Six stripes of the trail, top to bottom. */
const RAINBOW = ["#ff3b3b", "#ff9f1a", "#ffe14d", "#3ddc6a", "#3aa0ff", "#8a5cff"];
const SEGMENTS = 14;
/** Twinkling stars: [left %, top %, delay s]. */
const STARS: [number, number, number][] = [
  [8, 18, 0], [22, 70, 0.3], [35, 30, 0.6], [48, 78, 0.15], [61, 22, 0.45],
  [74, 64, 0.75], [86, 36, 0.2], [93, 80, 0.55], [15, 45, 0.9], [55, 52, 0.35],
];

/**
 * Easter egg (#97): a space cat in a helmet crosses the screen once, with a rainbow trail and stars.
 * Original drawing made for this app; no sound. "Reduce motion" shows it still, then fades it out.
 */
export function SpaceCat({ onDone }: { onDone: () => void }) {
  useEffect(() => {
    const id = setTimeout(onDone, 5600); // in case animationend never fires
    return () => clearTimeout(id);
  }, [onDone]);

  return (
    <div className="pointer-events-none fixed inset-0 z-[200] overflow-hidden" aria-hidden>
      {STARS.map(([x, y, d], i) => (
        <span key={i} className="spacecat-star absolute" style={{ left: `${x}%`, top: `${y}%`, animationDelay: `${d}s` }} />
      ))}
      <div className="spacecat-fly absolute top-[38%] left-0 flex items-center" onAnimationEnd={onDone}>
        {/* Trail: stepped segments that wave up and down, ending at the cat. */}
        <div className="flex">
          {Array.from({ length: SEGMENTS }, (_, i) => (
            <div key={i} className={`spacecat-wave flex flex-col ${i % 2 ? "spacecat-wave-alt" : ""}`}>
              {RAINBOW.map((c) => (
                <span key={c} className="block h-[7px] w-[34px]" style={{ background: c }} />
              ))}
            </div>
          ))}
        </div>
        <svg className="spacecat-bob -ml-3 h-[96px] w-[144px]" viewBox="0 0 120 80" role="img">
          {/* Tail */}
          <path d="M20 44 Q6 40 6 26" fill="none" stroke="#2b3440" strokeWidth="9" strokeLinecap="round" />
          <path d="M20 44 Q6 40 6 26" fill="none" stroke="#a7b1ba" strokeWidth="5" strokeLinecap="round" />
          {/* Legs */}
          {[24, 38, 54, 66].map((x, i) => (
            <rect key={x} className={i % 2 ? "spacecat-leg-alt" : "spacecat-leg"} x={x} y="54" width="9" height="12" rx="3" fill="#a7b1ba" stroke="#2b3440" strokeWidth="2" />
          ))}
          {/* Body: a small white suit with a cyan stripe */}
          <rect x="16" y="28" width="60" height="32" rx="15" fill="#eef4f7" stroke="#2b3440" strokeWidth="2.5" />
          <rect x="30" y="40" width="34" height="6" rx="3" fill="#00c2ee" />
          {/* Head */}
          <polygon points="70,24 73,7 84,19" fill="#a7b1ba" stroke="#2b3440" strokeWidth="2" strokeLinejoin="round" />
          <polygon points="89,18 99,7 102,25" fill="#a7b1ba" stroke="#2b3440" strokeWidth="2" strokeLinejoin="round" />
          <polygon points="74,20 75,12 80,18" fill="#f49ab5" />
          <polygon points="92,17 97,12 98,21" fill="#f49ab5" />
          <circle cx="86" cy="36" r="19" fill="#a7b1ba" stroke="#2b3440" strokeWidth="2.5" />
          <circle cx="79" cy="34" r="3.3" fill="#1b2128" />
          <circle cx="93" cy="34" r="3.3" fill="#1b2128" />
          <circle cx="80.2" cy="32.8" r="1.1" fill="#fff" />
          <circle cx="94.2" cy="32.8" r="1.1" fill="#fff" />
          <circle cx="75" cy="42" r="3" fill="#f49ab5" opacity="0.7" />
          <circle cx="97" cy="42" r="3" fill="#f49ab5" opacity="0.7" />
          <polygon points="84.5,40 87.5,40 86,42" fill="#e5577f" />
          <path d="M82 44 Q84 46.5 86 44 Q88 46.5 90 44" fill="none" stroke="#2b3440" strokeWidth="1.5" strokeLinecap="round" />
          {/* Helmet glass */}
          <circle cx="86" cy="36" r="27" fill="#bfe9ff" fillOpacity="0.16" stroke="#cdeffc" strokeWidth="2.5" />
          <path d="M70 22 Q76 14 86 13" fill="none" stroke="#ffffff" strokeOpacity="0.8" strokeWidth="2.5" strokeLinecap="round" />
        </svg>
      </div>
    </div>
  );
}

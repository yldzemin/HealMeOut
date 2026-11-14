// Daily check-in items. Basis: PSYCHOLOGY_AND_LONG_DURATION.md §4.1
// (ISS Standard Measures VAS items, Picano 2024, Landon 2024, Kanas 2007,
// Douglas 2025). Wording is ours unless quoted. Insight rules are OUR DESIGN,
// not NASA criteria, and never diagnostic.

export interface CheckIn {
  mood: number;
  fatigue: number;
  stress: number;
  conflict: number;
  sleepHours: number;
  sleepQuality: number;
  workload: number;
  control: number;
  crewWorksWell: number;
  sameCrew: number;
  teamPerformed: number;
  mccFrustration: number;
  daysSinceFamilyCall: number;
  familyEvent: boolean;
  restorative: string[];
  foodPercent: number;
  favouritesOnly: boolean;
}

export const DEFAULT_CHECKIN: CheckIn = {
  mood: 60,
  fatigue: 50,
  stress: 40,
  conflict: 20,
  sleepHours: 6.5,
  sleepQuality: 3,
  workload: 3,
  control: 3,
  crewWorksWell: 4,
  sameCrew: 4,
  teamPerformed: 4,
  mccFrustration: 2,
  daysSinceFamilyCall: 3,
  familyEvent: false,
  restorative: [],
  foodPercent: 80,
  favouritesOnly: false,
};

export const RESTORATIVE = ["Cupola / Earth view", "Earth photography", "Exercise", "Journal", "Call with family"];

export const STEPS = [
  { id: "feel", title: "How do you feel right now?", basis: "Visual analogue scales, as in ISS Standard Measures" },
  { id: "sleep", title: "How did you sleep?", basis: "Sleep log (HRP sleep risk)" },
  { id: "work", title: "How was your workload?", basis: "Workload and timeline control (Picano 2024)" },
  { id: "crew", title: "How is the crew climate?", basis: "Group living items (Landon 2024) and a team item (APEX)" },
  { id: "mcc", title: "How did today go with mission control?", basis: "Tracks displacement toward the ground (Kanas 2007)" },
  { id: "family", title: "Family and connection", basis: "Family conferences and family events (Picano 2024)" },
  { id: "rest", title: "What helped you recharge today?", basis: "Earth view, photography, exercise, journaling (OCHMO-TB-016)" },
  { id: "food", title: "How much did you eat today?", basis: "Food intake and variety (Douglas 2025; NTRS 20060049121)" },
] as const;

export interface Insight {
  text: string;
  tone: "info" | "attention";
}

/** Supportive, non-diagnostic notes. Thresholds are our design. */
export function insights(c: CheckIn, recentShortNights: number): Insight[] {
  const out: Insight[] = [];
  const shortTonight = c.sleepHours < 6 ? 1 : 0;
  const shortNights = recentShortNights + shortTonight;
  if (shortNights >= 3)
    out.push({ text: `${shortNights} short nights (under 6 h) this week. Protect your next sleep window and raise it at your next private conference.`, tone: "attention" });
  else if (shortTonight) out.push({ text: "Short night. Keep tonight’s sleep window free of tasks.", tone: "info" });
  if (c.stress >= 70 || c.mood <= 30)
    out.push({ text: "Today felt heavy. You can talk about it at your next private conference, or ask for one sooner.", tone: "attention" });
  if (c.control <= 2 && c.workload >= 4) out.push({ text: "High workload with little control over your timeline. Tell the ground team what could move.", tone: "info" });
  if (c.familyEvent) out.push({ text: "Important family event noted. You can request a private family conference.", tone: "attention" });
  if (c.foodPercent < 70 || c.favouritesOnly)
    out.push({ text: "Eating less or only favourites. That is common on long missions; tell your CMO if it continues.", tone: "info" });
  if (c.restorative.length === 0) out.push({ text: "No break today. Ten minutes at the Cupola counts.", tone: "info" });
  return out;
}

// One simulated profile per crew member, so the Personal screen differs per
// astronaut. Every value here is SIMULATED. Each story is one of the most likely
// conditions on a 6-month ISS mission in the NASA Integrated Medical Model
// ranking (data/cache/imm_leo.json), or a sourced routine event (pre-landing
// fluid loading, OCHMO-TB-027). Medication always goes through the CMO; no drug
// is named until the ISS formulary question (#11) is decided.
//
// The backend seeds the same values (backend CrewRoster: vitals, 7 nights,
// mood, and the L1+ stories as condition alerts). Keep both in sync.

import type { StatusLevel } from "@/types";

export type StoryRegion = "head" | "eyes" | "heart" | "lungs" | "blood" | "musculo" | "whole";

export interface Story {
  region: StoryRegion;
  level: StatusLevel;
  status: string;
  meaning: string;
  todo: string;
  /** IMM probability of at least one event on a 6-month ISS mission, when the story is an IMM condition. */
  imm?: { condition: string; probability: number };
  extraSource?: { label: string; url: string };
}

export interface CrewProfile {
  missionDay: number;
  sleep7: number[]; // last 7 nights, hours
  mood: number; // 0–100, from check-in
  reactionMs: number;
  reactionBestMs: number;
  hr: number;
  hrBaselineMean: number;
  hrBaselineSd: number;
  hr7: number[];
  bp: string;
  spo2: number;
  breathing: number;
  doseToday: number; // mSv
  tempC: number;
  exercise: { done: number; planned: number };
  fluidsL: number;
  streakDays: number;
  lastEyeExam: string;
  nextEyeExam: string;
  story?: Story;
}

export const IMM_SOURCE = {
  label: "NASA IMM: ranking of medical conditions by probability",
  url: "https://www.nasa.gov/wp-content/uploads/2026/08/ranking-of-medical-conditions-by-probability.pdf",
};

export const PROFILES: Record<string, CrewProfile> = {
  C1: {
    missionDay: 120,
    sleep7: [6.8, 7.1, 6.6, 7.0, 6.9, 7.2, 6.9],
    mood: 78,
    reactionMs: 236,
    reactionBestMs: 229,
    hr: 58,
    hrBaselineMean: 57,
    hrBaselineSd: 4,
    hr7: [57, 58, 56, 59, 57, 58, 58],
    bp: "110/69",
    spo2: 98,
    breathing: 13,
    doseToday: 0.31,
    tempC: 37.3,
    exercise: { done: 1, planned: 1 },
    fluidsL: 2.0,
    streakDays: 34,
    lastEyeExam: "FD90",
    nextEyeExam: "R-30",
  },
  C2: {
    missionDay: 64,
    sleep7: [6.6, 6.3, 6.7, 6.5, 6.2, 6.4, 6.1],
    mood: 55,
    reactionMs: 262,
    reactionBestMs: 244,
    hr: 66,
    hrBaselineMean: 63,
    hrBaselineSd: 5,
    hr7: [62, 63, 64, 62, 65, 64, 66],
    bp: "113/72",
    spo2: 98,
    breathing: 15,
    doseToday: 0.33,
    tempC: 37.4,
    exercise: { done: 1, planned: 1 },
    fluidsL: 1.1,
    streakDays: 12,
    lastEyeExam: "FD30",
    nextEyeExam: "FD90",
    story: {
      region: "head",
      level: "L1",
      status: "Headache since this afternoon. You are behind on fluids, and cabin CO2 can also cause headaches.",
      meaning:
        "Headaches are among the most common health events on the ISS. Dehydration and raised cabin CO2 are two usual causes; NASA keeps the 7-day CO2 average under 2.5 mmHg to keep headache risk low.",
      todo:
        "Drink water now, rest in a well-ventilated module and ease off heavy work. If you need a pain reliever, ask the CMO for one from the ISS medical kit. Tell the CMO if the headache gets worse or comes with vision changes.",
      imm: { condition: "Late headache", probability: 0.592 },
      extraSource: { label: "NASA OCHMO-TB-004 Carbon dioxide", url: "https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf" },
    },
  },
  C3: {
    missionDay: 88,
    sleep7: [6.4, 6.1, 6.8, 5.9, 6.3, 6.0, 5.6],
    mood: 64,
    reactionMs: 254,
    reactionBestMs: 241,
    hr: 64,
    hrBaselineMean: 62,
    hrBaselineSd: 5,
    hr7: [61, 63, 62, 65, 63, 62, 64],
    bp: "111/70",
    spo2: 98,
    breathing: 14,
    doseToday: 0.32,
    tempC: 37.4,
    exercise: { done: 0, planned: 1 },
    fluidsL: 1.4,
    streakDays: 6,
    lastEyeExam: "FD30",
    nextEyeExam: "FD90",
    story: {
      region: "head",
      level: "L1",
      status: "You slept 5.6 h last night, under 6 hours. Protect tonight’s sleep window.",
      meaning:
        "Insomnia is the most likely health event on a 6-month ISS mission. ISS crews sleep about 6.1 hours a night on average, against 8.5 hours scheduled; short sleep slows reaction time and makes decisions harder.",
      todo:
        "Keep tonight’s sleep window free of tasks. Switch your module lights to the pre-sleep setting an hour before bed. If short nights continue, raise it at your next private conference.",
      imm: { condition: "Late insomnia", probability: 0.912 },
    },
  },
  C4: {
    missionDay: 3,
    sleep7: [7.4, 7.0, 6.9, 7.2, 5.8, 5.4, 6.0],
    mood: 49,
    reactionMs: 271,
    reactionBestMs: 252,
    hr: 72,
    hrBaselineMean: 64,
    hrBaselineSd: 5,
    hr7: [63, 64, 62, 65, 74, 73, 72],
    bp: "114/73",
    spo2: 98,
    breathing: 16,
    doseToday: 0.3,
    tempC: 37.0,
    exercise: { done: 0, planned: 0 },
    fluidsL: 0.9,
    streakDays: 3,
    lastEyeExam: "L-10",
    nextEyeExam: "FD30",
    story: {
      region: "head",
      level: "L2",
      status: "Nausea since arrival on flight day 1. Space motion sickness is common in the first days.",
      meaning:
        "In weightlessness the balance organs and the eyes send conflicting signals, which can cause nausea and loss of appetite in the first days. NASA’s Integrated Medical Model rates it among the most likely events on an ISS mission.",
      todo:
        "Anti-nausea medication from the ISS kit, decided by CMO-1 (you are CMO-2, so another CMO approves). Keep head movements slow, sip water, eat small amounts and keep a fixed visual reference.",
      imm: { condition: "Space motion sickness", probability: 0.73 },
    },
  },
  C5: {
    missionDay: 140,
    sleep7: [6.9, 6.5, 6.8, 6.6, 7.0, 6.7, 6.8],
    mood: 70,
    reactionMs: 248,
    reactionBestMs: 238,
    hr: 61,
    hrBaselineMean: 60,
    hrBaselineSd: 4,
    hr7: [60, 61, 59, 62, 61, 60, 61],
    bp: "112/71",
    spo2: 98,
    breathing: 14,
    doseToday: 0.34,
    tempC: 37.6,
    exercise: { done: 1, planned: 1 },
    fluidsL: 1.9,
    streakDays: 21,
    lastEyeExam: "FD90",
    nextEyeExam: "FD150",
    story: {
      region: "whole",
      level: "L1",
      status: "Itchy rash on the left forearm. You reported it this morning.",
      meaning:
        "Skin rashes and abrasions are two of the most common health events on a 6-month ISS mission in NASA’s Integrated Medical Model. Most are minor, but a rash that spreads needs a look.",
      todo:
        "Keep the area clean and dry and avoid scratching; use a moisturiser. Show the CMO if it spreads, blisters or does not improve in a few days.",
      imm: { condition: "Skin rash", probability: 0.821 },
    },
  },
  C6: {
    missionDay: 12,
    sleep7: [6.2, 6.6, 6.4, 6.8, 6.5, 6.3, 6.6],
    mood: 62,
    reactionMs: 245,
    reactionBestMs: 239,
    hr: 67,
    hrBaselineMean: 65,
    hrBaselineSd: 5,
    hr7: [66, 65, 67, 66, 68, 66, 67],
    bp: "112/70",
    spo2: 99,
    breathing: 14,
    doseToday: 0.3,
    tempC: 37.2,
    exercise: { done: 1, planned: 1 },
    fluidsL: 1.8,
    streakDays: 9,
    lastEyeExam: "L-10",
    nextEyeExam: "FD30",
    story: {
      region: "musculo",
      level: "L1",
      status: "Lower back pain since flight day 2. Back pain is common early in a mission (space adaptation).",
      meaning:
        "Back pain while the body adapts to weightlessness is common early in a mission; NASA’s Integrated Medical Model lists it as back pain from space adaptation, with about a one-in-two chance on a 6-month ISS mission.",
      todo:
        "Gentle stretching and your planned exercise session usually help. Tell the CMO if the pain spreads into a leg or comes with numbness or weakness.",
      imm: { condition: "Back pain (space adaptation)", probability: 0.525 },
    },
  },
  C7: {
    missionDay: 176,
    sleep7: [6.7, 6.9, 6.4, 6.8, 7.0, 6.6, 6.9],
    mood: 74,
    reactionMs: 240,
    reactionBestMs: 232,
    hr: 63,
    hrBaselineMean: 62,
    hrBaselineSd: 4,
    hr7: [62, 63, 61, 64, 62, 63, 63],
    bp: "109/68",
    spo2: 98,
    breathing: 13,
    doseToday: 0.33,
    tempC: 37.5,
    exercise: { done: 1, planned: 1 },
    fluidsL: 2.2,
    streakDays: 58,
    lastEyeExam: "FD150",
    nextEyeExam: "R-30",
    story: {
      region: "heart",
      level: "L0",
      status: "Return to Earth in 10 days. Your heart and blood pressure are fine; plan your fluid loading before deorbit.",
      meaning:
        "After months in weightlessness your body holds less fluid. Back in gravity, blood pools in the legs and standing up can make you dizzy or faint. NASA prepares crews with fluid loading before deorbit.",
      todo:
        "Before deorbit: weight-based fluid loading with salt tablets, as in the NASA protocol. Keep up your exercise to protect bones and muscles for the return.",
      extraSource: { label: "NASA OCHMO-TB-027 Water (fluid loading)", url: "https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf" },
    },
  },
};

export function profileFor(crewId: string): CrewProfile {
  return PROFILES[crewId] ?? PROFILES.C3;
}

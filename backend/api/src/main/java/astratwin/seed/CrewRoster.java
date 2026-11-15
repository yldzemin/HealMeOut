package astratwin.seed;

import astratwin.domain.BaselineStats;
import astratwin.domain.CheckIn;
import astratwin.domain.ConditionReport;
import astratwin.domain.CrewMember;
import astratwin.domain.CrewPersona;
import astratwin.domain.Level;
import astratwin.domain.MedicalCard;
import astratwin.domain.MissionTag;
import astratwin.domain.Reading;
import astratwin.domain.Role;
import astratwin.twin.BaselineStatistics;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * The 7-person crew (TECH_OVERVIEW §4.4), the same as {@code frontend/src/mock/crew.json}; module ids
 * match {@code frontend/src/data/station.ts}. Callsigns are fictional. C1–C4 use the real Inspiration4
 * baselines (C001–C004); C5–C7 and every in-flight value here are simulated.
 *
 * <p>Vitals, the last 7 nights, mood and reported conditions come from the per-crew profiles in
 * {@code frontend/src/data/crewProfiles.ts} (#71), so the live app tells the same story as mock mode.
 */
public final class CrewRoster {
    /** Simulated per-crew values; keep in sync with {@code PROFILES} in crewProfiles.ts. */
    private record Profile(String id, int hr, int bpSys, int bpDia, int spo2, double tempC, double[] sleep7, int mood) {
    }

    private static final List<Profile> PROFILES = List.of(
            new Profile("C1", 58, 110, 69, 98, 37.3, new double[] {6.8, 7.1, 6.6, 7.0, 6.9, 7.2, 6.9}, 78),
            new Profile("C2", 66, 113, 72, 98, 37.4, new double[] {6.6, 6.3, 6.7, 6.5, 6.2, 6.4, 6.1}, 55),
            new Profile("C3", 64, 111, 70, 98, 37.4, new double[] {6.4, 6.1, 6.8, 5.9, 6.3, 6.0, 5.6}, 64),
            new Profile("C4", 72, 114, 73, 98, 37.0, new double[] {7.4, 7.0, 6.9, 7.2, 5.8, 5.4, 6.0}, 49),
            new Profile("C5", 61, 112, 71, 98, 37.6, new double[] {6.9, 6.5, 6.8, 6.6, 7.0, 6.7, 6.8}, 70),
            new Profile("C6", 67, 112, 70, 99, 37.2, new double[] {6.2, 6.6, 6.4, 6.8, 6.5, 6.3, 6.6}, 62),
            new Profile("C7", 63, 109, 68, 98, 37.5, new double[] {6.7, 6.9, 6.4, 6.8, 7.0, 6.6, 6.9}, 74));
    /** Small day-to-day changes in the earlier check-ins (oldest first); the last one is the profile's mood. */
    private static final int[] MOOD_DRIFT = {3, 1, 4, 2, -1, 2, 0};
    private static final String IMM = "NASA IMM, ranking of medical conditions by probability (6-month ISS mission)";

    private static final List<CrewMember> MEMBERS = List.of(
            member("C1", "ORION", Role.CMO1, "Destiny", true),
            member("C2", "VEGA", Role.CREW, "Harmony (Node 2)", true),
            member("C3", "LYRA", Role.CREW, "Columbus", true),
            member("C4", "ALTAIR", Role.CMO2, "Kibo", true),
            member("C5", "RIGEL", Role.CREW, "Tranquility (Node 3)", false),
            member("C6", "NOVA", Role.CREW, "Unity (Node 1)", false),
            member("C7", "ATLAS", Role.CREW, "Zvezda", false));

    private final BaselineStatistics baselines;

    public CrewRoster(BaselineStatistics baselines) {
        this.baselines = baselines;
    }

    private static CrewMember member(String id, String callsign, Role role, String module, boolean realBaseline) {
        return new CrewMember(id, callsign, role, module, Level.L0,
                realBaseline ? "real" : "simulated");
    }

    public List<CrewMember> members() {
        return MEMBERS;
    }

    public CrewMember member(String id) {
        return MEMBERS.stream().filter(m -> m.id().equals(id)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("no crew member " + id));
    }

    /** Commercial-mission tag of the member's real baselines, or null for simulated members. */
    public MissionTag mission(String crewId) {
        return baselines.forCrew(crewId).stream().map(BaselineStats::mission).filter(m -> m != null).findFirst().orElse(null);
    }

    /** Fictional emergency cards. */
    public List<MedicalCard> cards() {
        String p = "simulated (fictional)";
        return List.of(
                new MedicalCard("C1", List.of(), List.of(), false, p),
                new MedicalCard("C2", List.of(), List.of(), false, p),
                new MedicalCard("C3", List.of(), List.of("Melatonin 3 mg at bedtime"), true, p),
                new MedicalCard("C4", List.of(), List.of(), false, p),
                new MedicalCard("C5", List.of("Latex"), List.of(), false, p),
                new MedicalCard("C6", List.of(), List.of(), false, p),
                new MedicalCard("C7", List.of(), List.of(), false, p));
    }

    /**
     * Fictional daily check-ins for the 7 days before {@code today} (oldest first), so today's check-in
     * is still to do, as in mock mode. Self-reported sleep equals the wrist monitor's nights, so the
     * Twin's sleep debt matches the Personal screen's sleep chart. No member starts in the psych-trend range.
     */
    public List<CheckIn> checkIns(LocalDate today) {
        String source = "simulated (fictional)";
        List<CheckIn> out = new ArrayList<>();
        for (Profile p : PROFILES) {
            for (int d = 0; d < p.sleep7().length; d++) {
                double sleep = p.sleep7()[d];
                int mood = p.mood() + MOOD_DRIFT[d];
                int fatigue = (int) Math.round(25 + (8.5 - sleep) * 12);
                int stress = Math.max(10, Math.min(60, 95 - mood));
                out.add(new CheckIn(p.id(), today.minusDays(p.sleep7().length - d).toString(), mood, fatigue, stress, 15,
                        sleep, source));
            }
        }
        return out;
    }

    /**
     * Fictional companion profiles, one per crew member, "set by the astronaut before flight". Diverse on
     * purpose (two not religious); the companion adapts to them and never assumes beyond them.
     */
    public List<CrewPersona> personas() {
        String p = "simulated (fictional)";
        return List.of(
                new CrewPersona("C1", List.of("calm", "methodical", "a dry sense of humour"),
                        List.of("chess", "amateur astronomy", "cooking for friends"),
                        List.of("a run on the treadmill", "a short to-do list", "a call with your partner"), null,
                        List.of("letting the crew down in an emergency"), "brief and practical",
                        "a partner and two teenagers", p),
                new CrewPersona("C2", List.of("energetic", "sociable", "a bit of a perfectionist"),
                        List.of("guitar", "football", "Earth photography"),
                        List.of("music", "joking with the crew", "photographing Earth", "prayer"),
                        "Catholic; prays the rosary on Sundays", List.of("missing their daughter's milestones"),
                        "warm, with some humour", "a husband and a six-year-old daughter", p),
                new CrewPersona("C3", List.of("introverted", "reflective", "detail-oriented"),
                        List.of("reading", "sketching", "journaling"),
                        List.of("a 10-minute meditation", "journaling", "quiet time at the Cupola", "prayer"),
                        "practising Muslim; prays daily", List.of("feeling closed in inside the sleep station"),
                        "gentle, with few questions", "parents and a grandmother they are close to", p),
                new CrewPersona("C4", List.of("optimistic", "curious", "on their first flight"),
                        List.of("rock climbing", "podcasts", "video games"),
                        List.of("talking things through", "a bit of humour", "exercise"), null,
                        List.of("not doing well enough as a rookie"), "casual and upbeat",
                        "single; close to two siblings", p),
                new CrewPersona("C5", List.of("stoic", "private", "practical"),
                        List.of("crosswords", "classical music", "woodworking at home"),
                        List.of("meditation", "classical music", "a crossword"),
                        "Buddhist; meditates every morning", List.of("crowded, noisy spaces"), "calm and short",
                        "a partner", p),
                new CrewPersona("C6", List.of("outgoing", "competitive", "impatient"),
                        List.of("running", "yoga", "cooking"),
                        List.of("stretching or yoga", "a call with friends", "cooking with the crew"),
                        "Orthodox Christian; prays now and then", List.of("blood draws and needles"),
                        "direct and motivating", "a spouse and a baby son", p),
                new CrewPersona("C7", List.of("a veteran", "a mentor to newer crew", "nostalgic"),
                        List.of("ham radio contacts with schools", "Earth photography", "history books"),
                        List.of("a ham radio contact with a school", "photographing Earth", "mentoring the newer crew"),
                        "Jewish; keeps Shabbat when the schedule allows",
                        List.of("readjusting to gravity and to family life after return"), "reflective; likes stories",
                        "a spouse and two adult children", p));
    }

    /**
     * Stories about a single night (C3's short night). They end when the station moves to the next day
     * (#81); the other stories are ongoing and stay.
     */
    public static boolean oneNight(ConditionReport c) {
        return c.crewId().equals("C3") && c.region().equals("head");
    }

    /** Simulated reported conditions: the stories of the frontend profiles that need attention (L1+). */
    public List<ConditionReport> conditions() {
        return List.of(
                new ConditionReport("C2", "head", Level.L1, "Headache since this afternoon",
                        "You are behind on fluids, and cabin CO2 can also cause headaches.",
                        "Drink water now, rest in a well-ventilated module and ease off heavy work. If you need a pain "
                                + "reliever, the ISS kit has acetaminophen and ibuprofen: ask the CMO. Tell the CMO if the "
                                + "headache gets worse or comes with vision changes.",
                        "Simulated story. NASA IMM: late headache, 59% chance on a 6-month ISS mission. A 7-day CO2 "
                                + "average under 2.5 mmHg keeps headache risk low (OCHMO-TB-004). Medication named only "
                                + "from the ISS kit list (decision #11).",
                        IMM + "; OCHMO-TB-004; " + IssFormulary.SOURCE),
                new ConditionReport("C3", "head", Level.L1, "You slept 5.6 h last night, under 6 hours",
                        "Protect tonight’s sleep window.",
                        "Keep tonight’s sleep window free of tasks. Switch your module lights to the pre-sleep setting an "
                                + "hour before bed. If short nights continue, raise it at your next private conference.",
                        "Simulated story (wrist activity monitor). The under-6-hours flag is our design, not a NASA "
                                + "threshold. NASA IMM: late insomnia, 91% chance on a 6-month ISS mission.",
                        IMM + "; Barger 2014"),
                new ConditionReport("C4", "head", Level.L2, "Nausea since arrival on flight day 1",
                        "Space motion sickness is common in the first days.",
                        "Anti-nausea medication from the ISS kit (promethazine or ondansetron), decided by CMO-1 (you are "
                                + "CMO-2, so another CMO approves). Keep head movements slow, sip water, eat small amounts and "
                                + "keep a fixed visual reference.",
                        "Simulated story. NASA IMM: space motion sickness, 73% chance on a 6-month ISS mission. "
                                + "Medication named only from the ISS kit list (decision #11).",
                        IMM + "; " + IssFormulary.SOURCE),
                new ConditionReport("C5", "whole", Level.L1, "Itchy rash on the left forearm", "You reported it this morning.",
                        "Keep the area clean and dry and avoid scratching; use a moisturiser. Show the CMO if it spreads, "
                                + "blisters or does not improve in a few days.",
                        "Simulated story. NASA IMM: skin rash, 82% chance on a 6-month ISS mission.", IMM),
                new ConditionReport("C6", "musculo", Level.L1, "Lower back pain since flight day 2",
                        "Back pain is common early in a mission (space adaptation).",
                        "Gentle stretching and your planned exercise session usually help. Tell the CMO if the pain spreads "
                                + "into a leg or comes with numbness or weakness.",
                        "Simulated story. NASA IMM: back pain (space adaptation), 53% chance on a 6-month ISS mission.", IMM));
    }

    /** Simulated in-flight vitals from each member's profile (BACKEND §3.1). */
    public List<Reading> nominalReadings(Instant now) {
        List<Reading> out = new ArrayList<>();
        for (Profile p : PROFILES) {
            String id = p.id();
            out.add(sim(id, "hr", p.hr(), "bpm", "live", now,
                    "Simulated. Resting 50–100 bpm; 24-h heart rate is unchanged in flight (Norsk 2015)."));
            out.add(sim(id, "bp_sys", p.bpSys(), "mmHg", "periodic", now,
                    "Simulated, cuff-calibrated. In-flight 24-h BP is about 112/71 (Norsk 2015)."));
            out.add(sim(id, "bp_dia", p.bpDia(), "mmHg", "periodic", now,
                    "Simulated, cuff-calibrated. In-flight 24-h BP is about 112/71 (Norsk 2015)."));
            out.add(sim(id, "spo2", p.spo2(), "%", "live", now,
                    "Simulated. Normal 96–99% in the ISS cabin (14.7 psia, 21% O2)."));
            out.add(sim(id, "temp_c", p.tempC(), "°C", "live", now,
                    "Simulated. Resting temperature rises about 1 °C over ~2.5 months in flight (Stahn 2017)."));
            Optional<BaselineStats> glucose = baselines.stats(id, "glucose");
            out.add(sim(id, "glucose", glucose.map(s -> Math.round(s.mean() * 10) / 10.0).orElse(90.0), "mg/dL", "periodic", now,
                    glucose.isPresent()
                            ? "Simulated in-flight value set to the personal pre-flight mean (OSD-575). Team assumption (#30): glucose is measurable in flight."
                            : "Simulated. Team assumption (#30): glucose is measurable in flight."));
        }
        return out;
    }

    private static Reading sim(String crewId, String metric, double value, String unit, String measuredHow,
            Instant now, String note) {
        return new Reading(crewId, metric, value, unit, now, "simulated", measuredHow, note);
    }
}

package astratwin.seed;

import astratwin.domain.BaselineStats;
import astratwin.domain.CheckIn;
import astratwin.domain.CrewMember;
import astratwin.domain.Level;
import astratwin.domain.MedicalCard;
import astratwin.domain.MissionTag;
import astratwin.domain.Reading;
import astratwin.domain.Role;
import astratwin.twin.BaselineStatistics;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * The 7-person crew (TECH_OVERVIEW §4.4), the same as {@code frontend/src/mock/crew.json}; module ids
 * match {@code frontend/src/data/station.ts}. Callsigns are fictional. C1–C4 use the real Inspiration4
 * baselines (C001–C004); C5–C7 and every in-flight value here are simulated.
 */
public final class CrewRoster {
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

    /** Fictional psychological check-ins (used by the privacy test and demo). */
    public List<CheckIn> checkIns() {
        String p = "simulated (fictional)";
        return List.of(
                new CheckIn("C3", "2026-11-13", 42, 61, 66, 18, 5.6, p),
                new CheckIn("C6", "2026-11-13", 70, 35, 30, 10, 6.8, p));
    }

    /** Simulated in-flight vitals around each member's baseline (BACKEND §3.1). */
    public List<Reading> nominalReadings(Instant now) {
        List<Reading> out = new ArrayList<>();
        for (int i = 0; i < MEMBERS.size(); i++) {
            String id = MEMBERS.get(i).id();
            out.add(sim(id, "hr", 60 + 2 * i, "bpm", "live", now,
                    "Simulated. Resting 50–100 bpm; 24-h heart rate is unchanged in flight (Norsk 2015)."));
            out.add(sim(id, "bp_sys", 112, "mmHg", "periodic", now,
                    "Simulated, cuff-calibrated. In-flight 24-h BP is about 112/71 (Norsk 2015)."));
            out.add(sim(id, "bp_dia", 71, "mmHg", "periodic", now,
                    "Simulated, cuff-calibrated. In-flight 24-h BP is about 112/71 (Norsk 2015)."));
            out.add(sim(id, "spo2", 98, "%", "live", now,
                    "Simulated. Normal 96–99% in the ISS cabin (14.7 psia, 21% O2)."));
            out.add(sim(id, "temp_c", 37.4, "°C", "live", now,
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

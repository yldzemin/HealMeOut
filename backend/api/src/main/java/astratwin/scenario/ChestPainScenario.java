package astratwin.scenario;

import astratwin.domain.CrewMember;
import java.util.List;

/** MVP hero scenario: a crewmate collapses; the crew coordinates (BACKEND §5, FRONTEND §6). */
public final class ChestPainScenario implements Scenario {
    private final CrewMember patient;
    private final CrewMember lead;
    private final CrewMember assist;
    private final CrewMember fetcher;
    private final CrewMember comms;

    public ChestPainScenario(CrewMember patient, CrewMember lead, CrewMember assist, CrewMember fetcher, CrewMember comms) {
        this.patient = patient;
        this.lead = lead;
        this.assist = assist;
        this.fetcher = fetcher;
        this.comms = comms;
    }

    @Override
    public String id() {
        return "chest-pain";
    }

    @Override
    public String title() {
        return "Crewmate chest pain and collapse";
    }

    @Override
    public String priority() {
        return "MVP";
    }

    @Override
    public String source() {
        return "Hinkelbein 2020 (doi:10.1186/s13049-020-00793-y); NASA-STD-3001 §5";
    }

    @Override
    public List<ScenarioStep> steps() {
        String who = patient.callsign() + " (" + patient.id() + ")";
        return List.of(
                ScenarioStep.at("T+0 s", "Collapse detected")
                        .detail(who + "'s wristband pattern indicates a collapse. The rules engine raises an L4 "
                                + "emergency. Wristband detection is a concept, simulated here; times are illustrative.")
                        .reading(patient.id(), "collapse_detected", 1, "flag", "live",
                                "Concept: wristband collapse detection (simulated)")
                        .build(),
                ScenarioStep.at("T+5 s", "Wrist alert to every crewmate")
                        .detail("Every wristband and the Crew Deck switch to emergency mode. The location comes from "
                                + "a wearable tag (concept, simulated).")
                        .banner("EMERGENCY — " + who + " needs help, " + patient.module())
                        .build(),
                ScenarioStep.at("T+15 s", "Roles assigned")
                        .detail("Two trained CMOs lead the response.")
                        .role("Lead", label(lead))
                        .role("Assist", label(assist))
                        .role("Life-support pack and defibrillator", label(fetcher))
                        .role("Comms and log", label(comms))
                        .evidence("At least 2 CMOs per vehicle (NASA-STD-3001 §5).")
                        .build(),
                ScenarioStep.at("T+30 s", "Microgravity CPR checklist")
                        .detail("Steps from the microgravity CPR guideline.")
                        .check("Start chest compressions with the Evetts-Russomano method")
                        .check("If that is not possible or the rescuer tires, switch to the Reverse Bear Hug")
                        .check("Fix the patient on the Crew Medical Restraint System (CMRS) for advanced life support and defibrillation")
                        .evidence("Hinkelbein et al. 2020, doi:10.1186/s13049-020-00793-y")
                        .build(),
                ScenarioStep.at("T+45 s", "Emergency card opened")
                        .detail("Only allergies and current medication are shown (BACKEND §6). Switch the viewer to "
                                + "EMERGENCY to see it.")
                        .build(),
                ScenarioStep.at("T+1 min", "Ground informed")
                        .detail("On the ISS the ground link has seconds of delay and 90–95% S-band coverage, so the "
                                + "flight surgeon can guide — but the crew does not wait.")
                        .banner("EMERGENCY — " + who + ", " + patient.module() + " · ground informed, crew acting")
                        .evidence("NASA ISS Technology Demonstration mini-book (S-band 90–95%).")
                        .build());
    }

    private static String label(CrewMember m) {
        return m.id() + " " + m.callsign() + (m.isCmo() ? " (" + m.role() + ")" : "");
    }
}

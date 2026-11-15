package astratwin.scenario;

import astratwin.domain.CheckIn;
import astratwin.rules.FamilyEventRule;
import astratwin.rules.ReducedTaskingRule;
import astratwin.rules.SupportPlanRule;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Target: bad news from home (BACKEND §5, PSYCHOLOGY_AND_LONG_DURATION.md §4.5). Private to one
 * astronaut: they see the whole story and their support steps; the CMO and ground see the support plan
 * they approve; the Crew Deck sees only "requested reduced tasking". Everything is simulated.
 */
public final class BadNewsScenario implements Scenario {
    static final String EVIDENCE = "Picano 2024 (NTRS 20240000994): death of a family member on Earth is a top NASA "
            + "behavioral health concern; OCHMO-TB-016; PSYCHOLOGY_AND_LONG_DURATION.md §4.5.";
    private static final String HIDDEN = "Nothing to show here: this scenario is private to the crew member it concerns. "
            + "Switch the role to that astronaut to see it.";
    private final String crewId;

    public BadNewsScenario(String crewId) {
        this.crewId = crewId;
    }

    @Override
    public String id() {
        return "bad-news";
    }

    @Override
    public String title() {
        return "Bad news from home";
    }

    @Override
    public String publicTitle() {
        return "Crew support (private)";
    }

    @Override
    public String privateTo() {
        return crewId;
    }

    @Override
    public String priority() {
        return "Target";
    }

    @Override
    public String source() {
        return "Picano 2024 (NTRS 20240000994); OCHMO-TB-016; values and story are simulated";
    }

    @Override
    public List<ScenarioStep> steps() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        String later = LocalDate.now(ZoneOffset.UTC).plusDays(2).toString();
        return List.of(
                ScenarioStep.at("07:30", "A message from home")
                        .detail(crewId + " receives a private message from home: a family member has died (simulated). "
                                + "The Personal screen now shows private support steps on the head region.")
                        .reading(crewId, FamilyEventRule.FLAG, 1, "flag", "periodic", "Private family message (simulated)")
                        .evidence(EVIDENCE)
                        .publicView("Nominal day", HIDDEN)
                        .build(),
                ScenarioStep.at("08:00", "A hard night")
                        .detail("The morning check-in shows it: mood 28/100, stress 74/100, 4.5 h of sleep. Only "
                                + crewId + " sees it; the Twin's sleep debt goes up.")
                        .checkIn(new CheckIn(crewId, today, 28, 72, 74, 15, 4.5, "simulated (scenario)"))
                        .evidence(EVIDENCE)
                        .publicView("Nominal day", HIDDEN)
                        .build(),
                ScenarioStep.at("09:00", "Reduced tasking requested")
                        .detail(crewId + " asks the CMO for reduced tasking without giving a reason. The Crew Deck shows "
                                + "only \"requested reduced tasking\".")
                        .reading(crewId, ReducedTaskingRule.FLAG, 1, "flag", "periodic", "Crew request (simulated)")
                        .evidence(EVIDENCE)
                        .publicView(crewId + " requested reduced tasking",
                                "No reason is shared. Team care: cover their tasks, keep normal contact, offer help.")
                        .build(),
                ScenarioStep.at("09:30", "Earth Twin plan for ground approval")
                        .detail("The Earth Twin proposes a 24–72 h plan: a lighter schedule, a Private Family Conference "
                                + "today and a protected sleep window. Ground approves it; the CMO and " + crewId
                                + " see the plan, the Crew Deck does not.")
                        .reading(crewId, SupportPlanRule.FLAG, 1, "flag", "periodic", "Earth Twin plan (simulated)")
                        .evidence(EVIDENCE)
                        .publicView(crewId + " requested reduced tasking", "Ground is reviewing a schedule change.")
                        .build(),
                ScenarioStep.at("13:00", "Private Family Conference")
                        .detail("The conference takes place over the ISS link; crews can ask for one beyond the weekly "
                                + "family conference. The support companion is there any time.")
                        .evidence(EVIDENCE)
                        .publicView(crewId + " requested reduced tasking", "No reason is shared.")
                        .build(),
                ScenarioStep.at("Day 3, 07:30", "Back to the usual schedule")
                        .detail("The check-in is better (mood 46/100, 6.4 h of sleep). Reduced tasking ends after "
                                + "72 h. The private support steps stay until the next Private Psychological Conference.")
                        .checkIn(new CheckIn(crewId, later, 46, 45, 50, 10, 6.4, "simulated (scenario)"))
                        .reading(crewId, ReducedTaskingRule.FLAG, 0, "flag", "periodic", "Crew request (simulated)")
                        .reading(crewId, SupportPlanRule.FLAG, 0, "flag", "periodic", "Earth Twin plan (simulated)")
                        .evidence(EVIDENCE)
                        .publicView(crewId + " is back on the usual schedule", "Reduced tasking has ended.")
                        .build());
    }
}

package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * The Earth Twin's 24–72 h support plan (flag {@code support_plan}) → an L3 alert for ground approval,
 * category PLAN: the astronaut, the CMO and ground see it, the Crew Deck does not. It names the
 * request, never the reason, and never changes the crew level.
 */
public final class SupportPlanRule implements Rule {
    public static final String FLAG = "support_plan";
    static final String EVIDENCE = "Earth Twin support plan from PSYCHOLOGY_AND_LONG_DURATION.md §4.5; crews can ask "
            + "for Private Family Conferences beyond the weekly one (§2). Ground approves schedule changes.";
    static final String SOURCE = "PSYCHOLOGY_AND_LONG_DURATION.md §2, §4.5; Picano 2024";

    @Override
    public String id() {
        return "plan";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            if (ctx.reports(m.id(), FLAG)) {
                out.add(Alert.open(id() + ":" + m.id(), m.id(), Level.L3, "24–72 h support plan",
                        "Requested by " + m.id() + ": a lighter schedule for 24–72 h and a Private Family Conference "
                                + "today. The reason stays with the astronaut.",
                        "Ground (flight surgeon with the flight director) approves; the CMO moves non-critical tasks, "
                                + "protects the sleep window and keeps the exercise session.",
                        EVIDENCE, SOURCE, DataKind.PLAN, ctx.now()));
            }
        }
        return out;
    }
}

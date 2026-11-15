package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * Hard news from home (flag {@code family_event}) → the astronaut's private support steps, category
 * PSYCH: only they see it, and it never changes the crew level. Wording follows
 * PSYCHOLOGY_AND_LONG_DURATION.md §2–§4 and is never diagnostic.
 */
public final class FamilyEventRule implements Rule {
    public static final String FLAG = "family_event";
    static final String EVIDENCE = "NASA behavioral health lists the death of a family member on Earth as a top concern "
            + "(Picano 2024). Delayed news of a family death once caused acute social withdrawal (OCHMO-TB-016). Support "
            + "options from PSYCHOLOGY_AND_LONG_DURATION.md §2–§3. Private: NASA-STD-3001 Vol 1 V1 6010/6011.";
    static final String SOURCE = "Picano 2024 (NTRS 20240000994); OCHMO-TB-016";

    @Override
    public String id() {
        return "family-event";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            if (ctx.reports(m.id(), FLAG)) {
                out.add(Alert.open(id() + ":" + m.id(), m.id(), Level.L1, "A hard message from home",
                        "What you feel is normal, and you do not have to handle it alone.",
                        "Ask for a Private Family Conference today, or talk to your behavioral health provider. You can "
                                + "ask the CMO for reduced tasking without giving a reason. Keep your sleep window. The "
                                + "Cupola, Earth photography and journaling help some people. The support companion is "
                                + "there any time.",
                        EVIDENCE, SOURCE, DataKind.PSYCH, ctx.now()));
            }
        }
        return out;
    }
}

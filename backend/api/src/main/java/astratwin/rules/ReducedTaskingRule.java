package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * A crew member asked for reduced tasking (flag {@code tasking_reduced}) → a STATUS notice everyone
 * sees, with no reason. The Crew Deck learns only that the tasking changed.
 */
public final class ReducedTaskingRule implements Rule {
    public static final String FLAG = "tasking_reduced";
    static final String EVIDENCE = "Team care from NASA Expeditionary Skills training (PSYCHOLOGY_AND_LONG_DURATION.md "
            + "§4.5). The reason stays private (NASA-STD-3001 Vol 1 V1 6010/6011).";
    static final String SOURCE = "PSYCHOLOGY_AND_LONG_DURATION.md §4.5";

    @Override
    public String id() {
        return "tasking";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            if (ctx.reports(m.id(), FLAG)) {
                out.add(Alert.open(id() + ":" + m.id(), m.id(), Level.L1, "Requested reduced tasking",
                        "No reason is shared.",
                        "Team care: cover their tasks, keep normal contact and offer help. Do not ask for the reason.",
                        EVIDENCE, SOURCE, DataKind.STATUS, ctx.now()));
            }
        }
        return out;
    }
}

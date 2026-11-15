package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.ConditionReport;
import astratwin.domain.DataKind;
import java.util.List;

/**
 * Reported conditions → alerts {@code condition:<crewId>:<region>} at the reported level (category
 * VITALS: self, CMO and ground see the detail; the Crew Deck sees only the level).
 */
public final class ConditionReportRule implements Rule {
    @Override
    public String id() {
        return "condition";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        return ctx.conditions().stream().map(c -> alert(c, ctx)).toList();
    }

    private Alert alert(ConditionReport c, RuleContext ctx) {
        return Alert.open(id() + ":" + c.crewId() + ":" + c.region(), c.crewId(), c.level(), c.title(), c.explanation(),
                c.recommendation(), c.evidence(), c.source(), DataKind.VITALS, ctx.now());
    }
}

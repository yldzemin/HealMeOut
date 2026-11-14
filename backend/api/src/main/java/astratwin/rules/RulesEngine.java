package astratwin.rules;

import astratwin.domain.Alert;
import java.util.ArrayList;
import java.util.List;

/** Runs every rule; knows only the {@link Rule} interface. */
public final class RulesEngine {
    private final List<Rule> rules;

    public RulesEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<Alert> evaluate(RuleContext context) {
        List<Alert> firing = new ArrayList<>();
        for (Rule rule : rules) {
            firing.addAll(rule.evaluate(context));
        }
        return firing;
    }

    public List<String> ruleIds() {
        return rules.stream().map(Rule::id).toList();
    }
}

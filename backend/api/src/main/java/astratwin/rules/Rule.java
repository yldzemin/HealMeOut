package astratwin.rules;

import astratwin.domain.Alert;
import java.util.List;

/** One threshold rule (BACKEND §4). Rules are stateless; the engine stores their output. */
public interface Rule {
    String id();

    /** Alerts that fire now. An alert id must be stable so a CMO decision survives re-evaluation. */
    List<Alert> evaluate(RuleContext context);
}

package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Environment;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/** Cabin CO2 (1-h average): >3 → L1; ≥4.5 or headache → L2; ≥6.8 → L3 (BACKEND §4). */
public final class Co2Rule implements Rule {
    static final String EVIDENCE = "1-hour average limit 3 mmHg (NASA-STD-3001 V2 6004). Headache is reported at "
            + "2.8–4.5 mmHg. 6.8 mmHg is a NASA contingency proposal (6.8–10 mmHg for ≤4 h), not yet a standard.";
    static final String SOURCE = "OCHMO-TB-004; RESEARCH_REPORT.md §4; ICES-2025-402";

    @Override
    public String id() {
        return "co2";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Environment> withCo2 = ctx.environment().stream().filter(e -> e.co2_mmHg() != null).toList();
        if (withCo2.isEmpty()) {
            return List.of();
        }
        double co2 = withCo2.get(withCo2.size() - 1).co2_mmHg();
        boolean headache = ctx.crew().stream().map(CrewMember::id).anyMatch(id -> ctx.reports(id, "symptom_headache"));
        Level level;
        String recommendation;
        if (co2 >= 6.8) {
            level = Level.L3;
            recommendation = "Follow the CO2 contingency procedure now. CMO and ground informed.";
        } else if (co2 >= 4.5 || (headache && co2 > 3)) {
            level = Level.L2;
            recommendation = "CMO reviews symptoms (headache). Check CO2 removal and ventilation; ground informed.";
        } else if (co2 > 3) {
            level = Level.L1;
            recommendation = "Check CO2 removal and ventilation in the module; report headache if it starts.";
        } else {
            return List.of();
        }
        String explanation = "Cabin CO2 " + co2 + " mmHg (1-hour average)" + (headache ? "; a crew member reports a headache." : ".");
        List<Alert> out = new ArrayList<>();
        out.add(Alert.open(id() + ":" + Alert.STATION, Alert.STATION, level, "Cabin CO2 above limit", explanation,
                recommendation, EVIDENCE, SOURCE, DataKind.ENVIRONMENT, ctx.now()));
        return out;
    }
}

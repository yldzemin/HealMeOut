package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.domain.Reading;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Hypoglycemia: <70 mg/dL → L1, <54 → L3, with reported confusion → L4 (BACKEND §4). */
public final class GlucoseRule implements Rule {
    static final String EVIDENCE = "ADA 2025: level 1 hypoglycemia is <70 mg/dL, level 2 is <54 mg/dL, level 3 needs "
            + "another person's help. Team assumption (#30): in-flight glucose measurement is assumed; not verified "
            + "for the ISS.";
    static final String SOURCE = "ADA Standards of Care 2025, doi:10.2337/dc25-S006";

    @Override
    public String id() {
        return "glucose";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            Optional<Reading> reading = ctx.latest(m.id(), "glucose");
            if (reading.isEmpty() || reading.get().value() >= 70) {
                continue;
            }
            double v = reading.get().value();
            boolean confused = ctx.reports(m.id(), "symptom_confusion");
            Level level = confused ? Level.L4 : v < 54 ? Level.L3 : Level.L1;
            String title = confused ? "Low blood sugar with confusion" : "Low blood sugar";
            String recommendation = switch (level) {
                case L4 -> "CMO acts now per protocol; another crew member stays with them. Ground informed, not awaited.";
                case L3 -> "Take 15 g of fast-acting carbohydrate now and re-check in 15 minutes. CMO and ground informed.";
                default -> "Take 15 g of fast-acting carbohydrate and re-check in 15 minutes (15-15 rule).";
            };
            out.add(Alert.open(id() + ":" + m.id(), m.id(), level, title,
                    "Glucose " + fmt(v) + " mg/dL is below 70 mg/dL.", recommendation, EVIDENCE, SOURCE,
                    DataKind.VITALS, ctx.now()));
        }
        return out;
    }

    static String fmt(double v) {
        return v == Math.rint(v) ? Long.toString((long) v) : Double.toString(v);
    }
}

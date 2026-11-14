package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/** SpO2 below 90% → L4 (BACKEND §4). */
public final class SpO2Rule implements Rule {
    static final String EVIDENCE = "SpO2 below 90% is hypoxaemia. Normal on the ISS is 96–99% (cabin 14.7 psia, "
            + "21% O2). NASA's own limit is inspired O2 pressure ≥122 mmHg.";
    static final String SOURCE = "ARASTIRMA_RAPORU.md §4; OCHMO-TB-003";

    @Override
    public String id() {
        return "spo2";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            ctx.latest(m.id(), "spo2").filter(r -> r.value() < 90).ifPresent(r -> out.add(Alert.open(
                    id() + ":" + m.id(), m.id(), Level.L4, "Low blood oxygen",
                    "SpO2 " + GlucoseRule.fmt(r.value()) + "% is below 90%.",
                    "CMO now: check the sensor, give oxygen per protocol, check cabin pressure and O2. "
                            + "Ground informed, not awaited.",
                    EVIDENCE, SOURCE, DataKind.VITALS, ctx.now())));
        }
        return out;
    }
}

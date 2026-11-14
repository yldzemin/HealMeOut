package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/** Wristband collapse detection → L4 emergency broadcast (chest-pain scenario, BACKEND §5). */
public final class CollapseRule implements Rule {
    static final String EVIDENCE = "Concept: wristband collapse detection and crew location are simulated in the app; "
            + "no such ISS device is assumed. CPR steps: Hinkelbein et al. 2020.";
    static final String SOURCE = "docs/PITCH.md slide 4; Hinkelbein 2020, doi:10.1186/s13049-020-00793-y";

    @Override
    public String id() {
        return "collapse";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            if (ctx.reports(m.id(), "collapse_detected")) {
                out.add(Alert.open(id() + ":" + m.id(), m.id(), Level.L4,
                        m.callsign() + " (" + m.id() + ") needs help — " + m.module(),
                        "Wristband pattern indicates a collapse (simulated).",
                        "Emergency: CMO-1 leads, CMO-2 assists, one crew member brings the life-support pack and "
                                + "defibrillator, one handles comms and log. Ground informed, not awaited.",
                        EVIDENCE, SOURCE, DataKind.EMERGENCY, ctx.now()));
            }
        }
        return out;
    }
}

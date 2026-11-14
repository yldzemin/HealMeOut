package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.BaselineStats;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.domain.MissionTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** |z| ≥ 2 against the personal pre-flight baseline → L1 (BACKEND §4; our design). */
public final class ZScoreRule implements Rule {
    @Override
    public String id() {
        return "z-score";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            for (BaselineStats s : ctx.baselines().forCrew(m.id())) {
                ctx.latest(m.id(), s.analyte()).ifPresent(r -> ctx.baselines().zScore(m.id(), s.analyte(), r.value())
                        .filter(z -> Math.abs(z) >= 2)
                        .ifPresent(z -> out.add(Alert.open(id() + ":" + m.id() + ":" + s.analyte(), m.id(), Level.L1,
                                s.analyte() + " differs from personal baseline",
                                String.format(Locale.ROOT, "%s %s %s is %.1f SD from the pre-flight mean %s.",
                                        s.analyte(), GlucoseRule.fmt(r.value()), s.unit(), z, GlucoseRule.fmt(s.mean())),
                                "Re-measure; mention it to the CMO if it stays outside your usual range.",
                                evidence(s), s.source() + " (NASA OSDR)", DataKind.VITALS, ctx.now()))));
            }
        }
        return out;
    }

    private static String evidence(BaselineStats s) {
        MissionTag t = s.mission();
        String mission = t == null ? "" : String.format(" Baseline data: %s mission %s, %s to %s, %s, hosted by %s.",
                t.kind(), t.name(), t.start(), t.end(), t.orbit(), t.hostedBy());
        return "Our design: |z| ≥ 2 against the personal pre-flight baseline (L-92, L-44, L-3; n=" + s.n() + ")."
                + " No universal in-flight threshold exists for this value." + mission;
    }
}

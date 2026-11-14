package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.CheckIn;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent low check-ins → a private L1 prompt to raise it at the next PPC (BACKEND §4). Category
 * PSYCH, so only the astronaut sees it, and it never changes the crew level shown on the Crew Deck.
 * Thresholds are our design: NASA has no public numeric escalation criteria.
 */
public final class PsychTrendRule implements Rule {
    static final int RUN = 3;
    static final int LOW_MOOD = 40;
    static final int HIGH_STRESS = 70;
    static final String EVIDENCE = "Our design: " + RUN + " check-ins in a row with mood ≤ " + LOW_MOOD
            + "/100 or stress ≥ " + HIGH_STRESS + "/100. No public NASA numeric escalation criteria exist. "
            + "Wording follows PSYCHOLOGY_AND_LONG_DURATION.md §4.4 and is never diagnostic.";
    static final String SOURCE = "PSYCHOLOGY_AND_LONG_DURATION.md §4.4";

    @Override
    public String id() {
        return "psych-trend";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        List<Alert> out = new ArrayList<>();
        for (CrewMember m : ctx.crew()) {
            List<CheckIn> list = ctx.checkIns().getOrDefault(m.id(), List.of());
            if (list.size() < RUN) {
                continue;
            }
            List<CheckIn> last = list.subList(list.size() - RUN, list.size());
            if (last.stream().allMatch(c -> c.mood() <= LOW_MOOD || c.stress() >= HIGH_STRESS)) {
                out.add(Alert.open(id() + ":" + m.id(), m.id(), Level.L1,
                        "Your last " + RUN + " check-ins were low",
                        "Mood or stress has been in a hard range for " + RUN + " check-ins in a row.",
                        "Discuss it at your next Private Psychological Conference, or contact your flight surgeon now.",
                        EVIDENCE, SOURCE, DataKind.PSYCH, ctx.now()));
            }
        }
        return out;
    }
}

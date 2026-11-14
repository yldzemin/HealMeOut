package astratwin.scenario;

import java.util.List;
import java.util.Map;

/** Where the running scenario is; step is 1-based. */
public record ScenarioState(
        String id, String title, int step, int total, String at, String stepTitle, String detail,
        Map<String, String> roles, List<String> checklist, String banner, String evidence,
        List<String> log, boolean finished) {
}

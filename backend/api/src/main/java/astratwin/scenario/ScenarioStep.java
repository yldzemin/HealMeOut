package astratwin.scenario;

import astratwin.domain.Environment;
import astratwin.domain.Reading;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One step of a scenario: data it adds (readings, environment) and what it shows (roles,
 * checklist, banner). Readings and environment samples without a timestamp get "now" when applied.
 */
public record ScenarioStep(
        String at, String title, String detail, List<Reading> readings, List<Environment> environment,
        Map<String, String> roles, List<String> checklist, String banner, String evidence) {

    public static Builder at(String at, String title) {
        return new Builder(at, title);
    }

    public static final class Builder {
        private final String at;
        private final String title;
        private String detail = "";
        private final List<Reading> readings = new ArrayList<>();
        private final List<Environment> environment = new ArrayList<>();
        private final Map<String, String> roles = new LinkedHashMap<>();
        private final List<String> checklist = new ArrayList<>();
        private String banner;
        private String evidence;

        private Builder(String at, String title) {
            this.at = at;
            this.title = title;
        }

        public Builder detail(String text) {
            this.detail = text;
            return this;
        }

        /** A simulated reading; every scenario value is simulated unless it comes from data/cache. */
        public Builder reading(String crewId, String metric, double value, String unit, String measuredHow, String note) {
            readings.add(new Reading(crewId, metric, value, unit, null, "simulated", measuredHow, note));
            return this;
        }

        public Builder co2(double mmHg) {
            environment.add(Environment.co2(mmHg, null, "simulated"));
            return this;
        }

        public Builder environment(List<Environment> samples) {
            environment.addAll(samples);
            return this;
        }

        public Builder role(String task, String who) {
            roles.put(task, who);
            return this;
        }

        public Builder check(String item) {
            checklist.add(item);
            return this;
        }

        public Builder banner(String text) {
            this.banner = text;
            return this;
        }

        public Builder evidence(String text) {
            this.evidence = text;
            return this;
        }

        public ScenarioStep build() {
            return new ScenarioStep(at, title, detail, List.copyOf(readings), List.copyOf(environment),
                    Map.copyOf(roles), List.copyOf(checklist), banner, evidence);
        }
    }
}

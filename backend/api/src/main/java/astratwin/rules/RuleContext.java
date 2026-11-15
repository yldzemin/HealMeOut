package astratwin.rules;

import astratwin.domain.CheckIn;
import astratwin.domain.ConditionReport;
import astratwin.domain.CrewMember;
import astratwin.domain.Environment;
import astratwin.domain.Reading;
import astratwin.store.ReadingRepository;
import astratwin.twin.BaselineStatistics;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** What a rule can see when it runs. */
public record RuleContext(
        List<CrewMember> crew, ReadingRepository readings, List<Environment> environment,
        BaselineStatistics baselines, Map<String, List<CheckIn>> checkIns, List<ConditionReport> conditions,
        Instant now) {

    public Optional<Reading> latest(String crewId, String metric) {
        return readings.latest(crewId, metric);
    }

    /** True if the latest reading of a yes/no symptom is "yes" (value ≥ 1). */
    public boolean reports(String crewId, String symptom) {
        return latest(crewId, symptom).map(r -> r.value() >= 1).orElse(false);
    }
}

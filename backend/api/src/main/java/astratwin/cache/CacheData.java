package astratwin.cache;

import astratwin.domain.Baseline;
import astratwin.domain.ConditionProbability;
import astratwin.domain.Environment;
import java.util.List;

/** Everything read from data/cache/ at start-up. */
public record CacheData(
        List<Baseline> baselines, List<Environment> issDose, List<ConditionProbability> imm,
        String baselineSource, String doseSource, String immSource) {
}

package astratwin.cache;

import astratwin.domain.Baseline;
import astratwin.domain.ConditionProbability;
import astratwin.domain.Environment;
import java.util.List;

/**
 * Everything read from data/cache/ at start-up. {@code partnerDose} is the DLR DosTel2 series from
 * NASA OSDR RadLab (#13); empty if that optional file is missing.
 */
public record CacheData(
        List<Baseline> baselines, List<Environment> issDose, List<ConditionProbability> imm,
        String baselineSource, String doseSource, String immSource,
        List<Environment> partnerDose, String partnerDoseSource) {
}

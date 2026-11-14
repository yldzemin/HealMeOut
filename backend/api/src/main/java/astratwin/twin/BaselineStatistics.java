package astratwin.twin;

import astratwin.domain.Baseline;
import astratwin.domain.BaselineStats;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Personal baselines (BACKEND §3.1): mean and SD of each analyte's pre-flight samples. */
public final class BaselineStatistics {
    private final Map<String, BaselineStats> byKey = new LinkedHashMap<>();

    public BaselineStatistics(List<Baseline> baselines) {
        Map<String, List<Baseline>> groups = new LinkedHashMap<>();
        for (Baseline b : baselines) {
            if (b.preFlight()) {
                groups.computeIfAbsent(key(b.crewId(), b.analyte()), k -> new ArrayList<>()).add(b);
            }
        }
        groups.forEach((key, group) -> byKey.put(key, summarize(group)));
    }

    private static BaselineStats summarize(List<Baseline> group) {
        int n = group.size();
        double mean = group.stream().mapToDouble(Baseline::value).average().orElse(Double.NaN);
        Double sd = null;
        if (n >= 2) {
            double ss = group.stream().mapToDouble(b -> (b.value() - mean) * (b.value() - mean)).sum();
            sd = Math.sqrt(ss / (n - 1));
        }
        Baseline last = group.get(n - 1);
        return new BaselineStats(last.crewId(), last.analyte(), last.unit(), round(mean), sd == null ? null : round(sd),
                n, last.refLow(), last.refHigh(), last.source(), last.mission());
    }

    public Optional<BaselineStats> stats(String crewId, String analyte) {
        return Optional.ofNullable(byKey.get(key(crewId, analyte)));
    }

    public List<BaselineStats> forCrew(String crewId) {
        return byKey.values().stream().filter(s -> s.crewId().equals(crewId)).toList();
    }

    /**
     * z = (value − mean) / SD of the personal baseline. When the SD is missing or zero, the
     * reference range is read as mean ± 2 SD (our design, labelled in the alert evidence).
     */
    public Optional<Double> zScore(String crewId, String analyte, double value) {
        return stats(crewId, analyte).flatMap(s -> {
            if (s.sd() != null && s.sd() > 0) {
                return Optional.of((value - s.mean()) / s.sd());
            }
            if (s.refLow() != null && s.refHigh() != null && s.refHigh() > s.refLow()) {
                double mid = (s.refLow() + s.refHigh()) / 2;
                return Optional.of((value - mid) / ((s.refHigh() - s.refLow()) / 4));
            }
            return Optional.empty();
        });
    }

    private static String key(String crewId, String analyte) {
        return crewId + "|" + analyte;
    }

    private static double round(double v) {
        return Math.round(v * 1000) / 1000.0;
    }
}

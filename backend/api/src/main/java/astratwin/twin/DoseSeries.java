package astratwin.twin;

import astratwin.domain.Environment;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Hourly ISS dose-equivalent series (μSv/h, ICRP-60, at the instrument; not effective dose).
 * Shared by the dose-rate rule and the cumulative-dose forecast.
 */
public final class DoseSeries {
    private static final Duration DAY = Duration.ofHours(24);
    private final List<Environment> hourly;

    public DoseSeries(List<Environment> environment) {
        this.hourly = environment.stream()
                .filter(e -> e.doseEqRate_uSv_h() != null && e.timestamp() != null)
                .sorted(Comparator.comparing(Environment::timestamp))
                .toList();
    }

    public boolean isEmpty() {
        return hourly.isEmpty();
    }

    public Instant start() {
        return hourly.get(0).timestamp();
    }

    public Instant end() {
        return hourly.get(hourly.size() - 1).timestamp();
    }

    /** Mean dose-equivalent rate of the samples in (from, to]. */
    public Optional<Double> mean(Instant from, Instant to) {
        double sum = 0;
        int n = 0;
        for (Environment e : hourly) {
            if (e.timestamp().isAfter(from) && !e.timestamp().isAfter(to)) {
                sum += e.doseEqRate_uSv_h();
                n++;
            }
        }
        return n == 0 ? Optional.empty() : Optional.of(sum / n);
    }

    /** Mean of the last 24 h, in μSv/h. */
    public Optional<Double> last24hMean() {
        return isEmpty() ? Optional.empty() : mean(end().minus(DAY), end());
    }

    /**
     * Last 24-h mean divided by the median of the 7 daily means before it (BACKEND §4, #30).
     * Daily means are used because single hours include South Atlantic Anomaly spikes.
     * Needs at least 4 earlier days.
     */
    public Optional<Double> ratioToWeeklyMedian() {
        if (isEmpty()) {
            return Optional.empty();
        }
        Instant end = end();
        List<Double> daily = new ArrayList<>();
        for (int k = 1; k <= 7; k++) {
            mean(end.minus(DAY.multipliedBy(k + 1)), end.minus(DAY.multipliedBy(k))).ifPresent(daily::add);
        }
        if (daily.size() < 4) {
            return Optional.empty();
        }
        daily.sort(Comparator.naturalOrder());
        int m = daily.size();
        double median = m % 2 == 1 ? daily.get(m / 2) : (daily.get(m / 2 - 1) + daily.get(m / 2)) / 2;
        return last24hMean().map(current -> current / median);
    }

    /** Sum of the hourly samples (each counts as one hour), in mSv. */
    public double cumulative_mSv() {
        return hourly.stream().mapToDouble(Environment::doseEqRate_uSv_h).sum() / 1000.0;
    }
}

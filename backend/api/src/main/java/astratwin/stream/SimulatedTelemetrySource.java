package astratwin.stream;

import astratwin.domain.Reading;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Simulated wearable telemetry (heart rate, SpO2, temperature) in the style of the CSA Bio-Monitor
 * (TECH_OVERVIEW §4.3). Each value is a mean-reverting random walk around a target; the targets
 * start at the nominal readings and follow any scenario or manual reading of the same metric.
 * Seeded, so a run is reproducible. Every row is labelled {@code simulated}.
 */
public final class SimulatedTelemetrySource implements TelemetrySource {
    /** metric → unit, step SD, decimals, lower bound, upper bound. */
    private record Spec(String unit, double sd, int decimals, double min, double max) {
    }

    static final Map<String, Spec> METRICS = Map.of(
            "hr", new Spec("bpm", 1.2, 0, 30, 220),
            "spo2", new Spec("%", 0.3, 0, 50, 100),
            "temp_c", new Spec("°C", 0.02, 1, 34, 42));
    private static final double PULL = 0.3;

    private final Random random;
    private final Map<String, Double> target = new LinkedHashMap<>();
    private final Map<String, Double> current = new LinkedHashMap<>();

    public SimulatedTelemetrySource(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public String name() {
        return "simulated";
    }

    @Override
    public synchronized void observe(Reading reading) {
        if (METRICS.containsKey(reading.metric())) {
            String key = reading.crewId() + "|" + reading.metric();
            target.put(key, reading.value());
            current.put(key, reading.value());
        }
    }

    @Override
    public synchronized Optional<List<TelemetryRow>> nextTick(Instant now) {
        List<TelemetryRow> rows = new ArrayList<>();
        for (Map.Entry<String, Double> e : target.entrySet()) {
            String[] key = e.getKey().split("\\|", 2);
            Spec spec = METRICS.get(key[1]);
            double prev = current.getOrDefault(e.getKey(), e.getValue());
            double next = prev + PULL * (e.getValue() - prev) + random.nextGaussian() * spec.sd();
            next = Math.max(spec.min(), Math.min(spec.max(), next));
            current.put(e.getKey(), next);
            double factor = Math.pow(10, spec.decimals());
            rows.add(new TelemetryRow(now, key[0], key[1], Math.round(next * factor) / factor, spec.unit(),
                    "simulated", "live"));
        }
        return Optional.of(rows);
    }
}

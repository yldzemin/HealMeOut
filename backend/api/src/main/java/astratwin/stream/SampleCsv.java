package astratwin.stream;

import astratwin.domain.Reading;
import astratwin.seed.CrewRoster;
import astratwin.twin.BaselineStatistics;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes {@code data/stream/sample_telemetry.csv}: 20 one-minute ticks of simulated telemetry for the
 * 7 crew members, with a simulated SpO2 drop for C5 from minute 12 so a replay shows a rule firing.
 * Usage: {@code java -cp out/classes astratwin.stream.SampleCsv ../../data/stream/sample_telemetry.csv}
 */
public final class SampleCsv {
    static final Instant START = Instant.parse("2026-11-15T08:00:00Z");
    static final double[] C5_SPO2 = {96, 94, 92, 90, 88, 88, 88, 88};

    private SampleCsv() {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "../../data/stream/sample_telemetry.csv");
        SimulatedTelemetrySource sim = new SimulatedTelemetrySource(20261115L);
        new CrewRoster(new BaselineStatistics(List.of())).nominalReadings(START).forEach(sim::observe);
        List<String> lines = new ArrayList<>();
        lines.add(TelemetryRow.HEADER);
        for (int minute = 0; minute < 20; minute++) {
            if (minute >= 12) {
                double spo2 = C5_SPO2[minute - 12];
                sim.observe(new Reading("C5", "spo2", spo2, "%", START, "simulated", "live", "sample event"));
            }
            for (TelemetryRow row : sim.nextTick(START.plus(Duration.ofMinutes(minute))).orElseThrow()) {
                lines.add(row.toCsv());
            }
        }
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.write(out, lines, StandardCharsets.UTF_8);
        System.out.println("wrote " + (lines.size() - 1) + " rows to " + out);
    }
}

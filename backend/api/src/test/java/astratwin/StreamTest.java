package astratwin;

import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.domain.Reading;
import astratwin.domain.Viewer;
import astratwin.privacy.ViewRequest;
import astratwin.store.InMemoryReadingRepository;
import astratwin.stream.CsvReplaySource;
import astratwin.stream.SimulatedTelemetrySource;
import astratwin.stream.TelemetryRow;
import astratwin.stream.TelemetryStreamer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

final class StreamTest {
    private StreamTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        Path sample = cache.getParent().resolve("stream").resolve("sample_telemetry.csv");
        tests.put("stream: CSV row round trip, including a quoted field", () -> {
            TelemetryRow row = new TelemetryRow(Instant.parse("2026-11-15T08:00:00Z"), "C1", "temp_c", 37.4, "°C",
                    "simulated, test", "live");
            Check.equal("2026-11-15T08:00:00Z,C1,temp_c,37.4,°C,\"simulated, test\",live", row.toCsv(), "csv");
            Check.equal(row, TelemetryRow.parse(row.toCsv()), "round trip");
        });
        tests.put("stream: sample CSV replays as 20 ticks of 21 rows, then finishes", () -> {
            CsvReplaySource replay = new CsvReplaySource(sample);
            Check.equal(20, replay.size(), "ticks");
            int rows = 0;
            for (int i = 0; i < 20; i++) {
                rows += replay.nextTick(Instant.now()).orElseThrow().size();
            }
            Check.equal(420, rows, "rows");
            Check.isTrue(replay.nextTick(Instant.now()).isEmpty(), "finished");
        });
        tests.put("stream: replaying the sample through the station fires C5's SpO2 alert and logs a CSV", () -> {
            Station station = Station.load(cache);
            Path logDir = Files.createTempDirectory("astra-stream");
            try (TelemetryStreamer streamer = new TelemetryStreamer(station, logDir)) {
                streamer.start(new CsvReplaySource(sample), TelemetryStreamer.MAX_INTERVAL_MS);
                for (int i = 0; i < 25 && streamer.status().running(); i++) {
                    streamer.tick();
                }
                TelemetryStreamer.StreamStatus status = streamer.status();
                Check.isTrue(status.finished() && !status.running(), "finished: " + status);
                Check.equal(420L, status.rows(), "rows streamed");
                Check.equal(Level.L4, station.member("C5").orElseThrow().level(), "C5 level after SpO2 88");
                Check.equal(421, Files.readAllLines(Path.of(status.logFile())).size(), "log lines incl. header");
            }
        });
        tests.put("stream: simulator follows a manual reading instead of overwriting it", () -> {
            SimulatedTelemetrySource sim = new SimulatedTelemetrySource(1);
            sim.observe(new Reading("C1", "spo2", 98, "%", Instant.now(), "simulated", "live", null));
            sim.observe(new Reading("C1", "spo2", 88, "%", Instant.now(), "simulated", "live", null));
            for (int i = 0; i < 30; i++) {
                for (TelemetryRow r : sim.nextTick(Instant.now()).orElseThrow()) {
                    Check.isTrue(Math.abs(r.value() - 88) <= 2, "spo2 stays near 88: " + r.value());
                }
            }
        });
        tests.put("stream: a SELF subscriber receives only its own rows", () -> {
            Station station = Station.load(cache);
            try (TelemetryStreamer streamer = new TelemetryStreamer(station, Files.createTempDirectory("astra-stream"))) {
                streamer.start(new SimulatedTelemetrySource(7), TelemetryStreamer.MAX_INTERVAL_MS);
                ViewRequest self = new ViewRequest(Viewer.SELF, "C3");
                streamer.tick();
                List<TelemetryRow> seen = streamer.recent(500, r -> station.allows(DataKind.VITALS, self, r.crewId()));
                Check.isTrue(!seen.isEmpty(), "C3 sees its own rows");
                Check.isTrue(seen.stream().allMatch(r -> r.crewId().equals("C3")), "only C3 rows");
            }
        });
        tests.put("store: bounded history keeps the latest value of evicted metrics", () -> {
            InMemoryReadingRepository repo = new InMemoryReadingRepository(3);
            repo.add(new Reading("C1", "glucose", 90, "mg/dL", Instant.now(), "simulated", "periodic", null));
            for (int i = 0; i < 5; i++) {
                repo.add(new Reading("C1", "hr", 60 + i, "bpm", Instant.now(), "simulated", "live", null));
            }
            Check.equal(3, repo.all().size(), "history size");
            Check.equal(90.0, repo.latest("C1", "glucose").orElseThrow().value(), "latest glucose survives");
            Check.equal(64.0, repo.latest("C1", "hr").orElseThrow().value(), "latest hr");
        });
    }
}

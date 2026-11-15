package astratwin;

import astratwin.domain.CheckIn;
import astratwin.domain.Forecast;
import astratwin.domain.Viewer;
import astratwin.privacy.ViewRequest;
import astratwin.twin.SleepDebtModel;
import astratwin.twin.TwinContext;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Earth Twin forecasts (BACKEND §3.2) and who may see them (§6). */
final class TwinTest {
    private TwinTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("twin: scheduled sleep period is 21:30–06:00 GMT", () -> {
            Instant evening = Instant.parse("2026-11-15T20:00:00Z");
            Check.equal(4.5, SleepDebtModel.scheduledSleepHours(evening, evening.plusSeconds(6 * 3600)), "20:00 +6 h");
            Check.equal(8.5, SleepDebtModel.scheduledSleepHours(evening, evening.plusSeconds(12 * 3600)), "20:00 +12 h");
            Instant morning = Instant.parse("2026-11-15T08:00:00Z");
            Check.equal(0.0, SleepDebtModel.scheduledSleepHours(morning, morning.plusSeconds(6 * 3600)), "08:00 +6 h");
            Instant night = Instant.parse("2026-11-15T03:00:00Z");
            Check.equal(3.0, SleepDebtModel.scheduledSleepHours(night, night.plusSeconds(6 * 3600)), "03:00 +6 h");
        });
        tests.put("twin: sleep debt sums the last 7 check-ins and projects the recent average", () -> {
            Station station = Station.load(cache);
            List<CheckIn> checkIns = new ArrayList<>();
            for (int day = 1; day <= 8; day++) {
                // The first night is outside the 7-night window: its 0 h must not count.
                checkIns.add(new CheckIn("C5", "2026-11-0" + day, 60, 40, 40, 10, day == 1 ? 0 : 6.5, "test"));
            }
            Instant evening = Instant.parse("2026-11-15T20:00:00Z");
            Forecast f = new SleepDebtModel().forecast(new TwinContext(List.of(), List.of(), checkIns, evening),
                    station.member("C5").orElseThrow(), 12).orElseThrow();
            Check.equal("sleep_debt", f.metric(), "metric");
            Check.equal("modelled", f.label(), "label");
            Check.equal(14.0, f.points().get(0).value(), "T+0: 7 nights × (8.5 − 6.5) h");
            Check.equal(16.0, f.points().get(1).value(), "T+12: one full night at 6.5 h adds 2 h");
        });
        tests.put("privacy: only the astronaut sees the sleep-debt forecast", () -> {
            Station station = Station.load(cache);
            List<Forecast> self = station.forecasts("C3", new ViewRequest(Viewer.SELF, "C3"));
            Check.isTrue(self.stream().anyMatch(f -> f.metric().equals("sleep_debt")), "SELF must see its sleep debt");
            Check.isTrue(self.stream().anyMatch(f -> f.metric().equals("cumulative_dose")), "SELF must see its dose");
            Check.equal(16.4, self.stream().filter(f -> f.metric().equals("sleep_debt")).findFirst().orElseThrow()
                    .points().get(0).value(), "C3 T+0: 7 seeded nights, the same as the Personal sleep chart");
            for (Viewer viewer : new Viewer[] {Viewer.CMO, Viewer.GROUND, Viewer.EMERGENCY}) {
                List<Forecast> other = station.forecasts("C3", new ViewRequest(viewer, null));
                Check.isTrue(other.stream().noneMatch(f -> f.metric().equals("sleep_debt")), viewer + " sees sleep debt");
                Check.isTrue(other.stream().anyMatch(f -> f.metric().equals("cumulative_dose")), viewer + " lost the dose");
            }
        });
    }
}

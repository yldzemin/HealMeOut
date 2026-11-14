package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.Level;
import astratwin.domain.Viewer;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import astratwin.scenario.ScenarioState;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

final class ScenarioTest {
    private ScenarioTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("nominal station has no alerts (vitals sit on the personal baselines)", () -> {
            StationView view = Station.load(cache).view(new ViewRequest(Viewer.CMO, null));
            Check.equal(Level.L0, view.stationLevel(), "station level");
            Check.equal(0, view.alerts().size(), "alerts: " + view.alerts());
        });
        tests.put("spe-iss: dose-rate alert fires on Sep 11 with real data and clears on Sep 12", () -> {
            Station station = Station.load(cache);
            station.startScenario("spe-iss");                         // Sep 9
            Check.isTrue(doseAlert(station).isEmpty(), "no alert on Sep 9");
            station.step();                                            // Sep 10
            Check.isTrue(doseAlert(station).isEmpty(), "no alert on Sep 10");
            station.step();                                            // Sep 11, 00–11
            station.step();                                            // Sep 11, 12–23
            Alert alert = doseAlert(station).orElseThrow(() -> new AssertionError("no dose alert after Sep 11"));
            Check.isTrue(alert.active() && alert.level() == Level.L2, "active L2: " + alert);
            station.step();                                            // Sep 12
            Check.isTrue(!doseAlert(station).orElseThrow().active(), "alert must be inactive on Sep 12");
        });
        tests.put("chest-pain: patient goes to L4, roles and checklist accumulate", () -> {
            Station station = Station.load(cache);
            station.startScenario("chest-pain");
            ScenarioState s = null;
            for (int i = 0; i < 5; i++) {
                s = station.step();
            }
            Check.isTrue(s.finished(), "finished after 6 steps");
            Check.equal(4, s.roles().size(), "roles");
            Check.equal(3, s.checklist().size(), "checklist");
            Check.equal(Level.L4, station.member("C2").orElseThrow().level(), "patient level (C2, as in the frontend)");
        });
        tests.put("co2: L1 → L2 (headache) → L3 → inactive", () -> {
            Station station = Station.load(cache);
            station.startScenario("co2");
            Level[] expected = {Level.L1, Level.L2, Level.L2, Level.L3};
            for (Level level : expected) {
                station.step();
                Check.equal(level, co2Alert(station).orElseThrow().level(), "co2 level");
            }
            station.step();
            Check.isTrue(!co2Alert(station).orElseThrow().active(), "inactive after recovery");
        });
    }

    private static Optional<Alert> doseAlert(Station station) {
        return find(station, "dose-rate:STATION");
    }

    private static Optional<Alert> co2Alert(Station station) {
        return find(station, "co2:STATION");
    }

    private static Optional<Alert> find(Station station, String id) {
        return station.view(new ViewRequest(Viewer.CMO, null)).alerts().stream().filter(a -> a.id().equals(id)).findFirst();
    }
}

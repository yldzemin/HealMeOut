package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.domain.Viewer;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import astratwin.scenario.ScenarioState;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ScenarioTest {
    private ScenarioTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("nominal station: only the crew's reported conditions; no rule fires (vitals sit on the baselines)", () -> {
            StationView view = Station.load(cache).view(new ViewRequest(Viewer.CMO, null));
            Check.equal(Level.L2, view.stationLevel(), "station level (C4's motion sickness)");
            Check.equal(List.of("condition:C2:head", "condition:C3:head", "condition:C4:head", "condition:C5:whole",
                    "condition:C6:musculo"), view.alerts().stream().map(Alert::id).toList(), "alerts");
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
        tests.put("bad-news: private to C3; CMO and ground see the plan; Crew Deck only 'reduced tasking'", () -> {
            Station station = Station.load(cache);
            ViewRequest deck = new ViewRequest(Viewer.CREW_DECK, null);
            ViewRequest self = new ViewRequest(Viewer.SELF, "C3");
            ViewRequest ground = new ViewRequest(Viewer.GROUND, null);
            ViewRequest cmo = new ViewRequest(Viewer.CMO, null);
            Level c3Level = station.member("C3").orElseThrow().level();
            station.startScenario("bad-news");
            for (int step = 1; step <= 6; step++) {
                if (step > 1) {
                    station.step();
                }
                for (ViewRequest other : List.of(deck, cmo, ground, new ViewRequest(Viewer.SELF, "C2"),
                        new ViewRequest(Viewer.EMERGENCY, null))) {
                    StationView v = station.view(other);
                    ScenarioState s = v.scenario();
                    String shown = (s.title() + " " + s.stepTitle() + " " + s.detail() + " " + s.log()).toLowerCase();
                    for (String secret : List.of("family", "died", "home", "night", "mood", "bad news")) {
                        Check.isTrue(!shown.contains(secret), other.viewer() + " step " + step + " sees '" + secret + "': " + shown);
                    }
                    Check.isTrue(s.evidence() == null && s.checklist().isEmpty(), "no evidence or checklist for " + other.viewer());
                    Check.isTrue(v.alerts().stream().noneMatch(a -> a.category() == DataKind.PSYCH), other.viewer() + " sees psych");
                }
                Check.equal("Bad news from home", station.view(self).scenario().title(), "C3 sees the full scenario");
                Check.equal(c3Level, station.member("C3").orElseThrow().level(), "C3's level is unchanged at step " + step);
                Check.isTrue(station.view(deck).alerts().stream()
                        .noneMatch(a -> a.category() == DataKind.PLAN), "the Crew Deck sees the plan at step " + step);
                switch (step) {
                    case 1 -> Check.isTrue(active(station, self, "family-event:C3"), "C3's private support steps");
                    case 2 -> Check.equal(28, station.view(self).crew().get(2).checkIns().getLast().mood(), "C3's check-in");
                    case 3 -> {
                        Alert t = station.view(deck).alerts().stream().filter(a -> a.id().equals("tasking:C3")).findFirst()
                                .orElseThrow(() -> new AssertionError("the Crew Deck must see the reduced tasking"));
                        Check.equal("No reason is shared.", t.explanation(), "no reason");
                    }
                    case 4 -> {
                        Check.isTrue(active(station, ground, "plan:C3") && active(station, cmo, "plan:C3"), "ground and CMO see the plan");
                        Check.isTrue(active(station, self, "plan:C3"), "C3 sees the plan");
                        station.setAlertStatus("plan:C3", astratwin.domain.AlertStatus.APPROVED, ground);
                    }
                    case 6 -> {
                        Check.isTrue(!active(station, deck, "tasking:C3"), "reduced tasking ends");
                        Check.isTrue(!active(station, ground, "plan:C3"), "plan ends");
                        Check.isTrue(active(station, self, "family-event:C3"), "private support steps stay");
                        Check.isTrue(station.view(self).scenario().finished(), "finished");
                    }
                    default -> {
                    }
                }
            }
        });
    }

    private static boolean active(Station station, ViewRequest viewer, String id) {
        return station.view(viewer).alerts().stream().anyMatch(a -> a.id().equals(id) && a.active());
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

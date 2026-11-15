package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.CrewMember;
import astratwin.domain.Environment;
import astratwin.domain.Level;
import astratwin.domain.Reading;
import astratwin.domain.Role;
import astratwin.rules.Co2Rule;
import astratwin.rules.GlucoseRule;
import astratwin.rules.RuleContext;
import astratwin.rules.SpO2Rule;
import astratwin.store.InMemoryReadingRepository;
import astratwin.twin.BaselineStatistics;
import java.time.Instant;
import java.util.List;
import java.util.Map;

final class RulesTest {
    private static final CrewMember C1 = new CrewMember("C1", "Alpha", Role.CMO1, "Lab", Level.L0, "test");

    private RulesTest() {
    }

    static void register(Map<String, TestRunner.Test> tests) {
        tests.put("glucose: 70 no alert, 65 → L1, 50 → L3, 65 with confusion → L4", () -> {
            Check.equal(List.of(), new GlucoseRule().evaluate(ctx(r("glucose", 70))), "70 mg/dL");
            Check.equal(Level.L1, only(new GlucoseRule().evaluate(ctx(r("glucose", 65)))).level(), "65 mg/dL");
            Check.equal(Level.L3, only(new GlucoseRule().evaluate(ctx(r("glucose", 50)))).level(), "50 mg/dL");
            Alert confused = only(new GlucoseRule().evaluate(ctx(r("glucose", 65), r("symptom_confusion", 1))));
            Check.equal(Level.L4, confused.level(), "confusion");
            Check.isTrue(confused.evidence().contains("Team assumption"), "glucose evidence must state the assumption");
        });
        tests.put("spo2: 90 no alert, 88 → L4", () -> {
            Check.equal(List.of(), new SpO2Rule().evaluate(ctx(r("spo2", 90))), "90%");
            Check.equal(Level.L4, only(new SpO2Rule().evaluate(ctx(r("spo2", 88)))).level(), "88%");
        });
        tests.put("co2: 3.0 none, 3.3 → L1, 4.5 → L2, 3.3 + headache → L2, 6.8 → L3", () -> {
            Check.equal(List.of(), new Co2Rule().evaluate(ctx(List.of(co2(3.0)))), "3.0");
            Check.equal(Level.L1, only(new Co2Rule().evaluate(ctx(List.of(co2(3.3))))).level(), "3.3");
            Check.equal(Level.L2, only(new Co2Rule().evaluate(ctx(List.of(co2(4.5))))).level(), "4.5");
            Check.equal(Level.L2, only(new Co2Rule().evaluate(ctx(List.of(co2(3.3)), r("symptom_headache", 1)))).level(),
                    "3.3 + headache");
            Check.equal(Level.L3, only(new Co2Rule().evaluate(ctx(List.of(co2(6.8))))).level(), "6.8");
        });
    }

    private static Alert only(List<Alert> alerts) {
        Check.equal(1, alerts.size(), "number of alerts");
        return alerts.get(0);
    }

    private static Reading r(String metric, double value) {
        return new Reading("C1", metric, value, "", Instant.now(), "simulated", "periodic", null);
    }

    private static Environment co2(double mmHg) {
        return Environment.co2(mmHg, Instant.now(), "simulated");
    }

    private static RuleContext ctx(Reading... readings) {
        return ctx(List.of(), readings);
    }

    private static RuleContext ctx(List<Environment> environment, Reading... readings) {
        InMemoryReadingRepository repo = new InMemoryReadingRepository();
        for (Reading reading : readings) {
            repo.add(reading);
        }
        return new RuleContext(List.of(C1), repo, environment, new BaselineStatistics(List.of()), Map.of(), List.of(),
                Instant.now());
    }
}

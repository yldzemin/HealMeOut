package astratwin.scenario;

import astratwin.domain.Environment;
import java.time.Instant;
import java.util.List;

/**
 * Target: replay of the real ISS TEPC dose data around the Sep 10, 2017 solar particle event.
 * The station starts with Sep 4–8 already loaded (see {@link #NOMINAL_END}).
 */
public final class SpeIssScenario implements Scenario {
    /** Dose data before this instant is part of the nominal station state. */
    public static final Instant NOMINAL_END = Instant.parse("2017-09-09T00:00:00Z");
    private final List<Environment> dose;

    public SpeIssScenario(List<Environment> dose) {
        this.dose = List.copyOf(dose);
    }

    @Override
    public String id() {
        return "spe-iss";
    }

    @Override
    public String title() {
        return "Solar particle event on the ISS, Sep 2017 (real data)";
    }

    @Override
    public String priority() {
        return "Target";
    }

    @Override
    public String source() {
        return "CDAWeb ISS_DOSANL_TEPC (NASA SRAG), hourly means";
    }

    @Override
    public List<ScenarioStep> steps() {
        return List.of(
                step("Sep 9", "Normal day", "2017-09-09T00:00:00Z", "2017-09-10T00:00:00Z",
                        "Normal ISS dose-equivalent rate. Hourly spikes are South Atlantic Anomaly passes."),
                step("Sep 10", "Normal day before the event", "2017-09-10T00:00:00Z", "2017-09-11T00:00:00Z",
                        "Still a normal daily mean."),
                step("Sep 11, 00–11 UTC", "Dose rising", "2017-09-11T00:00:00Z", "2017-09-11T12:00:00Z",
                        "Solar particles reach the ISS; the highest hours are around 07:00 and 09:00 UTC."),
                step("Sep 11, 12–23 UTC", "Elevated for a full day", "2017-09-11T12:00:00Z", "2017-09-12T00:00:00Z",
                        "Sep 11 averages 1.71 mSv/day, 1.90× the weekly median: the dose-rate rule fires."),
                step("Sep 12", "Decreasing", "2017-09-12T00:00:00Z", "2017-09-13T00:00:00Z",
                        "1.17× the weekly median; the alert becomes inactive."),
                step("Sep 13–19", "Back to normal", "2017-09-13T00:00:00Z", "2017-09-20T00:00:00Z",
                        "Normal daily means again."));
    }

    private ScenarioStep step(String at, String title, String from, String to, String detail) {
        Instant start = Instant.parse(from);
        Instant end = Instant.parse(to);
        List<Environment> slice = dose.stream()
                .filter(e -> !e.timestamp().isBefore(start) && e.timestamp().isBefore(end))
                .toList();
        return ScenarioStep.at(at, title).detail(detail).environment(slice)
                .evidence("Instrument dose equivalent (ICRP-60), not effective dose. NASA_DATA_SOURCES.md §4.3.")
                .build();
    }
}

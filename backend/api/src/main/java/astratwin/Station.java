package astratwin;

import astratwin.cache.CacheData;
import astratwin.cache.CacheLoader;
import astratwin.domain.Alert;
import astratwin.domain.AlertStatus;
import astratwin.domain.BaselineStats;
import astratwin.domain.ConditionProbability;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Environment;
import astratwin.domain.Forecast;
import astratwin.domain.Level;
import astratwin.domain.Reading;
import astratwin.privacy.CrewCard;
import astratwin.privacy.PrivacyFilter;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import astratwin.rules.CollapseRule;
import astratwin.rules.Co2Rule;
import astratwin.rules.DoseRateRule;
import astratwin.rules.GlucoseRule;
import astratwin.rules.RuleContext;
import astratwin.rules.RulesEngine;
import astratwin.rules.SpO2Rule;
import astratwin.rules.ZScoreRule;
import astratwin.scenario.ChestPainScenario;
import astratwin.scenario.Co2Scenario;
import astratwin.scenario.ScenarioEngine;
import astratwin.scenario.ScenarioState;
import astratwin.scenario.ScenarioStep;
import astratwin.scenario.ScenarioSummary;
import astratwin.scenario.SpeIssScenario;
import astratwin.seed.CrewRoster;
import astratwin.store.AlertRepository;
import astratwin.store.CheckInRepository;
import astratwin.store.CrewRepository;
import astratwin.store.EnvironmentRepository;
import astratwin.store.InMemoryAlertRepository;
import astratwin.store.InMemoryCheckInRepository;
import astratwin.store.InMemoryCrewRepository;
import astratwin.store.InMemoryEnvironmentRepository;
import astratwin.store.InMemoryMedicalCardRepository;
import astratwin.store.InMemoryReadingRepository;
import astratwin.store.MedicalCardRepository;
import astratwin.store.ReadingRepository;
import astratwin.twin.BaselineStatistics;
import astratwin.twin.CumulativeDoseModel;
import astratwin.twin.TwinContext;
import astratwin.twin.TwinEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Composition root and facade: wires repositories, engines and the privacy filter, and offers the
 * operations the web layer needs. Moving to a server swaps only the {@code InMemory…} repositories.
 */
public final class Station {
    public static final String DISCLAIMER = "Simulation — not medical advice.";
    static final List<String> VITAL_METRICS = List.of("collapse_detected", "hr", "bp_sys", "bp_dia", "spo2",
            "temp_c", "glucose", "symptom_headache", "symptom_confusion");

    private final CacheData data;
    private final BaselineStatistics baselines;
    private final CrewRoster roster;
    /** Readings kept in history; the latest value of every metric is kept separately. */
    static final int READING_HISTORY = 20_000;

    private final CrewRepository crew = new InMemoryCrewRepository();
    private final ReadingRepository readings = new InMemoryReadingRepository(READING_HISTORY);
    private final List<Consumer<Reading>> readingListeners = new CopyOnWriteArrayList<>();
    private final EnvironmentRepository environment = new InMemoryEnvironmentRepository();
    private final AlertRepository alerts = new InMemoryAlertRepository();
    private final CheckInRepository checkIns = new InMemoryCheckInRepository();
    private final MedicalCardRepository cards = new InMemoryMedicalCardRepository();
    private final RulesEngine rules;
    private final TwinEngine twin;
    private final PrivacyFilter privacy;
    private final ScenarioEngine scenarios;

    public Station(CacheData data) {
        this.data = data;
        this.baselines = new BaselineStatistics(data.baselines());
        this.roster = new CrewRoster(baselines);
        this.rules = new RulesEngine(List.of(new CollapseRule(), new GlucoseRule(), new SpO2Rule(), new Co2Rule(),
                new DoseRateRule(), new ZScoreRule()));
        this.twin = new TwinEngine(List.of(new CumulativeDoseModel()));
        this.privacy = PrivacyFilter.standard();
        this.scenarios = new ScenarioEngine(List.of(
                new ChestPainScenario(roster.member("C4"), roster.member("C1"), roster.member("C2"),
                        roster.member("C3"), roster.member("C5")),
                new Co2Scenario("C6"),
                new SpeIssScenario(data.issDose())));
        roster.cards().forEach(cards::save);
        resetData();
    }

    public static Station load(Path cacheDir) throws IOException {
        return new Station(CacheLoader.load(cacheDir));
    }

    /** Nominal station: simulated vitals, nominal cabin CO2, and the real dose data of Sep 4–8, 2017. */
    public synchronized void resetData() {
        readings.clear();
        environment.clear();
        alerts.clear();
        checkIns.clear();
        roster.members().forEach(crew::save);
        Instant now = Instant.now();
        roster.nominalReadings(now).forEach(this::record);
        roster.checkIns().forEach(checkIns::add);
        List<Environment> nominal = new ArrayList<>();
        nominal.add(Environment.co2(2.1, now, "simulated"));
        data.issDose().stream().filter(e -> e.timestamp().isBefore(SpeIssScenario.NOMINAL_END)).forEach(nominal::add);
        environment.addAll(nominal);
        reevaluate();
    }

    public synchronized ScenarioState startScenario(String id) {
        resetData();
        apply(scenarios.start(id));
        return scenarios.state().orElseThrow();
    }

    public synchronized ScenarioState step() {
        scenarios.next().ifPresent(this::apply);
        return scenarios.state().orElseThrow(() -> new IllegalStateException("no scenario is running"));
    }

    public synchronized void stopScenario() {
        scenarios.reset();
        resetData();
    }

    public synchronized void addReading(String crewId, String metric, double value, String unit) {
        member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        if (metric == null || metric.isBlank()) {
            throw new IllegalArgumentException("metric is required");
        }
        record(new Reading(crewId, metric, value, unit, Instant.now(), "simulated", measuredHow(metric),
                "Entered in the demo console"));
        reevaluate();
    }

    /**
     * Adds a batch of live telemetry and re-runs the rules once. Telemetry is not sent back to the
     * reading listeners, so a stream never feeds itself.
     */
    public synchronized void ingestTelemetry(List<Reading> batch) {
        for (Reading r : batch) {
            member(r.crewId()).orElseThrow(() -> new NoSuchElementException("no crew member " + r.crewId()));
            readings.add(r);
        }
        reevaluate();
    }

    /** Called for every non-telemetry reading (nominal, scenario, manual); used by the telemetry simulator. */
    public void addReadingListener(Consumer<Reading> listener) {
        readingListeners.add(listener);
    }

    private void record(Reading reading) {
        readings.add(reading);
        readingListeners.forEach(l -> l.accept(reading));
    }

    public synchronized Alert setAlertStatus(String alertId, AlertStatus status) {
        return alerts.setStatus(alertId, status);
    }

    public synchronized StationView view(ViewRequest request) {
        List<CrewCard> crewCards = crew.all().stream()
                .map(m -> privacy.card(request, m, roster.mission(m.id()), vitals(m.id()), cards.find(m.id()),
                        checkIns.forCrew(m.id())))
                .toList();
        List<Alert> visible = privacy.alerts(request, alerts.all(), cards::find);
        Level stationLevel = alerts.all().stream().filter(Alert::active).map(Alert::level).reduce(Level.L0, Level::max);
        return new StationView(request.viewer(), request.viewerCrewId(), stationLevel, crewCards, visible,
                environment.latest(e -> e.co2_mmHg() != null).orElse(null),
                environment.latest(e -> e.doseEqRate_uSv_h() != null).orElse(null),
                scenarios.state().orElse(null), DISCLAIMER);
    }

    /** Latest reading of every crew member's vital metrics; seeds the telemetry simulator. */
    public synchronized List<Reading> latestReadings() {
        List<Reading> out = new ArrayList<>();
        for (CrewMember m : crew.all()) {
            out.addAll(vitals(m.id()));
        }
        return out;
    }

    public boolean allows(DataKind kind, ViewRequest request, String crewId) {
        return privacy.allows(kind, request, crewId, cards.find(crewId));
    }

    public List<BaselineStats> baselines(String crewId) {
        return baselines.forCrew(crewId);
    }

    public synchronized List<Forecast> forecasts(String crewId) {
        CrewMember m = member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        return twin.forecasts(new TwinContext(environment.all(), readings.all(), Instant.now()), m);
    }

    public synchronized List<Environment> doseSeries() {
        return environment.all().stream().filter(e -> e.doseEqRate_uSv_h() != null).toList();
    }

    public List<ConditionProbability> imm() {
        return data.imm();
    }

    public List<ScenarioSummary> scenarioList() {
        return scenarios.list();
    }

    public synchronized Optional<CrewMember> member(String id) {
        return crew.find(id);
    }

    private void apply(ScenarioStep step) {
        Instant now = Instant.now();
        for (Reading r : step.readings()) {
            record(r.timestamp() != null ? r : new Reading(r.crewId(), r.metric(), r.value(), r.unit(), now,
                    r.provenance(), r.measuredHow(), r.note()));
        }
        environment.addAll(step.environment().stream()
                .map(e -> e.timestamp() != null ? e : new Environment(e.co2_mmHg(), e.temp_C(), e.rh_pct(),
                        e.doseRate_uGy_h(), e.doseEqRate_uSv_h(), e.instrument(), now, e.provenance()))
                .toList());
        reevaluate();
    }

    private void reevaluate() {
        RuleContext context = new RuleContext(crew.all(), readings, environment.all(), baselines, Instant.now());
        List<Alert> firing = rules.evaluate(context);
        alerts.sync(firing);
        for (CrewMember m : crew.all()) {
            Level level = firing.stream().filter(a -> a.crewId().equals(m.id())).map(Alert::level)
                    .reduce(Level.L0, Level::max);
            crew.save(m.withLevel(level));
        }
    }

    private List<Reading> vitals(String crewId) {
        List<Reading> out = new ArrayList<>();
        for (String metric : VITAL_METRICS) {
            readings.latest(crewId, metric).ifPresent(out::add);
        }
        return out;
    }

    private static String measuredHow(String metric) {
        return switch (metric) {
            case "hr", "spo2", "temp_c", "collapse_detected" -> "live";
            default -> "periodic";
        };
    }
}

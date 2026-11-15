package astratwin;

import astratwin.cache.CacheData;
import astratwin.cache.CacheLoader;
import astratwin.domain.Alert;
import astratwin.domain.AlertStatus;
import astratwin.domain.BaselineStats;
import astratwin.domain.CheckIn;
import astratwin.domain.ConditionReport;
import astratwin.domain.ConditionProbability;
import astratwin.domain.CrewMember;
import astratwin.domain.CrewPersona;
import astratwin.domain.DataKind;
import astratwin.domain.Environment;
import astratwin.domain.Forecast;
import astratwin.domain.Level;
import astratwin.domain.PartnerTag;
import astratwin.domain.Reading;
import astratwin.domain.StationClock;
import astratwin.privacy.CrewCard;
import astratwin.privacy.PrivacyFilter;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import astratwin.rules.CollapseRule;
import astratwin.rules.ConditionReportRule;
import astratwin.rules.Co2Rule;
import astratwin.rules.DoseRateRule;
import astratwin.rules.FamilyEventRule;
import astratwin.rules.GlucoseRule;
import astratwin.rules.PsychTrendRule;
import astratwin.rules.ReducedTaskingRule;
import astratwin.rules.RuleContext;
import astratwin.rules.RulesEngine;
import astratwin.rules.SpO2Rule;
import astratwin.rules.SupportPlanRule;
import astratwin.rules.ZScoreRule;
import astratwin.scenario.BadNewsScenario;
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
import astratwin.support.CompanionContext;
import astratwin.support.SupportAlert;
import astratwin.twin.BaselineStatistics;
import astratwin.twin.CumulativeDoseModel;
import astratwin.twin.SleepDebtModel;
import astratwin.twin.TwinContext;
import astratwin.twin.TwinEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    /** Open "needs support now" requests (#51): crew id → time of the latest crisis message. Kept across resets. */
    private final Map<String, Instant> supportRequests = new LinkedHashMap<>();
    /** Reported conditions (simulated stories of the crew profiles); reset with the station. */
    private final List<ConditionReport> conditions = new ArrayList<>();
    private final RulesEngine rules;
    private final TwinEngine twin;
    private final PrivacyFilter privacy;
    private final ScenarioEngine scenarios;
    /** Whole days added to real time by Next day (#81); zero after every reset. */
    private Duration clockOffset = Duration.ZERO;

    public Station(CacheData data) {
        this.data = data;
        this.baselines = new BaselineStatistics(data.baselines());
        this.roster = new CrewRoster(baselines);
        this.rules = new RulesEngine(List.of(new CollapseRule(), new GlucoseRule(), new SpO2Rule(), new Co2Rule(),
                new DoseRateRule(), new ZScoreRule(), new PsychTrendRule(), new ConditionReportRule(),
                new FamilyEventRule(), new ReducedTaskingRule(), new SupportPlanRule()));
        this.twin = new TwinEngine(List.of(new CumulativeDoseModel(), new SleepDebtModel()));
        this.privacy = PrivacyFilter.standard();
        this.scenarios = new ScenarioEngine(List.of(
                // Same patient and roles as the frontend's proposeRoles(): CMOs lead and assist, then the
                // nearest crew members by hatches fetch the equipment and run comms.
                new ChestPainScenario(roster.member("C2"), roster.member("C1"), roster.member("C4"),
                        roster.member("C3"), roster.member("C6")),
                new Co2Scenario("C6"),
                new SpeIssScenario(data.issDose()),
                // C3, as in PSYCHOLOGY_AND_LONG_DURATION.md §4.5.
                new BadNewsScenario("C3")));
        roster.cards().forEach(cards::save);
        resetData();
    }

    public static Station load(Path cacheDir) throws IOException {
        return new Station(CacheLoader.load(cacheDir));
    }

    /** Nominal station: simulated vitals, nominal cabin CO2, and the real dose data of Sep 4–8, 2017. */
    public synchronized void resetData() {
        clockOffset = Duration.ZERO;
        readings.clear();
        environment.clear();
        alerts.clear();
        checkIns.clear();
        roster.members().forEach(crew::save);
        Instant now = now();
        roster.nominalReadings(now).forEach(this::record);
        roster.checkIns(today()).forEach(checkIns::add);
        conditions.clear();
        conditions.addAll(roster.conditions());
        List<Environment> nominal = new ArrayList<>();
        nominal.add(Environment.co2(2.1, now, "simulated"));
        data.issDose().stream().filter(e -> e.timestamp().isBefore(SpeIssScenario.NOMINAL_END)).forEach(nominal::add);
        environment.addAll(nominal);
        reevaluate();
    }

    /** Station time: real UTC time plus the demo's day offset. Every station timestamp uses it. */
    public synchronized Instant now() {
        return Instant.now().plus(clockOffset);
    }

    /** The station's UTC date: the date a new check-in gets. */
    public synchronized LocalDate today() {
        return now().atZone(ZoneOffset.UTC).toLocalDate();
    }

    public synchronized StationClock clock() {
        Instant now = now();
        return new StationClock(now, now.atZone(ZoneOffset.UTC).toLocalDate().toString(), (int) clockOffset.toDays());
    }

    /**
     * Demo (#81): moves the station one day ahead, so the next check-in counts as a new night and the
     * Twin moves with it. Records a fresh set of nominal vitals and cabin CO2 at the new time, and ends
     * the stories about a single night (C3's short night is about day 0). Refused
     * while a scenario runs or is still shown: scenarios have their own timeline.
     */
    public synchronized StationClock nextDay() {
        if (scenarios.state().isPresent()) {
            throw new IllegalStateException("a scenario is running: reset it first, scenarios have their own timeline");
        }
        clockOffset = clockOffset.plusDays(1);
        conditions.removeIf(CrewRoster::oneNight);
        Instant now = now();
        roster.nominalReadings(now).forEach(this::record);
        environment.addAll(List.of(Environment.co2(2.1, now, "simulated")));
        reevaluate();
        return clock();
    }

    /** Back to today: the nominal station again; check-ins entered on later days are dropped. */
    public synchronized StationClock backToToday() {
        if (!clockOffset.isZero()) {
            resetData();
        }
        return clock();
    }

    public synchronized ScenarioState startScenario(String id) {
        resetData();
        apply(scenarios.start(id));
        return scenarios.state().orElseThrow();
    }

    /** The running scenario as this viewer may see it: a private scenario is redacted for everyone else. */
    public synchronized Optional<ScenarioState> scenarioState(ViewRequest request) {
        boolean full = scenarios.privateTo().map(request::isSelf).orElse(true);
        return full ? scenarios.state() : scenarios.publicState();
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
        record(new Reading(crewId, metric, value, unit, now(), "simulated", measuredHow(metric),
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

    /**
     * CMO or ground decision on an alert this viewer can see; other alerts are reported as missing, so
     * their existence is not revealed. Deciding a support alert closes the request.
     */
    public synchronized Alert setAlertStatus(String alertId, AlertStatus status, ViewRequest request) {
        Alert alert = alerts.find(alertId)
                .filter(a -> allows(a.category(), request, a.crewId()))
                .orElseThrow(() -> new NoSuchElementException("no alert " + alertId));
        Alert decided = alerts.setStatus(alertId, status);
        if (alert.category() == DataKind.SUPPORT) {
            supportRequests.remove(alert.crewId());
            reevaluate();
            return alerts.find(alertId).orElse(decided);
        }
        return decided;
    }

    /**
     * Crisis wording in the support companion (#51): the CMO gets a "needs support now" alert with
     * no message content. A new request reopens an alert the CMO has already acknowledged.
     */
    public synchronized void raiseSupportRequest(String crewId) {
        member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        supportRequests.put(crewId, now());
        String id = SupportAlert.id(crewId);
        alerts.find(id).filter(a -> a.status() != AlertStatus.OPEN).ifPresent(a -> alerts.setStatus(id, AlertStatus.OPEN));
        reevaluate();
    }

    public synchronized StationView view(ViewRequest request) {
        List<CrewCard> crewCards = crew.all().stream()
                .map(m -> privacy.card(request, m, roster.mission(m.id()), vitals(m.id()), cards.find(m.id()),
                        checkIns.forCrew(m.id())))
                .toList();
        List<Alert> visible = privacy.alerts(request, alerts.all(), cards::find);
        Level stationLevel = alerts.all().stream().filter(a -> a.active() && changesLevel(a))
                .map(Alert::level).reduce(Level.L0, Level::max);
        return new StationView(request.viewer(), request.viewerCrewId(), stationLevel, crewCards, visible,
                environment.latest(e -> e.co2_mmHg() != null).orElse(null),
                environment.latest(e -> e.doseEqRate_uSv_h() != null).orElse(null),
                scenarioState(request).orElse(null), clock(), DISCLAIMER);
    }

    /**
     * Stores a check-in from the astronaut (SELF only at the web layer) and re-runs the rules. One per
     * day: a second check-in on the same date replaces the first.
     */
    public synchronized List<CheckIn> addCheckIn(CheckIn checkIn) {
        member(checkIn.crewId()).orElseThrow(() -> new NoSuchElementException("no crew member " + checkIn.crewId()));
        checkIns.add(checkIn);
        reevaluate();
        return checkIns.forCrew(checkIn.crewId());
    }

    public synchronized List<CheckIn> checkIns(String crewId) {
        return checkIns.forCrew(crewId);
    }

    /** Days of data the support companion gets. */
    static final int COMPANION_DAYS = 3;

    /**
     * What the support companion knows about one crew member: their own profile and a plain summary of
     * their last {@value #COMPANION_DAYS} days (check-ins, vital ranges, open health notes they can see).
     * Private to the astronaut: only viewer SELF reads it, and the companion runs on the station.
     */
    public synchronized Optional<CompanionContext> companionContext(String crewId) {
        Optional<CrewPersona> persona = roster.personas().stream().filter(p -> p.crewId().equals(crewId)).findFirst();
        if (persona.isEmpty()) {
            return Optional.empty();
        }
        Instant now = now();
        String since = today().minusDays(COMPANION_DAYS).toString();
        List<String> recent = new ArrayList<>();
        List<CheckIn> days = checkIns.forCrew(crewId).stream().filter(c -> c.date().compareTo(since) >= 0).toList();
        if (days.isEmpty()) {
            recent.add("No check-ins in the last " + COMPANION_DAYS + " days.");
        } else {
            recent.add("Sleep from your check-ins, oldest first: "
                    + String.join(", ", days.stream().map(c -> c.sleepHours() + " h").toList()) + ".");
            recent.add("Mood from your check-ins, oldest first: "
                    + String.join(", ", days.stream().map(c -> c.mood() + "/100").toList())
                    + "; stress " + String.join(", ", days.stream().map(c -> c.stress() + "/100").toList()) + ".");
        }
        List<String> vitals = new ArrayList<>();
        vitalRange(crewId, "hr", "heart rate", "bpm", now).ifPresent(vitals::add);
        readings.latest(crewId, "bp_sys").ifPresent(sys -> readings.latest(crewId, "bp_dia").ifPresent(dia ->
                vitals.add("blood pressure " + Math.round(sys.value()) + "/" + Math.round(dia.value()) + " mmHg")));
        vitalRange(crewId, "spo2", "SpO2", "%", now).ifPresent(vitals::add);
        vitalRange(crewId, "temp_c", "temperature", "°C", now).ifPresent(vitals::add);
        vitalRange(crewId, "glucose", "glucose", "mg/dL", now).ifPresent(vitals::add);
        if (!vitals.isEmpty()) {
            recent.add("Vitals over the last " + COMPANION_DAYS + " days (simulated): " + String.join("; ", vitals) + ".");
        }
        ViewRequest self = new ViewRequest(astratwin.domain.Viewer.SELF, crewId);
        List<String> notes = privacy.alerts(self, alerts.all(), cards::find).stream()
                .filter(a -> a.active() && a.category() != DataKind.SUPPORT)
                .filter(a -> a.crewId().equals(crewId) || a.crewId().equals(Alert.STATION))
                .map(a -> (a.crewId().equals(Alert.STATION) ? "station: " : "") + a.title()
                        + (a.status() == astratwin.domain.AlertStatus.OPEN && a.level().ordinal() >= Level.L2.ordinal()
                                ? " (waiting for the CMO)" : ""))
                .toList();
        recent.add(notes.isEmpty() ? "Open health notes: none." : "Open health notes: " + String.join("; ", notes) + ".");
        CrewMember m = member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        String role = switch (m.role()) {
            case CMO1 -> "crew medical officer 1";
            case CMO2 -> "crew medical officer 2";
            default -> "crew member";
        };
        String module = m.module().replaceAll(" \\(.*\\)$", "");
        return Optional.of(new CompanionContext(crewId, m.callsign(), role, module, persona.get(), List.copyOf(recent)));
    }

    /** "heart rate 64 bpm", or "heart rate 58–72 bpm, now 64" when it varied in the window. */
    private Optional<String> vitalRange(String crewId, String metric, String label, String unit, Instant now) {
        Optional<Reading> latest = readings.latest(crewId, metric);
        if (latest.isEmpty()) {
            return Optional.empty();
        }
        Instant from = now.minus(Duration.ofDays(COMPANION_DAYS));
        java.util.DoubleSummaryStatistics s = readings.all().stream()
                .filter(r -> r.crewId().equals(crewId) && r.metric().equals(metric) && r.timestamp() != null
                        && !r.timestamp().isBefore(from))
                .mapToDouble(Reading::value).summaryStatistics();
        String now1 = fmt(latest.get().value());
        String u = unit.equals("%") ? unit : " " + unit;
        if (s.getCount() < 2 || s.getMin() == s.getMax()) {
            return Optional.of(label + " " + now1 + u);
        }
        return Optional.of(label + " " + fmt(s.getMin()) + "–" + fmt(s.getMax()) + u + ", now " + now1);
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(Math.round(v * 10) / 10.0);
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

    /** T+6/T+12 forecasts for one member; each model's data kind decides whether this viewer sees it. */
    public synchronized List<Forecast> forecasts(String crewId, ViewRequest request) {
        CrewMember m = member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        TwinContext context = new TwinContext(environment.all(), readings.all(), checkIns.forCrew(crewId), now());
        return twin.forecasts(context, m, kind -> allows(kind, request, crewId));
    }

    /** Partner-instrument tag for {@link #partnerDose()} (team decision #13). */
    public static final PartnerTag DOSTEL_TAG = new PartnerTag("DosTel2 (DOSIS 3D)", "DLR (German Aerospace Center)",
            "ISS Columbus module", "NASA OSDR RadLab",
            "https://visualization.osdr.nasa.gov/radlab/gui/",
            "Partner-agency instrument. NASA OSDR RadLab hosts and distributes the data, so the team counts it as "
                    + "NASA-hosted data (decision #13); credit to DLR. Silicon detector: absorbed dose only, not "
                    + "comparable one-to-one with the tissue-equivalent TEPC.");

    /**
     * Real hourly absorbed dose in the Columbus module, Sep 4–19, 2017 (DLR DosTel2 via NASA OSDR RadLab),
     * with the whole NASA TEPC series of the same days as {@code reference} (the station replays TEPC
     * only up to its current scenario time).
     */
    public Map<String, Object> partnerDose() {
        return Map.of("tag", DOSTEL_TAG, "source", String.valueOf(data.partnerDoseSource()), "items", data.partnerDose(),
                "reference", data.issDose());
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
        Instant now = now();
        for (Reading r : step.readings()) {
            record(r.timestamp() != null ? r : new Reading(r.crewId(), r.metric(), r.value(), r.unit(), now,
                    r.provenance(), r.measuredHow(), r.note()));
        }
        step.checkIns().forEach(checkIns::add);
        environment.addAll(step.environment().stream()
                .map(e -> e.timestamp() != null ? e : new Environment(e.co2_mmHg(), e.temp_C(), e.rh_pct(),
                        e.doseRate_uGy_h(), e.doseEqRate_uSv_h(), e.instrument(), now, e.provenance()))
                .toList());
        reevaluate();
    }

    private void reevaluate() {
        Map<String, List<CheckIn>> byCrew = new HashMap<>();
        for (CrewMember m : crew.all()) {
            byCrew.put(m.id(), checkIns.forCrew(m.id()));
        }
        RuleContext context = new RuleContext(crew.all(), readings, environment.all(), baselines, byCrew,
                List.copyOf(conditions), now());
        List<Alert> firing = new ArrayList<>(rules.evaluate(context));
        supportRequests.forEach((crewId, at) -> firing.add(SupportAlert.of(crewId, at)));
        alerts.sync(firing);
        for (CrewMember m : crew.all()) {
            Level level = firing.stream().filter(a -> a.crewId().equals(m.id()) && changesLevel(a))
                    .map(Alert::level)
                    .reduce(Level.L0, Level::max);
            crew.save(m.withLevel(level));
        }
    }

    /** Psych, support and plan alerts never change a level: levels are shown to everyone on the Crew Deck. */
    private static boolean changesLevel(Alert alert) {
        return alert.category() != DataKind.PSYCH && alert.category() != DataKind.SUPPORT
                && alert.category() != DataKind.PLAN;
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

package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.CheckIn;
import astratwin.domain.DataKind;
import astratwin.domain.Forecast;
import astratwin.domain.Level;
import astratwin.domain.StationClock;
import astratwin.domain.Viewer;
import astratwin.privacy.CrewCard;
import astratwin.privacy.StationView;
import astratwin.privacy.ViewRequest;
import astratwin.stream.TelemetryStreamer;
import astratwin.support.GuidedResponder;
import astratwin.support.SupportService;
import astratwin.web.ApiServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Backend side of the frontend integration (#56). */
final class IntegrationTest {
    private IntegrationTest() {
    }

    static void register(Map<String, TestRunner.Test> tests, Path cache) {
        tests.put("roster matches the frontend crew (callsigns, CMOs, module ids)", () -> {
            StationView v = Station.load(cache).view(new ViewRequest(Viewer.CMO, null));
            Check.equal(List.of("ORION", "VEGA", "LYRA", "ALTAIR", "RIGEL", "NOVA", "ATLAS"),
                    v.crew().stream().map(CrewCard::callsign).toList(), "callsigns");
            Check.equal(List.of("Destiny", "Harmony (Node 2)", "Columbus", "Kibo", "Tranquility (Node 3)",
                    "Unity (Node 1)", "Zvezda"), v.crew().stream().map(CrewCard::module).toList(), "modules");
            Check.equal("CMO2", v.crew().get(3).role().name(), "C4 is CMO2");
            Check.equal("real", v.crew().get(0).baselineProvenance(), "C1 baseline");
            Check.equal("simulated", v.crew().get(6).baselineProvenance(), "C7 baseline");
        });
        tests.put("crew profiles (#71): live levels, vitals, check-ins and conditions match the frontend stories", () -> {
            Station station = Station.load(cache);
            StationView deck = station.view(new ViewRequest(Viewer.CREW_DECK, null));
            // Same levels as frontend/src/mock/crew.json.
            Check.equal(List.of(Level.L0, Level.L1, Level.L1, Level.L2, Level.L1, Level.L1, Level.L0),
                    deck.crew().stream().map(CrewCard::level).toList(), "Crew Deck levels");
            Check.isTrue(deck.alerts().isEmpty(), "the Crew Deck sees no condition detail: " + deck.alerts());
            StationView cmo = station.view(new ViewRequest(Viewer.CMO, null));
            Alert c4 = cmo.alerts().stream().filter(a -> a.id().equals("condition:C4:head")).findFirst()
                    .orElseThrow(() -> new AssertionError("CMO must see C4's motion sickness"));
            Check.equal(Level.L2, c4.level(), "C4 level");
            Check.equal(DataKind.VITALS, c4.category(), "C4 category");
            Check.equal(72.0, cmo.crew().get(3).vitals().stream().filter(r -> r.metric().equals("hr")).findFirst()
                    .orElseThrow().value(), "C4 heart rate from the profile");
            Check.equal(37.6, cmo.crew().get(4).vitals().stream().filter(r -> r.metric().equals("temp_c")).findFirst()
                    .orElseThrow().value(), "C5 temperature from the profile");
            List<CheckIn> c3 = station.view(new ViewRequest(Viewer.SELF, "C3")).crew().get(2).checkIns();
            Check.equal(7, c3.size(), "C3 check-ins: one per night");
            Check.equal(5.6, c3.get(6).sleepHours(), "C3 last night");
            Check.equal(64, c3.get(6).mood(), "C3 mood from the profile");
            // Decision #11: drugs are named only from the ISS kit list, and the source is credited.
            java.util.regex.Pattern drug = java.util.regex.Pattern.compile("(?i)\\b(acetaminophen|paracetamol|ibuprofen|aspirin|"
                    + "ondansetron|promethazine|melatonin|zolpidem|modafinil|sertraline|oxymetazoline|epinephrine|metoprolol)\\b");
            for (Alert a : cmo.alerts()) {
                java.util.regex.Matcher m = drug.matcher(String.valueOf(a.recommendation()));
                while (m.find()) {
                    Check.isTrue(astratwin.seed.IssFormulary.find(m.group(1)).isPresent(), m.group(1) + " is not on the ISS kit list");
                    Check.isTrue(a.source().contains("CC BY 4.0"), "kit list credited in " + a.id());
                }
            }
            Check.isTrue(c4.recommendation().contains("promethazine"), "C4's card names the kit's anti-nausea options");
            Map<String, Object> partner = station.partnerDose();
            Check.equal(384, ((List<?>) partner.get("items")).size(), "DosTel2 hourly means, Sep 4–19 2017 (#13)");
            Check.equal("DLR (German Aerospace Center)", ((astratwin.domain.PartnerTag) partner.get("tag")).agency(), "partner tag");
            station.startScenario("chest-pain");
            station.stopScenario();
            Check.equal(5, station.view(new ViewRequest(Viewer.CMO, null)).alerts().size(), "conditions come back after a reset");
        });
        tests.put("ground (flight surgeon) sees vitals and medication, never check-ins", () -> {
            StationView v = Station.load(cache).view(new ViewRequest(Viewer.GROUND, null));
            for (CrewCard c : v.crew()) {
                Check.isTrue(c.vitals() != null, "ground sees vitals of " + c.id());
                Check.isTrue(c.checkIns() == null, "ground must not see check-ins of " + c.id());
            }
        });
        tests.put("low check-ins → private psych alert; never on the Crew Deck and never changes the level", () -> {
            Station station = Station.load(cache);
            Level c5Before = station.member("C5").orElseThrow().level();
            Level stationBefore = station.view(new ViewRequest(Viewer.CMO, null)).stationLevel();
            // Three low days in a row, one check-in per day (#81).
            for (int i = 0; i < 3; i++) {
                station.nextDay();
                station.addCheckIn(new CheckIn("C5", station.today().toString(), 35, 60, 75, 20, 5.0, "test"));
            }
            StationView self = station.view(new ViewRequest(Viewer.SELF, "C5"));
            Alert alert = self.alerts().stream().filter(a -> a.id().equals("psych-trend:C5")).findFirst()
                    .orElseThrow(() -> new AssertionError("SELF must see its psych alert"));
            Check.equal(DataKind.PSYCH, alert.category(), "category");
            for (Viewer v : List.of(Viewer.CREW_DECK, Viewer.CMO, Viewer.GROUND, Viewer.EMERGENCY)) {
                StationView view = station.view(new ViewRequest(v, null));
                Check.isTrue(view.alerts().stream().noneMatch(a -> a.category() == DataKind.PSYCH), v + " sees psych");
                Check.equal(c5Before, view.crew().get(4).level(), v + " level of C5");
                Check.equal(stationBefore, view.stationLevel(), v + " station level");
            }
        });
        tests.put("next day (#81): one check-in per day; each new night moves the Twin; refused in a scenario", () -> {
            Station station = Station.load(cache);
            ViewRequest self = new ViewRequest(Viewer.SELF, "C3");
            Check.equal(0, station.clock().dayOffset(), "starts today");
            Check.equal(7, station.checkIns("C3").size(), "seeded week");
            LocalDate day0 = station.today();
            station.addCheckIn(new CheckIn("C3", day0.toString(), 50, 50, 50, 10, 3.0, "test"));
            double first = sleepDebt(station, self);
            station.addCheckIn(new CheckIn("C3", day0.toString(), 50, 50, 50, 10, 3.0, "test"));
            Check.equal(8, station.checkIns("C3").size(), "a second check-in on the same day replaces the first");
            Check.equal(first, sleepDebt(station, self), "same day, same debt");

            StationClock next = station.nextDay();
            Check.equal(1, next.dayOffset(), "offset");
            Check.equal(day0.plusDays(1).toString(), next.today(), "station date");
            Check.equal(next.today(), station.view(self).clock().today(), "the station view carries the clock");
            Check.isTrue(station.view(self).alerts().stream().noneMatch(a -> a.active() && a.id().equals("condition:C3:head")),
                    "the story about day 0's short night ends");
            Check.isTrue(station.view(new ViewRequest(Viewer.CMO, null)).alerts().stream()
                    .anyMatch(a -> a.active() && a.id().equals("condition:C6:musculo")), "ongoing stories stay");
            Check.isTrue(station.latestReadings().stream()
                    .allMatch(r -> Duration.between(r.timestamp(), next.now()).abs().toSeconds() < 5),
                    "fresh vitals at the new time");
            station.addCheckIn(new CheckIn("C3", station.today().toString(), 50, 50, 50, 10, 3.0, "test"));
            Check.isTrue(sleepDebt(station, self) > first, "a short night on the new day adds debt");
            Check.equal(day0.plusDays(1).toString(), station.checkIns("C3").getLast().date(), "dated by the station clock");

            station.nextDay();
            station.nextDay();
            String recent = String.join(" ", station.companionContext("C1").orElseThrow().recent());
            Check.isTrue(recent.contains("No check-ins in the last 3 days"), "the companion's window follows the station: " + recent);

            Check.equal(0, station.backToToday().dayOffset(), "back to today");
            Check.equal(7, station.checkIns("C3").size(), "back to the seeded week");
            station.startScenario("co2");
            try {
                station.nextDay();
                throw new AssertionError("next day must be refused during a scenario");
            } catch (IllegalStateException expected) {
                // scenarios keep their own timeline
            }
            station.stopScenario();
            Check.equal(1, station.nextDay().dayOffset(), "allowed again after the reset");
            station.startScenario("co2");
            Check.equal(0, station.clock().dayOffset(), "every reset starts from today");
        });
        tests.put("HTTP: check-ins are SELF only; ground may approve; UI is served with SPA fallback", () -> {
            Station station = Station.load(cache);
            Path ui = Files.createTempDirectory("astra-ui");
            Files.writeString(ui.resolve("index.html"), "<!doctype html><title>ui</title>");
            Files.createDirectories(ui.resolve("assets"));
            Files.writeString(ui.resolve("assets/app.js"), "console.log(1)");
            Files.writeString(ui.getParent().resolve("astra-secret.txt"), "secret");
            try (TelemetryStreamer streamer = new TelemetryStreamer(station, Files.createTempDirectory("astra-stream"))) {
                ApiServer server = new ApiServer(station, streamer, cache.getParent().resolve("stream"),
                        new SupportService(null, new GuidedResponder(), id -> Optional.empty()), "127.0.0.1", 0)
                        .serveUi(ui);
                server.start();
                try {
                    HttpClient http = HttpClient.newHttpClient();
                    String base = "http://127.0.0.1:" + server.port();
                    String body = "{\"mood\":55,\"fatigue\":40,\"stress\":30,\"conflict\":10,\"sleepHours\":6.5}";
                    Check.equal(200, post(http, base + "/api/checkins?viewer=SELF&crewId=C3", body).statusCode(), "SELF post");
                    Check.equal(403, post(http, base + "/api/checkins?viewer=CMO", body).statusCode(), "CMO post");
                    Check.equal(403, get(http, base + "/api/checkins?viewer=GROUND").statusCode(), "GROUND read");
                    Check.equal(400, post(http, base + "/api/checkins?viewer=SELF&crewId=C3", "{\"mood\":500}").statusCode(), "validation");
                    station.addReading("C2", "glucose", 50, "mg/dL");
                    Check.equal(200, post(http, base + "/api/alerts/glucose:C2/approve?viewer=GROUND", "").statusCode(), "ground approves");
                    Check.equal(403, post(http, base + "/api/alerts/glucose:C2/reject?viewer=SELF&crewId=C2", "").statusCode(), "self cannot");
                    Check.isTrue(get(http, base + "/").body().contains("<title>ui</title>"), "index at /");
                    Check.isTrue(get(http, base + "/crew/deck").body().contains("<title>ui</title>"), "SPA fallback");
                    Check.equal("console.log(1)", get(http, base + "/assets/app.js").body(), "asset");
                    Check.equal(404, get(http, base + "/missing.js").statusCode(), "missing asset");
                    Check.equal(404, get(http, base + "/..%2fastra-secret.txt").statusCode(), "path traversal");
                    Check.equal(200, get(http, base + "/api/health").statusCode(), "api still works");
                    // Found by the API sweep (2026-11-15): unknown viewers and crew ids are refused, with plain messages.
                    HttpResponse<String> bogus = get(http, base + "/api/station?viewer=BOGUS");
                    Check.equal(400, bogus.statusCode(), "unknown viewer");
                    Check.isTrue(bogus.body().contains("viewer must be"), "plain message: " + bogus.body());
                    Check.equal(400, get(http, base + "/api/station?viewer=SELF").statusCode(), "SELF without crewId");
                    Check.equal(404, get(http, base + "/api/station?viewer=SELF&crewId=C99").statusCode(), "SELF unknown crew");
                    Check.equal(404, get(http, base + "/api/crew/C99/baselines?viewer=CMO").statusCode(), "unknown crew baselines");
                    HttpResponse<String> nextDay = post(http, base + "/api/clock/next-day", "");
                    Check.equal(200, nextDay.statusCode(), "next day");
                    Check.isTrue(nextDay.body().contains("\"dayOffset\":1"), "clock in the answer: " + nextDay.body());
                    Check.isTrue(get(http, base + "/api/station?viewer=CMO").body().contains("\"dayOffset\":1"), "clock in the station view");
                    Check.equal(400, post(http, base + "/api/clock/later", "").statusCode(), "unknown clock route");
                    Check.isTrue(post(http, base + "/api/clock/today", "").body().contains("\"dayOffset\":0"), "back to today");
                    post(http, base + "/api/scenarios/co2/start", "");
                    HttpResponse<String> refused = post(http, base + "/api/clock/next-day", "");
                    Check.equal(400, refused.statusCode(), "next day during a scenario");
                    Check.isTrue(refused.body().contains("scenario"), "plain message: " + refused.body());
                    post(http, base + "/api/scenario/stop", "");
                    HttpResponse<String> interval = post(http, base + "/api/stream/start?intervalMs=abc", "");
                    Check.equal(400, interval.statusCode(), "bad intervalMs");
                    Check.isTrue(interval.body().contains("whole number"), "plain message: " + interval.body());
                } finally {
                    server.stop();
                }
            }
        });
    }

    /** The sleep debt now (T+0 of the 12-hour forecast), as the astronaut sees it. */
    private static double sleepDebt(Station station, ViewRequest self) {
        return station.forecasts(self.viewerCrewId(), self).stream()
                .filter(f -> f.metric().equals("sleep_debt") && f.horizonHours() == 12)
                .map(Forecast::points).findFirst().orElseThrow().getFirst().value();
    }

    private static HttpResponse<String> get(HttpClient http, String url) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient http, String url, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}

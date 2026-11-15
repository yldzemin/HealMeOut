package astratwin;

import astratwin.domain.Alert;
import astratwin.domain.CheckIn;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
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
        tests.put("ground (flight surgeon) sees vitals and medication, never check-ins", () -> {
            StationView v = Station.load(cache).view(new ViewRequest(Viewer.GROUND, null));
            for (CrewCard c : v.crew()) {
                Check.isTrue(c.vitals() != null, "ground sees vitals of " + c.id());
                Check.isTrue(c.checkIns() == null, "ground must not see check-ins of " + c.id());
            }
        });
        tests.put("low check-ins → private psych alert; never on the Crew Deck and never changes the level", () -> {
            Station station = Station.load(cache);
            for (int i = 0; i < 3; i++) {
                station.addCheckIn(new CheckIn("C5", "2026-11-1" + i, 35, 60, 75, 20, 5.0, "test"));
            }
            StationView self = station.view(new ViewRequest(Viewer.SELF, "C5"));
            Alert alert = self.alerts().stream().filter(a -> a.id().equals("psych-trend:C5")).findFirst()
                    .orElseThrow(() -> new AssertionError("SELF must see its psych alert"));
            Check.equal(DataKind.PSYCH, alert.category(), "category");
            for (Viewer v : List.of(Viewer.CREW_DECK, Viewer.CMO, Viewer.GROUND, Viewer.EMERGENCY)) {
                StationView view = station.view(new ViewRequest(v, null));
                Check.isTrue(view.alerts().stream().noneMatch(a -> a.category() == DataKind.PSYCH), v + " sees psych");
                Check.equal(Level.L0, view.crew().get(4).level(), v + " level of C5");
                Check.equal(Level.L0, view.stationLevel(), v + " station level");
            }
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
                } finally {
                    server.stop();
                }
            }
        });
    }

    private static HttpResponse<String> get(HttpClient http, String url) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient http, String url, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}

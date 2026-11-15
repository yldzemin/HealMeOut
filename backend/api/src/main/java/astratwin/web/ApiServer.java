package astratwin.web;

import astratwin.Station;
import astratwin.domain.AlertStatus;
import astratwin.domain.CheckIn;
import astratwin.domain.DataKind;
import astratwin.domain.Viewer;
import astratwin.json.Json;
import astratwin.json.JsonParser;
import astratwin.privacy.ViewRequest;
import astratwin.stream.CsvReplaySource;
import astratwin.stream.SimulatedTelemetrySource;
import astratwin.stream.TelemetryRow;
import astratwin.stream.TelemetrySource;
import astratwin.stream.TelemetryStreamer;
import astratwin.support.SupportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * HTTP API on the JDK's built-in server (no dependencies, #33). Routes:
 * GET /api/health, /api/station, /api/scenarios, /api/crew/{id}/baselines, /api/crew/{id}/forecasts,
 * /api/environment/dose, /api/imm; POST /api/scenarios/{id}/start, /api/scenario/step,
 * /api/scenario/stop, /api/readings, /api/alerts/{id}/approve|reject.
 * Telemetry: GET /api/stream/status, /api/stream/telemetry.csv (live, chunked), /api/stream/recent.csv;
 * POST /api/stream/start?source=sim|replay&file=sample_telemetry.csv&intervalMs=1000, /api/stream/stop.
 * Check-ins (SELF only): GET /api/checkins, POST /api/checkins (body mood, fatigue, stress, conflict, sleepHours).
 * Support companion (SELF only): GET /api/support/status, /api/support/conversation;
 * POST /api/support/message (body {"text"}), /api/support/clear.
 * Viewer: ?viewer=CMO|CREW_DECK|EMERGENCY|SELF&crewId=C1; the default is CREW_DECK (most restricted).
 * The demo console is served at /demo/.
 */
public final class ApiServer {
    private final Station station;
    private final TelemetryStreamer streamer;
    private final Path streamDir;
    private final SupportService support;
    private final HttpServer server;
    private Path uiDir;

    public ApiServer(Station station, TelemetryStreamer streamer, Path streamDir, SupportService support,
            String host, int port) throws IOException {
        this.station = station;
        this.streamer = streamer;
        this.streamDir = streamDir;
        this.support = support;
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        // One virtual thread per request, so long-lived CSV streams never block other requests.
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/api/", this::handleApi);
        server.createContext("/demo", this::handleDemo);
        server.createContext("/", this::handleRoot);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    /**
     * Also serve a built frontend ({@code vite build} output) at {@code /} (plan B in docs/FRONTEND.md).
     * Without it, {@code /} redirects to the demo console.
     */
    public ApiServer serveUi(Path dir) {
        if (dir != null && !Files.isRegularFile(dir.resolve("index.html"))) {
            throw new IllegalArgumentException(dir + " has no index.html; run npm run build in frontend/");
        }
        this.uiDir = dir == null ? null : dir.toAbsolutePath().normalize();
        return this;
    }

    private void handleRoot(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (uiDir == null) {
            if (path.equals("/")) {
                redirect(ex, "/demo/");
            } else {
                send(ex, 404, Map.of("error", "not found"));
            }
            return;
        }
        Path file = uiDir.resolve(path.substring(1)).normalize();
        if (!file.startsWith(uiDir)) {
            send(ex, 404, Map.of("error", "not found"));
            return;
        }
        if (!Files.isRegularFile(file)) {
            // Single-page app: unknown paths without a file extension get index.html.
            String last = path.substring(path.lastIndexOf('/') + 1);
            if (last.contains(".")) {
                send(ex, 404, Map.of("error", "not found"));
                return;
            }
            file = uiDir.resolve("index.html");
        }
        byte[] bytes = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", contentType(file.getFileName().toString()));
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String contentType(String name) {
        String n = name.toLowerCase(java.util.Locale.ROOT);
        if (n.endsWith(".html")) return "text/html; charset=utf-8";
        if (n.endsWith(".js") || n.endsWith(".mjs")) return "text/javascript; charset=utf-8";
        if (n.endsWith(".css")) return "text/css; charset=utf-8";
        if (n.endsWith(".json")) return "application/json; charset=utf-8";
        if (n.endsWith(".svg")) return "image/svg+xml";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".ico")) return "image/x-icon";
        if (n.endsWith(".woff2")) return "font/woff2";
        if (n.endsWith(".woff")) return "font/woff";
        if (n.endsWith(".glb")) return "model/gltf-binary";
        if (n.endsWith(".wasm")) return "application/wasm";
        return "application/octet-stream";
    }

    private void handleDemo(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/demo")) {
            redirect(ex, "/demo/");
            return;
        }
        String file = path.equals("/demo/") ? "index.html" : path.substring("/demo/".length());
        if (file.contains("..") || file.contains("/")) {
            send(ex, 404, Map.of("error", "not found"));
            return;
        }
        try (InputStream in = ApiServer.class.getResourceAsStream("/demo/" + file)) {
            if (in == null) {
                send(ex, 404, Map.of("error", "not found"));
                return;
            }
            byte[] bytes = in.readAllBytes();
            String type = file.endsWith(".html") ? "text/html; charset=utf-8"
                    : file.endsWith(".js") ? "text/javascript; charset=utf-8"
                    : file.endsWith(".css") ? "text/css; charset=utf-8" : "application/octet-stream";
            ex.getResponseHeaders().set("Content-Type", type);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private void handleApi(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return;
        }
        try {
            String path = ex.getRequestURI().getPath();
            if (ex.getRequestMethod().equals("GET") && path.equals("/api/stream/telemetry.csv")) {
                streamCsv(ex);
                return;
            }
            if (ex.getRequestMethod().equals("GET") && path.equals("/api/stream/recent.csv")) {
                recentCsv(ex);
                return;
            }
            send(ex, 200, route(ex));
        } catch (NoSuchElementException e) {
            send(ex, 404, Map.of("error", String.valueOf(e.getMessage())));
        } catch (SecurityException e) {
            send(ex, 403, Map.of("error", String.valueOf(e.getMessage())));
        } catch (IllegalArgumentException | IllegalStateException e) {
            send(ex, 400, Map.of("error", String.valueOf(e.getMessage())));
        } catch (RuntimeException e) {
            send(ex, 500, Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private Object route(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String[] p = ex.getRequestURI().getPath().substring("/api/".length()).split("/");
        Map<String, String> query = query(ex.getRequestURI().getRawQuery());
        // Privacy by default: without a viewer, the most restricted view (Crew Deck) applies.
        ViewRequest request = new ViewRequest(Viewer.parse(query.getOrDefault("viewer", "CREW_DECK")), query.get("crewId"));

        return switch (method + " " + p[0]) {
            case "GET health" -> Map.of("status", "ok", "disclaimer", Station.DISCLAIMER);
            case "GET station" -> station.view(request);
            case "GET scenarios" -> station.scenarioList();
            case "POST scenarios" -> {
                require(p.length == 3 && p[2].equals("start"), "use POST /api/scenarios/{id}/start");
                yield station.startScenario(p[1]);
            }
            case "POST scenario" -> {
                require(p.length == 2, "use POST /api/scenario/step or /api/scenario/stop");
                if (p[1].equals("step")) {
                    yield station.step();
                }
                require(p[1].equals("stop"), "use POST /api/scenario/step or /api/scenario/stop");
                station.stopScenario();
                yield Map.of("stopped", true);
            }
            case "POST readings" -> {
                Map<String, Object> body = JsonParser.parseObject(new String(ex.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));
                if (!(body.get("value") instanceof Double value)) {
                    throw new IllegalArgumentException("value must be a number");
                }
                station.addReading(String.valueOf(body.get("crewId")), String.valueOf(body.get("metric")), value,
                        body.get("unit") == null ? "" : String.valueOf(body.get("unit")));
                yield station.view(request);
            }
            case "POST alerts" -> {
                require(p.length == 3, "use POST /api/alerts/{id}/approve or /reject");
                if (request.viewer() != Viewer.CMO && request.viewer() != Viewer.GROUND) {
                    throw new SecurityException("only the CMO or ground view can approve or reject");
                }
                AlertStatus status = switch (p[2]) {
                    case "approve" -> AlertStatus.APPROVED;
                    case "reject" -> AlertStatus.REJECTED;
                    default -> throw new IllegalArgumentException("use approve or reject");
                };
                yield station.setAlertStatus(p[1], status, request);
            }
            case "GET crew" -> {
                require(p.length == 3, "use GET /api/crew/{id}/baselines or /forecasts");
                if (!station.allows(DataKind.VITALS, request, p[1])) {
                    throw new SecurityException(request.viewer() + " may not see " + p[1] + "'s health data");
                }
                yield switch (p[2]) {
                    case "baselines" -> station.baselines(p[1]);
                    case "forecasts" -> station.forecasts(p[1], request);
                    default -> throw new NoSuchElementException("no route " + ex.getRequestURI().getPath());
                };
            }
            case "GET environment" -> {
                require(p.length == 2 && (p[1].equals("dose") || p[1].equals("partner-dose")),
                        "use GET /api/environment/dose or /api/environment/partner-dose");
                yield p[1].equals("dose") ? station.doseSeries() : station.partnerDose();
            }
            case "GET imm" -> station.imm();
            case "GET checkins" -> station.checkIns(selfCrewId(request));
            case "POST checkins" -> {
                String crewId = selfCrewId(request);
                Map<String, Object> body = JsonParser.parseObject(new String(ex.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));
                yield station.addCheckIn(new CheckIn(crewId, java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString(),
                        score(body, "mood"), score(body, "fatigue"), score(body, "stress"), score(body, "conflict"),
                        hours(body, "sleepHours"), "entered by the astronaut"));
            }
            case "GET support" -> {
                require(p.length == 2, "use GET /api/support/status or /api/support/conversation");
                if (p[1].equals("status")) {
                    yield support.status();
                }
                require(p[1].equals("conversation"), "use GET /api/support/status or /api/support/conversation");
                yield support.conversation(selfCrewId(request));
            }
            case "POST support" -> {
                require(p.length == 2, "use POST /api/support/message or /api/support/clear");
                String crewId = selfCrewId(request);
                if (p[1].equals("clear")) {
                    support.clear(crewId);
                    yield Map.of("cleared", true);
                }
                require(p[1].equals("message"), "use POST /api/support/message or /api/support/clear");
                Map<String, Object> body = JsonParser.parseObject(new String(ex.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));
                if (!(body.get("text") instanceof String text)) {
                    throw new IllegalArgumentException("text must be a string");
                }
                yield Map.of("reply", support.send(crewId, text), "conversation", support.conversation(crewId));
            }
            case "GET stream" -> {
                require(p.length == 2 && p[1].equals("status"), "use GET /api/stream/status, /telemetry.csv or /recent.csv");
                yield streamer.status();
            }
            case "POST stream" -> {
                require(p.length == 2, "use POST /api/stream/start or /api/stream/stop");
                if (p[1].equals("stop")) {
                    yield streamer.stop();
                }
                require(p[1].equals("start"), "use POST /api/stream/start or /api/stream/stop");
                long interval = Long.parseLong(query.getOrDefault("intervalMs", "1000"));
                yield streamer.start(source(query), interval);
            }
            default -> throw new NoSuchElementException("no route " + method + " " + ex.getRequestURI().getPath());
        };
    }

    /**
     * Support conversations are psychological data (PSYCH policy): only the astronaut, as
     * viewer SELF with their own crewId, may use them. CMO, Crew Deck and emergency get 403.
     */
    private String selfCrewId(ViewRequest request) {
        if (request.viewer() != Viewer.SELF || request.viewerCrewId() == null) {
            throw new SecurityException("only the astronaut (viewer=SELF&crewId=…) can use their support companion");
        }
        String crewId = request.viewerCrewId();
        station.member(crewId).orElseThrow(() -> new NoSuchElementException("no crew member " + crewId));
        if (!station.allows(DataKind.PSYCH, request, crewId)) {
            throw new SecurityException("not allowed");
        }
        return crewId;
    }

    /** {@code source=sim} (default) or {@code source=replay&file=<name in data/stream/>}. */
    private TelemetrySource source(Map<String, String> query) throws IOException {
        String kind = query.getOrDefault("source", "sim");
        if (kind.equals("sim")) {
            return new SimulatedTelemetrySource(Long.parseLong(query.getOrDefault("seed", "20261115")));
        }
        require(kind.equals("replay"), "source must be sim or replay");
        String file = query.getOrDefault("file", "sample_telemetry.csv");
        require(file.matches("[A-Za-z0-9._-]+\\.csv"), "file must be a .csv name inside data/stream/");
        Path path = streamDir.resolve(file);
        if (!Files.isRegularFile(path)) {
            throw new NoSuchElementException("no file data/stream/" + file);
        }
        return new CsvReplaySource(path);
    }

    /** Vitals policy per row: CMO and emergency see every crew member, SELF only itself, Crew Deck nothing. */
    private Predicate<TelemetryRow> telemetryFilter(ViewRequest request) {
        if (request.viewer() == Viewer.CREW_DECK) {
            throw new SecurityException("the Crew Deck gets no vitals stream");
        }
        return row -> station.allows(DataKind.VITALS, request, row.crewId());
    }

    private ViewRequest viewRequest(HttpExchange ex) {
        Map<String, String> query = query(ex.getRequestURI().getRawQuery());
        return new ViewRequest(Viewer.parse(query.getOrDefault("viewer", "CREW_DECK")), query.get("crewId"));
    }

    /** Live CSV: header, then one line per row as it arrives, until the stream stops or {@code limit} rows. */
    private void streamCsv(HttpExchange ex) throws IOException {
        Map<String, String> query = query(ex.getRequestURI().getRawQuery());
        Predicate<TelemetryRow> filter = telemetryFilter(viewRequest(ex));
        long limit = Long.parseLong(query.getOrDefault("limit", String.valueOf(Long.MAX_VALUE)));
        ex.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(200, 0);
        try (TelemetryStreamer.Subscription sub = streamer.subscribe(filter); OutputStream out = ex.getResponseBody()) {
            out.write((TelemetryRow.HEADER + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            long sent = 0;
            while (sent < limit) {
                Optional<TelemetryRow> next = sub.poll(Duration.ofSeconds(30));
                if (next == null) {
                    continue;
                }
                if (next.isEmpty()) {
                    break;
                }
                out.write((next.get().toCsv() + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
                sent++;
            }
        } catch (IOException e) {
            // The client went away; nothing to do.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Snapshot CSV of the last {@code n} rows (default 100) this viewer may see. */
    private void recentCsv(HttpExchange ex) throws IOException {
        Map<String, String> query = query(ex.getRequestURI().getRawQuery());
        int n = Math.max(1, Math.min(500, Integer.parseInt(query.getOrDefault("n", "100"))));
        StringBuilder csv = new StringBuilder(TelemetryRow.HEADER).append('\n');
        for (TelemetryRow row : streamer.recent(n, telemetryFilter(viewRequest(ex)))) {
            csv.append(row.toCsv()).append('\n');
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static int score(Map<String, Object> body, String key) {
        if (!(body.get(key) instanceof Double d) || d < 0 || d > 100) {
            throw new IllegalArgumentException(key + " must be a number from 0 to 100");
        }
        return (int) Math.round(d);
    }

    private static double hours(Map<String, Object> body, String key) {
        if (!(body.get(key) instanceof Double d) || d < 0 || d > 24) {
            throw new IllegalArgumentException(key + " must be a number from 0 to 24");
        }
        return Math.round(d * 10) / 10.0;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> out = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            if (!value.isEmpty()) {
                out.put(key, value);
            }
        }
        return out;
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static void send(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}

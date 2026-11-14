package astratwin.web;

import astratwin.Station;
import astratwin.domain.AlertStatus;
import astratwin.domain.DataKind;
import astratwin.domain.Viewer;
import astratwin.json.Json;
import astratwin.json.JsonParser;
import astratwin.privacy.ViewRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * HTTP API on the JDK's built-in server (no dependencies, #33). Routes:
 * GET /api/health, /api/station, /api/scenarios, /api/crew/{id}/baselines, /api/crew/{id}/forecasts,
 * /api/environment/dose, /api/imm; POST /api/scenarios/{id}/start, /api/scenario/step,
 * /api/scenario/stop, /api/readings, /api/alerts/{id}/approve|reject.
 * Viewer: ?viewer=CMO|CREW_DECK|EMERGENCY|SELF&crewId=C1; the default is CREW_DECK (most restricted).
 * The demo console is served at /demo/.
 */
public final class ApiServer {
    private final Station station;
    private final HttpServer server;

    public ApiServer(Station station, String host, int port) throws IOException {
        this.station = station;
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
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

    private void handleRoot(HttpExchange ex) throws IOException {
        if (ex.getRequestURI().getPath().equals("/")) {
            redirect(ex, "/demo/");
        } else {
            send(ex, 404, Map.of("error", "not found"));
        }
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
                if (request.viewer() != Viewer.CMO) {
                    throw new SecurityException("only the CMO view can approve or reject");
                }
                AlertStatus status = switch (p[2]) {
                    case "approve" -> AlertStatus.APPROVED;
                    case "reject" -> AlertStatus.REJECTED;
                    default -> throw new IllegalArgumentException("use approve or reject");
                };
                yield station.setAlertStatus(p[1], status);
            }
            case "GET crew" -> {
                require(p.length == 3, "use GET /api/crew/{id}/baselines or /forecasts");
                if (!station.allows(DataKind.VITALS, request, p[1])) {
                    throw new SecurityException(request.viewer() + " may not see " + p[1] + "'s health data");
                }
                yield switch (p[2]) {
                    case "baselines" -> station.baselines(p[1]);
                    case "forecasts" -> station.forecasts(p[1]);
                    default -> throw new NoSuchElementException("no route " + ex.getRequestURI().getPath());
                };
            }
            case "GET environment" -> {
                require(p.length == 2 && p[1].equals("dose"), "use GET /api/environment/dose");
                yield station.doseSeries();
            }
            case "GET imm" -> station.imm();
            default -> throw new NoSuchElementException("no route " + method + " " + ex.getRequestURI().getPath());
        };
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

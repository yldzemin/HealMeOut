package astratwin;

import astratwin.cache.CacheLoader;
import astratwin.stream.SimulatedTelemetrySource;
import astratwin.stream.TelemetryStreamer;
import astratwin.support.GuidedResponder;
import astratwin.support.OllamaResponder;
import astratwin.support.SupportService;
import astratwin.web.ApiServer;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Starts the ASTRA-TWIN API:
 * {@code java -jar astra-twin-api.jar [--port 8080] [--host 127.0.0.1] [--data <data/cache>] [--stream sim]
 * [--llm-url http://127.0.0.1:11434] [--llm-model phi3.5] [--llm off] [--ui <frontend/dist>]}.
 */
public final class App {
    private static final String USAGE =
            "usage: astra-twin-api [--port 8080] [--host 127.0.0.1] [--data <path to data/cache>] [--stream sim]"
                    + " [--llm-url http://127.0.0.1:11434] [--llm-model phi3.5] [--llm off] [--ui <frontend/dist>]";

    private App() {
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String host = "127.0.0.1";
        Path data = null;
        boolean streamOnStart = false;
        String llmUrl = "http://127.0.0.1:11434";
        String llmModel = "phi3.5";
        boolean llmOn = true;
        Path ui = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port" -> port = Integer.parseInt(value(args, ++i));
                case "--host" -> host = value(args, ++i);
                case "--data" -> data = Path.of(value(args, ++i));
                case "--stream" -> streamOnStart = value(args, ++i).equals("sim");
                case "--llm-url" -> llmUrl = value(args, ++i);
                case "--llm-model" -> llmModel = value(args, ++i);
                case "--llm" -> llmOn = !value(args, ++i).equals("off");
                case "--ui" -> ui = Path.of(value(args, ++i));
                default -> {
                    System.err.println(USAGE);
                    System.exit(2);
                }
            }
        }
        if (data == null) {
            data = CacheLoader.findCacheDir(Path.of(""));
        }
        Path dataRoot = data.toAbsolutePath().normalize().getParent();
        Station station = Station.load(data);
        TelemetryStreamer streamer = new TelemetryStreamer(station, dataRoot.resolve("stream").resolve("out"));
        OllamaResponder llm = llmOn ? new OllamaResponder(URI.create(llmUrl), llmModel, Duration.ofSeconds(90)) : null;
        SupportService support = new SupportService(llm, new GuidedResponder(), station::latestCheckIn,
                station::raiseSupportRequest);
        ApiServer server = new ApiServer(station, streamer, dataRoot.resolve("stream"), support, host, port).serveUi(ui);
        server.start();
        if (streamOnStart) {
            streamer.start(new SimulatedTelemetrySource(20261115L), 1000);
        }
        System.out.println("ASTRA-TWIN API on http://" + host + ":" + server.port() + "  (data: " + data + ")");
        System.out.println("Demo console:  http://" + host + ":" + server.port() + "/demo/");
        if (ui != null) {
            System.out.println("Frontend:      http://" + host + ":" + server.port() + "/  (from " + ui + ")");
        }
        System.out.println("Support companion: " + (llm == null ? "guided (model off)"
                : llm.name() + (llm.available() ? " (ready)" : " (not reachable; guided fallback)")));
        System.out.println(Station.DISCLAIMER);
    }

    private static String value(String[] args, int i) {
        if (i >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
        }
        return args[i];
    }
}

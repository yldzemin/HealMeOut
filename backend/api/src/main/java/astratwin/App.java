package astratwin;

import astratwin.cache.CacheLoader;
import astratwin.web.ApiServer;
import java.nio.file.Path;

/** Starts the ASTRA-TWIN API: {@code java -jar astra-twin-api.jar [--port 8080] [--host 127.0.0.1] [--data <data/cache>]}. */
public final class App {
    private static final String USAGE = "usage: astra-twin-api [--port 8080] [--host 127.0.0.1] [--data <path to data/cache>]";

    private App() {
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String host = "127.0.0.1";
        Path data = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port" -> port = Integer.parseInt(value(args, ++i));
                case "--host" -> host = value(args, ++i);
                case "--data" -> data = Path.of(value(args, ++i));
                default -> {
                    System.err.println(USAGE);
                    System.exit(2);
                }
            }
        }
        if (data == null) {
            data = CacheLoader.findCacheDir(Path.of(""));
        }
        Station station = Station.load(data);
        ApiServer server = new ApiServer(station, host, port);
        server.start();
        System.out.println("ASTRA-TWIN API on http://" + host + ":" + server.port() + "  (data: " + data + ")");
        System.out.println("Demo console:  http://" + host + ":" + server.port() + "/demo/");
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

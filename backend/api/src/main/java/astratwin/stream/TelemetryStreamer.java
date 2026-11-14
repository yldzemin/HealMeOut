package astratwin.stream;

import astratwin.Station;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Pulls rows from a {@link TelemetrySource} at a fixed rate and, for every tick: feeds them to the
 * station (rules re-run), appends them to a CSV log, and pushes them to live subscribers.
 * Lock order is streamer → source, then streamer → station; the station never calls the streamer.
 */
public final class TelemetryStreamer implements AutoCloseable {
    public static final long MIN_INTERVAL_MS = 100;
    public static final long MAX_INTERVAL_MS = 60_000;
    private static final int RECENT = 500;
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    public record StreamStatus(boolean running, boolean finished, String source, long rows, long intervalMs,
            Instant startedAt, String logFile, int subscribers) {
    }

    private final Station station;
    private final Path logDir;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "telemetry");
        t.setDaemon(true);
        return t;
    });
    private final List<Subscription> subscribers = new CopyOnWriteArrayList<>();
    private final Deque<TelemetryRow> recent = new ArrayDeque<>();
    private volatile TelemetrySource observed;
    private TelemetrySource source;
    private String sourceName;
    private ScheduledFuture<?> task;
    private BufferedWriter log;
    private Path logFile;
    private long rows;
    private long intervalMs;
    private Instant startedAt;
    private boolean finished;

    public TelemetryStreamer(Station station, Path logDir) {
        this.station = station;
        this.logDir = logDir;
        station.addReadingListener(reading -> {
            TelemetrySource s = observed;
            if (s != null) {
                s.observe(reading);
            }
        });
    }

    /** Starts streaming from {@code newSource}; a running stream is stopped first. */
    public synchronized StreamStatus start(TelemetrySource newSource, long requestedIntervalMs) throws IOException {
        stopInternal();
        intervalMs = Math.max(MIN_INTERVAL_MS, Math.min(MAX_INTERVAL_MS, requestedIntervalMs));
        station.latestReadings().forEach(newSource::observe);
        Files.createDirectories(logDir);
        logFile = logDir.resolve("telemetry-" + FILE_TIME.format(Instant.now()) + ".csv");
        log = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
        log.write(TelemetryRow.HEADER);
        log.newLine();
        log.flush();
        source = newSource;
        sourceName = newSource.name();
        observed = newSource;
        rows = 0;
        finished = false;
        startedAt = Instant.now();
        recent.clear();
        task = scheduler.scheduleAtFixedRate(this::tickSafely, 0, intervalMs, TimeUnit.MILLISECONDS);
        return status();
    }

    public synchronized StreamStatus stop() {
        stopInternal();
        return status();
    }

    public synchronized StreamStatus status() {
        return new StreamStatus(task != null, finished, sourceName, rows, intervalMs, startedAt,
                logFile == null ? null : logFile.toString(), subscribers.size());
    }

    /** A live subscription; rows the filter rejects are never queued. Ends when the stream stops. */
    public synchronized Subscription subscribe(Predicate<TelemetryRow> filter) {
        Subscription s = new Subscription(filter);
        subscribers.add(s);
        if (task == null) {
            s.end();
        }
        return s;
    }

    /** The last {@code n} rows the filter accepts, oldest first. */
    public synchronized List<TelemetryRow> recent(int n, Predicate<TelemetryRow> filter) {
        List<TelemetryRow> accepted = recent.stream().filter(filter).toList();
        return accepted.subList(Math.max(0, accepted.size() - n), accepted.size());
    }

    /** Runs one tick now (used by tests and by the scheduler). */
    public synchronized void tick() {
        if (source == null) {
            return;
        }
        Optional<List<TelemetryRow>> next = source.nextTick(Instant.now());
        if (next.isEmpty()) {
            finished = true;
            stopInternal();
            return;
        }
        List<TelemetryRow> batch = next.get();
        String note = "Live telemetry (" + sourceName + ")";
        station.ingestTelemetry(batch.stream().map(r -> r.toReading(note)).toList());
        try {
            for (TelemetryRow r : batch) {
                log.write(r.toCsv());
                log.newLine();
                recent.addLast(r);
                if (recent.size() > RECENT) {
                    recent.removeFirst();
                }
                subscribers.forEach(s -> s.offer(r));
                rows++;
            }
            log.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void tickSafely() {
        try {
            tick();
        } catch (RuntimeException e) {
            System.err.println("telemetry tick failed: " + e);
        }
    }

    private void stopInternal() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        observed = null;
        source = null;
        if (log != null) {
            try {
                log.close();
            } catch (IOException e) {
                System.err.println("cannot close telemetry log: " + e);
            }
            log = null;
        }
        subscribers.forEach(Subscription::end);
    }

    @Override
    public void close() {
        stop();
        scheduler.shutdownNow();
    }

    /** Rows for one HTTP client. {@link #poll} returns null on timeout and empty when the stream ended. */
    public final class Subscription implements AutoCloseable {
        private final BlockingQueue<Optional<TelemetryRow>> queue = new LinkedBlockingQueue<>(10_000);
        private final Predicate<TelemetryRow> filter;

        private Subscription(Predicate<TelemetryRow> filter) {
            this.filter = filter;
        }

        public Optional<TelemetryRow> poll(Duration timeout) throws InterruptedException {
            return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        void offer(TelemetryRow row) {
            if (filter.test(row)) {
                queue.offer(Optional.of(row));
            }
        }

        void end() {
            queue.offer(Optional.empty());
        }

        @Override
        public void close() {
            subscribers.remove(this);
        }
    }
}

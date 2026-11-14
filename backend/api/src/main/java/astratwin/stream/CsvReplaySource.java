package astratwin.stream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Replays a telemetry CSV ({@link TelemetryRow#HEADER}) as a live stream: every distinct timestamp
 * in the file is one tick, re-stamped to "now" so it looks live.
 */
public final class CsvReplaySource implements TelemetrySource {
    private final String name;
    private final List<List<TelemetryRow>> ticks;
    private int index;

    public CsvReplaySource(Path file) throws IOException {
        this.name = "replay:" + file.getFileName();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).replace("﻿", "").trim().equals(TelemetryRow.HEADER)) {
            throw new IllegalArgumentException(file + ": first line must be " + TelemetryRow.HEADER);
        }
        Map<Instant, List<TelemetryRow>> byTime = new LinkedHashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            if (!line.isBlank()) {
                TelemetryRow row = TelemetryRow.parse(line.trim());
                byTime.computeIfAbsent(row.timestamp(), t -> new ArrayList<>()).add(row);
            }
        }
        this.ticks = List.copyOf(byTime.values());
    }

    @Override
    public String name() {
        return name;
    }

    public int size() {
        return ticks.size();
    }

    @Override
    public synchronized Optional<List<TelemetryRow>> nextTick(Instant now) {
        if (index >= ticks.size()) {
            return Optional.empty();
        }
        return Optional.of(ticks.get(index++).stream().map(r -> r.at(now)).toList());
    }
}

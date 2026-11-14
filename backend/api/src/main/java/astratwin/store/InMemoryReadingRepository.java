package astratwin.store;

import astratwin.domain.Reading;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps the most recent {@code capacity} readings as history, plus the latest reading of every
 * crew × metric, so a live stream cannot push slow metrics (glucose, blood pressure) out of view.
 */
public final class InMemoryReadingRepository implements ReadingRepository {
    private final int capacity;
    private final Deque<Reading> history = new ArrayDeque<>();
    private final Map<String, Reading> latest = new HashMap<>();

    public InMemoryReadingRepository() {
        this(Integer.MAX_VALUE);
    }

    public InMemoryReadingRepository(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    @Override
    public void add(Reading reading) {
        history.addLast(reading);
        while (history.size() > capacity) {
            history.removeFirst();
        }
        latest.put(key(reading.crewId(), reading.metric()), reading);
    }

    @Override
    public List<Reading> all() {
        return List.copyOf(history);
    }

    @Override
    public Optional<Reading> latest(String crewId, String metric) {
        return Optional.ofNullable(latest.get(key(crewId, metric)));
    }

    @Override
    public void clear() {
        history.clear();
        latest.clear();
    }

    private static String key(String crewId, String metric) {
        return crewId + "|" + metric;
    }
}

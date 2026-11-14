package astratwin.store;

import astratwin.domain.Reading;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InMemoryReadingRepository implements ReadingRepository {
    private final List<Reading> readings = new ArrayList<>();

    @Override
    public void add(Reading reading) {
        readings.add(reading);
    }

    @Override
    public List<Reading> all() {
        return List.copyOf(readings);
    }

    @Override
    public Optional<Reading> latest(String crewId, String metric) {
        for (int i = readings.size() - 1; i >= 0; i--) {
            Reading r = readings.get(i);
            if (r.crewId().equals(crewId) && r.metric().equals(metric)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    @Override
    public void clear() {
        readings.clear();
    }
}

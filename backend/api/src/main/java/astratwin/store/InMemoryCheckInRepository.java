package astratwin.store;

import astratwin.domain.CheckIn;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InMemoryCheckInRepository implements CheckInRepository {
    private final List<CheckIn> checkIns = new ArrayList<>();

    @Override
    public void add(CheckIn checkIn) {
        checkIns.removeIf(c -> c.crewId().equals(checkIn.crewId()) && c.date().equals(checkIn.date()));
        checkIns.add(checkIn);
        checkIns.sort(Comparator.comparing(CheckIn::date)); // ISO dates sort as text
    }

    @Override
    public List<CheckIn> forCrew(String crewId) {
        return checkIns.stream().filter(c -> c.crewId().equals(crewId)).toList();
    }

    @Override
    public void clear() {
        checkIns.clear();
    }
}

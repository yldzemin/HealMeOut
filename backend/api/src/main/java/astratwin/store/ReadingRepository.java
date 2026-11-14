package astratwin.store;

import astratwin.domain.Reading;
import java.util.List;
import java.util.Optional;

public interface ReadingRepository {
    void add(Reading reading);

    List<Reading> all();

    /** Most recently added reading of this metric for this crew member. */
    Optional<Reading> latest(String crewId, String metric);

    void clear();
}

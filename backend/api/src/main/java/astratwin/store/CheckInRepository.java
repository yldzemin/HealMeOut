package astratwin.store;

import astratwin.domain.CheckIn;
import java.util.List;

public interface CheckInRepository {
    /** One check-in per crew member and date: a later one replaces the earlier. */
    void add(CheckIn checkIn);

    /** In date order, oldest first. */
    List<CheckIn> forCrew(String crewId);

    void clear();
}

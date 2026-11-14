package astratwin.store;

import astratwin.domain.CheckIn;
import java.util.List;

public interface CheckInRepository {
    void add(CheckIn checkIn);

    List<CheckIn> forCrew(String crewId);

    void clear();
}

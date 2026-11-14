package astratwin.store;

import astratwin.domain.CrewMember;
import java.util.List;
import java.util.Optional;

public interface CrewRepository {
    List<CrewMember> all();

    Optional<CrewMember> find(String id);

    void save(CrewMember member);
}

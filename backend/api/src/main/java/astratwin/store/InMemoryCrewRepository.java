package astratwin.store;

import astratwin.domain.CrewMember;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCrewRepository implements CrewRepository {
    private final Map<String, CrewMember> byId = new LinkedHashMap<>();

    @Override
    public List<CrewMember> all() {
        return List.copyOf(byId.values());
    }

    @Override
    public Optional<CrewMember> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void save(CrewMember member) {
        byId.put(member.id(), member);
    }
}

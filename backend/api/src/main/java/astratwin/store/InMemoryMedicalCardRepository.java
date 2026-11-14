package astratwin.store;

import astratwin.domain.MedicalCard;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryMedicalCardRepository implements MedicalCardRepository {
    private final Map<String, MedicalCard> byCrew = new HashMap<>();

    @Override
    public Optional<MedicalCard> find(String crewId) {
        return Optional.ofNullable(byCrew.get(crewId));
    }

    @Override
    public void save(MedicalCard card) {
        byCrew.put(card.crewId(), card);
    }
}

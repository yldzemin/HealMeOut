package astratwin.store;

import astratwin.domain.MedicalCard;
import java.util.Optional;

public interface MedicalCardRepository {
    Optional<MedicalCard> find(String crewId);

    void save(MedicalCard card);
}

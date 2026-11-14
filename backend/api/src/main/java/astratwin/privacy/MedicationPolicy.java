package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import astratwin.domain.Viewer;
import java.util.Optional;

/** Medication: self, CMO and ground; Crew Deck only if the astronaut opted in; emergency view (card only). */
public final class MedicationPolicy implements ViewPolicy {
    @Override
    public DataKind kind() {
        return DataKind.MEDICATION;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return switch (request.viewer()) {
            case SELF -> request.isSelf(subjectCrewId);
            case CMO, GROUND, EMERGENCY -> true;
            case CREW_DECK -> card.map(MedicalCard::shareWithCrew).orElse(false);
        };
    }
}

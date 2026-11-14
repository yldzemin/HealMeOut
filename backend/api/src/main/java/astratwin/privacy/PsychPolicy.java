package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import java.util.Optional;

/**
 * Psychological check-ins: only the astronaut. Never the Crew Deck or the emergency view; the CMO
 * sees them only if the astronaut shares them with the behavioral health provider (not built).
 * NASA-STD-3001 Vol 1 V1 6010/6011.
 */
public final class PsychPolicy implements ViewPolicy {
    @Override
    public DataKind kind() {
        return DataKind.PSYCH;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return request.isSelf(subjectCrewId);
    }
}

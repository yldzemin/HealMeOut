package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import astratwin.domain.Viewer;
import java.util.Optional;

/**
 * "Needs support now" alerts from the support companion (#51): the astronaut and the CMO on board.
 * Not ground, the Crew Deck or the emergency view. The alert never carries the message itself.
 */
public final class SupportPolicy implements ViewPolicy {
    @Override
    public DataKind kind() {
        return DataKind.SUPPORT;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return request.isSelf(subjectCrewId) || request.viewer() == Viewer.CMO;
    }
}

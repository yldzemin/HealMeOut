package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import astratwin.domain.Viewer;
import java.util.Optional;

/** Support and schedule plans for one crew member: the astronaut, the CMO and ground (who approves). */
public final class PlanPolicy implements ViewPolicy {
    @Override
    public DataKind kind() {
        return DataKind.PLAN;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return request.isSelf(subjectCrewId) || request.viewer() == Viewer.CMO || request.viewer() == Viewer.GROUND;
    }
}

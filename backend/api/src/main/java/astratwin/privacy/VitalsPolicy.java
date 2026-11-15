package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import astratwin.domain.Viewer;
import java.util.Optional;

/** Vitals detail: self, CMO, ground and emergency view; never the Crew Deck. */
public final class VitalsPolicy implements ViewPolicy {
    @Override
    public DataKind kind() {
        return DataKind.VITALS;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return request.isSelf(subjectCrewId) || request.viewer() == Viewer.CMO || request.viewer() == Viewer.GROUND
                || request.viewer() == Viewer.EMERGENCY;
    }
}

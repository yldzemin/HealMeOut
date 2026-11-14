package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import java.util.Optional;

/** Decides whether one kind of data about one crew member may be shown (BACKEND §6). */
public interface ViewPolicy {
    DataKind kind();

    boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card);
}

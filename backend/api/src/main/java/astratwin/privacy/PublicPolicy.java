package astratwin.privacy;

import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import java.util.Optional;

/** Status level, module, station environment and emergency broadcasts: everyone sees them. */
public final class PublicPolicy implements ViewPolicy {
    private final DataKind kind;

    public PublicPolicy(DataKind kind) {
        this.kind = kind;
    }

    @Override
    public DataKind kind() {
        return kind;
    }

    @Override
    public boolean allows(ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return true;
    }
}

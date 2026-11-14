package astratwin.privacy;

import astratwin.domain.Alert;
import astratwin.domain.CheckIn;
import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.MedicalCard;
import astratwin.domain.MissionTag;
import astratwin.domain.Reading;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Server-side privacy filter (BACKEND §6): every view of crew data and alerts goes through it. */
public final class PrivacyFilter {
    private final Map<DataKind, ViewPolicy> policies = new EnumMap<>(DataKind.class);

    public PrivacyFilter(List<ViewPolicy> policyList) {
        for (ViewPolicy p : policyList) {
            policies.put(p.kind(), p);
        }
        for (DataKind kind : DataKind.values()) {
            if (!policies.containsKey(kind)) {
                throw new IllegalArgumentException("no privacy policy for " + kind);
            }
        }
    }

    public static PrivacyFilter standard() {
        return new PrivacyFilter(List.of(new PublicPolicy(DataKind.STATUS), new PublicPolicy(DataKind.ENVIRONMENT),
                new PublicPolicy(DataKind.EMERGENCY), new VitalsPolicy(), new MedicationPolicy(), new PsychPolicy()));
    }

    public boolean allows(DataKind kind, ViewRequest request, String subjectCrewId, Optional<MedicalCard> card) {
        return policies.get(kind).allows(request, subjectCrewId, card);
    }

    /** Builds a crew tile with only the parts this viewer may see. */
    public CrewCard card(ViewRequest request, CrewMember member, MissionTag mission, List<Reading> vitals,
            Optional<MedicalCard> medical, List<CheckIn> checkIns) {
        CrewCard card = CrewCard.of(member, mission);
        if (allows(DataKind.VITALS, request, member.id(), medical)) {
            card = card.withVitals(vitals);
        }
        if (medical.isPresent() && allows(DataKind.MEDICATION, request, member.id(), medical)) {
            card = card.withMedical(medical.get());
        }
        if (allows(DataKind.PSYCH, request, member.id(), medical)) {
            card = card.withCheckIns(checkIns);
        }
        return card;
    }

    /** Alerts this viewer may see; station-wide alerts use their category's policy too. */
    public List<Alert> alerts(ViewRequest request, List<Alert> alerts, Function<String, Optional<MedicalCard>> cards) {
        return alerts.stream()
                .filter(a -> allows(a.category(), request, a.crewId(), cards.apply(a.crewId())))
                .toList();
    }
}

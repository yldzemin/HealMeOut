package astratwin.domain;

import java.util.List;

/** Emergency card: allergies and current medication only (BACKEND §6). */
public record MedicalCard(
        String crewId, List<String> allergies, List<String> currentMedication,
        boolean shareWithCrew, String provenance) {
}

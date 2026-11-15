package astratwin.privacy;

import astratwin.domain.CheckIn;
import astratwin.domain.CrewMember;
import astratwin.domain.Level;
import astratwin.domain.MedicalCard;
import astratwin.domain.MissionTag;
import astratwin.domain.Reading;
import astratwin.domain.Role;
import java.util.List;

/** One crew tile after the privacy filter. Fields the viewer may not see are null (left out of JSON). */
public record CrewCard(
        String id, String callsign, Role role, String module, Level level, String baselineProvenance,
        MissionTag baselineMission, List<Reading> vitals, MedicalCard medical, List<CheckIn> checkIns) {

    static CrewCard of(CrewMember m, MissionTag mission) {
        return new CrewCard(m.id(), m.callsign(), m.role(), m.module(), m.level(), m.baselineProvenance(), mission,
                null, null, null);
    }

    CrewCard withVitals(List<Reading> v) {
        return new CrewCard(id, callsign, role, module, level, baselineProvenance, baselineMission, v, medical, checkIns);
    }

    CrewCard withMedical(MedicalCard c) {
        return new CrewCard(id, callsign, role, module, level, baselineProvenance, baselineMission, vitals, c, checkIns);
    }

    CrewCard withCheckIns(List<CheckIn> c) {
        return new CrewCard(id, callsign, role, module, level, baselineProvenance, baselineMission, vitals, medical, c);
    }
}

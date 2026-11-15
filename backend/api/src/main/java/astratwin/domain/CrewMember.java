package astratwin.domain;

/** A member of the 7-person ISS crew. Callsigns are fictional (TECH_OVERVIEW §4.4). */
public record CrewMember(String id, String callsign, Role role, String module, Level level, String baselineProvenance) {

    public CrewMember withLevel(Level newLevel) {
        return new CrewMember(id, callsign, role, module, newLevel, baselineProvenance);
    }

    public boolean isCmo() {
        return role == Role.CMO1 || role == Role.CMO2;
    }
}

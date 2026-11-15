package astratwin.support;

import astratwin.domain.CrewPersona;
import java.util.List;

/**
 * What the support companion knows about the crew member it talks to: who they are (call sign, role,
 * module), their own profile and a plain summary of their last 3 days (check-ins, vitals, open health
 * notes). The astronaut sees the same text in the app, so nothing is hidden from them.
 */
public record CompanionContext(String crewId, String callsign, String role, String module, CrewPersona persona,
        List<String> recent) {

    /** "LYRA (C3), crew member in the Columbus module". */
    public String who() {
        return callsign + " (" + crewId + "), " + role + " in the " + module + " module";
    }
}

package astratwin.support;

import astratwin.domain.CrewPersona;
import java.util.List;

/**
 * What the support companion knows about the crew member it talks to: their own profile and a plain
 * summary of their last 3 days (check-ins, vitals, open health notes). The astronaut sees the same
 * text in the app, so nothing is hidden from them.
 */
public record CompanionContext(CrewPersona persona, List<String> recent) {
}

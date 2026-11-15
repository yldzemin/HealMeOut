package astratwin.domain;

import java.util.List;

/**
 * A crew member's own profile for the support companion: personality, hobbies, what helps them, faith,
 * worries and the style they like. Fictional and set "by the astronaut before flight". Private: only
 * the astronaut and the on-station companion use it (BACKEND §6, §10). {@code faith} is null for a
 * crew member who is not religious; the companion then never brings up faith or prayer.
 * {@code helps} items read naturally in "X helps you".
 */
public record CrewPersona(
        String crewId, List<String> traits, List<String> hobbies, List<String> helps, String faith,
        List<String> worries, String style, String family, String provenance) {

    public boolean religious() {
        return faith != null;
    }
}

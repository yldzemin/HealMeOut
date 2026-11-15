package astratwin.scenario;

import java.util.List;

/** A scripted timeline (BACKEND §5). Each scenario is its own class. */
public interface Scenario {
    String id();

    String title();

    /** MVP, Target or Extra (TECH_OVERVIEW §6). */
    String priority();

    String source();

    List<ScenarioStep> steps();

    /**
     * Crew id of the astronaut whose private data the scenario is about, or null. Everyone else sees
     * only the public title and each step's public view.
     */
    default String privateTo() {
        return null;
    }

    /** Title shown to everyone but {@link #privateTo()}. */
    default String publicTitle() {
        return title();
    }
}

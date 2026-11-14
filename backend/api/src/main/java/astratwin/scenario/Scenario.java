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
}

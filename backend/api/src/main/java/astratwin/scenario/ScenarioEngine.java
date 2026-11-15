package astratwin.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Steps through one scenario at a time; knows only the {@link Scenario} interface. */
public final class ScenarioEngine {
    private final Map<String, Scenario> scenarios = new LinkedHashMap<>();
    private Scenario active;
    private List<ScenarioStep> steps = List.of();
    private int index = -1;
    private final Map<String, String> roles = new LinkedHashMap<>();
    private final List<String> checklist = new ArrayList<>();
    private final List<String> log = new ArrayList<>();
    private String banner;

    public ScenarioEngine(List<Scenario> list) {
        for (Scenario s : list) {
            scenarios.put(s.id(), s);
        }
    }

    public List<ScenarioSummary> list() {
        return scenarios.values().stream()
                .map(s -> new ScenarioSummary(s.id(), s.title(), s.priority(), s.steps().size(), s.source()))
                .toList();
    }

    /** Starts a scenario and returns its first step. */
    public ScenarioStep start(String id) {
        Scenario scenario = scenarios.get(id);
        if (scenario == null) {
            throw new NoSuchElementException("no scenario '" + id + "'");
        }
        reset();
        active = scenario;
        steps = scenario.steps();
        return next().orElseThrow(() -> new IllegalStateException("scenario '" + id + "' has no steps"));
    }

    /** Advances one step; empty when the scenario has finished. */
    public Optional<ScenarioStep> next() {
        if (active == null || index + 1 >= steps.size()) {
            return Optional.empty();
        }
        ScenarioStep step = steps.get(++index);
        roles.putAll(step.roles());
        checklist.addAll(step.checklist());
        if (step.banner() != null) {
            banner = step.banner();
        }
        log.add(step.at() + " — " + step.title());
        return Optional.of(step);
    }

    public void reset() {
        active = null;
        steps = List.of();
        index = -1;
        roles.clear();
        checklist.clear();
        log.clear();
        banner = null;
    }

    public Optional<ScenarioState> state() {
        if (active == null || index < 0) {
            return Optional.empty();
        }
        ScenarioStep s = steps.get(index);
        return Optional.of(new ScenarioState(active.id(), active.title(), index + 1, steps.size(), s.at(), s.title(),
                s.detail(), Map.copyOf(roles), List.copyOf(checklist), banner, s.evidence(), List.copyOf(log),
                index + 1 >= steps.size()));
    }

    /** Crew id the running scenario is private to, or null. */
    public Optional<String> privateTo() {
        return Optional.ofNullable(active).map(Scenario::privateTo);
    }

    /**
     * The running scenario as everyone but its astronaut sees it: public titles and details only, no
     * roles, checklist, banner or evidence. Same as {@link #state()} for a scenario that is not private.
     */
    public Optional<ScenarioState> publicState() {
        if (active == null || active.privateTo() == null) {
            return state();
        }
        if (index < 0) {
            return Optional.empty();
        }
        ScenarioStep s = steps.get(index);
        List<String> publicLog = steps.subList(0, index + 1).stream().map(x -> x.at() + " — " + x.publicTitle()).toList();
        return Optional.of(new ScenarioState(active.id(), active.publicTitle(), index + 1, steps.size(), s.at(),
                s.publicTitle(), s.publicDetail(), Map.of(), List.of(), null, null, publicLog,
                index + 1 >= steps.size()));
    }
}

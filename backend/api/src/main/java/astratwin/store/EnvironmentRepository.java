package astratwin.store;

import astratwin.domain.Environment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface EnvironmentRepository {
    void addAll(Collection<Environment> samples);

    List<Environment> all();

    /** Most recently added sample that matches, e.g. one that has a CO2 value. */
    Optional<Environment> latest(Predicate<Environment> filter);

    void clear();
}

package astratwin.store;

import astratwin.domain.Environment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class InMemoryEnvironmentRepository implements EnvironmentRepository {
    private final List<Environment> samples = new ArrayList<>();

    @Override
    public void addAll(Collection<Environment> newSamples) {
        samples.addAll(newSamples);
    }

    @Override
    public List<Environment> all() {
        return List.copyOf(samples);
    }

    @Override
    public Optional<Environment> latest(Predicate<Environment> filter) {
        for (int i = samples.size() - 1; i >= 0; i--) {
            if (filter.test(samples.get(i))) {
                return Optional.of(samples.get(i));
            }
        }
        return Optional.empty();
    }

    @Override
    public void clear() {
        samples.clear();
    }
}

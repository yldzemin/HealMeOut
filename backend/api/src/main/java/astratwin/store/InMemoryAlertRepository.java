package astratwin.store;

import astratwin.domain.Alert;
import astratwin.domain.AlertStatus;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class InMemoryAlertRepository implements AlertRepository {
    private final Map<String, Alert> byId = new LinkedHashMap<>();

    @Override
    public void sync(Collection<Alert> firing) {
        Set<String> firingIds = firing.stream().map(Alert::id).collect(Collectors.toSet());
        byId.replaceAll((id, a) -> firingIds.contains(id) ? a : a.withActive(false));
        for (Alert alert : firing) {
            Alert previous = byId.get(alert.id());
            if (previous == null) {
                byId.put(alert.id(), alert);
            } else {
                // Keep the CMO's decision and the first time the alert fired.
                byId.put(alert.id(), new Alert(alert.id(), alert.crewId(), alert.level(), alert.title(),
                        alert.explanation(), alert.recommendation(), alert.evidence(), alert.source(),
                        previous.status(), alert.category(), true, previous.createdAt()));
            }
        }
    }

    @Override
    public List<Alert> all() {
        return List.copyOf(byId.values());
    }

    @Override
    public Optional<Alert> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Alert setStatus(String id, AlertStatus status) {
        Alert alert = find(id).orElseThrow(() -> new NoSuchElementException("no alert " + id)).withStatus(status);
        byId.put(id, alert);
        return alert;
    }

    @Override
    public void clear() {
        byId.clear();
    }
}

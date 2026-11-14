package astratwin.store;

import astratwin.domain.Alert;
import astratwin.domain.AlertStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    /**
     * Stores the alerts produced by one rules evaluation. Existing alerts keep their status
     * (a CMO decision survives re-evaluation); alerts that no longer fire become inactive.
     */
    void sync(Collection<Alert> firing);

    List<Alert> all();

    Optional<Alert> find(String id);

    Alert setStatus(String id, AlertStatus status);

    void clear();
}

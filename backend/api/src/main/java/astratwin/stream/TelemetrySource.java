package astratwin.stream;

import astratwin.domain.Reading;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Where live telemetry comes from. One call = one tick of the stream. */
public interface TelemetrySource {
    /** Short name shown in the stream status, e.g. {@code simulated} or {@code replay:sample_telemetry.csv}. */
    String name();

    /** Rows for this tick; {@link Optional#empty()} when the source has finished. */
    Optional<List<TelemetryRow>> nextTick(Instant now);

    /** A non-telemetry reading (nominal, scenario, manual) arrived; sources may use it as a new target. */
    default void observe(Reading reading) {
    }
}

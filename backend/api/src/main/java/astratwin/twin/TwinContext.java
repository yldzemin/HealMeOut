package astratwin.twin;

import astratwin.domain.Environment;
import astratwin.domain.Reading;
import java.time.Instant;
import java.util.List;

/** What a forecast model can see. */
public record TwinContext(List<Environment> environment, List<Reading> readings, Instant now) {
}

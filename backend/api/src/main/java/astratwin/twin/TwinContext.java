package astratwin.twin;

import astratwin.domain.CheckIn;
import astratwin.domain.Environment;
import astratwin.domain.Reading;
import java.time.Instant;
import java.util.List;

/** What a forecast model can see. Check-ins are the subject's own; only PSYCH-kind models read them. */
public record TwinContext(List<Environment> environment, List<Reading> readings, List<CheckIn> checkIns, Instant now) {
}

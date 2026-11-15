package astratwin.twin;

import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Forecast;
import java.util.Optional;

/** One Earth Twin forecast (BACKEND §3.2). Every model labels its output "modelled". */
public interface ForecastModel {
    String metric();

    /** Which privacy policy decides who sees this forecast (BACKEND §6). */
    default DataKind kind() {
        return DataKind.VITALS;
    }

    Optional<Forecast> forecast(TwinContext context, CrewMember member, int horizonHours);
}

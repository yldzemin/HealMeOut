package astratwin.twin;

import astratwin.domain.CrewMember;
import astratwin.domain.Forecast;
import java.util.Optional;

/** One Earth Twin forecast (BACKEND §3.2). Every model labels its output "modelled". */
public interface ForecastModel {
    String metric();

    Optional<Forecast> forecast(TwinContext context, CrewMember member, int horizonHours);
}

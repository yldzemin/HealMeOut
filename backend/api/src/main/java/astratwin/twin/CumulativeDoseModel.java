package astratwin.twin;

import astratwin.domain.CrewMember;
import astratwin.domain.Forecast;
import astratwin.domain.Forecast.ForecastPoint;
import java.util.List;
import java.util.Optional;

/**
 * Cumulative dose so far plus the last 24-h mean rate projected over the horizon.
 * Instrument dose equivalent (ICRP-60), not effective dose; compared with the 600 mSv career limit.
 */
public final class CumulativeDoseModel implements ForecastModel {
    static final String EVIDENCE = "Instrument dose equivalent (ICRP-60) from the ISS TEPC, not effective dose. "
            + "Projection: last 24-h mean rate × horizon. NASA's ISS reference is about 0.2–0.5 mSv/day effective "
            + "(Simonsen 2025). Career limit 600 mSv (NASA-STD-3001 V1 4030).";
    static final String SOURCE = "CDAWeb ISS_DOSANL_TEPC (NASA SRAG); Simonsen 2025, NTRS 20250006168";

    @Override
    public String metric() {
        return "cumulative_dose";
    }

    @Override
    public Optional<Forecast> forecast(TwinContext context, CrewMember member, int horizonHours) {
        DoseSeries series = new DoseSeries(context.environment());
        Optional<Double> rate = series.last24hMean();
        if (rate.isEmpty()) {
            return Optional.empty();
        }
        double now = series.cumulative_mSv();
        double later = now + rate.get() * horizonHours / 1000.0;
        List<ForecastPoint> points = List.of(new ForecastPoint("T+0", round(now)),
                new ForecastPoint("T+" + horizonHours, round(later)));
        String evidence = "Cumulative since the first loaded sample (" + series.start() + "), not the whole mission. "
                + EVIDENCE;
        return Optional.of(new Forecast(member.id(), horizonHours, metric(), "mSv", points, "modelled", evidence, SOURCE));
    }

    private static double round(double v) {
        return Math.round(v * 1000) / 1000.0;
    }
}

package astratwin.twin;

import astratwin.domain.CrewMember;
import astratwin.domain.Forecast;
import java.util.ArrayList;
import java.util.List;

/** Runs every forecast model for the T+6 and T+12 horizons (BACKEND §3.2). */
public final class TwinEngine {
    public static final List<Integer> HORIZONS = List.of(6, 12);
    private final List<ForecastModel> models;

    public TwinEngine(List<ForecastModel> models) {
        this.models = List.copyOf(models);
    }

    public List<Forecast> forecasts(TwinContext context, CrewMember member) {
        List<Forecast> out = new ArrayList<>();
        for (ForecastModel model : models) {
            for (int horizon : HORIZONS) {
                model.forecast(context, member, horizon).ifPresent(out::add);
            }
        }
        return out;
    }
}

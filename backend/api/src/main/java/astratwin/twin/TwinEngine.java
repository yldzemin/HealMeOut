package astratwin.twin;

import astratwin.domain.CrewMember;
import astratwin.domain.DataKind;
import astratwin.domain.Forecast;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Runs every forecast model for the T+6 and T+12 horizons (BACKEND §3.2). */
public final class TwinEngine {
    public static final List<Integer> HORIZONS = List.of(6, 12);
    private final List<ForecastModel> models;

    public TwinEngine(List<ForecastModel> models) {
        this.models = List.copyOf(models);
    }

    /** Forecasts of the models whose data kind the viewer may see; the others are not run. */
    public List<Forecast> forecasts(TwinContext context, CrewMember member, Predicate<DataKind> visible) {
        List<Forecast> out = new ArrayList<>();
        for (ForecastModel model : models) {
            if (!visible.test(model.kind())) {
                continue;
            }
            for (int horizon : HORIZONS) {
                model.forecast(context, member, horizon).ifPresent(out::add);
            }
        }
        return out;
    }
}

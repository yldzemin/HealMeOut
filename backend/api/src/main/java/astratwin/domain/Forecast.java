package astratwin.domain;

import java.util.List;

/** Earth Twin forecast for T+6 h or T+12 h; always labelled "modelled" (BACKEND §3.2). */
public record Forecast(
        String crewId, int horizonHours, String metric, String unit, List<ForecastPoint> points,
        String label, String evidence, String source) {

    public record ForecastPoint(String at, double value) {
    }
}

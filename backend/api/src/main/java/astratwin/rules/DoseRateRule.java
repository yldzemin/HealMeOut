package astratwin.rules;

import astratwin.domain.Alert;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import astratwin.twin.DoseSeries;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** 24-h mean dose-equivalent rate > 1.5× the preceding 7-day median → L2, shelter advice (BACKEND §4, #30). */
public final class DoseRateRule implements Rule {
    public static final double THRESHOLD = 1.5;
    static final String EVIDENCE = "Our design (#30): the last 24-h mean is compared with the median of the 7 daily "
            + "means before it. Calibration on Sep 2017 ISS TEPC data: normal days 0.86–1.04×, Sep 11 (solar particle "
            + "event) 1.90×. Single hours are not used: South Atlantic Anomaly passes reach 183 μSv/h on normal days. "
            + "Values are instrument dose equivalent (ICRP-60), not effective dose.";
    static final String SOURCE = "CDAWeb ISS_DOSANL_TEPC (NASA SRAG); NASA_DATA_SOURCES.md §4.3";

    @Override
    public String id() {
        return "dose-rate";
    }

    @Override
    public List<Alert> evaluate(RuleContext ctx) {
        DoseSeries series = new DoseSeries(ctx.environment());
        Optional<Double> ratio = series.ratioToWeeklyMedian();
        if (ratio.isEmpty() || ratio.get() <= THRESHOLD) {
            return List.of();
        }
        String explanation = String.format(Locale.ROOT,
                "Dose-equivalent rate over the last 24 h is %.0f μSv/h, %.2f× the weekly median.",
                series.last24hMean().orElse(Double.NaN), ratio.get());
        return List.of(Alert.open(id() + ":" + Alert.STATION, Alert.STATION, Level.L2,
                "Radiation dose rate elevated (possible solar particle event)", explanation,
                "Move to the most shielded area per flight rules; avoid EVA. Ground informed.",
                EVIDENCE, SOURCE, DataKind.ENVIRONMENT, ctx.now()));
    }
}

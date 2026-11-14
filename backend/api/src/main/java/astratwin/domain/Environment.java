package astratwin.domain;

import java.time.Instant;

/**
 * Cabin environment or radiation sample (TECH_OVERVIEW §4.2). Unused fields are null.
 * Dose: {@code doseRate_uGy_h} is absorbed dose, {@code doseEqRate_uSv_h} is ICRP-60 dose
 * equivalent at the instrument, not effective dose.
 */
public record Environment(
        Double co2_mmHg, Double temp_C, Double rh_pct, Double doseRate_uGy_h, Double doseEqRate_uSv_h,
        String instrument, Instant timestamp, String provenance) {

    public static Environment co2(double mmHg, Instant at, String provenance) {
        return new Environment(mmHg, null, null, null, null, null, at, provenance);
    }
}

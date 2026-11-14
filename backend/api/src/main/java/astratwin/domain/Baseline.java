package astratwin.domain;

/** One crew × analyte × timepoint value from NASA OSDR (TECH_OVERVIEW §4.2). */
public record Baseline(
        String crewId, String source, String analyte, String timepoint, double value,
        String unit, Double refLow, Double refHigh, MissionTag mission) {

    /** Pre-flight samples (L-92, L-44, L-3) define the personal baseline (BACKEND §3.1). */
    public boolean preFlight() {
        return timepoint.startsWith("L");
    }
}

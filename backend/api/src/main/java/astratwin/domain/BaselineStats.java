package astratwin.domain;

/** Personal baseline of one analyte: mean and SD of the pre-flight samples. */
public record BaselineStats(
        String crewId, String analyte, String unit, double mean, Double sd, int n,
        Double refLow, Double refHigh, String source, MissionTag mission) {
}

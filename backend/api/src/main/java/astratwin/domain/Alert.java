package astratwin.domain;

import java.time.Instant;

/**
 * Output of a rule (TECH_OVERVIEW §4.2). {@code category} drives the privacy filter;
 * {@code active} is false once the rule no longer fires.
 */
public record Alert(
        String id, String crewId, Level level, String title, String explanation, String recommendation,
        String evidence, String source, AlertStatus status, DataKind category, boolean active, Instant createdAt) {

    /** Crew id used for station-wide alerts (environment, radiation). */
    public static final String STATION = "STATION";

    public static Alert open(String id, String crewId, Level level, String title, String explanation,
            String recommendation, String evidence, String source, DataKind category, Instant now) {
        return new Alert(id, crewId, level, title, explanation, recommendation, evidence, source,
                AlertStatus.OPEN, category, true, now);
    }

    public Alert withStatus(AlertStatus newStatus) {
        return new Alert(id, crewId, level, title, explanation, recommendation, evidence, source,
                newStatus, category, active, createdAt);
    }

    public Alert withActive(boolean isActive) {
        return new Alert(id, crewId, level, title, explanation, recommendation, evidence, source,
                status, category, isActive, createdAt);
    }
}

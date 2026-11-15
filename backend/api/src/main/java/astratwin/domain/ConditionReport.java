package astratwin.domain;

/**
 * A health event the crew member has reported, such as a headache or a rash (simulated stories from
 * the crew profiles, #71). {@code region} is the body region the frontend shows it on: head, eyes,
 * heart, lungs, blood, musculo or whole. The condition-report rule turns each into an alert.
 */
public record ConditionReport(
        String crewId, String region, Level level, String title, String explanation, String recommendation,
        String evidence, String source) {
}

package astratwin.domain;

import java.time.Instant;

/** One measurement or reported symptom (TECH_OVERVIEW §4.2). */
public record Reading(
        String crewId, String metric, double value, String unit, Instant timestamp,
        String provenance, String measuredHow, String note) {
}

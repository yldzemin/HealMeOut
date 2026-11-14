package astratwin.domain;

/** Kind of data, used by the privacy filter to pick a policy (BACKEND §6 rows). */
public enum DataKind {
    /** Status level and module: visible to everyone. */
    STATUS,
    /** Vital signs and lab values. */
    VITALS,
    /** Medication and allergies. */
    MEDICATION,
    /** Psychological check-in data: never on the Crew Deck. */
    PSYCH,
    /** Cabin environment and radiation: station-wide. */
    ENVIRONMENT,
    /** A crew member needs help now: broadcast to everyone. */
    EMERGENCY
}

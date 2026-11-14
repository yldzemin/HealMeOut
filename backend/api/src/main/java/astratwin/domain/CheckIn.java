package astratwin.domain;

/** Daily psychological check-in (PSYCHOLOGY_AND_LONG_DURATION.md §4.1). Never on the Crew Deck. */
public record CheckIn(
        String crewId, String date, int mood, int fatigue, int stress, int conflict,
        double sleepHours, String provenance) {
}

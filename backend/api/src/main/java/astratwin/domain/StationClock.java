package astratwin.domain;

import java.time.Instant;

/**
 * Station time for the demo: real UTC time plus a whole-day offset that the Next day button moves
 * (#81). {@code today} is the UTC date of {@code now}, the date a new check-in gets.
 */
public record StationClock(Instant now, String today, int dayOffset) {
}

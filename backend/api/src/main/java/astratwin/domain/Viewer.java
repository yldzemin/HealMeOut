package astratwin.domain;

import java.util.Locale;

/**
 * Who is looking at the data (BACKEND §6 privacy table columns). {@code GROUND} is the flight surgeon;
 * provisionally the same tier as the CMO, without psychological data (team decision open, #56).
 */
public enum Viewer {
    SELF, CMO, GROUND, CREW_DECK, EMERGENCY;

    public static Viewer parse(String text) {
        try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("viewer must be SELF, CMO, GROUND, CREW_DECK or EMERGENCY");
        }
    }
}

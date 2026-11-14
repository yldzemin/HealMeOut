package astratwin.domain;

import java.util.Locale;

/** Who is looking at the data (BACKEND §6 privacy table columns). */
public enum Viewer {
    SELF, CMO, CREW_DECK, EMERGENCY;

    public static Viewer parse(String text) {
        return valueOf(text.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}

package astratwin.domain;

import java.util.Locale;

/** Alert status from TECH_OVERVIEW §4.2; written in lower case in JSON. */
public enum AlertStatus {
    OPEN, APPROVED, REJECTED;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AlertStatus parse(String text) {
        return valueOf(text.trim().toUpperCase(Locale.ROOT));
    }
}

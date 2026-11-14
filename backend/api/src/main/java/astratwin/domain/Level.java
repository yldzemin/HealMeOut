package astratwin.domain;

/** Status levels from TECH_OVERVIEW §4.1. */
public enum Level {
    L0, L1, L2, L3, L4;

    public Level max(Level other) {
        return other.ordinal() > ordinal() ? other : this;
    }
}

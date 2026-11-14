package astratwin;

import java.util.Objects;

/** Tiny assertion helper (no JUnit: the build has no dependencies, #33). */
final class Check {
    private Check() {
    }

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void equal(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}

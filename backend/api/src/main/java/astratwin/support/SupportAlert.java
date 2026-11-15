package astratwin.support;

import astratwin.domain.Alert;
import astratwin.domain.DataKind;
import astratwin.domain.Level;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * The CMO's alert when crisis wording reaches the support companion (team decision, #51): who and
 * when, never what was written. Level L2 (CMO), but category SUPPORT, so it changes neither the crew
 * level nor the station level, which everyone sees on the Crew Deck.
 */
public final class SupportAlert {
    public static final String RULE = "support";
    static final String EVIDENCE = "Team decision #51: crisis wording in the private support companion alerts the CMO "
            + "on board. The conversation stays private to the astronaut (NASA-STD-3001 Vol 1 V1 6010/6011).";
    static final String SOURCE = "Issue #51; docs/BACKEND.md §6 and §10";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.UTC);

    private SupportAlert() {
    }

    public static String id(String crewId) {
        return RULE + ":" + crewId;
    }

    public static Alert of(String crewId, Instant at) {
        return Alert.open(id(crewId), crewId, Level.L2, "Needs support now",
                "Crisis wording in the private support companion at " + TIME.format(at) + " GMT. "
                        + "What they wrote is not shared.",
                "Check on them in person now and stay with them. Set up a private conference with the flight "
                        + "surgeon or behavioral health provider.",
                EVIDENCE, SOURCE, DataKind.SUPPORT, at);
    }
}

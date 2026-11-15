package astratwin.support;

import astratwin.domain.CheckIn;
import java.util.List;
import java.util.Optional;

/**
 * What a responder gets: the new message, recent history and the member's own latest check-in.
 * {@code attempt} is 0 for the first try and 1 for the retry after a rejected reply.
 */
public record SupportRequest(String crewId, String text, List<SupportMessage> history, Optional<CheckIn> checkIn, int attempt) {

    public SupportRequest(String crewId, String text, List<SupportMessage> history, Optional<CheckIn> checkIn) {
        this(crewId, text, history, checkIn, 0);
    }

    public SupportRequest withAttempt(int n) {
        return new SupportRequest(crewId, text, history, checkIn, n);
    }
}

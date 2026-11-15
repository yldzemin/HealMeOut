package astratwin.support;

import java.util.List;
import java.util.Optional;

/**
 * What a responder gets: the new message, recent history and what the companion knows about the crew
 * member (their profile and last 3 days). {@code attempt} is 0 for the first try and 1 for the retry
 * after a rejected reply.
 */
public record SupportRequest(String crewId, String text, List<SupportMessage> history, Optional<CompanionContext> context,
        int attempt) {

    public SupportRequest(String crewId, String text, List<SupportMessage> history, Optional<CompanionContext> context) {
        this(crewId, text, history, context, 0);
    }

    public SupportRequest withAttempt(int n) {
        return new SupportRequest(crewId, text, history, context, n);
    }
}

package astratwin.support;

import astratwin.domain.CheckIn;
import java.util.List;
import java.util.Optional;

/** What a responder gets: the new message, recent history and the member's own latest check-in. */
public record SupportRequest(String crewId, String text, List<SupportMessage> history, Optional<CheckIn> checkIn) {
}

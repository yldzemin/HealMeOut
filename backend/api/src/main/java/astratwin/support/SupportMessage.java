package astratwin.support;

import java.time.Instant;
import java.util.List;

/** One line of a private support conversation. {@code author} is {@code crew} or {@code companion}. */
public record SupportMessage(String crewId, String author, String text, Instant timestamp, String source, List<String> flags) {
}

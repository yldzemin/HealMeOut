package astratwin.support;

import java.util.List;

/** A companion reply; {@code source} names the engine (e.g. {@code local-llm:phi3.5}, {@code guided}, {@code safety}). */
public record SupportReply(String text, String source, List<String> flags) {
}

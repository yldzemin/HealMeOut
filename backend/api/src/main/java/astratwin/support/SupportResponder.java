package astratwin.support;

import java.util.Optional;

/** Something that can answer a crew member's support message. */
public interface SupportResponder {
    /** Shown in status and on every reply, e.g. {@code local-llm:phi3.5}. */
    String name();

    /** Whether the responder can answer right now (e.g. the local model is running). */
    boolean available();

    /** The reply, or empty if this responder could not answer (the service then falls back). */
    Optional<SupportReply> reply(SupportRequest request);
}

package astratwin.support;

import astratwin.domain.CheckIn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Private support companion: safety gate → primary responder (local LLM) → output filter, with the
 * guided responder as fallback. Conversations are psychological data: the web layer lets only the
 * astronaut (viewer SELF) read or write their own. Kept in memory; never logged.
 */
public final class SupportService {
    static final int MAX_INPUT = 1000;
    static final int MAX_STORED = 100;

    private final SafetyGate gate = new SafetyGate();
    private final OutputFilter filter = new OutputFilter();
    private final SupportResponder primary;
    private final SupportResponder fallback;
    private final Function<String, Optional<CheckIn>> checkIns;
    private final Map<String, List<SupportMessage>> conversations = new ConcurrentHashMap<>();

    /** {@code primary} may be null (model disabled); {@code fallback} must always answer. */
    public SupportService(SupportResponder primary, SupportResponder fallback, Function<String, Optional<CheckIn>> checkIns) {
        this.primary = primary;
        this.fallback = fallback;
        this.checkIns = checkIns;
    }

    public SupportMessage send(String crewId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
        if (text.length() > MAX_INPUT) {
            throw new IllegalArgumentException("text is longer than " + MAX_INPUT + " characters");
        }
        List<SupportMessage> conversation = conversations.computeIfAbsent(crewId, k -> new ArrayList<>());
        List<SupportMessage> history;
        synchronized (conversation) {
            history = List.copyOf(conversation);
            conversation.add(new SupportMessage(crewId, "crew", text.strip(), Instant.now(), "crew", List.of()));
        }
        SupportReply reply = answer(new SupportRequest(crewId, text.strip(), history, checkIns.apply(crewId)));
        SupportMessage out = new SupportMessage(crewId, "companion", reply.text(), Instant.now(), reply.source(), reply.flags());
        synchronized (conversation) {
            conversation.add(out);
            while (conversation.size() > MAX_STORED) {
                conversation.remove(0);
            }
        }
        return out;
    }

    SupportReply answer(SupportRequest request) {
        SafetyGate.Verdict verdict = gate.check(request.text());
        switch (verdict.kind()) {
            case CRISIS -> {
                return new SupportReply(verdict.reply(), "safety", List.of("crisis"));
            }
            case MEDICAL -> {
                return new SupportReply(verdict.reply(), "safety", List.of("medical-redirect"));
            }
            case OK -> {
                // continue below
            }
        }
        if (primary == null) {
            return fallback(request, "llm-off");
        }
        if (!primary.available()) {
            return fallback(request, "llm-unavailable");
        }
        Optional<SupportReply> reply = primary.reply(request);
        if (reply.isEmpty()) {
            return fallback(request, "llm-error");
        }
        Optional<String> safe = filter.check(reply.get().text());
        return safe.map(text -> new SupportReply(text, reply.get().source(), reply.get().flags()))
                .orElseGet(() -> fallback(request, "filtered"));
    }

    private SupportReply fallback(SupportRequest request, String why) {
        SupportReply r = fallback.reply(request).orElseThrow(() -> new IllegalStateException("fallback must answer"));
        return new SupportReply(r.text(), r.source(), List.of(why));
    }

    public List<SupportMessage> conversation(String crewId) {
        List<SupportMessage> c = conversations.get(crewId);
        if (c == null) {
            return List.of();
        }
        synchronized (c) {
            return List.copyOf(c);
        }
    }

    public void clear(String crewId) {
        conversations.remove(crewId);
    }

    public Map<String, Object> status() {
        return Map.of("primary", primary == null ? "off" : primary.name(),
                "primaryAvailable", primary != null && primary.available(),
                "fallback", fallback.name(),
                "safety", "crisis and medical requests get a fixed reply; model output is filtered",
                "privacy", "only the astronaut (viewer SELF) can read or write their conversation");
    }
}

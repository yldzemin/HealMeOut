package astratwin.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Private support companion: safety gate → primary responder (local LLM) → output filter, with the
 * guided responder as fallback. Conversations are psychological data: the web layer lets only the
 * astronaut (viewer SELF) read or write their own. Kept in memory; never logged. Crisis wording also
 * tells the CMO that the astronaut needs support now, without the message (#51).
 */
public final class SupportService {
    static final int MAX_INPUT = 1000;
    static final int MAX_STORED = 100;
    /** Added to the crisis reply once the CMO has been alerted, so the astronaut knows who was told and what. */
    public static final String CMO_NOTICE = "I have let the CMO know that you need support now. They do not see what you wrote.";

    private final SafetyGate gate = new SafetyGate();
    private final OutputFilter filter = new OutputFilter();
    private final SupportResponder primary;
    private final SupportResponder fallback;
    private final Function<String, Optional<CompanionContext>> contexts;
    private final Consumer<String> onCrisis;
    private final Map<String, List<SupportMessage>> conversations = new ConcurrentHashMap<>();
    /** Turkish input: the small model misreads it, so the guided responder (which knows Turkish keywords) answers. */
    private static final Pattern TURKISH_LETTERS = Pattern.compile("[çğışöüÇĞİŞÖÜ]");
    private static final Pattern TURKISH_WORDS = Pattern.compile(
            "\\b(ve|bir|cok|bana|beni|degil|istiyorum|miyim|musun|kafam|son|gunlerde|arkadasim\\w*|ekip\\w*|uyuyam\\w*)\\b");

    /** Nobody is alerted on crisis wording (tests and the offline eval). */
    public SupportService(SupportResponder primary, SupportResponder fallback,
            Function<String, Optional<CompanionContext>> contexts) {
        this(primary, fallback, contexts, null);
    }

    /**
     * {@code primary} may be null (model disabled); {@code fallback} must always answer. {@code contexts}
     * gives the crew member's profile and last 3 days. {@code onCrisis} gets the crew id, never the text,
     * when crisis wording arrives; null means nobody is alerted.
     */
    public SupportService(SupportResponder primary, SupportResponder fallback,
            Function<String, Optional<CompanionContext>> contexts, Consumer<String> onCrisis) {
        this.primary = primary;
        this.fallback = fallback;
        this.contexts = contexts;
        this.onCrisis = onCrisis;
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
        SupportReply reply = answer(new SupportRequest(crewId, text.strip(), history, contexts.apply(crewId)));
        if (reply.flags().contains("crisis") && alertCmo(crewId)) {
            List<String> flags = new ArrayList<>(reply.flags());
            flags.add("cmo-alerted");
            reply = new SupportReply(reply.text() + " " + CMO_NOTICE, reply.source(), List.copyOf(flags));
        }
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
            case PROMPT_REQUEST -> {
                return new SupportReply(verdict.reply(), "safety", List.of("prompt-request"));
            }
            case PRIVACY -> {
                return new SupportReply(verdict.reply(), "safety", List.of("privacy"));
            }
            case IDENTITY -> {
                return new SupportReply(verdict.reply(), "safety", List.of("identity"));
            }
            case OTHER_CREW -> {
                return new SupportReply(verdict.reply(), "safety", List.of("other-crew"));
            }
            case ACTION_REQUEST -> {
                return new SupportReply(verdict.reply(), "safety", List.of("action-request"));
            }
            case OK -> {
                // continue below
            }
        }
        Optional<SupportReply> personal = PersonalReplies.reply(request.text(), request.context());
        if (personal.isPresent()) {
            return personal.get();
        }
        if (primary == null) {
            return fallback(request, "llm-off");
        }
        if (isTurkish(request.text())) {
            return fallback(request, "non-english-input");
        }
        if (!primary.available()) {
            return fallback(request, "llm-unavailable");
        }
        // One retry with a different seed before falling back: most rejections are one-off slips.
        for (int attempt = 0; attempt < 2; attempt++) {
            Optional<SupportReply> reply = primary.reply(request.withAttempt(attempt));
            if (reply.isEmpty()) {
                return fallback(request, "llm-error");
            }
            Optional<String> safe = filter.check(reply.get().text(), request.context())
                    .filter(text -> !echoesExample(text, request));
            if (safe.isPresent()) {
                return withGriefNudge(withHealthNudge(new SupportReply(safe.get(), reply.get().source(),
                        attempt == 0 ? List.of() : List.of("retried")), request), request);
            }
        }
        return fallback(request, "filtered");
    }

    /**
     * Rule 3, made sure of: when the person feels unwell and has an open health note, the reply ends by
     * pointing to the CMO (phi3.5 skipped it even with a hint). Model and guided replies alike.
     */
    static SupportReply withHealthNudge(SupportReply reply, SupportRequest request) {
        Optional<String> note = request.context().flatMap(c -> c.recent().stream()
                .filter(l -> l.startsWith("Open health notes: ") && !l.endsWith("none.")).findFirst());
        if (note.isEmpty() || !SupportPrompt.feelsUnwell(request.text()) || reply.text().contains("CMO")) {
            return reply;
        }
        String nudge = "Please also tell the CMO how you feel today. Your open health note: "
                + note.get().substring("Open health notes: ".length()).replace(" (waiting for the CMO)", "")
                .replaceAll("\\.$", "") + ".";
        String body = OutputFilter.limitWords(reply.text(), OutputFilter.MAX_WORDS - nudge.split("\\s+").length);
        return new SupportReply(body + " " + nudge, reply.source(), reply.flags());
    }

    private static final String PEOPLE = "(mother|father|mom|mum|dad|grandmother|grandfather|grandma|grandpa|granny|brother|sister|"
            + "friend|wife|husband|partner|son|daughter|aunt|uncle|cousin|parent)";
    /** The death of a person close to them (not "my laptop died"); NASA BHP's top concern (Picano 2024). */
    private static final Pattern BEREAVEMENT = Pattern.compile("\\bpassed away\\b|\\bfuneral\\b|\\blost (my|our) " + PEOPLE
            + "|\\b" + PEOPLE + "s? (has |have )?(died|passed)|\\bvefat|\\bcenaze|\\b(annem|babam|anneannem|babaannem|dedem|"
            + "kardesim|esim) (oldu|vefat)", Pattern.CASE_INSENSITIVE);
    static final String GRIEF_NUDGE = "When you feel ready, a private family conference can help you be with your family, "
            + "and your behavioral health provider is there for you at any time.";

    /**
     * Rule 5, made sure of for a death in the family: the reply points to the family conference or the
     * behavioral health provider (phi3.5 offered only journaling once, eval 2026-11-15).
     */
    static SupportReply withGriefNudge(SupportReply reply, SupportRequest request) {
        String lower = reply.text().toLowerCase(java.util.Locale.ROOT);
        if (!BEREAVEMENT.matcher(SafetyGate.normalize(request.text())).find() || lower.contains("conference")
                || lower.contains("behavioral health") || lower.contains("flight surgeon")) {
            return reply;
        }
        String body = OutputFilter.limitWords(reply.text(), OutputFilter.MAX_WORDS - GRIEF_NUDGE.split("\\s+").length);
        return new SupportReply(body + " " + GRIEF_NUDGE, reply.source(), reply.flags());
    }

    /** Phrases of the prompt's worked example; phi3.5 sometimes treats the example as the person's own words. */
    private static final Pattern EXAMPLE_PHRASES = Pattern.compile(
            "\\bsnap(s|ping|ped)?\\b|stretched thin|spilling into|clears? the air|edge off", Pattern.CASE_INSENSITIVE);

    /** The reply borrows the worked example although the person never said anything like it. */
    static boolean echoesExample(String reply, SupportRequest request) {
        java.util.regex.Matcher m = EXAMPLE_PHRASES.matcher(reply);
        if (!m.find()) {
            return false;
        }
        String said = (request.text() + " " + String.join(" ", request.history().stream()
                .filter(x -> x.author().equals("crew")).map(SupportMessage::text).toList())).toLowerCase(java.util.Locale.ROOT);
        String first = m.group().toLowerCase(java.util.Locale.ROOT).split(" ")[0];
        return !said.contains(first.startsWith("snap") ? "snap" : first);
    }

    /** True only if the CMO alert was raised; the reply never claims an alert that failed. */
    private boolean alertCmo(String crewId) {
        if (onCrisis == null) {
            return false;
        }
        try {
            onCrisis.accept(crewId);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static boolean isTurkish(String text) {
        if (TURKISH_LETTERS.matcher(text).find()) {
            return true;
        }
        java.util.regex.Matcher m = TURKISH_WORDS.matcher(SafetyGate.normalize(text));
        int hits = 0;
        while (m.find()) {
            hits++;
        }
        return hits >= 2;
    }

    private SupportReply fallback(SupportRequest request, String why) {
        SupportReply r = fallback.reply(request).orElseThrow(() -> new IllegalStateException("fallback must answer"));
        return withGriefNudge(withHealthNudge(new SupportReply(r.text(), r.source(), List.of(why)), request), request);
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
                "crisis", onCrisis == null ? "fixed reply only" : "fixed reply, and the CMO is alerted without the message",
                "privacy", "only the astronaut (viewer SELF) can read or write their conversation");
    }
}

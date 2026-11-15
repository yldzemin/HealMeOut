package astratwin.support;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Rule-based fallback, used when the local model is off, fails or its reply is filtered. Every reply
 * uses only the sourced support options in {@link SupportPrompt}.
 */
public final class GuidedResponder implements SupportResponder {
    private record Topic(Pattern pattern, String reply) {
    }

    private static final String PPC = " If this lasts several days, it is worth raising at your next Private Psychological "
            + "Conference or with your flight surgeon.";
    private static final List<Topic> TOPICS = List.of(
            // Good news first: the default reply asks "what feels heaviest", which is wrong for a happy message.
            new Topic(Pattern.compile("\\b(great day|good day|happy|amazing|wonderful|beautiful|so cool|saw an? (aurora|storm)|best day)\\b"
                    + "|harika|mutluyum|cok guzel"),
                    "That sounds like a great moment. Moments like that are worth keeping: a photo of Earth or a few lines "
                            + "in a journal help many crew members hold on to them."),
            new Topic(Pattern.compile("\\b(sleep|tired|insomnia|awake|exhaust\\w*)|uyku|yorgun|uyuyam"),
                    "Poor sleep up here is common, and it makes everything feel heavier. Two things that can help: keep "
                            + "the same sleep window each night and plan your light before sleep; noise, CO2 and a busy "
                            + "timeline can all disturb it." + PPC),
            new Topic(Pattern.compile("\\b(crew\\w*|argu\\w*|conflict|annoy\\w*|irritat\\w*|angry)\\b|ekip|tartis|sinir"),
                    "Friction in a small crew is normal and hard. When you are ready, a calm one-to-one talk at a good "
                            + "moment usually works better than in front of others — the team-care training is there for "
                            + "exactly this." + PPC),
            new Topic(Pattern.compile("\\b(family|home|miss|missing|kids|partner|wife|husband)\\b|aile|ozledim|cocug|cocuk"),
                    "Missing home is part of a long mission. Your weekly private family conference, and the monthly "
                            + "special conference, are good moments to reconnect; writing down what you want to share can "
                            + "help in between." + PPC),
            new Topic(Pattern.compile("\\b(work|workload|timeline|schedule|stress\\w*|pressure|mission control|ground)\\b|is yuku|stres|program"),
                    "A packed timeline can wear anyone down. It is fair to raise workload and timeline worries with the "
                            + "CMO and mission control, and a short break for exercise can help reset." + PPC),
            new Topic(Pattern.compile("\\b(lonely|alone|sad|down|bored|empty)\\b|yalniz|uzgun|sikil|mutsuz"),
                    "Thank you for sharing that. Some crew find it helps to spend time at the Cupola looking at Earth, "
                            + "to photograph it, or to write a few lines in a journal. Exercise also lifts mood." + PPC));
    private static final String DEFAULT = "Thank you for telling me. Things that help many crew members: a few minutes at "
            + "the Cupola, photographing Earth, journaling, or your daily exercise. Your Private Psychological Conference "
            + "every two weeks is a confidential place to talk it through. What feels heaviest right now?";

    @Override
    public String name() {
        return "guided";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public Optional<SupportReply> reply(SupportRequest request) {
        String text = SafetyGate.normalize(request.text());
        String reply = TOPICS.stream().filter(t -> t.pattern().matcher(text).find()).map(Topic::reply).findFirst()
                .orElse(DEFAULT);
        return Optional.of(new SupportReply(reply + personal(request), name(), List.of()));
    }

    /** One line from the person's own profile: the first thing that helps them (the guided reply adapts too). */
    static String personal(SupportRequest request) {
        return request.context().map(c -> c.persona().helps()).filter(h -> !h.isEmpty())
                .map(h -> " " + Character.toUpperCase(h.get(0).charAt(0)) + h.get(0).substring(1)
                        + " helps you, so it may be worth a few minutes today.")
                .orElse("");
    }
}

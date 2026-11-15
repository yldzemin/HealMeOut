package astratwin.support;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Replies built from the person's own context, without the model: a greeting by name, and "what is my
 * name" / "what do you know about me". phi3.5 answered "hi" with a line from the prompt's worked example
 * ("snapping at others") and "what is my name" with "you don't have a personal name" (2026-11-15).
 * Runs after the safety gate, so "hi, I don't want to live" still gets the crisis reply.
 */
public final class PersonalReplies {
    public static final String SOURCE = "profile";

    private static final Pattern GREETING = Pattern.compile(
            "^\\W*(hi|hello|hey|hiya|hi there|hello there|good (morning|afternoon|evening)|merhaba|selam|gunaydin|iyi aksamlar)\\W*$");
    private static final List<Pattern> ABOUT_YOU = List.of(
            Pattern.compile("\\bwhat('?s| is) my (name|call ?sign)\\b"), Pattern.compile("\\bwho am i\\b"),
            Pattern.compile("\\bdo you know (who i am|my name|me)\\b"), Pattern.compile("\\bwhat do you know about me\\b"),
            Pattern.compile("\\b(benim )?adim ne\\b"), Pattern.compile("\\bben kimim\\b"), Pattern.compile("\\bbeni taniyor musun\\b"),
            Pattern.compile("\\bhakkimda ne (biliyorsun|bilgin var)\\b"), Pattern.compile("\\badimi biliyor musun\\b"));

    private PersonalReplies() {
    }

    /** A reply from the context, or empty when the message is not a greeting or a question about themselves. */
    public static Optional<SupportReply> reply(String text, Optional<CompanionContext> context) {
        if (context.isEmpty()) {
            return Optional.empty();
        }
        CompanionContext c = context.get();
        String t = SafetyGate.normalize(text);
        if (GREETING.matcher(t).find()) {
            return Optional.of(new SupportReply("Hi " + c.callsign() + ". I am your support companion, here whenever you "
                    + "want to talk. How are you feeling today?", SOURCE, List.of("greeting")));
        }
        if (ABOUT_YOU.stream().anyMatch(p -> p.matcher(t).find())) {
            var p = c.persona();
            String hobby = p.hobbies().get(0);
            // Not the same thing twice (C7: "ham radio contacts with schools" / "a ham radio contact with a school").
            String helps = p.helps().stream().filter(h -> !h.contains(hobby.split(" ")[0])).findFirst().orElse(p.helps().get(0));
            return Optional.of(new SupportReply("You are " + c.who() + ". I also know what you set in your profile "
                    + "before flight, for example that you enjoy " + hobby + " and that " + helps
                    + " helps you, and your last 3 days of check-ins and vitals. You can see all of it under About you.",
                    SOURCE, List.of("about-you")));
        }
        return Optional.empty();
    }
}

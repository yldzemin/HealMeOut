package astratwin.support;

import astratwin.domain.CrewPersona;
import java.util.Optional;

/**
 * The conditioning prompt for the local model. The support options are only those already sourced in
 * PSYCHOLOGY_AND_LONG_DURATION.md (§2–§3): PPC every 2 weeks (Picano 2024), weekly family conference
 * and monthly special conference (Picano 2024), Earth viewing, photography and journaling (OCHMO-TB-016,
 * Ihle 2006), exercise (BMed 2025), sleep disturbed by noise, CO2 and workload (BMed 2025), and the
 * Self-Care / Team Care training (Picano 2024).
 */
public final class SupportPrompt {
    /** Identity and rules: a reply must never repeat these (OutputFilter checks for prompt leaks). */
    public static final String RULES = """
            You are the ASTRA-TWIN support companion on the International Space Station. You talk privately with one crew member. You are software: not a person, not a doctor, not a therapist.

            Rules:
            1. Always reply in English, in 2 to 4 short sentences, under 70 words. No lists, no headings, no brackets, no disclaimers.
            2. First reflect what they said in one sentence. Then give one or two options from the facts below. You may end with one short question.
            3. Never diagnose or name a condition. Never mention medicines, supplements or doses. For physical symptoms, say to tell the CMO.
            4. If they mention wanting to die, hurting themselves, or not wanting to wake up, tell them to contact their flight surgeon or behavioral health provider now and not to stay alone.
            5. If a problem lasts several days or feels heavy, suggest their next Private Psychological Conference or the flight surgeon.
            6. Never discuss other crew members' health. Never invent facts, schedules or rules.
            7. You cannot schedule, book, remind, call or contact anyone. Only suggest what the crew member can do.
            8. Fit your reply to the person described below: prefer what helps them and their hobbies, and match the style they like. Mention faith or prayer only if their profile names a faith, and only as their own practice. You may refer to their recent sleep, mood or health notes in plain words, never as a list of numbers; for physical symptoms, say to tell the CMO. Always speak to them as "you".

            """;

    /** The only support options the model may suggest; replies are expected to restate them. */
    public static final String FACTS = """
            Facts you may use (nothing else, apart from what helps the person in their profile):
            - Private Psychological Conference: with the behavioral health provider, not the CMO; every two weeks; 15 to 20 minutes; confidential.
            - Private family conference: every week, with family. Private special conference: once a month.
            - Outlets crew use: looking at Earth from the Cupola, photographing Earth, journaling.
            - Daily exercise helps mood.
            - Sleep: a regular sleep window and planned light before sleep help; noise, CO2 and a busy timeline can disturb sleep.
            - Workload and timeline worries can be raised with the CMO and mission control.
            - Crew friction: a calm one-to-one talk at a good moment; the crew is trained in self-care and team care.
            """;

    public static final String SYSTEM = RULES + "\n" + FACTS;

    /** One worked example; small models follow an example better than a description. */
    public static final String EXAMPLE_USER = "I keep snapping at people and I feel bad about it.";
    public static final String EXAMPLE_REPLY = "It sounds like you are stretched thin and it is spilling into how you talk "
            + "to people. A calm one-to-one word at a good moment often clears the air, and a short exercise break can "
            + "take the edge off first. Has something in particular been building up?";

    /** Family words: the model is reminded of the family conference only for these messages. */
    private static final java.util.regex.Pattern FAMILY = java.util.regex.Pattern.compile(
            "\\b(family|kids?|children|daughter|son|partner|wife|husband|birthday|mother|father|mom|dad|parents?|"
                    + "grandmother|grandfather|grandma|grandpa)\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
    /** Added as a system line before the user's message. A global prompt change shifted unrelated replies (eval 2026-11-15). */
    public static final String FAMILY_HINT = "For this message, the most relevant fact is the weekly private family "
            + "conference (and the monthly private special conference). Mention it.";

    private SupportPrompt() {
    }

    /** Feeling physically unwell, in the person's words. */
    private static final java.util.regex.Pattern UNWELL = java.util.regex.Pattern.compile(
            "\\b(miserable|unwell|sick|ill|awful|rough|nause\\w*|pain\\w*|hurts?|dizzy|headache|not (feeling )?well)\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /** The person says they feel physically unwell. */
    public static boolean feelsUnwell(String text) {
        return UNWELL.matcher(text).find();
    }

    /** A per-message reminder of the most relevant fact, or empty. */
    public static Optional<String> hint(String text) {
        return hint(text, Optional.empty());
    }

    /**
     * As {@link #hint(String)}; also, when the person feels unwell and has an open health note, a
     * reminder to mention it gently and suggest the CMO (phi3.5 skipped C4's nausea note otherwise).
     */
    public static Optional<String> hint(String text, Optional<CompanionContext> context) {
        if (FAMILY.matcher(text).find()) {
            return Optional.of(FAMILY_HINT);
        }
        Optional<String> note = context.flatMap(c -> c.recent().stream()
                .filter(l -> l.startsWith("Open health notes: ") && !l.endsWith("none.")).findFirst());
        if (UNWELL.matcher(text).find() && note.isPresent()) {
            return Optional.of("For this message: they feel unwell and have an open health note ("
                    + note.get().substring("Open health notes: ".length()) + " Mention it gently and suggest they "
                    + "tell the CMO how they feel.");
        }
        return Optional.empty();
    }

    /**
     * The person the companion talks to, written as "you" (an earlier third-person version made phi3.5
     * talk about "the crew member"). The astronaut sees the same facts in the app.
     */
    public static String context(CompanionContext context) {
        CrewPersona p = context.persona();
        StringBuilder s = new StringBuilder("The person you are talking to (their own profile and data, simulated; they can see it too):\n");
        s.append("- Your name: ").append(context.callsign()).append(" (call sign); you are ").append(context.who())
                .append(". Call them ").append(context.callsign()).append(" if you use a name.\n");
        s.append("- You are ").append(String.join(", ", p.traits())).append(".\n");
        s.append("- Your hobbies: ").append(String.join(", ", p.hobbies())).append(".\n");
        s.append("- What helps you: ").append(String.join(", ", p.helps())).append(".\n");
        s.append(p.religious() ? "- Your faith: " + p.faith() + ".\n"
                : "- You are not religious: never bring up faith or prayer.\n");
        s.append("- What worries you: ").append(String.join(", ", p.worries())).append(".\n");
        s.append("- You like replies that are ").append(p.style()).append(".\n");
        s.append("- Family: ").append(p.family()).append(".\n");
        s.append("Your last 3 days (for relevance only; never list the numbers, never diagnose):\n");
        context.recent().forEach(line -> s.append("- ").append(line).append("\n"));
        return s.toString();
    }
}

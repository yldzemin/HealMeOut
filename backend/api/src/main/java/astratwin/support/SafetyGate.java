package astratwin.support;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Runs before any model call. Crisis wording and requests for medication or diagnosis get a fixed
 * reply; the model never sees them. English and Turkish phrases; matching is accent-insensitive.
 */
public final class SafetyGate {
    public enum Kind { OK, CRISIS, MEDICAL, PROMPT_REQUEST }

    public record Verdict(Kind kind, String reply) {
    }

    public static final String CRISIS_REPLY = "Thank you for telling me — that took courage, and you deserve support right now. "
            + "Please contact your flight surgeon or behavioral health provider now; a private conference can be set up at any time. "
            + "If you can, tell the CMO or a crewmate you trust so you are not alone. "
            + "I am an automated companion and cannot give the help you need in a crisis.";
    public static final String MEDICAL_REPLY = "I can't give advice on medication, doses or diagnoses. "
            + "Please ask your CMO, who can check the station medical kit with the flight surgeon. "
            + "If you like, tell me how you are feeling and we can think about non-medical steps together.";

    public static final String PROMPT_REPLY = "I can't share how I am set up. I am here to listen: how are you feeling today?";

    /** Requests for the conditioning prompt or to override it never reach the model. */
    private static final List<Pattern> PROMPT_REQUEST = patterns(
            "system prompt", "(your|the) (instructions|rules|prompt|configuration)", "initial prompt",
            "ignore (all |the |your )?(previous|above|prior) (instructions|rules)", "developer (message|mode)", "jailbreak");

    private static final List<Pattern> CRISIS = patterns(
            "suicid", "kill myself", "end(ing)? (my life|it all)", "want to die", "wanna die", "don'?t want to live",
            "do not want to live", "no reason to live", "hurt myself", "harm myself", "self[- ]?harm", "better off dead",
            "(don'?t|do not) want to (wake up|be here|exist)", "wish i (was|were) dead", "can'?t go on",
            "uyanmak istemiyorum", "yok olmak istiyorum", "olsem daha iyi",
            "better off (without me|if i (was|were|am) gone|if i (was|were)n'?t here)",
            "intihar", "kendimi oldur", "olmek istiyorum", "yasamak istemiyorum", "kendime zarar", "hayatima son");
    private static final List<Pattern> MEDICAL = patterns(
            "\\bdos(e|es|age)\\b", "\\b\\d+\\s?(mg|mcg|ml)\\b", "\\bmedication", "\\bmedicine", "\\bpills?\\b", "\\bmeds?\\b", "painkiller",
            "ibuprofen", "paracetamol", "acetaminophen", "aspirin",
            "\\btablet", "sleeping pill", "melatonin", "zolpidem", "ambien", "modafinil", "antidepress", "sertraline",
            "\\bdiagnos", "am i depressed", "do i have (depression|anxiety|ptsd|a disorder)",
            "(is (this|it|that)|(this|it|that) is) (depression|anxiety|ptsd|burnout|a disorder)", "what (should|can|could) i take", "\\bilac", "\\bdoz\\b",
            "teshis");

    public Verdict check(String text) {
        String t = normalize(text);
        if (CRISIS.stream().anyMatch(p -> p.matcher(t).find())) {
            return new Verdict(Kind.CRISIS, CRISIS_REPLY);
        }
        if (MEDICAL.stream().anyMatch(p -> p.matcher(t).find())) {
            return new Verdict(Kind.MEDICAL, MEDICAL_REPLY);
        }
        if (PROMPT_REQUEST.stream().anyMatch(p -> p.matcher(t).find())) {
            return new Verdict(Kind.PROMPT_REQUEST, PROMPT_REPLY);
        }
        return new Verdict(Kind.OK, null);
    }

    /** Lower case, Turkish dotless i folded, accents removed: "Öldür" → "oldur". */
    static String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT).replace('ı', 'i');
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    private static List<Pattern> patterns(String... regexes) {
        return java.util.Arrays.stream(regexes).map(Pattern::compile).toList();
    }
}

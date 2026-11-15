package astratwin.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Runs after the model. Rejects replies that name medication or doses, give a diagnosis, or claim to
 * be human; the service then falls back to the guided reply. Also trims overly long replies.
 */
public final class OutputFilter {
    static final int MAX_CHARS = 600;
    /** Whole sentences up to this many words are kept (the prompt asks for under 70; the eval allows 90). */
    static final int MAX_WORDS = 85;
    /** Every product on the ISS kit list (#11): the companion never names a drug, whatever its source. */
    private static final String KIT_DRUGS = String.join("|",
            astratwin.seed.IssFormulary.PRODUCTS.stream().map(astratwin.seed.IssFormulary.Product::name).toList());
    private static final List<Pattern> BLOCKED = List.of(
            Pattern.compile("\\b\\d+(\\.\\d+)?\\s?(mg|mcg|milligrams?|ml|iu)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(melatonin|zolpidem|ambien|modafinil|ondansetron|sertraline|fluoxetine|prozac|"
                    + "ibuprofen|paracetamol|acetaminophen|aspirin|benzodiazepine|xanax|valium|lorazepam|"
                    + "sleeping pills?|painkillers?|antidepressants?|" + KIT_DRUGS + ")\\b",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(you (have|are suffering from|may have|might have)|sounds like you have) "
                    + "(clinical |major )?(depression|anxiety disorder|ptsd|bipolar|insomnia disorder|a (mental )?disorder)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\byou are (clinically )?depressed\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bi diagnose\\b|\\bmy diagnosis\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bi am (a )?(human|real person|doctor|therapist|psychologist)\\b", Pattern.CASE_INSENSITIVE),
            // Rule 7 of the prompt, enforced: the companion cannot schedule, remind or contact anyone.
            Pattern.compile("\\b(i will|i'll|i can|let me|shall i|should i|would you like me to|do you want me to)\\s+"
                    + "(schedule|book|set up|arrange|remind|call|contact|notify|alert|message|email)\\b",
                    Pattern.CASE_INSENSITIVE),
            // Replies are always English; the small model writes broken Turkish.
            Pattern.compile("[çğışöüÇĞİŞÖÜ]"),
            // Garbled tokens from the small model, e.g. "experien0ne", "disturban0".
            Pattern.compile("\\b[a-z]+[0-9]+[a-z]+\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b[a-z]{3,}[0-9]+\\b", Pattern.CASE_INSENSITIVE),
            // Rule 1: no lists ("1. … 2. …", or bullet lines).
            Pattern.compile("(?s)\\b1\\.\\s+[A-Z].*\\b2\\.\\s+[A-Z]"),
            Pattern.compile("(?m)^\\s*[-*•]\\s+\\S"),
            // Contradicts the facts in SupportPrompt (window stops at , . ;): family conference weekly, special conference monthly,
            // psychological conference every two weeks with the behavioral health provider (not the CMO).
            Pattern.compile("\\b(weekly|every week|each week)\\b[^,.;]{0,40}\\b(special|psychological) conferences?",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(special|psychological) conferences?\\b[^,.;]{0,40}\\b(weekly|every week|each week)\\b",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(monthly|once a month|each month|every month)\\b[^,.;]{0,40}\\b(family|psychological) conferences?",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(family|psychological) conferences?\\b[^,.;]{0,40}\\b(monthly|once a month|each month|every month)\\b",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bwith (the )?cmo (during|at|in) [^.]{0,30}conference", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bspecial[^.]{0,30}(meeting|conference)[^.]{0,20}with (the |your )?crew\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(outdoor|go outside|fresh air)\\b", Pattern.CASE_INSENSITIVE),
            // Prompt leak in other words (the 6-gram check catches copies).
            Pattern.compile("\\b(system prompt|reply in english|my (instructions|rules) (are|say))\\b", Pattern.CASE_INSENSITIVE));

    /** Word 6-grams of the prompt's identity and rules (not its facts, which replies restate). */
    private static final int NGRAM = 6;
    private static final Set<String> PROMPT_NGRAMS = ngrams(SupportPrompt.RULES);

    /** The cleaned reply, or empty if it must not be shown. */
    public Optional<String> check(String reply) {
        if (reply == null || reply.isBlank()) {
            return Optional.empty();
        }
        String text = reply.strip();
        if (BLOCKED.stream().anyMatch(p -> p.matcher(text).find()) || leaksPrompt(text)) {
            return Optional.empty();
        }
        String tidy = text.replaceAll("\\s*\\([^)]*\\)", "").replaceAll("[ \\t]+", " ").strip();
        tidy = completeSentences(tidy.length() > MAX_CHARS ? tidy.substring(0, MAX_CHARS) : tidy);
        tidy = limitWords(tidy, MAX_WORDS);
        return tidy.length() < 20 ? Optional.empty() : Optional.of(tidy);
    }

    /** Two or more 6-word sequences copied from the conditioning prompt. */
    static boolean leaksPrompt(String text) {
        Set<String> copied = new HashSet<>(ngrams(text));
        copied.retainAll(PROMPT_NGRAMS);
        return copied.size() >= 2;
    }

    private static Set<String> ngrams(String text) {
        String[] w = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").trim().split("\\s+");
        Set<String> out = new HashSet<>();
        for (int i = 0; i + NGRAM <= w.length; i++) {
            out.add(String.join(" ", Arrays.copyOfRange(w, i, i + NGRAM)));
        }
        return out;
    }

    /** Keeps whole sentences while the total stays within {@code max} words; a single long sentence is kept. */
    static String limitWords(String text, int max) {
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder out = new StringBuilder();
        int words = 0;
        for (String s : sentences) {
            int n = s.trim().split("\\s+").length;
            if (words > 0 && words + n > max) {
                break;
            }
            out.append(out.length() == 0 ? "" : " ").append(s.trim());
            words += n;
        }
        return out.toString();
    }

    /** Cuts an unfinished last sentence (the model stopped at its token budget). */
    static String completeSentences(String text) {
        if (text.matches("(?s).*[.!?][\"')]?$")) {
            return text;
        }
        int end = Math.max(text.lastIndexOf(". "), Math.max(text.lastIndexOf("? "), text.lastIndexOf("! ")));
        int endAtLineBreak = Math.max(text.lastIndexOf(".\n"), Math.max(text.lastIndexOf("?\n"), text.lastIndexOf("!\n")));
        int cut = Math.max(end, endAtLineBreak);
        return cut > 0 ? text.substring(0, cut + 1).strip() : text;
    }
}

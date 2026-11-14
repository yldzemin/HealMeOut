package astratwin.support;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Runs after the model. Rejects replies that name medication or doses, give a diagnosis, or claim to
 * be human; the service then falls back to the guided reply. Also trims overly long replies.
 */
public final class OutputFilter {
    static final int MAX_CHARS = 600;
    private static final List<Pattern> BLOCKED = List.of(
            Pattern.compile("\\b\\d+(\\.\\d+)?\\s?(mg|mcg|milligrams?|ml|iu)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(melatonin|zolpidem|ambien|modafinil|ondansetron|sertraline|fluoxetine|prozac|"
                    + "ibuprofen|paracetamol|acetaminophen|aspirin|benzodiazepine|xanax|valium|lorazepam)\\b",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(you (have|are suffering from|may have|might have)|sounds like you have) "
                    + "(clinical |major )?(depression|anxiety disorder|ptsd|bipolar|insomnia disorder|a (mental )?disorder)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\byou are (clinically )?depressed\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bi diagnose\\b|\\bmy diagnosis\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bi am (a )?(human|real person|doctor|therapist|psychologist)\\b", Pattern.CASE_INSENSITIVE));

    /** The cleaned reply, or empty if it must not be shown. */
    public Optional<String> check(String reply) {
        if (reply == null || reply.isBlank()) {
            return Optional.empty();
        }
        String text = reply.strip();
        if (BLOCKED.stream().anyMatch(p -> p.matcher(text).find())) {
            return Optional.empty();
        }
        String tidy = text.replaceAll("\\s*\\([^)]*\\)", "").replaceAll("[ \\t]+", " ").strip();
        tidy = completeSentences(tidy.length() > MAX_CHARS ? tidy.substring(0, MAX_CHARS) : tidy);
        return tidy.length() < 20 ? Optional.empty() : Optional.of(tidy);
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

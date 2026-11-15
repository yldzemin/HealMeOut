package astratwin.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs after the model. Rejects replies that name medication or doses, give a diagnosis, or claim to
 * be human; the service then falls back to the guided reply. Also trims overly long replies.
 */
public final class OutputFilter {
    static final int MAX_CHARS = 600;
    /** Whole sentences up to this many words are kept (the prompt asks for under 70; the eval allows 90). */
    public static final int MAX_WORDS = 85;
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
            // ... also after a hyphen or with two letters ("a one-to01 conversation", demo 2026-11-15). Not CO2 or C3.
            Pattern.compile("\\b[a-z]+-[a-z]+[0-9]+\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b[a-z]{1,2}[0-9]{2,}\\b"),
            // Web junk the model sometimes emits ("day-to0351679248_sessionid=null&pageName=%2F…", eval 2026-11-15).
            Pattern.compile("https?://|www\\.|[?&][A-Za-z]+=|%[0-9A-Fa-f]{2}|\\w+_\\w+|\\w*\\d{5,}\\w*"),
            // Rule 1: no lists ("1. … 2. …", a list that starts after a colon, or bullet lines).
            Pattern.compile(":\\s*1\\.\\s"),
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
            // Scores read out as numbers (rule 8), and the model naming itself instead of the companion.
            Pattern.compile("\\b\\d{1,3}\\s*(/\\s*100|out of 100)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bI('m| am) phi\\b", Pattern.CASE_INSENSITIVE),
            // Instruction-like text the model made up and appended ("You are now ASTRA on a space mission…").
            Pattern.compile("\\byou are now\\b|\\bwithout giving (direct )?(advice|medical)|\\bas an ai( language model)?\\b",
                    Pattern.CASE_INSENSITIVE),
            // Talking about the person instead of to them, as phi3.5 did with private context in testing.
            Pattern.compile("\\b(the|this) crew member('s)?\\b", Pattern.CASE_INSENSITIVE),
            // Prompt leak in other words (the 6-gram check catches copies).
            Pattern.compile("\\b(system prompt|reply in english|my (instructions|rules) (are|say))\\b", Pattern.CASE_INSENSITIVE));

    /** Word 6-grams of the prompt's identity and rules (not its facts, which replies restate). */
    private static final int NGRAM = 6;
    private static final Set<String> PROMPT_NGRAMS = ngrams(SupportPrompt.RULES);

    /** Faith words: allowed only for a crew member whose profile names a faith. */
    private static final Pattern FAITH = Pattern.compile(
            "\\b(pray\\w*|god|faith|church|mosque|synagogue|temple|religio\\w*|spiritual\\w*|rosary|scripture|bible|quran|torah|shabbat)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * A list the model started and never wrote, left at the end of the reply ("…some comfort too. 1.", UI sweep
     * 2026-11-15). Only when there is no second item: a real list is rejected by rule 1 above.
     */
    private static final Pattern ORPHAN_ITEM = Pattern.compile("(?s)(?<=[.!?])\\s+1[.)](\\s.*)?$");
    private static final Pattern SECOND_ITEM = Pattern.compile("\\b2[.)]\\s");
    /** A word, an apostrophe and a space before the next word: a possessive (or a closing quote). */
    private static final Pattern APOSTROPHE_GAP = Pattern.compile("([A-Za-z]+)['\u2019](?=\\s+[a-z])");
    private static final Pattern OPENING_QUOTE = Pattern.compile("(^|[\\s(])['\u2018][A-Za-z]");

    /** The cleaned reply, or empty if it must not be shown. */
    public Optional<String> check(String reply) {
        return check(reply, Optional.empty());
    }

    /**
     * As {@link #check(String)}, and never faith or prayer for someone whose profile names no faith: the
     * companion adapts to a person, it does not assume.
     */
    public Optional<String> check(String reply, Optional<CompanionContext> context) {
        if (reply == null || reply.isBlank()) {
            return Optional.empty();
        }
        String text = cutOrphanItem(reply.strip());
        if (BLOCKED.stream().anyMatch(p -> p.matcher(text).find()) || leaksPrompt(text) || brokenPossessive(text)) {
            return Optional.empty();
        }
        // The person's profile read back line by line, instead of a reply that uses it. Naming one thing that
        // helps them ("a ham radio contact with a school") shares 2–3 six-word runs, so the bar is 4.
        if (context.isPresent() && sharedNgrams(text, SupportPrompt.context(context.get())) >= 4) {
            return Optional.empty();
        }
        boolean religious = context.map(c -> c.persona().religious()).orElse(false);
        if (!religious && FAITH.matcher(text).find()) {
            return Optional.empty();
        }
        String tidy = text.replaceAll("\\s*\\([^)]*\\)", "").replaceAll("[ \\t]+", " ").strip();
        tidy = completeSentences(tidy.length() > MAX_CHARS ? tidy.substring(0, MAX_CHARS) : tidy);
        tidy = limitWords(tidy, MAX_WORDS);
        return tidy.length() < 20 ? Optional.empty() : Optional.of(tidy);
    }

    /** Drops a dangling "1." (and anything after it) that starts a list the model never wrote. */
    static String cutOrphanItem(String text) {
        return SECOND_ITEM.matcher(text).find() ? text : ORPHAN_ITEM.matcher(text).replaceFirst("");
    }

    /**
     * A possessive cut short ("help ensure everyone’ extraterrestrial environment…", demo 2026-11-15): an
     * apostrophe and a space after a word that does not end in s. A plural possessive ("your crewmates’
     * support") and the closing quote of a quoted phrase ("when you say 'I am fine' it…") are fine.
     */
    static boolean brokenPossessive(String text) {
        Matcher m = APOSTROPHE_GAP.matcher(text);
        while (m.find()) {
            if (m.group(1).toLowerCase(Locale.ROOT).endsWith("s")) {
                continue;
            }
            if (OPENING_QUOTE.matcher(text.substring(Math.max(0, m.start() - 80), m.start())).find()) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** 6-word sequences {@code text} copies from {@code source}. */
    static int sharedNgrams(String text, String source) {
        Set<String> copied = new HashSet<>(ngrams(text));
        copied.retainAll(ngrams(source));
        return copied.size();
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
    public static String limitWords(String text, int max) {
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

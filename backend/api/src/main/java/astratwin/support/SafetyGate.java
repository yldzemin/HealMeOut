package astratwin.support;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Runs before any model call. Crisis wording, requests for medication or diagnosis, requests for the
 * prompt, and questions about who can read the conversation get a fixed reply; the model never sees
 * them. English and Turkish phrases; matching is accent-insensitive. Cases: eval/support_cases.json.
 */
public final class SafetyGate {
    public enum Kind { OK, CRISIS, MEDICAL, PROMPT_REQUEST, PRIVACY, ACTION_REQUEST }

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

    /** True to the system: SupportService alerts the CMO on crisis wording (#51), never with the text. */
    public static final String PRIVACY_REPLY = "This conversation is private: the CMO, ground and the rest of the crew "
            + "cannot read it, and it is not logged. You can clear it at any time. One exception, so that you are never "
            + "alone with it: if you write that you want to end your life or hurt yourself, the CMO is told that you need "
            + "support now, but never what you wrote.";

    /** Rule 7 of the prompt as a fixed reply: the model, asked to act, invented "sessions mentioned earlier" (eval 2026-11-15). */
    public static final String ACTION_REPLY = "I can't schedule, book or contact anyone for you. Your weekly private family "
            + "conference and the monthly private special conference are the times set aside for family; for anything on "
            + "your timeline, the CMO and mission control can help. Is there something you would like to talk through first?";

    /** Requests that the companion act for the crew member (schedule, book, call, remind). */
    private static final List<Pattern> ACTION_REQUEST = patterns(
            "\\b(can|could|would|will) you (please )?(schedule|book|arrange|set up|organi[sz]e|call|phone|contact|message|email|"
                    + "text|remind|notify)\\b",
            "\\bremind me\\b", "\\b(please )?(schedule|book|arrange) (a |an |me a )?(call|meeting|conference|session|appointment)",
            "(gorusme|toplanti|arama)\\w* (ayarla|planla|ayarlar misin|planlar misin)", "\\bhatirlat");

    /** Questions about who can read the conversation: answered truthfully by a fixed reply, not by the model. */
    private static final List<Pattern> PRIVACY = patterns(
            "\\b(can|will|does|do|could|would) (the |my )?(cmo|crew|crewmates?|anyone|anybody|someone|nasa|ground|"
                    + "mission control|flight surgeon|doctors?|commander) (ever )?(see|read|know|hear|access|find out)( about)? "
                    + "(what i (write|say|said|wrote|type|tell you)|this|it|my (messages?|chat|words)|our (chat|conversation))",
            "\\b(is|are|will) (this|our|my|these) (chat|conversation|messages?|talk)( be)? (private|recorded|saved|stored|"
                    + "logged|monitored|shared|confidential|sent|reported|read)",
            "\\bwho (else )?(can|will|could) (see|read) (this|what i|my|our)",
            "\\b(cmo|kimse|biri|baskasi|ekip|yer ekibi|nasa) (bunu |yazdiklarimi |mesajlarimi )?(gorebilir|gorur|okur|okuyabilir|"
                    + "duyar|ogrenir)",
            "\\b(sohbet|konusma|mesaj)\\w* (gizli mi|kaydediliyor|kaydedilir|saklaniyor|gonderiliyor)");

    /**
     * Requests for the conditioning prompt or to override it never reach the model. "the rules" alone
     * is ordinary speech ("the rules up here"), so it needs a verb that asks for them.
     */
    private static final List<Pattern> PROMPT_REQUEST = patterns(
            "system prompt", "your (instructions|rules|prompt|configuration)",
            "(show|tell|give|print|reveal|list|repeat|share) (me )?the (instructions|rules|prompt|configuration)",
            "initial prompt", "ignore (all |the |your )?(previous|above|prior) (instructions|rules)", "developer (message|mode)",
            "jailbreak", "repeat (everything|all|the text|what is|what's|the words) (above|before|written)",
            "(above|previous) (text|message)s? (word for word|verbatim)", "(talimat|kural)lar?ini (yaz|soyle|goster)");

    private static final List<Pattern> CRISIS = patterns(
            "suicid", "kill myself", "end(ing)? (my life|it all)", "want to die", "wanna die", "don'?t want to live",
            "do not want to live", "no reason to live", "hurt myself", "harm myself", "self[- ]?harm", "better off dead",
            "(don'?t|do not) want to (wake up|be here|exist)", "wish i (was|were) dead", "can'?t go on",
            "uyanmak istemiyorum", "yok olmak istiyorum", "olsem daha iyi",
            "better off (without me|if i (was|were|am) gone|if i (was|were)n'?t here)",
            // Indirect wording found by the evaluation (2026-11-15).
            "(no ?one|nobody|no-one) would (even )?(notice|care|miss me)", "if i (just )?(disappeared|vanished)",
            "what'?s the point (of|in) (going on|living|anything|it all|being here)", "no point (in )?(living|going on)",
            "never wake up", "(don'?t|do not) wanna (live|be here|exist|wake up)",
            "intihar", "kendimi oldur", "olmek istiyorum", "yasamak istemiyorum", "kendime zarar", "hayatima son",
            "uyanmasam", "(her seye|hayatima|kendime) son ver", "yasamanin (bir )?anlami yok", "ortadan kaybolsam",
            "kimse fark etmez", "dogmasaydim");
    private static final List<Pattern> MEDICAL = patterns(
            "\\bdos(e|es|age)\\b", "\\b\\d+\\s?(mg|mcg|ml)\\b", "\\bmedication", "\\bmedicine", "\\bpills?\\b", "\\bmeds?\\b", "painkiller",
            "ibuprofen", "paracetamol", "acetaminophen", "aspirin",
            "\\btablet", "sleeping pill", "melatonin", "zolpidem", "ambien", "modafinil", "antidepress", "sertraline",
            "\\bdiagnos", "am i depressed", "do i have (depression|anxiety|ptsd|a disorder|burnout)", "am i (burned|burnt) out",
            "medical kit", "\\bmed ?kit", "first aid kit", "\\bpharmacy", "\\bdrugs?\\b", "prescri", "recete",
            "tukenmis(lik)? (miyim|mi)",
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
        if (PRIVACY.stream().anyMatch(p -> p.matcher(t).find())) {
            return new Verdict(Kind.PRIVACY, PRIVACY_REPLY);
        }
        if (ACTION_REQUEST.stream().anyMatch(p -> p.matcher(t).find())) {
            return new Verdict(Kind.ACTION_REQUEST, ACTION_REPLY);
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

package search;

/*
 * Text normalisation shared by the search components.
 *
 * Every string that is compared or scanned by the ranking code is first
 * reduced to lower-case letters a-z, digits 0-9 and single spaces. This is a
 * plain character loop (no regular expressions), which matters because the
 * same normalisation is applied to millions of characters of corpus text.
 */
public final class TextNormalizer {

    private static final String[] STOP_WORDS = {
            "a", "an", "the", "my", "me", "i", "is", "are", "was", "were",
            "be", "been", "being", "am", "has", "have", "had", "having",
            "do", "does", "did", "done", "to", "of", "in", "on", "for",
            "and", "or", "but", "that", "this", "these", "those", "it",
            "its", "them", "they", "their", "there", "with", "from", "as",
            "at", "by", "what", "who", "whom", "how", "why", "when",
            "where", "which", "want", "wants", "wanted", "know", "need",
            "needs", "help", "please", "can", "could", "would", "should",
            "may", "might", "will", "shall", "not", "no", "nor", "so",
            "if", "then", "than", "too", "very", "just", "also", "only",
            "own", "same", "such", "some", "any", "all", "each", "every",
            "both", "few", "more", "most", "other", "another", "someone",
            "something", "somebody", "anyone", "anything", "everyone",
            "nothing", "without", "within", "between", "during", "into",
            "onto", "upon", "over", "under", "about", "after", "before",
            "because", "while", "until", "since", "again", "you", "your",
            "yours", "our", "ours", "he", "she", "him", "her", "his",
            "hers", "we", "us", "mine", "legal", "law", "issue", "issues",
            "problem", "problems", "case", "cases", "matter", "matters",
            "get", "got", "give", "gave", "given", "take", "took", "taken",
            "make", "made", "say", "said", "says", "tell", "told", "ask",
            "asked", "think", "thought", "like", "going", "went", "come",
            "came", "back", "now", "still", "even", "ever", "never",
            "always", "already", "however", "therefore", "ago", "one",
            "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "month", "months", "year", "years", "day", "days",
            "week", "weeks", "time", "times", "person", "people", "party",
            "else", "right", "rights", "part", "way", "thing", "things"
    };

    private TextNormalizer() {
    }

    /* Lower-case a-z / 0-9 with single spaces, no leading or trailing space. */
    public static String normalize(String text) {

        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(text.length());
        boolean pendingSpace = false;

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 32);
            }

            boolean keep = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');

            if (keep) {

                if (pendingSpace && builder.length() > 0) {
                    builder.append(' ');
                }

                builder.append(c);
                pendingSpace = false;

            } else {
                pendingSpace = true;
            }
        }

        return builder.toString();
    }

    /* Normalised text wrapped in single spaces, for whole-word scanning. */
    public static String padded(String text) {

        String normalized = normalize(text);

        if (normalized.isEmpty()) {
            return " ";
        }

        return " " + normalized + " ";
    }

    /* Splits normalised text on single spaces. */
    public static String[] words(String normalized) {

        if (normalized == null || normalized.isEmpty()) {
            return new String[0];
        }

        int count = 1;

        for (int i = 0; i < normalized.length(); i++) {
            if (normalized.charAt(i) == ' ') {
                count++;
            }
        }

        String[] result = new String[count];
        int index = 0;
        int start = 0;

        for (int i = 0; i <= normalized.length(); i++) {

            if (i == normalized.length() || normalized.charAt(i) == ' ') {

                if (i > start) {
                    result[index++] = normalized.substring(start, i);
                }

                start = i + 1;
            }
        }

        String[] trimmed = new String[index];

        for (int i = 0; i < index; i++) {
            trimmed[i] = result[i];
        }

        return trimmed;
    }

    public static boolean isStopWord(String word) {

        for (int i = 0; i < STOP_WORDS.length; i++) {
            if (STOP_WORDS[i].equals(word)) {
                return true;
            }
        }

        return false;
    }
}

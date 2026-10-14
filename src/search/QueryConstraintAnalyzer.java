package search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General-purpose extraction and validation of explicit negative conditions
 * in user queries. This is deliberately domain-independent: it does not know
 * about warrants, property seizure, salary, tenancy, or any individual case.
 *
 * It is a conservative lexical evidence check, not a semantic/legal proof.
 * If a query explicitly says a condition was absent (e.g. without notice), a
 * candidate must contain a nearby negative cue and at least one target term
 * from that condition before it can be presented as a case-specific candidate.
 */
public final class QueryConstraintAnalyzer {
    private static final Pattern NEGATED_CLAUSE = Pattern.compile(
            "\\b(without|lacking|absence of|lack of|no|never|not|failed to|did not|does not|do not|was not|were not|has not|have not|had not)\\b\\s+([^,.;!?]+?)(?=\\s+\\b(?:and|but)\\b|[,.;!?]|$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TOKEN = Pattern.compile("[a-z0-9]+(?:'[a-z]+)?");
    private static final List<String> STOP_WORDS = Arrays.asList(
            "a", "an", "the", "my", "me", "i", "we", "our", "you", "your",
            "he", "she", "they", "them", "it", "any", "some", "this", "that",
            "was", "were", "is", "are", "be", "been", "being", "to", "of", "in",
            "on", "at", "by", "for", "from", "with", "as", "and", "or", "but",
            "had", "has", "have", "did", "does", "do", "not", "no", "never",
            "without", "failed", "lacking", "absence", "lack", "prior", "before",
            "after", "any", "thing", "things", "someone", "something"
    );
    private static final String[] NEGATION_CUES = {
            "without", "no", "not", "never", "failed to", "did not", "does not",
            "do not", "was not", "were not", "has not", "have not", "had not",
            "absence of", "lack of", "lacking"
    };

    private QueryConstraintAnalyzer() { }

    public static final class Constraint {
        private final String cue;
        private final String sourcePhrase;
        private final List<String> targetTerms;

        Constraint(String cue, String sourcePhrase, List<String> targetTerms) {
            this.cue = cue;
            this.sourcePhrase = sourcePhrase;
            this.targetTerms = Collections.unmodifiableList(new ArrayList<>(targetTerms));
        }

        public String getCue() { return cue; }
        public String getSourcePhrase() { return sourcePhrase; }
        public List<String> getTargetTerms() { return targetTerms; }

        @Override public String toString() {
            return sourcePhrase + " -> " + targetTerms;
        }
    }

    public static List<Constraint> extract(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        String normalized = normalize(query);
        Matcher matcher = NEGATED_CLAUSE.matcher(normalized);
        List<Constraint> constraints = new ArrayList<>();
        while (matcher.find()) {
            String cue = matcher.group(1).trim();
            String clause = matcher.group(2).trim();
            List<String> terms = meaningfulTerms(clause);
            if (!terms.isEmpty()) {
                constraints.add(new Constraint(cue, cue + " " + clause, terms));
            }
        }
        return Collections.unmodifiableList(constraints);
    }

    /**
     * Every explicit negative condition must have local textual support and,
     * when event positions are available, be close to the query's action and
     * object evidence. This prevents a random negated sentence elsewhere in a
     * judgment from validating an unrelated search result.
     */
    public static boolean isSupportedNearEvent(
            String body,
            List<Constraint> constraints,
            int[][] positions,
            int[] actionGroups,
            int[] objectGroups,
            int maxEventDistance) {
        if (constraints == null || constraints.isEmpty()) return true;
        if (body == null || body.isEmpty()) return false;
        String normalized = " " + normalize(body) + " ";
        for (Constraint constraint : constraints) {
            boolean supported = false;
            for (String term : constraint.targetTerms) {
                int termAt = normalized.indexOf(" " + term + " ");
                while (termAt >= 0) {
                    int cueAt = cueIndex(normalized, constraint.cue, termAt);
                    if (cueAt >= 0 && cueAt <= termAt && (termAt - cueAt) <= 60
                            && nearGroups(termAt, positions, actionGroups, maxEventDistance)
                            && nearGroups(termAt, positions, objectGroups, maxEventDistance)) {
                        supported = true;
                        break;
                    }
                    termAt = normalized.indexOf(" " + term + " ", termAt + 1);
                }
                if (supported) break;
            }
            if (!supported) return false;
        }
        return true;
    }

    private static boolean nearGroups(
            int conditionAt, int[][] positions, int[] groups, int maxDistance) {
        if (groups == null || groups.length == 0) return true;
        if (positions == null) return false;
        for (int group : groups) {
            if (group < 0 || group >= positions.length || positions[group] == null) continue;
            for (int position : positions[group]) {
                // Aho-Corasick positions are end-exclusive in padded text;
                // the normalized condition index differs by at most one.
                if (Math.abs(position - conditionAt) <= maxDistance + 2) return true;
            }
        }
        return false;
    }

    private static List<String> meaningfulTerms(String clause) {
        List<String> terms = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(clause);
        while (matcher.find()) {
            String token = matcher.group().toLowerCase(Locale.ROOT);
            if (token.length() < 3 || STOP_WORDS.contains(token)) continue;
            if (!terms.contains(token)) terms.add(token);
        }
        return terms;
    }

    private static boolean containsCue(String sentence) {
        for (String cue : NEGATION_CUES) {
            if (containsPhrase(sentence, cue)) return true;
        }
        return false;
    }

    private static int cueIndex(String sentence, String cue, int targetAt) {
        if (targetAt < 0 || cue == null || cue.isEmpty()) return -1;
        int best = -1;
        int distance = Integer.MAX_VALUE;
        String pattern = " " + cue + " ";
        int at = sentence.indexOf(pattern);
        while (at >= 0) {
            int d = Math.abs(at - targetAt);
            if (d < distance) { distance = d; best = at; }
            at = sentence.indexOf(pattern, at + 1);
        }
        return best;
    }

    private static boolean containsWord(String text, String word) {
        return (" " + text + " ").contains(" " + word + " ");
    }

    private static boolean containsPhrase(String text, String phrase) {
        return (" " + text + " ").contains(" " + phrase + " ");
    }

    private static String normalize(String text) {
        return " " + text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9']+", " ").trim().replaceAll("\\s+", " ") + " ";
    }
}

package search;

import structures.SimpleList;

import analysis.LegalProblemAnalyzer;

import algorithms.KMP;
import algorithms.RabinKarp;
import algorithms.ZAlgorithm;

import model.LegalDocument;

import search.RelevanceScorer;
import search.LegalVocabulary;

public class SearchEngine {

    public static final int DEFAULT_TOP_K = 10;

    private static final String[] STOP_WORDS = {
            "a",
            "an",
            "the",
            "my",
            "me",
            "i",
            "is",
            "are",
            "was",
            "were",
            "has",
            "have",
            "had",
            "to",
            "of",
            "in",
            "on",
            "for",
            "and",
            "or",
            "but",
            "that",
            "this",
            "it",
            "them",
            "they",
            "their",
            "with",
            "from",
            "as",
            "what",
            "who",
            "how",
            "why",
            "when",
            "where",
            "want",
            "know",
            "not",
            "someone",
            "claiming",
            "claims",
            "says",
            "said",
            "belong",
            "belongs",
            "belonged",
            "other",
            "party",
            "three",
            "months",
            "legal",
            "issues",
            "issue",
            "arise",
            "arises",
            "follow",
            "refusing",
            "refuse",
            "signed",
            "bought",
            "our",
            "your",
            "their",
            "mine",
            "person",
            "people",
            "until",
            "after",
            "before",
            "because",
            "while",
            "about",
            "under",
            "over",
            "against",
            "across",
            "around",
            "through",
            "without",
            "within",
            "between",
            "during",
            "into",
            "throughout"
    };

    public enum Algorithm {
        KMP,
        RABIN_KARP,
        Z_ALGORITHM
    }

    public enum MatchType {
        TITLE_MATCH,
        CONTENT_REFERENCE,
        DIRECT_MATCH,
        RELATED_MATCH,
        WEAK_MATCH
    }

    public static class SearchResult {

        private final LegalDocument document;
        private final int position;
        private final Algorithm algorithm;
        private final MatchType matchType;
        private final int relevanceScore;
        private final SimpleList<String> matchedTerms;
        private final SimpleList<String> matchedConcepts;
        private final SimpleList<String> explanation;

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType) {

            this(
                    document,
                    position,
                    algorithm,
                    matchType,
                    0,
                    new SimpleList<String>(),
                    new SimpleList<String>()
            );
        }

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType,
                int relevanceScore,
                SimpleList<String> matchedTerms) {

            this(
                    document,
                    position,
                    algorithm,
                    matchType,
                    relevanceScore,
                    matchedTerms,
                    new SimpleList<String>()
            );
        }

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType,
                int relevanceScore,
                SimpleList<String> matchedTerms,
                SimpleList<String> matchedConcepts) {

            this.document = document;
            this.position = position;
            this.algorithm = algorithm;
            this.matchType = matchType;
            this.relevanceScore = relevanceScore;
            this.matchedTerms = matchedTerms;
            this.matchedConcepts = matchedConcepts;
            this.explanation = new SimpleList<String>();
        }

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType,
                int relevanceScore,
                SimpleList<String> matchedTerms,
                SimpleList<String> matchedConcepts,
                SimpleList<String> explanation) {

            this.document = document;
            this.position = position;
            this.algorithm = algorithm;
            this.matchType = matchType;
            this.relevanceScore = relevanceScore;
            this.matchedTerms = matchedTerms;
            this.matchedConcepts = matchedConcepts;
            this.explanation = explanation == null
                    ? new SimpleList<String>() : explanation;
        }

        public LegalDocument getDocument() {
            return document;
        }

        public int getPosition() {
            return position;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public MatchType getMatchType() {
            return matchType;
        }

        public int getRelevanceScore() {
            return relevanceScore;
        }

        public SimpleList<String> getMatchedTerms() {
            return matchedTerms;
        }

        public SimpleList<String> getMatchedConcepts() {
            return matchedConcepts;
        }

        public SimpleList<String> getExplanation() {
            return explanation;
        }
    }

    public static class SearchRankedOutcome {

        private final SimpleList<SearchResult> results;
        private final int candidateCount;
        private final int displayedCount;
        private final SimpleList<String> legalConcepts;
        private final boolean hasDirectMatch;

        public SearchRankedOutcome(
                SimpleList<SearchResult> results,
                int candidateCount,
                int displayedCount,
                SimpleList<String> legalConcepts) {
            this(results, candidateCount, displayedCount, legalConcepts, false);
        }

        public SearchRankedOutcome(
                SimpleList<SearchResult> results,
                int candidateCount,
                int displayedCount,
                SimpleList<String> legalConcepts,
                boolean hasDirectMatch) {

            this.results = results;
            this.candidateCount = candidateCount;
            this.displayedCount = displayedCount;
            this.legalConcepts = legalConcepts;
            this.hasDirectMatch = hasDirectMatch;
        }

        public SimpleList<SearchResult> getResults() {
            return results;
        }

        public int getCandidateCount() {
            return candidateCount;
        }

        public int getDisplayedCount() {
            return displayedCount;
        }

        public SimpleList<String> getLegalConcepts() {
            return legalConcepts;
        }

        public boolean hasDirectMatch() {
            return hasDirectMatch;
        }
    }

    private final SimpleList<LegalDocument> documents;
    private final LegalTermCatalog legalTermCatalog;
    private RelevanceScorer relevanceScorer;

    public SearchEngine(SimpleList<LegalDocument> documents) {
        this.documents = documents;
        this.legalTermCatalog = new LegalTermCatalog();
    }

    /*
     * ============================================================
     * AUTOMATIC ALGORITHM SELECTION
     * ============================================================
     *
     * The user does not select an algorithm.
     * The system selects one based on the query type.
     */

    public static Algorithm chooseAlgorithm(
            QueryProcessor.QueryType queryType) {

        if (queryType
                == QueryProcessor.QueryType.EXACT_LEGAL_TERM) {

            return Algorithm.KMP;

        } else if (queryType
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            return Algorithm.RABIN_KARP;

        } else {

            return Algorithm.Z_ALGORITHM;
        }
    }

    /*
     * Gives a human-readable explanation for the
     * automatically selected algorithm.
     */

    public static String getAlgorithmReason(
            QueryProcessor.QueryType queryType,
            Algorithm algorithm) {

        if (algorithm == Algorithm.KMP) {

            return "Exact legal-term query; KMP provides predictable O(n + m) pattern matching.";

        } else if (algorithm == Algorithm.RABIN_KARP) {

            return "Fuzzy-resolved legal term; Rabin-Karp uses hashing for efficient pattern comparison.";

        } else {

            return "General text query; Z Algorithm provides efficient prefix-based pattern matching.";
        }
    }

    public SimpleList<String> extractMeaningfulTerms(String query) {

        SimpleList<String> meaningfulTerms =
                new SimpleList<String>();

        if (query == null || query.trim().isEmpty()) {
            return meaningfulTerms;
        }

        String normalized =
                normalizeText(query);

        if (normalized.isEmpty()) {
            return meaningfulTerms;
        }

        String[] words =
                normalized.split(" ");

        for (int i = 0; i < words.length; i++) {

            String word = words[i].trim();
            String normalizedWord = normalizeForComparison(word);

            if (word.isEmpty()
                    || normalizedWord.isEmpty()
                    || !isMeaningfulTerm(word)
                    || meaningfulTerms.contains(normalizedWord)) {
                continue;
            }

            meaningfulTerms.add(normalizedWord);
        }

        return meaningfulTerms;
    }

    public SimpleList<SearchResult> searchRanked(
            String query,
            Algorithm algorithm,
            int topK) {

        return searchRankedWithMetadata(
                query,
                algorithm,
                topK
        ).getResults();
    }


    /*
     * General relevance ranking for a described legal situation.
     *
     * This replaces the earlier per-situation scoring blocks. All subject
     * knowledge (which words mean the same event, which are actions vs.
     * objects vs. actors) lives in LegalVocabulary; this method applies the
     * SAME model to every query, using RelevanceScorer. See RelevanceScorer
     * for the full explanation of coverage, event evidence and tiers.
     */
    public SearchRankedOutcome searchRankedWithMetadata(
            String query,
            Algorithm algorithm,
            int topK) {

        return searchRankedWithMetadata(
                query,
                algorithm,
                topK,
                null
        );
    }

    public SearchRankedOutcome searchRankedWithMetadata(
            String query,
            Algorithm algorithm,
            int topK,
            LegalProblemAnalyzer.AnalysisResult analysis) {

        SimpleList<SearchResult> empty = new SimpleList<SearchResult>();

        if (query == null || query.trim().isEmpty()) {
            return new SearchRankedOutcome(
                    empty, 0, 0, new SimpleList<String>(), false);
        }

        if (relevanceScorer == null) {
            relevanceScorer = new RelevanceScorer(documents);
        }

        SimpleList<String> situationTerms = new SimpleList<String>();
        SimpleList<String> analyserLabels = new SimpleList<String>();

        if (analysis != null) {

            SimpleList<String> actors = analysis.getSituation().getActors();
            SimpleList<String> actions = analysis.getSituation().getActions();
            SimpleList<String> objects = analysis.getSituation().getObjects();
            SimpleList<String> relationships =
                    analysis.getSituation().getRelationships();

            addAll(situationTerms, actors);
            addAll(situationTerms, actions);
            addAll(situationTerms, objects);
            addAll(situationTerms, relationships);

            analyserLabels = analysis.getSituation().getLegalDomains();
        }

        boolean allowDirect = analysis == null
                || !"LOW".equalsIgnoreCase(
                        analysis.getSituation().getConfidence());

        // Interpret the described event before retrieval. The original words stay
        // in the query; only a conservative canonical cue is added when multiple
        // independent situation slots agree. All ranking, evidence checks and
        // existing algorithms remain in the same shared search pipeline.
        String interpretedQuery = SituationInterpreter.expandForRetrieval(query);
        RelevanceScorer.Result result = relevanceScorer.score(
                interpretedQuery, situationTerms, analyserLabels, allowDirect, topK);

        // If the user describes a personal incident but no supported event frame
        // or legal issue was identified, never fill the screen with coincidental
        // lexical overlaps. Keep this separate from ordinary legal-term search.
        boolean noSupportedIncident = SituationInterpreter.isUnclassifiedPersonalIncident(query)
                && !result.hasDirect();
        if (noSupportedIncident) {
            return new SearchRankedOutcome(
                    new SimpleList<SearchResult>(), result.candidateCount, 0,
                    result.queryConcepts, false);
        }

        SimpleList<SearchResult> results = new SimpleList<SearchResult>();
        boolean hasDisplayedDirect = false;

        for (int i = 0; i < result.top.size(); i++) {

            RelevanceScorer.Scored scored = result.top.get(i);

            // A judgment mentioning a generic legal concept is not evidence for
            // a specific theft incident unless it also mentions the item described.
            if (scored.document != null
                    && "JUDGMENT".equalsIgnoreCase(scored.document.getKind())
                    && !SituationInterpreter.judgmentSupportsTheftObject(
                            query, scored.document.getTitle(), scored.document.getBody())) {
                continue;
            }

            if (scored.document != null
                    && SituationInterpreter.theftStatuteContradictsSituation(
                            query, scored.document.getTitle())) {
                continue;
            }

            MatchType matchType;

            if (scored.tier == RelevanceScorer.Tier.DIRECT) {
                matchType = MatchType.DIRECT_MATCH;
                hasDisplayedDirect = true;
            } else if (scored.tier == RelevanceScorer.Tier.RELATED) {
                matchType = MatchType.RELATED_MATCH;
            } else {
                matchType = MatchType.WEAK_MATCH;
            }

            results.add(new SearchResult(
                    scored.document,
                    scored.firstPosition,
                    algorithm,
                    matchType,
                    (int) Math.round(scored.score),
                    scored.matchedTerms,
                    scored.matchedConcepts,
                    scored.explanation
            ));
        }

        results = promoteSectionMatches(query, results);

        return new SearchRankedOutcome(
                results,
                result.candidateCount,
                results.size(),
                result.queryConcepts,
                hasDisplayedDirect
        );
    }

    private static final java.util.regex.Pattern SECTION_REF =
            java.util.regex.Pattern.compile("(?i)\\bsections?\\s+(\\d+[a-z]?)");

    /* If the user typed "section N", documents whose title names section N come first. */
    private static SimpleList<SearchResult> promoteSectionMatches(
            String query, SimpleList<SearchResult> results) {

        java.util.regex.Matcher m = SECTION_REF.matcher(query);
        SimpleList<String> numbers = new SimpleList<String>();
        while (m.find()) {
            numbers.add(m.group(1).toLowerCase());
        }
        if (numbers.isEmpty()) {
            return results;
        }

        SimpleList<SearchResult> matched = new SimpleList<SearchResult>();
        SimpleList<SearchResult> others = new SimpleList<SearchResult>();

        for (int i = 0; i < results.size(); i++) {
            SearchResult r = results.get(i);
            String title = (r.getDocument() == null || r.getDocument().getTitle() == null)
                    ? "" : r.getDocument().getTitle().toLowerCase();
            boolean hit = false;
            if (title.contains("section")) {
                for (int j = 0; j < numbers.size(); j++) {
                    String regex = "(?<![0-9a-z])"
                            + java.util.regex.Pattern.quote(numbers.get(j)) + "(?![0-9a-z])";
                    if (java.util.regex.Pattern.compile(regex).matcher(title).find()) {
                        hit = true;
                    }
                }
            }
            if (hit) {
                matched.add(r);
            } else {
                others.add(r);
            }
        }

        for (int i = 0; i < others.size(); i++) {
            matched.add(others.get(i));
        }
        return matched;
    }

    private static void addAll(
            SimpleList<String> target,
            SimpleList<String> source) {

        if (source == null) {
            return;
        }

        for (int i = 0; i < source.size(); i++) {

            String value = source.get(i);

            if (value != null && !value.trim().isEmpty()
                    && !target.contains(value)) {
                target.add(value);
            }
        }
    }

    public SimpleList<SearchResult> search(
            String query,
            Algorithm algorithm) {

        SimpleList<SearchResult> results =
                new SimpleList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery = query.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");

        if (normalizedQuery.isEmpty()) {
            return results;
        }

        SimpleList<String> searchVariants =
                buildSearchVariants(normalizedQuery);

        for (int i = 0; i < documents.size(); i++) {

            LegalDocument document =
                    documents.get(i);

            String content =
                    document.getContent().toLowerCase();
            String title =
                    document.getTitle().toLowerCase();

            int position = -1;
            String matchedVariant = "";
            boolean titleMatch = false;

            for (int j = 0; j < searchVariants.size(); j++) {
                String variant = searchVariants.get(j);
                int variantPosition = findWholeTermPosition(
                        content,
                        variant,
                        algorithm
                );
                int titlePosition = findWholeTermPosition(
                        title,
                        variant,
                        algorithm
                );

                if (variantPosition != -1
                        && (position == -1 || variantPosition < position)) {
                    position = variantPosition;
                    matchedVariant = variant;
                }

                if (titlePosition != -1) {
                    titleMatch = true;

                    if (matchedVariant.isEmpty()) {
                        matchedVariant = variant;
                    }
                }
            }

            if (position != -1 || titleMatch) {

                MatchType matchType;

                if (titleMatch) {

                    matchType =
                            MatchType.TITLE_MATCH;

                } else {

                    matchType =
                            MatchType.CONTENT_REFERENCE;
                }

                results.add(
                        new SearchResult(
                                document,
                                position == -1 ? 0 : position,
                                algorithm,
                                matchType
                        )
                );
            }
        }

        return results;
    }

    /*
     * Limits console output without discarding the complete result set
     * returned by search(). Title matches are shown first; ties keep the
     * deterministic corpus order.
     */
    public SimpleList<SearchResult> limitResults(
            SimpleList<SearchResult> source,
            int limit) {

        SimpleList<SearchResult> limited = new SimpleList<SearchResult>();

        if (source == null || limit <= 0) {
            return limited;
        }

        addLimitedByMatchType(
                limited,
                source,
                MatchType.TITLE_MATCH,
                limit
        );

        addLimitedByMatchType(
                limited,
                source,
                MatchType.CONTENT_REFERENCE,
                limit
        );

        return limited;
    }

    private void addLimitedByMatchType(
            SimpleList<SearchResult> target,
            SimpleList<SearchResult> source,
            MatchType matchType,
            int limit) {

        for (int i = 0; i < source.size() && target.size() < limit; i++) {
            SearchResult result = source.get(i);
            if (result.getMatchType() == matchType) {
                target.add(result);
            }
        }
    }

    private SimpleList<String> buildSearchVariants(String normalizedQuery) {

        SimpleList<String> variants = new SimpleList<String>();
        variants.add(normalizedQuery);

        addSearchVariant(variants, normalizedQuery,
                "dishonour", "dishonor");
        addSearchVariant(variants, normalizedQuery,
                "honouring", "honoring");
        addSearchVariant(variants, normalizedQuery,
                "not paid", "unpaid");

        return variants;
    }

    private void addSearchVariant(
            SimpleList<String> variants,
            String query,
            String left,
            String right) {

        if (query.equals(left) && !variants.contains(right)) {
            variants.add(right);
        }

        if (query.equals(right) && !variants.contains(left)) {
            variants.add(left);
        }
    }

    private int findWholeTermPosition(
            String text,
            String term,
            Algorithm algorithm) {

        if (text == null || term == null || term.isEmpty()) {
            return -1;
        }

        int searchStart = 0;

        while (searchStart <= text.length() - term.length()) {
            int relativePosition = searchWithAlgorithm(
                    text.substring(searchStart),
                    term,
                    algorithm
            );

            if (relativePosition == -1) {
                return -1;
            }

            int position = searchStart + relativePosition;

            if (hasWordBoundaries(text, position, term.length())) {
                return position;
            }

            searchStart = position + 1;
        }

        return -1;
    }

    private int searchWithAlgorithm(
            String text,
            String pattern,
            Algorithm algorithm) {

        if (algorithm == Algorithm.KMP) {
            return KMP.search(text, pattern);
        }

        if (algorithm == Algorithm.RABIN_KARP) {
            return RabinKarp.search(text, pattern);
        }

        return ZAlgorithm.search(text, pattern);
    }

    private boolean hasWordBoundaries(
            String text,
            int position,
            int length) {

        int end = position + length;

        return (position == 0 || !isWordCharacter(text.charAt(position - 1)))
                && (end == text.length()
                || !isWordCharacter(text.charAt(end)));
    }

    private boolean isWordCharacter(char value) {
        return (value >= 'a' && value <= 'z')
                || (value >= '0' && value <= '9');
    }

    private boolean isMeaningfulTerm(String word) {

        if (word == null || word.isEmpty()) {
            return false;
        }

        String normalizedWord = normalizeForComparison(word);

        if (normalizedWord.isEmpty()) {
            return false;
        }

        if (isCatalogTerm(normalizedWord)) {
            return true;
        }

        if (normalizedWord.length() <= 3) {
            return false;
        }

        for (int i = 0; i < STOP_WORDS.length; i++) {

            if (STOP_WORDS[i].equals(word)) {
                return false;
            }
        }

        return isKnownContextWord(normalizedWord);
    }

    private boolean isCatalogTerm(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        SimpleList<String> catalogTerms = legalTermCatalog.getTerms();

        for (int i = 0; i < catalogTerms.size(); i++) {
            String catalogTerm = normalizeForComparison(catalogTerms.get(i));
            if (!catalogTerm.isEmpty() && catalogTerm.equals(word)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKnownContextWord(String word) {

        String normalizedWord =
                normalizeForComparison(word);

        if (normalizedWord.isEmpty()) {
            return false;
        }

        // General legal vocabulary, shared with the ranking model, replaces
        // the old fixed LEGAL_CONCEPT_NAMES / LEGAL_CONCEPT_KEYWORDS table.
        for (int g = 0; g < LegalVocabulary.size(); g++) {

            String[] phrases = LegalVocabulary.phrases(g);

            for (int j = 0; j < phrases.length; j++) {

                if (phrases[j].indexOf(' ') == -1
                        && normalizedWord.equals(phrases[j])) {
                    return true;
                }
            }
        }

        return false;
    }


    /*
     * Legal-concept labels the query touches, using the same vocabulary as
     * the ranking model (LegalVocabulary), so "why relevant" explanations and
     * the concept list shown to the user are always consistent with each
     * other.
     */
    public SimpleList<String> extractLegalConcepts(String query) {

        SimpleList<String> concepts = new SimpleList<String>();

        if (query == null || query.trim().isEmpty()) {
            return concepts;
        }

        String padded = " " + normalizeText(query) + " ";

        for (int g = 0; g < LegalVocabulary.size(); g++) {

            String[] phrases = LegalVocabulary.phrases(g);

            for (int p = 0; p < phrases.length; p++) {

                if (padded.indexOf(" " + phrases[p] + " ") >= 0) {
                    concepts.add(LegalVocabulary.label(g));
                    break;
                }
            }
        }

        return concepts;
    }

    private String normalizeForComparison(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                value.toLowerCase();

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9 ]",
                        " "
                );

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        String[] words =
                splitWords(normalized);

        StringBuilder builder =
                new StringBuilder();

        for (int i = 0; i < words.length; i++) {

            String word =
                    singularizeToken(words[i]);

            if (word.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(word);
        }

        return builder.toString().trim();
    }

    private String singularizeToken(String token) {

        if (token == null || token.isEmpty()) {
            return "";
        }

        String normalized = token.trim();

        if (normalized.length() <= 3) {
            return normalized;
        }

        if (normalized.endsWith("ies")
                && normalized.length() > 4) {
            return normalized.substring(0, normalized.length() - 3) + "y";
        }

        if (normalized.endsWith("sses")
                || normalized.endsWith("xes")
                || normalized.endsWith("zes")
                || normalized.endsWith("ches")
                || normalized.endsWith("shes")) {
            return normalized.substring(0, normalized.length() - 2);
        }

        if (normalized.endsWith("s")
                && !normalized.endsWith("ss")) {
            return normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        String normalized =
                text.toLowerCase();

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9 ]",
                        " "
                );

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        return normalized.trim();
    }

    private boolean containsPhrase(
            String text,
            String phrase) {

        if (text == null || phrase == null) {
            return false;
        }

        String[] textWords =
                splitWords(text);
        String[] phraseWords =
                splitWords(phrase);

        if (phraseWords.length == 0
                || phraseWords.length > textWords.length) {
            return false;
        }

        for (int i = 0;
                i <= textWords.length - phraseWords.length;
                i++) {

            boolean match = true;

            for (int j = 0; j < phraseWords.length; j++) {

                if (!textWords[i + j].equals(phraseWords[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }

    private String[] splitWords(String text) {

        String[] rawWords =
                text.split(" ");

        int count = 0;

        for (int i = 0; i < rawWords.length; i++) {

            if (!rawWords[i].trim().isEmpty()) {
                count++;
            }
        }

        String[] words = new String[count];
        int index = 0;

        for (int i = 0; i < rawWords.length; i++) {

            String word = rawWords[i].trim();

            if (!word.isEmpty()) {
                words[index] = word;
                index++;
            }
        }

        return words;
    }
}

package search;

import structures.SimpleList;

public class QueryProcessor {

    private final LegalTermCatalog legalTermCatalog;
    private final FuzzyLegalSearch fuzzySearch;

    public QueryProcessor() {

        legalTermCatalog = new LegalTermCatalog();
        fuzzySearch = new FuzzyLegalSearch(legalTermCatalog);
    }

    public QueryAnalysis analyze(String query) {

        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.isEmpty()) {

            return new QueryAnalysis(
                    query,
                    normalizedQuery,
                    QueryType.INVALID,
                    new SimpleList<String>(),
                    0
            );
        }

        // ============================================================
        // STEP 1: EXACT LEGAL TERM MATCH
        // ============================================================

        SimpleList<String> catalogTerms =
                legalTermCatalog.getTerms();

        for (int i = 0; i < catalogTerms.size(); i++) {

            String term = catalogTerms.get(i);

            if (term.equalsIgnoreCase(normalizedQuery)) {

                SimpleList<String> exactMatch =
                        new SimpleList<String>();

                exactMatch.add(term);

                return new QueryAnalysis(
                        query,
                        normalizedQuery,
                        QueryType.EXACT_LEGAL_TERM,
                        exactMatch,
                        0
                );
            }
        }

        // ============================================================
        // STEP 2: FUZZY LEGAL TERM MATCH
        // ============================================================

        // Fuzzy correction is a single-token convenience only. Running edit
        // distance across a whole natural-language sentence can silently
        // replace its meaning with a catalog term. Multi-word inputs stay as
        // their original text-search query.
        SimpleList<FuzzyLegalSearch.FuzzyResult> matches =
                normalizedQuery.matches("[A-Za-z0-9]+")
                        ? fuzzySearch.search(normalizedQuery, 2)
                        : new SimpleList<FuzzyLegalSearch.FuzzyResult>();

        if (!matches.isEmpty()) {

            int bestDistance =
                    matches.get(0).getDistance();

            // Find the smallest edit distance
            for (int i = 1; i < matches.size(); i++) {

                FuzzyLegalSearch.FuzzyResult match =
                        matches.get(i);

                if (match.getDistance() < bestDistance) {

                    bestDistance =
                            match.getDistance();
                }
            }

            // Keep only the best fuzzy matches
            SimpleList<String> bestMatches =
                    new SimpleList<String>();

            for (int i = 0; i < matches.size(); i++) {

                FuzzyLegalSearch.FuzzyResult match =
                        matches.get(i);

                if (match.getDistance() == bestDistance) {

                    bestMatches.add(match.getTerm());
                }
            }

            return new QueryAnalysis(
                    query,
                    normalizedQuery,
                    QueryType.FUZZY_LEGAL_TERM,
                    bestMatches,
                    bestDistance
            );
        }

        // ============================================================
        // STEP 3: GENERAL TEXT SEARCH
        // ============================================================

        return new QueryAnalysis(
                query,
                normalizedQuery,
                QueryType.TEXT_SEARCH,
                new SimpleList<String>(),
                -1
        );
    }

    // ================================================================
    // QUERY TYPES
    // ================================================================

    public enum QueryType {

        INVALID,

        EXACT_LEGAL_TERM,

        FUZZY_LEGAL_TERM,

        TEXT_SEARCH
    }

    // ================================================================
    // QUERY ANALYSIS RESULT
    // ================================================================

    public static class QueryAnalysis {

        private final String originalQuery;
        private final String normalizedQuery;
        private final QueryType type;
        private final SimpleList<String> resolvedTerms;
        private final int distance;

        public QueryAnalysis(
                String originalQuery,
                String normalizedQuery,
                QueryType type,
                SimpleList<String> resolvedTerms,
                int distance) {

            this.originalQuery = originalQuery;
            this.normalizedQuery = normalizedQuery;
            this.type = type;
            this.resolvedTerms = resolvedTerms;
            this.distance = distance;
        }

        public String getOriginalQuery() {
            return originalQuery;
        }

        public String getNormalizedQuery() {
            return normalizedQuery;
        }

        public QueryType getType() {
            return type;
        }

        public SimpleList<String> getResolvedTerms() {
            return resolvedTerms;
        }

        public int getDistance() {
            return distance;
        }
    }
}

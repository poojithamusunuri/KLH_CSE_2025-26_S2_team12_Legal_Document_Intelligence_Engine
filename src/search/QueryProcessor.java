package search;

import algorithms.Levenshtein;

import java.util.ArrayList;
import java.util.List;

public class QueryProcessor {

    private final LegalTermCatalog legalTermCatalog;
    private final FuzzyLegalSearch fuzzySearch;

    public QueryProcessor() {
        legalTermCatalog = new LegalTermCatalog();
        fuzzySearch = new FuzzyLegalSearch(legalTermCatalog);
    }

    public QueryAnalysis analyze(String query) {

        if (query == null || query.trim().isEmpty()) {
            return new QueryAnalysis(
                    query,
                    QueryType.INVALID,
                    new ArrayList<String>(),
                    0
            );
        }

        String normalized = query.trim();

        /*
         * First check for an exact legal term.
         */
        for (String term : legalTermCatalog.getTerms()) {

            if (term.equalsIgnoreCase(normalized)) {

                List<String> exactMatch =
                        new ArrayList<>();

                exactMatch.add(term);

                return new QueryAnalysis(
                        query,
                        QueryType.EXACT_LEGAL_TERM,
                        exactMatch,
                        0
                );
            }
        }

        /*
         * No exact match.
         * Try fuzzy matching using Levenshtein distance.
         */
        List<FuzzyLegalSearch.FuzzyResult> matches =
                fuzzySearch.search(normalized, 2);

        if (!matches.isEmpty()) {

            /*
             * Find the smallest edit distance.
             */
            int bestDistance =
                    matches.get(0).getDistance();

            for (
                    FuzzyLegalSearch.FuzzyResult match
                    : matches) {

                if (match.getDistance() < bestDistance) {
                    bestDistance =
                            match.getDistance();
                }
            }

            /*
             * Keep only the candidates having
             * the best distance.
             */
            List<String> bestMatches =
                    new ArrayList<>();

            for (
                    FuzzyLegalSearch.FuzzyResult match
                    : matches) {

                if (match.getDistance()
                        == bestDistance) {

                    bestMatches.add(
                            match.getTerm()
                    );
                }
            }

            return new QueryAnalysis(
                    query,
                    QueryType.FUZZY_LEGAL_TERM,
                    bestMatches,
                    bestDistance
            );
        }

        /*
         * No legal-term match.
         * Treat it as a general text search.
         */
        return new QueryAnalysis(
                query,
                QueryType.TEXT_SEARCH,
                new ArrayList<String>(),
                -1
        );
    }

    public enum QueryType {

        INVALID,

        EXACT_LEGAL_TERM,

        FUZZY_LEGAL_TERM,

        TEXT_SEARCH
    }

    public static class QueryAnalysis {

        private final String originalQuery;
        private final QueryType type;
        private final List<String> resolvedTerms;
        private final int distance;

        public QueryAnalysis(
                String originalQuery,
                QueryType type,
                List<String> resolvedTerms,
                int distance) {

            this.originalQuery = originalQuery;
            this.type = type;
            this.resolvedTerms = resolvedTerms;
            this.distance = distance;
        }

        public String getOriginalQuery() {
            return originalQuery;
        }

        public QueryType getType() {
            return type;
        }

        public List<String> getResolvedTerms() {
            return resolvedTerms;
        }

        public int getDistance() {
            return distance;
        }
    }
}
package search;

import algorithms.Levenshtein;

import java.util.ArrayList;
import java.util.List;

public class FuzzyLegalSearch {

    public static class FuzzyResult {

        private final String term;
        private final int distance;

        public FuzzyResult(String term, int distance) {
            this.term = term;
            this.distance = distance;
        }

        public String getTerm() {
            return term;
        }

        public int getDistance() {
            return distance;
        }
    }

    private final LegalTermCatalog catalog;

    public FuzzyLegalSearch(LegalTermCatalog catalog) {
        this.catalog = catalog;
    }

    public List<FuzzyResult> search(
            String query,
            int maxDistance) {

        List<FuzzyResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery =
                normalize(query);

        for (String term : catalog.getTerms()) {

            String baseTerm =
                    removeYear(term);

            int distance =
                    Levenshtein.distance(
                            normalizedQuery,
                            normalize(baseTerm)
                    );

            if (distance <= maxDistance) {

                results.add(
                        new FuzzyResult(
                                term,
                                distance
                        )
                );
            }
        }

        return results;
    }

    private static String normalize(String text) {

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String removeYear(String term) {

        return term
                .replaceAll(",?\\s*\\(?\\d{4}\\)?\\s*$", "")
                .trim();
    }
}

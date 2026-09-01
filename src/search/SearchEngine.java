package search;

import algorithms.KMP;
import algorithms.RabinKarp;
import model.LegalDocument;

import java.util.ArrayList;
import java.util.List;

public class SearchEngine {

    public enum Algorithm {
        KMP,
        RABIN_KARP
    }

    public enum MatchType {
        TITLE_MATCH,
        CONTENT_REFERENCE
    }

    public static class SearchResult {

        private final LegalDocument document;
        private final int position;
        private final Algorithm algorithm;
        private final MatchType matchType;

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType) {

            this.document = document;
            this.position = position;
            this.algorithm = algorithm;
            this.matchType = matchType;
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
    }

    private final List<LegalDocument> documents;

    public SearchEngine(List<LegalDocument> documents) {
        this.documents = documents;
    }

    public List<SearchResult> search(
            String query,
            Algorithm algorithm) {

        List<SearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery = query.toLowerCase();

        for (LegalDocument document : documents) {

            String content =
                    document.getContent().toLowerCase();

            int position;

            if (algorithm == Algorithm.KMP) {

                position = KMP.search(
                        content,
                        normalizedQuery
                );

            } else {

                position = RabinKarp.search(
                        content,
                        normalizedQuery
                );
            }

            if (position != -1) {

                MatchType matchType;

                String title =
                        document.getTitle().toLowerCase();

                if (title.contains(normalizedQuery)) {

                    matchType =
                            MatchType.TITLE_MATCH;

                } else {

                    matchType =
                            MatchType.CONTENT_REFERENCE;
                }

                results.add(
                        new SearchResult(
                                document,
                                position,
                                algorithm,
                                matchType
                        )
                );
            }
        }

        return results;
    }
}

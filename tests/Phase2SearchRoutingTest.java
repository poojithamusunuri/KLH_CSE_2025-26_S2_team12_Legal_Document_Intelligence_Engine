package tests;

import analysis.LegalProblemAnalyzer;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.QueryProcessor;
import search.SearchEngine;
import structures.SimpleList;

public class Phase2SearchRoutingTest {

    private static int failures;

    public static void main(String[] args) throws Exception {
        LegalCorpusLoader loader = new LegalCorpusLoader("data/corpus");
        SearchEngine engine = new SearchEngine(loader.loadDocuments());

        run(engine, "contract");
        run(engine, "contrct");
        run(engine, "dishonour");
        run(engine, "dishonor");
        run(engine, "agreement between parties");
        run(engine, "salary");
        run(engine, "salary salary salary");
        run(engine, "pay");
        run(engine, "xyzzy");
        run(engine, "Indian Contract Act");
        run(engine, "");

        System.out.println("SUMMARY | failures=" + failures);

        if (failures > 0) {
            throw new AssertionError("Phase 2 search-routing test failed.");
        }
    }

    private static void run(SearchEngine engine, String query) {
        QueryProcessor.QueryAnalysis queryAnalysis =
                new QueryProcessor().analyze(query);
        SearchEngine.Algorithm algorithm =
                SearchEngine.chooseAlgorithm(queryAnalysis.getType());
        SimpleList<SearchEngine.SearchResult> displayed;
        int candidates;
        String route;

        if (queryAnalysis.getType()
                == QueryProcessor.QueryType.INVALID) {
            displayed = new SimpleList<SearchEngine.SearchResult>();
            candidates = 0;
            route = "invalid input";
        } else if (!queryAnalysis.getResolvedTerms().isEmpty()) {
            SimpleList<SearchEngine.SearchResult> allResults =
                    new SimpleList<SearchEngine.SearchResult>();

            for (int i = 0;
                    i < queryAnalysis.getResolvedTerms().size();
                    i++) {
                addUniqueDocuments(
                        allResults,
                        engine.search(
                                queryAnalysis.getResolvedTerms().get(i),
                                algorithm
                        )
                );
            }

            candidates = allResults.size();
            displayed = engine.limitResults(
                    allResults,
                    SearchEngine.DEFAULT_TOP_K
            );
            route = queryAnalysis.getType()
                    == QueryProcessor.QueryType.EXACT_LEGAL_TERM
                    ? "exact catalog retrieval"
                    : "fuzzy-resolved catalog retrieval";
        } else {
            LegalProblemAnalyzer.AnalysisResult problemAnalysis =
                    new LegalProblemAnalyzer().analyze(
                            queryAnalysis.getOriginalQuery()
                    );
            SearchEngine.SearchRankedOutcome outcome =
                    engine.searchRankedWithMetadata(
                            queryAnalysis.getNormalizedQuery(),
                            algorithm,
                            SearchEngine.DEFAULT_TOP_K,
                            problemAnalysis
                    );
            candidates = outcome.getCandidateCount();
            displayed = outcome.getResults();
            route = "general text retrieval";
        }

        boolean validLimit = displayed.size() <= SearchEngine.DEFAULT_TOP_K;
        boolean validRoute = queryAnalysis.getType()
                == QueryProcessor.QueryType.INVALID
                || algorithm != null;

        if (!validLimit || !validRoute) {
            failures++;
        }

        System.out.println(
                "QUERY | " + printable(query)
                        + " | type=" + queryAnalysis.getType()
                        + " | algorithm=" + algorithm
                        + " | route=" + route
                        + " | candidates=" + candidates
                        + " | displayed=" + displayed.size()
        );
        System.out.println("  top=" + topTitles(displayed, 3));
    }

    private static void addUniqueDocuments(
            SimpleList<SearchEngine.SearchResult> target,
            SimpleList<SearchEngine.SearchResult> source) {

        for (int i = 0; i < source.size(); i++) {
            SearchEngine.SearchResult candidate = source.get(i);
            boolean exists = false;

            for (int j = 0; j < target.size(); j++) {
                if (target.get(j).getDocument().getDocumentId().equals(
                        candidate.getDocument().getDocumentId())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                target.add(candidate);
            }
        }
    }

    private static String topTitles(
            SimpleList<SearchEngine.SearchResult> results,
            int maximum) {

        if (results.isEmpty()) {
            return "None";
        }

        StringBuilder titles = new StringBuilder();
        int limit = Math.min(results.size(), maximum);

        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                titles.append(" | ");
            }

            SearchEngine.SearchResult result = results.get(i);
            titles.append(result.getDocument().getTitle());
            titles.append(" [").append(result.getMatchType()).append("]");
        }

        return titles.toString();
    }

    private static String printable(String value) {
        return value.isEmpty() ? "<empty>" : value;
    }
}

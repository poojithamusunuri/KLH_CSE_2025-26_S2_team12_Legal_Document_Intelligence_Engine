package tests;

import analysis.LegalProblemAnalyzer;
import loader.LegalCorpusLoader;
import search.SearchEngine;
import search.QueryProcessor;

/** Regression checks that unknown/short terms reach the real search engine. */
public class QueryFallbackTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        testUnknownTermStillSearches();
        testShortLegalTermDoesNotRequireScenario();
        System.out.println("QUERY FALLBACK SUMMARY | failures=" + failures);
        if (failures > 0) {
            throw new AssertionError("Query fallback checks failed.");
        }
    }

    private static void testUnknownTermStillSearches() throws Exception {
        String query = "mortgage";
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(query);
        SearchEngine engine = new SearchEngine(
                new LegalCorpusLoader("data/corpus").loadDocuments());
        SearchEngine.SearchRankedOutcome outcome =
                engine.searchRankedWithMetadata(
                        query, SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);

        check(analysis.getIssues().isEmpty(),
                "mortgage remains searchable without a forced scenario classification");
        check(outcome.getCandidateCount() > 0,
                "mortgage reaches the corpus search and returns candidates");
    }

    private static void testShortLegalTermDoesNotRequireScenario() {
        QueryProcessor processor = new QueryProcessor();
        QueryProcessor.QueryAnalysis analysis = processor.analyze("ragging");
        check(analysis.getType() == QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                "short legal term is recognised as a search term rather than a scenario request");
    }

    private static void check(boolean condition, String message) {
        if (condition) {
            System.out.println("PASS | " + message);
        } else {
            failures++;
            System.out.println("FAIL | " + message);
        }
    }
}

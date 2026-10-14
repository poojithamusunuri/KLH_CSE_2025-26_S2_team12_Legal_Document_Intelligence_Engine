package tests;

import analysis.LegalProblemAnalyzer;
import analysis.ResearchOptimizer;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.QueryProcessor;
import search.SearchEngine;
import structures.SimpleList;

public class RelevanceFocusedRegressionTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        SimpleList<LegalDocument> corpus = new LegalCorpusLoader("data/corpus").loadDocuments();
        SearchEngine engine = new SearchEngine(corpus);
        testWarrantRetrievalAndNegation(engine);
        testMissingNoticeCondition(engine);
        testLegalTermSearch(engine);
        testWeakTierCannotClaimCoverage(engine, corpus);
        testQueryRoutingLabels();
        System.out.println("RELEVANCE FOCUSED REGRESSION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Relevance-focused regression failed.");
    }

    private static void testWarrantRetrievalAndNegation(SearchEngine engine) {
        String query = "Police searched my house without showing me a warrant.";
        LegalProblemAnalyzer analyzer = new LegalProblemAnalyzer();
        LegalProblemAnalyzer.AnalysisResult analysis = analyzer.analyze(query);
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                query, SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);

        check(!outcome.getResults().isEmpty(), "warrant situation returns corpus candidates");
        check("JUDG047".equals(outcome.getResults().get(0).getDocument().getDocumentId()),
                "warrant/criminal-procedure judgment outranks unrelated property and copyright Acts");
        check(!outcome.hasDirectMatch(),
                "background warrant discussion is not promoted to a direct answer to the specific alleged search");
        check(analysis.getSituation().getActors().contains("police"),
                "warrant analysis extracts the police actor");
        check(analysis.getSituation().getActions().contains("searched"),
                "warrant analysis extracts the search event");
        boolean negativeConditionPreserved = false;
        for (int i = 0; i < analysis.getSituation().getEvidenceIndicators().size(); i++) {
            if (analysis.getSituation().getEvidenceIndicators().get(i).toLowerCase().contains("not shown")) {
                negativeConditionPreserved = true;
            }
        }
        check(negativeConditionPreserved,
                "warrant analysis preserves the user's negative condition");

        boolean unrelatedActInTopThree = false;
        for (int i = 0; i < Math.min(3, outcome.getResults().size()); i++) {
            String id = outcome.getResults().get(i).getDocument().getDocumentId();
            if ("ACT026".equals(id) || "COREACT001".equals(id) || "COREACT010".equals(id)) {
                unrelatedActInTopThree = true;
            }
        }
        check(!unrelatedActInTopThree,
                "known unrelated tenancy, BNS summary, and copyright documents do not occupy warrant top three");
    }

    private static void testMissingNoticeCondition(SearchEngine engine) {
        String query = "my property was seized without any information or prior notice";
        LegalProblemAnalyzer.AnalysisResult analysis = new LegalProblemAnalyzer().analyze(query);
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                query, SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);
        check(!outcome.hasDirectMatch(),
                "generic seizure/property mentions cannot be DIRECT without evidence for the stated prior-notice condition");
        boolean onlyBackground = true;
        for (int i = 0; i < outcome.getResults().size(); i++) {
            if (outcome.getResults().get(i).getMatchType() == SearchEngine.MatchType.DIRECT_MATCH) {
                onlyBackground = false;
            }
        }
        check(onlyBackground,
                "unsupported negative conditions keep all candidates in related/background status");
    }

    private static void testLegalTermSearch(SearchEngine engine) {
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                "search warrant", SearchEngine.Algorithm.KMP, 10,
                new LegalProblemAnalyzer().analyze("search warrant"));
        check(!outcome.getResults().isEmpty(), "legal-term-only query remains searchable");
        check("JUDG047".equals(outcome.getResults().get(0).getDocument().getDocumentId()),
                "search-warrant term query returns the corpus judgment discussing warrants first");
    }

    private static void testWeakTierCannotClaimCoverage(SearchEngine engine, SimpleList<LegalDocument> corpus) {
        String query = "Police searched my house without showing me a warrant.";
        LegalProblemAnalyzer.AnalysisResult analysis = new LegalProblemAnalyzer().analyze(query);
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                query, SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);
        SimpleList<LegalDocument> candidates = new SimpleList<>();
        int[] tiers = new int[outcome.getResults().size()];
        for (int i = 0; i < outcome.getResults().size(); i++) {
            candidates.add(outcome.getResults().get(i).getDocument());
            tiers[i] = outcome.getResults().get(i).getMatchType() == SearchEngine.MatchType.DIRECT_MATCH ? 2
                    : outcome.getResults().get(i).getMatchType() == SearchEngine.MatchType.RELATED_MATCH ? 1 : 0;
        }
        ResearchOptimizer.OptimizationResult optimized = new ResearchOptimizer().optimize(analysis, candidates, 3, tiers);
        check(optimized.getCoveredQuestions().isEmpty(),
                "related/weak corpus matches do not count as research-question coverage");
        check(optimized.getSelectedDocuments().size() <= 3,
                "warrant research pack respects the requested document budget");
    }

    private static void testQueryRoutingLabels() {
        QueryProcessor processor = new QueryProcessor();
        QueryProcessor.QueryAnalysis analysis = processor.analyze("search warrant");
        check(analysis.getType() == QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                "search warrant routes as a legal-term query rather than requiring a personal scenario");
    }

    private static void check(boolean condition, String message) {
        if (condition) System.out.println("PASS | " + message);
        else {
            failures++;
            System.out.println("FAIL | " + message);
        }
    }
}

package tests;

import analysis.LegalProblemAnalyzer;
import analysis.ResearchOptimizer;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

public class OptimizerValidationTest {

    private static int failures = 0;

    public static void main(String[] args) {
        validateBudgetEnforcement();
        validateTierWeightInfluence();
        validateZeroAndNegativeBudgets();
        validateTierWeightLength();
        validateDuplicateDocumentIds();
        validateDeterministicTieBreak();
        validateSearchEngineIntegration();

        System.out.println("OPTIMIZER VALIDATION SUMMARY | failures=" + failures);
        if (failures > 0) {
            throw new AssertionError("Optimizer validation failed.");
        }
    }

    private static void validateBudgetEnforcement() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                questions("salary wages");

        SimpleList<LegalDocument> docs = new SimpleList<>();

        docs.add(new LegalDocument(
                "D1",
                "Employer salary non-payment dispute",
                "Judgment",
                "Employment",
                "",
                "India",
                "",
                "",
                "The employer has not paid the employee salary for three months "
                        + "and the wages remain unpaid."
        ));

        docs.add(new LegalDocument(
                "D2",
                "General employment records overview",
                "Judgment",
                "Employment",
                "",
                "India",
                "",
                "",
                "The salary records and employment records show the employer "
                        + "relationship and pay details."
        ));

        docs.add(new LegalDocument(
                "D3",
                "General title",
                "Judgment",
                "Other",
                "",
                "India",
                "",
                "",
                "This document only mentions salary once in passing and is not "
                        + "the actual event."
        ));

        ResearchOptimizer optimizer = new ResearchOptimizer();

        ResearchOptimizer.OptimizationResult result =
                optimizer.optimize(analysis, docs, 1, new int[] {2, 1, 0});

        check(
                result.getSelectedDocuments().size() <= 1,
                "Budget limit must be enforced even when more documents are candidates."
        );

        check(
                result.getSelectedDocuments().size() == 1,
                "A single-document budget should return one selected document."
        );

        check(
                "D1".equals(
                        result.getSelectedDocuments().get(0).getDocumentId()),
                "The highest-value direct document should win under a single-document budget."
        );
    }

    private static void validateTierWeightInfluence() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                questions("salary wages");

        SimpleList<LegalDocument> nearMatchDocs = new SimpleList<>();

        nearMatchDocs.add(new LegalDocument(
                "B",
                "General employment records overview",
                "Judgment",
                "Employment",
                "",
                "India",
                "",
                "",
                "The salary records and employment records show the employer "
                        + "relationship and pay details."
        ));

        nearMatchDocs.add(new LegalDocument(
                "A",
                "Employer salary non-payment dispute",
                "Judgment",
                "Employment",
                "",
                "India",
                "",
                "",
                "The employer has not paid the employee salary for three months "
                        + "and the wages remain unpaid."
        ));

        ResearchOptimizer optimizer = new ResearchOptimizer();

        ResearchOptimizer.OptimizationResult weakerPriority =
                optimizer.optimize(
                        analysis,
                        nearMatchDocs,
                        1,
                        new int[] {0, 2}
                );

        ResearchOptimizer.OptimizationResult strongerPriority =
                optimizer.optimize(
                        analysis,
                        nearMatchDocs,
                        1,
                        new int[] {2, 0}
                );

        check(
                "A".equals(
                        weakerPriority.getSelectedDocuments()
                                .get(0).getDocumentId()),
                "A different tier bonus distribution should change the selected document when the candidate set is near-tied."
        );

        check(
                "B".equals(
                        strongerPriority.getSelectedDocuments()
                                .get(0).getDocumentId()),
                "The greedy optimizer should also change the winner when the component weights are reversed."
        );
    }

    private static void validateZeroAndNegativeBudgets() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(
                        "My employer has not paid my salary.");

        SimpleList<LegalDocument> docs = new SimpleList<>();
        docs.add(document(
                "D1",
                "Unpaid salary dispute",
                "The employer has not paid salary wages."
        ));

        ResearchOptimizer optimizer = new ResearchOptimizer();

        check(
                optimizer.optimize(analysis, docs, 0)
                        .getSelectedDocuments().isEmpty(),
                "A zero budget selects no documents."
        );

        check(
                optimizer.optimize(analysis, docs, -1)
                        .getSelectedDocuments().isEmpty(),
                "A negative budget selects no documents."
        );
    }

    private static void validateTierWeightLength() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(
                        "My employer has not paid my salary.");

        SimpleList<LegalDocument> docs = new SimpleList<>();
        docs.add(document(
                "D1",
                "Unpaid salary dispute",
                "salary employment"
        ));
        docs.add(document(
                "D2",
                "Employment records",
                "salary employment records"
        ));

        check(
                throwsIllegalArgument(() ->
                        new ResearchOptimizer().optimize(
                                analysis,
                                docs,
                                1,
                                new int[] {2}
                        )),
                "A short tier-weight array is rejected instead of silently skipping weights."
        );

        check(
                throwsIllegalArgument(() ->
                        new ResearchOptimizer().optimize(
                                analysis,
                                docs,
                                1,
                                new int[] {2, 1, 0}
                        )),
                "An oversized tier-weight array is rejected."
        );
    }

    private static void validateDuplicateDocumentIds() {
        SimpleList<String> questions = new SimpleList<>();
        questions.add("salary employment");
        questions.add("contract agreement");

        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer.AnalysisResult(
                        "controlled duplicate test",
                        new SimpleList<String>(),
                        questions,
                        new SimpleList<String>()
                );

        SimpleList<LegalDocument> docs = new SimpleList<>();
        docs.add(document(
                "DUP",
                "Salary employment record",
                "salary employment"
        ));
        docs.add(document(
                "DUP",
                "Contract agreement record",
                "contract agreement"
        ));

        ResearchOptimizer.OptimizationResult result =
                new ResearchOptimizer().optimize(analysis, docs, 2);

        check(
                result.getSelectedDocuments().size() == 1,
                "Duplicate document IDs cannot consume multiple budget slots."
        );

        check(
                result.getUncoveredQuestions().size() == 1,
                "A duplicate ID is not counted as a second independent source of coverage."
        );
    }

    private static void validateDeterministicTieBreak() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                questions("salary wages");

        SimpleList<LegalDocument> docs = new SimpleList<>();

        docs.add(document(
                "A",
                "General employment",
                "The employee salary wages remain unpaid."
        ));

        docs.add(document(
                "B",
                "General employment",
                "The employee salary wages remain unpaid."
        ));

        ResearchOptimizer.OptimizationResult result =
                new ResearchOptimizer().optimize(
                        analysis,
                        docs,
                        1,
                        new int[] {0, 0}
                );

        check(
                result.getSelectedDocuments().size() == 1
                        && "A".equals(
                                result.getSelectedDocuments()
                                        .get(0).getDocumentId()),
                "An equal-score tie is resolved deterministically by candidate order."
        );
    }

    private static void validateSearchEngineIntegration() {
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(
                        "My employer has not paid my salary.");

        SimpleList<LegalDocument> docs = new SimpleList<>();

        docs.add(new LegalDocument(
                "DIRECT",
                "Unpaid salary dispute",
                "Judgment",
                "Labour and Employment Law",
                "",
                "India",
                "",
                "",
                "The employer has not paid the employee salary; wages remain unpaid."
        ));

        docs.add(new LegalDocument(
                "RELATED",
                "Salary employment records",
                "Judgment",
                "Labour and Employment Law",
                "",
                "India",
                "",
                "",
                "The employee salary records describe the employer and monthly wages."
        ));

        SearchEngine engine = new SearchEngine(docs);

        SearchEngine.SearchRankedOutcome outcome =
                engine.searchRankedWithMetadata(
                        analysis.getOriginalProblem(),
                        SearchEngine.Algorithm.Z_ALGORITHM,
                        10,
                        analysis
                );

        SimpleList<LegalDocument> candidates = new SimpleList<>();
        int[] weights = new int[outcome.getResults().size()];

        boolean foundDirect = false;
        boolean foundRelated = false;

        for (int i = 0; i < outcome.getResults().size(); i++) {
            SearchEngine.SearchResult searchResult =
                    outcome.getResults().get(i);

            candidates.add(searchResult.getDocument());

            if (searchResult.getMatchType()
                    == SearchEngine.MatchType.DIRECT_MATCH) {
                weights[i] = 2;
                foundDirect = true;
            } else if (searchResult.getMatchType()
                    == SearchEngine.MatchType.RELATED_MATCH) {
                weights[i] = 1;
                foundRelated = true;
            }
        }

        ResearchOptimizer.OptimizationResult optimized =
                new ResearchOptimizer().optimize(
                        analysis,
                        candidates,
                        1,
                        weights
                );

        boolean aligned =
                candidates.size() == outcome.getResults().size()
                        && weights.length == candidates.size();

        for (int i = 0; i < candidates.size(); i++) {
            SearchEngine.MatchType type =
                    outcome.getResults().get(i).getMatchType();

            int expectedWeight =
                    type == SearchEngine.MatchType.DIRECT_MATCH ? 2
                            : type == SearchEngine.MatchType.RELATED_MATCH ? 1
                            : 0;

            aligned &= candidates.get(i).getDocumentId().equals(
                            outcome.getResults().get(i)
                                    .getDocument().getDocumentId())
                    && weights[i] == expectedWeight;
        }

        check(
                foundDirect && foundRelated,
                "Integration fixture contains both direct and related search results."
        );

        check(
                aligned,
                "SearchEngine tiers remain positionally aligned with optimizer weights."
        );

        check(
                optimized.getSelectedDocuments().size() <= 1
                        && (optimized.getSelectedDocuments().isEmpty()
                                || containsDocument(
                                        candidates,
                                        optimized.getSelectedDocuments()
                                                .get(0).getDocumentId())),
                "The integrated optimizer respects its budget and selects only retrieved candidates."
        );
    }

    private static LegalProblemAnalyzer.AnalysisResult questions(
            String... values) {

        SimpleList<String> questions = new SimpleList<>();

        for (int i = 0; i < values.length; i++) {
            questions.add(values[i]);
        }

        return new LegalProblemAnalyzer.AnalysisResult(
                "controlled optimizer test",
                new SimpleList<String>(),
                questions,
                new SimpleList<String>()
        );
    }

    private static LegalDocument document(
            String id,
            String title,
            String content) {

        return new LegalDocument(
                id,
                title,
                "Judgment",
                "Employment",
                "",
                "India",
                "",
                "",
                content
        );
    }

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    private static boolean containsDocument(
            SimpleList<LegalDocument> documents,
            String documentId) {

        for (int i = 0; i < documents.size(); i++) {
            if (documentId.equals(
                    documents.get(i).getDocumentId())) {
                return true;
            }
        }

        return false;
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
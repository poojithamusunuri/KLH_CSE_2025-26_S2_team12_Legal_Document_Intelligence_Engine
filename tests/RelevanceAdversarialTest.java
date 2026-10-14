package tests;

import analysis.LegalProblemAnalyzer;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

/**
 * Adversarial checks for lexical-overlap false positives and tier semantics.
 * These tests validate ranking labels, not legal correctness.
 */
public class RelevanceAdversarialTest {
    private static int failures;
    private static int tp;
    private static int fp;
    private static int fn;
    private static int tn;

    public static void main(String[] args) throws Exception {
        testSalaryTaxFalsePositive();
        testSalaryTaxPairedRegression();
        testDirectRelatedWeakTiers();
        testLowConfidenceDoesNotProduceDirectMatches();
        testContractEventRequiresCompatibleDomain();
        testPhraseOverlapDoesNotCreateDirectMatch();
        double precision = (tp + fp) == 0 ? 0.0 : (double) tp / (tp + fp);
        double recall = (tp + fn) == 0 ? 0.0 : (double) tp / (tp + fn);
        System.out.printf("DIRECT-TIER BENCHMARK | TP=%d FP=%d FN=%d TN=%d | precision=%.3f recall=%.3f%n",
                tp, fp, fn, tn, precision, recall);
        System.out.println("RELEVANCE ADVERSARIAL SUMMARY | failures=" + failures);
        if (failures > 0) {
            throw new AssertionError("Relevance adversarial checks failed.");
        }
    }

    private static void testSalaryTaxFalsePositive() throws Exception {
        LegalCorpusLoader loader = new LegalCorpusLoader("data/corpus");
        SearchEngine engine = new SearchEngine(loader.loadDocuments());
        String query = "My employer has not paid my salary for three months.";
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(query);
        SearchEngine.SearchRankedOutcome outcome =
                engine.searchRankedWithMetadata(
                        query, SearchEngine.Algorithm.Z_ALGORITHM, 300, analysis);

        boolean taxJudgmentFound = false;
        boolean taxJudgmentDirect = false;
        for (int i = 0; i < outcome.getResults().size(); i++) {
            SearchEngine.SearchResult result = outcome.getResults().get(i);
            if ("JUDG108".equals(result.getDocument().getDocumentId())) {
                taxJudgmentDirect =
                        result.getMatchType() == SearchEngine.MatchType.DIRECT_MATCH;
                recordDirectPrediction(false, taxJudgmentDirect);
                break;
            }
        }
        check(!taxJudgmentDirect,
                "JUDG108 (tax-related passage) must not be labelled a direct salary match");
    }

    private static void testSalaryTaxPairedRegression() throws Exception {
        SimpleList<LegalDocument> docs = new SimpleList<LegalDocument>();
        docs.add(new LegalDocument(
                "REG-SALARY",
                "Unpaid salary dispute",
                "Judgment",
                "Labour and Employment Law",
                "", "India", "", "", "", "", "",
                "The employer has not paid the employee salary for three months."));
        docs.add(new LegalDocument(
                "REG-TAX",
                "Tax payment dispute mentioning salary",
                "Judgment",
                "Labour and Employment Law",
                "", "India", "", "", "", "", "",
                "The employer has not paid tax. Salary figures are discussed separately in the record."));

        SearchEngine engine = new SearchEngine(docs);
        String query = "My employer has not paid my salary for three months.";
        LegalProblemAnalyzer.AnalysisResult analysis =
                new LegalProblemAnalyzer().analyze(query);

        SearchEngine.SearchRankedOutcome outcome =
                engine.searchRankedWithMetadata(
                        query, SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);

        SearchEngine.MatchType salary = findType(outcome, "REG-SALARY");
        SearchEngine.MatchType tax = findType(outcome, "REG-TAX");

        recordDirectPrediction(true, salary == SearchEngine.MatchType.DIRECT_MATCH);
        recordDirectPrediction(false, tax == SearchEngine.MatchType.DIRECT_MATCH);

        check(salary == SearchEngine.MatchType.DIRECT_MATCH,
                "nearest target object evidence makes a genuine salary event DIRECT");
        check(tax != SearchEngine.MatchType.DIRECT_MATCH,
                "a closer tax object prevents a distant salary mention from becoming DIRECT");
    }

    private static void testDirectRelatedWeakTiers() {
        SimpleList<LegalDocument> docs = new SimpleList<LegalDocument>();
        docs.add(new LegalDocument("D1", "Unpaid salary dispute", "Judgment",
                "Labour and Employment Law", "", "India", "", "", "",
                "", "", "The employer has not paid the employee salary. The wages remain unpaid."));
        docs.add(new LegalDocument("D2", "Salary and employment records", "Judgment",
                "Labour and Employment Law", "", "India", "", "", "",
                "", "", "The employee's salary records describe the employer and monthly wages."));
        docs.add(new LegalDocument("D3", "General legal reference", "Judgment",
                "Other", "", "India", "", "", "",
                "", "", "The word salary appears in this general discussion."));

        LegalProblemAnalyzer.AnalysisResult analysis = new LegalProblemAnalyzer().analyze(
                "My employer has not paid my salary.");
        SearchEngine engine = new SearchEngine(docs);
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                "My employer has not paid my salary.", SearchEngine.Algorithm.Z_ALGORITHM, 10, analysis);

        SearchEngine.MatchType direct = findType(outcome, "D1");
        SearchEngine.MatchType unrelated = findType(outcome, "D2");
        recordDirectPrediction(true, direct == SearchEngine.MatchType.DIRECT_MATCH);
        recordDirectPrediction(false, unrelated == SearchEngine.MatchType.DIRECT_MATCH);
        check(direct == SearchEngine.MatchType.DIRECT_MATCH,
                "clear evidence for the stated negative condition should be DIRECT");
        check(unrelated != SearchEngine.MatchType.DIRECT_MATCH,
                "a topical document without evidence for the explicit negative condition is never DIRECT");

        LegalProblemAnalyzer.AnalysisResult topicAnalysis = new LegalProblemAnalyzer().analyze("salary records employee company deadline");
        SearchEngine.SearchRankedOutcome topicOutcome = engine.searchRankedWithMetadata(
                "salary records employee company deadline", SearchEngine.Algorithm.Z_ALGORITHM, 10, topicAnalysis);
        SearchEngine.MatchType related = findType(topicOutcome, "D2");
        SearchEngine.MatchType weak = findType(topicOutcome, "D3");
        check(related == SearchEngine.MatchType.RELATED_MATCH,
                "topic overlap without the described event is RELATED");
        check(weak != SearchEngine.MatchType.DIRECT_MATCH,
                "limited lexical overlap is never promoted to DIRECT");
    }

        private static void testLowConfidenceDoesNotProduceDirectMatches() {
                assertNoDirectMatch(
                                "I paid my friend back for a personal loan.",
                                "a completed repayment should not be labelled a direct legal dispute");
                assertNoDirectMatch(
                                "There is an issue with my agreement.",
                                "a low-confidence, fact-free agreement query should not be DIRECT");
        }

        private static void testContractEventRequiresCompatibleDomain() {
                assertNoDirectMatch(
                                "We had a verbal deal and now they are not honoring it.",
                                "contract-event overlap in documents from incompatible domains should not be DIRECT");
        }

        private static void testPhraseOverlapDoesNotCreateDirectMatch() {
                assertNoDirectMatch(
                                "electricity consumer security deposit",
                                "an exact body phrase without a described event should not make unrelated documents DIRECT");
        }

        private static void assertNoDirectMatch(String query, String message) {
                try {
                        LegalCorpusLoader loader = new LegalCorpusLoader("data/corpus");
                        LegalProblemAnalyzer.AnalysisResult analysis =
                                        new LegalProblemAnalyzer().analyze(query);
                        SearchEngine engine = new SearchEngine(loader.loadDocuments());
                        SearchEngine.SearchRankedOutcome outcome =
                                        engine.searchRankedWithMetadata(
                                                        query, SearchEngine.Algorithm.Z_ALGORITHM, 5, analysis);

                        boolean foundDirect = false;
                        for (int i = 0; i < outcome.getResults().size(); i++) {
                                if (outcome.getResults().get(i).getMatchType()
                                                == SearchEngine.MatchType.DIRECT_MATCH) {
                                        foundDirect = true;
                                        break;
                                }
                        }

                        check(!foundDirect, message);
                } catch (Exception exception) {
                        failures++;
                        System.out.println("FAIL | " + message + " | " + exception.getMessage());
                }
        }

    private static void recordDirectPrediction(boolean expectedDirect, boolean predictedDirect) {
        if (expectedDirect && predictedDirect) tp++;
        else if (!expectedDirect && predictedDirect) fp++;
        else if (expectedDirect) fn++;
        else tn++;
    }

    private static SearchEngine.MatchType findType(
            SearchEngine.SearchRankedOutcome outcome, String id) {
        for (int i = 0; i < outcome.getResults().size(); i++) {
            SearchEngine.SearchResult result = outcome.getResults().get(i);
            if (id.equals(result.getDocument().getDocumentId())) {
                return result.getMatchType();
            }
        }
        return null;
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

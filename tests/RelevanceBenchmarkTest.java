package tests;

import analysis.LegalProblemAnalyzer;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

public class RelevanceBenchmarkTest {

    private static final String DIRECT = "DIRECT";
    private static final String RELATED = "RELATED";
    private static final String WEAK = "WEAK";
        private static final String NO_DIRECT = "NO_DIRECT";
    private static final String NO_MATCH = "NO_MATCH";

    private static int failures = 0;
    private static int retrievedCount = 0;
    private static int directTp = 0;
    private static int directFp = 0;
    private static int directFn = 0;
    private static int directTn = 0;
    private static double reciprocalRankSum = 0.0;
    private static int targetCaseCount = 0;
    private static int targetHitAt3 = 0;
    private static int targetHitAt10 = 0;

    private static final class BenchmarkCase {
        final String query;
        final String expectedDocumentId;
        final String expectedLabel;
        final String evidence;
        final String reason;

        BenchmarkCase(
                String query,
                String expectedDocumentId,
                String expectedLabel,
                String evidence,
                String reason) {
            this.query = query;
            this.expectedDocumentId = expectedDocumentId;
            this.expectedLabel = expectedLabel;
            this.evidence = evidence;
            this.reason = reason;
        }
    }

    public static void main(String[] args) throws Exception {
                validateNoMatchRejectsReturnedCandidates();

        BenchmarkCase[] cases = new BenchmarkCase[] {
                new BenchmarkCase(
                        "consumer protection",
                        "COREACT006",
                        RELATED,
                        "Curated Consumer Protection Act, 2019 summary in Consumer Law domain.",
                        "Exact legal-term statutory-background retrieval."),
                new BenchmarkCase(
                        "electricity consumer security deposit",
                        "JUDG076",
                        RELATED,
                        "JUDG076 discusses the electricity board's required consumer deposit of estimated three months' consumption charges.",
                        "Related deposit dispute; the query is a short topic phrase without a specific reported event."),
                new BenchmarkCase(
                        "specific performance of land sale agreement",
                        "JUDG002",
                        RELATED,
                        "JUDG002 concerns appeals in suits involving agreements for sale and specific performance.",
                        "Direct contract-for-sale litigation, not a generic breach exemplar."),
                new BenchmarkCase(
                        "bank account without permission",
                        "",
                        NO_DIRECT,
                        "No reviewed corpus judgment in this benchmark establishes unauthorized bank-account use.",
                        "No direct-match claim; returned related candidates are reported separately."),
                new BenchmarkCase(
                        "tax deduction",
                        "JUDG019",
                        RELATED,
                        "JUDG019 addresses the obligation to deduct tax at source under section 195.",
                        "Direct match to tax deduction at source."),
                new BenchmarkCase(
                        "dishonoured cheque",
                        "JUDG087",
                        RELATED,
                        "JUDG087 is a complaint under section 138 of the Negotiable Instruments Act involving dishonoured cheques.",
                        "Relevant cheque-dishonour judgment; standalone query has no factual context and is conservatively RELATED."),
                new BenchmarkCase(
                        "defective product refund",
                        "COREACT006",
                        RELATED,
                        "COREACT006 is a curated consumer-law summary mentioning defective goods and consumer remedies; it is not a product-specific judgment.",
                        "Related statutory background, not a direct factual incident."),
                new BenchmarkCase(
                        "vehicle accident injury",
                        "JUDG017",
                        DIRECT,
                        "JUDG017 concerns a bus accident, death, dependency compensation, and motor-vehicle liability.",
                        "Direct motor-vehicle accident judgment."),
                new BenchmarkCase(
                        "copyright infringement photo",
                        "COREACT010",
                        RELATED,
                        "COREACT010 is a curated Copyright Act summary; it does not establish an online defamation/photo incident.",
                        "Related copyright-law background, not a direct factual incident."),
                new BenchmarkCase(
                        "agreement between parties",
                        "COREACT007",
                        RELATED,
                        "COREACT007 is a curated Indian Contract Act summary, not evidence of a specific agreement dispute.",
                        "Related contract-law background."),
                new BenchmarkCase(
                        "my landlord refuses to return my security deposit",
                        "",
                        NO_DIRECT,
                        "JUDG007 concerns registration and specific performance of a land-sale agreement, not a residential landlord deposit.",
                        "No reviewed direct residential-deposit judgment is established by the returned candidates."),
                new BenchmarkCase(
                        "someone used my bank account without permission",
                        "",
                        NO_DIRECT,
                        "No reviewed corpus judgment in this benchmark establishes unauthorized bank-account use.",
                        "Paraphrase robustness check for absence of a direct corpus match."),
                new BenchmarkCase(
                        "My employer has not paid my salary for three months.",
                        "JUDG051",
                        RELATED,
                        "The document discusses employment and non-payment, but not the same event as the query.",
                        "Hard salary-versus-tax case: real employment non-payment, but legally different than a tax dispute."),
                new BenchmarkCase(
                        "my boss has not paid my wages",
                        "JUDG051",
                        RELATED,
                        "The judgment addresses employment and wage-related consequences without the specific query event.",
                        "Related employment background rather than a direct match."),
                new BenchmarkCase(
                        "unpaid wages",
                        "",
                        NO_DIRECT,
                        "The inspected top results are not established as wage-nonpayment judgments; JUDG051 concerns labor-court procedure and termination.",
                        "No direct wage-nonpayment match; related candidates must not be treated as a successful no-result case."),
                new BenchmarkCase(
                        "copyright infringement",
                        "COREACT010",
                        RELATED,
                        "COREACT010 is the Copyright Act summary and therefore legal background, not a specific infringement judgment.",
                        "Related legal background."),
                new BenchmarkCase(
                        "property ownership dispute",
                        "ACT026",
                        RELATED,
                        "ACT026 is the Ajmer Tenancy and Land Records Act, covering tenancy, land records, and possession remedies; it is not an individual ownership dispute.",
                        "Related statutory background."),
                new BenchmarkCase(
                        "inheritance property dispute",
                        "ACT026",
                        RELATED,
                        "ACT026 concerns Ajmer tenancy and land records and does not specifically address inheritance.",
                        "Related property/land background, not direct inheritance law."),
                new BenchmarkCase(
                        "CONSUMER PROTECTION",
                        "COREACT006",
                        RELATED,
                        "Consumer Protection Act, 2019 curated summary.",
                        "Capitalization-only variation; legal background is RELATED in situation-ranking mode."),
                new BenchmarkCase(
                        "pay",
                        "",
                        NO_MATCH,
                        "The production relevance search returns no candidates for this generic term.",
                        "Strict no-match case: requires zero candidates and zero results.")
        };

        LegalCorpusLoader loader = new LegalCorpusLoader("data/corpus");
        SimpleList<LegalDocument> documents = loader.loadDocuments();
        SearchEngine engine = new SearchEngine(documents);

        System.out.println("BENCHMARK | cases=" + cases.length);

        for (int i = 0; i < cases.length; i++) {
            BenchmarkCase benchmarkCase = cases[i];
            LegalProblemAnalyzer.AnalysisResult analysis =
                    new LegalProblemAnalyzer().analyze(benchmarkCase.query);

            SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(
                    benchmarkCase.query,
                    SearchEngine.Algorithm.Z_ALGORITHM,
                    documents.size(),
                    analysis);

            String actualLabel = NO_MATCH;
            int rank = -1;
            SimpleList<SearchEngine.SearchResult> results = outcome.getResults();

                        if (NO_DIRECT.equals(benchmarkCase.expectedLabel)) {
                                actualLabel = outcome.hasDirectMatch() ? DIRECT : NO_DIRECT;
                        } else if (NO_MATCH.equals(benchmarkCase.expectedLabel)
                                        && outcome.getCandidateCount() > 0) {
                                actualLabel = "RESULTS_RETURNED";
                        }

            for (int j = 0; j < results.size(); j++) {
                SearchEngine.SearchResult result = results.get(j);
                if (benchmarkCase.expectedDocumentId != null
                        && !benchmarkCase.expectedDocumentId.isEmpty()
                        && benchmarkCase.expectedDocumentId.equals(result.getDocument().getDocumentId())) {
                    actualLabel = matchTypeToLabel(result.getMatchType());
                    rank = j + 1;
                    break;
                }
            }

            boolean retrieved = rank > 0;
            if (retrieved) {
                retrievedCount++;
                reciprocalRankSum += 1.0 / rank;
            }

                        boolean pass = expectedMatchesActual(
                                        benchmarkCase, actualLabel, retrieved, outcome);
            if (!pass) {
                failures++;
                System.out.println("FAIL | query=" + benchmarkCase.query
                        + " expected=" + benchmarkCase.expectedLabel
                        + " actual=" + actualLabel
                        + " retrieved=" + retrieved);
            }

                        if (benchmarkCase.expectedDocumentId != null
                                        && !benchmarkCase.expectedDocumentId.isEmpty()) {
                                targetCaseCount++;
                                if (rank > 0 && rank <= 3) targetHitAt3++;
                                if (rank > 0 && rank <= 10) targetHitAt10++;
                                if (benchmarkCase.expectedLabel.equals(DIRECT) && actualLabel.equals(DIRECT)) {
                                        directTp++;
                                } else if (benchmarkCase.expectedLabel.equals(DIRECT) && !actualLabel.equals(DIRECT)) {
                                        directFn++;
                                } else if (!benchmarkCase.expectedLabel.equals(DIRECT) && actualLabel.equals(DIRECT)) {
                                        directFp++;
                                } else if (!benchmarkCase.expectedLabel.equals(DIRECT) && !actualLabel.equals(DIRECT)) {
                                        directTn++;
                                }
            }

            System.out.println("CASE " + (i + 1) + " | query=" + benchmarkCase.query
                    + " | expected=" + benchmarkCase.expectedLabel
                    + " | actual=" + actualLabel
                    + " | targetRetrieved=" + retrieved
                    + " | rank=" + (rank > 0 ? rank : "-")
                    + " | candidates=" + outcome.getCandidateCount()
                    + " | top=" + summarizeResults(results, 5));
            System.out.println("  evidence=" + benchmarkCase.evidence);
            System.out.println("  reason=" + benchmarkCase.reason);
        }

        double directPrecision = (directTp + directFp) == 0 ? 0.0 : (double) directTp / (directTp + directFp);
        double directRecall = (directTp + directFn) == 0 ? 0.0 : (double) directTp / (directTp + directFn);
        double mrr = targetCaseCount == 0
                ? 0.0 : reciprocalRankSum / targetCaseCount;

        System.out.println();
        System.out.println("BENCHMARK SUMMARY | cases=" + cases.length
                + " targetCases=" + targetCaseCount
                + " retrieved=" + retrievedCount
                + " failures=" + failures
                + " directTP=" + directTp
                + " directFP=" + directFp
                + " directFN=" + directFn
                + " directTN=" + directTn
                + " precision=" + String.format("%.3f", directPrecision)
                + " recall=" + String.format("%.3f", directRecall)
                + " targetHit@3=" + targetHitAt3 + "/" + targetCaseCount
                + " targetRecall@10=" + targetHitAt10 + "/" + targetCaseCount
                + " mrr=" + String.format("%.3f", mrr));

        if (failures > 0) {
            throw new AssertionError("Relevance benchmark failed.");
        }
    }

        private static boolean expectedMatchesActual(
                        BenchmarkCase benchmarkCase,
                        String actual,
                        boolean retrieved,
                        SearchEngine.SearchRankedOutcome outcome) {
                if (NO_MATCH.equals(benchmarkCase.expectedLabel)) {
                        return isStrictNoMatch(outcome);
                }
                if (NO_DIRECT.equals(benchmarkCase.expectedLabel)) {
                        return !outcome.hasDirectMatch();
        }
                return benchmarkCase.expectedLabel.equals(actual);
    }

        private static void validateNoMatchRejectsReturnedCandidates() {
                SimpleList<SearchEngine.SearchResult> irrelevantResults = new SimpleList<>();
                irrelevantResults.add(new SearchEngine.SearchResult(
                                new LegalDocument(
                                                "IRRELEVANT", "Unrelated document", "Judgment", "Other",
                                                "", "India", "", "", "Unrelated content."),
                                0,
                                SearchEngine.Algorithm.Z_ALGORITHM,
                                SearchEngine.MatchType.RELATED_MATCH));
                SearchEngine.SearchRankedOutcome outcome = new SearchEngine.SearchRankedOutcome(
                                irrelevantResults, 1, 1, new SimpleList<String>(), false);

                boolean rejected = !isStrictNoMatch(outcome);
                check(rejected,
                                "Strict NO_MATCH rejects arbitrary returned candidates even when no expected ID is present.");
        }

        private static boolean isStrictNoMatch(
                        SearchEngine.SearchRankedOutcome outcome) {
                return outcome.getCandidateCount() == 0
                                && outcome.getResults().isEmpty();
        }

        private static void check(boolean condition, String message) {
                if (condition) {
                        System.out.println("PASS | " + message);
                } else {
                        failures++;
                        System.out.println("FAIL | " + message);
                }
        }

        private static String summarizeResults(
                        SimpleList<SearchEngine.SearchResult> results,
                        int limit) {
                if (results.isEmpty()) {
                        return "[]";
                }

                StringBuilder builder = new StringBuilder("[");
                int shown = Math.min(results.size(), limit);
                for (int i = 0; i < shown; i++) {
                        if (i > 0) {
                                builder.append(", ");
                        }
                        SearchEngine.SearchResult result = results.get(i);
                        builder.append(result.getDocument().getDocumentId())
                                        .append(":")
                                        .append(result.getMatchType());
                }
                if (results.size() > shown) {
                        builder.append(", ...");
                }
                builder.append("]");
                return builder.toString();
        }

    private static String matchTypeToLabel(SearchEngine.MatchType matchType) {
        if (matchType == SearchEngine.MatchType.DIRECT_MATCH) {
            return DIRECT;
        }
        if (matchType == SearchEngine.MatchType.RELATED_MATCH) {
            return RELATED;
        }
        if (matchType == SearchEngine.MatchType.WEAK_MATCH) {
            return WEAK;
        }
        return NO_MATCH;
    }
}

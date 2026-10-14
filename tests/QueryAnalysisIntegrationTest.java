package tests;

import analysis.LegalProblemAnalyzer;
import search.QueryProcessor;

public class QueryAnalysisIntegrationTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        verify("conspiracy", QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                "Conspiracy", false, false);
        verify("  contrct  ", QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "Contract", false, false);
        verify("dishonour", QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                "Dishonour", false, false);
        verify("dishonor", QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                "Dishonor", false, false);
        verify("agreement between parties", QueryProcessor.QueryType.TEXT_SEARCH,
                null, false, false);
        verify("a banking transaction was not completed",
                QueryProcessor.QueryType.TEXT_SEARCH, null, false, false);
        verify("xyzzy", QueryProcessor.QueryType.TEXT_SEARCH,
                null, false, false);

        System.out.println("SUMMARY | passed=" + passed + " failed=" + failed);

        if (failed > 0) {
            throw new AssertionError("Query-analysis integration test failed.");
        }
    }

    private static void verify(
            String originalQuery,
            QueryProcessor.QueryType expectedType,
            String expectedResolvedTerm,
            boolean expectsBankingIssue,
            boolean expectsContractIssue) {

        QueryProcessor processor = new QueryProcessor();
        QueryProcessor.QueryAnalysis queryAnalysis =
                processor.analyze(originalQuery);
        LegalProblemAnalyzer.AnalysisResult problemAnalysis =
                new LegalProblemAnalyzer().analyze(
                        queryAnalysis.getOriginalQuery()
                );

        boolean success = originalQuery.equals(queryAnalysis.getOriginalQuery())
                && originalQuery.trim().equals(queryAnalysis.getNormalizedQuery())
                && queryAnalysis.getType() == expectedType
                && resolvedTermMatches(queryAnalysis, expectedResolvedTerm)
                && problemAnalysis.getOriginalProblem().equals(originalQuery)
                && containsIssue(problemAnalysis, "Banking") == expectsBankingIssue
                && containsIssue(problemAnalysis, "Contract") == expectsContractIssue;

        if (success) {
            passed++;
            System.out.println("PASS | " + originalQuery);
        } else {
            failed++;
            System.out.println("FAIL | " + originalQuery
                    + " | type=" + queryAnalysis.getType()
                    + " | normalized=" + queryAnalysis.getNormalizedQuery()
                    + " | resolved=" + resolvedTerms(queryAnalysis)
                    + " | issues=" + problemAnalysis.getIssues().size());
        }
    }

    private static boolean resolvedTermMatches(
            QueryProcessor.QueryAnalysis analysis,
            String expectedResolvedTerm) {

        if (expectedResolvedTerm == null) {
            return analysis.getResolvedTerms().isEmpty();
        }

        return analysis.getResolvedTerms().size() == 1
                && expectedResolvedTerm.equals(
                        analysis.getResolvedTerms().get(0)
                );
    }

    private static boolean containsIssue(
            LegalProblemAnalyzer.AnalysisResult analysis,
            String fragment) {

        for (int i = 0; i < analysis.getIssues().size(); i++) {
            if (analysis.getIssues().get(i).contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static String resolvedTerms(
            QueryProcessor.QueryAnalysis analysis) {

        if (analysis.getResolvedTerms().isEmpty()) {
            return "[]";
        }

        StringBuilder values = new StringBuilder("[");
        for (int i = 0; i < analysis.getResolvedTerms().size(); i++) {
            if (i > 0) {
                values.append(", ");
            }
            values.append(analysis.getResolvedTerms().get(i));
        }
        values.append("]");
        return values.toString();
    }
}

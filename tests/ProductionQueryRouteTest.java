package tests;

import search.QueryProcessor;
import search.SearchEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Checks both the internal query-router contract and the compact, user-facing
 * production output. Algorithm names are verified through the router API, not
 * required to leak into the normal search interface.
 */
public class ProductionQueryRouteTest {

    private static int failures;

    public static void main(String[] args) throws Exception {
        verifyProductionRoute(
                "conspiraci",
                QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                SearchEngine.Algorithm.RABIN_KARP,
                "fuzzy legal term",
                "Spelling correction: Conspiracy",
                "Results: 3");
        verifyProductionRoute(
                "ragging",
                QueryProcessor.QueryType.EXACT_LEGAL_TERM,
                SearchEngine.Algorithm.KMP,
                "exact legal term",
                null,
                "No document in the loaded corpus matched the full query.");

        System.out.println("PRODUCTION QUERY ROUTE SUMMARY | failures=" + failures);
        if (failures > 0) {
            throw new AssertionError("Production query route test failed.");
        }
    }

    private static void verifyProductionRoute(
            String query,
            QueryProcessor.QueryType expectedType,
            SearchEngine.Algorithm expectedAlgorithm,
            String expectedUserType,
            String expectedAdditionalEvidence,
            String expectedResultEvidence) throws Exception {

        QueryProcessor.QueryAnalysis analysis = new QueryProcessor().analyze(query);
        check(analysis.getType() == expectedType
                        && SearchEngine.chooseAlgorithm(analysis.getType()) == expectedAlgorithm,
                query + " routes internally as " + expectedType + " via " + expectedAlgorithm);

        String java = System.getProperty("java.home") + "/bin/java";
        Process process = new ProcessBuilder(
                java, "-cp", System.getProperty("java.class.path"), "Main")
                .redirectErrorStream(true)
                .start();
        try (OutputStream input = process.getOutputStream()) {
            input.write((query + "\nexit\n").getBytes(StandardCharsets.UTF_8));
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            check(false, query + " production process exits within 30 seconds");
            return;
        }
        String text = new String(output.toByteArray(), StandardCharsets.UTF_8);
        check(text.contains("Search type: " + expectedUserType),
                query + " shows a clear user-facing search type");
        if (expectedAdditionalEvidence != null) {
            check(text.contains(expectedAdditionalEvidence),
                    query + " shows spelling correction when appropriate");
        }
        check(text.contains(expectedResultEvidence),
                query + " reports the actual corpus-search outcome");
        check(!text.contains("Query router :") && !text.contains("Main ranking :"),
                query + " keeps implementation diagnostics out of compact results");
        check(process.exitValue() == 0,
                query + " production application exits successfully");
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

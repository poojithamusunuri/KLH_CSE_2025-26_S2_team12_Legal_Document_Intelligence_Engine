package tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class CLIRegressionTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        String java = System.getProperty("java.home") + "/bin/java";
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "Main")
                .redirectErrorStream(true).start();
        String input = String.join("\n",
                "flow current", "murde", "flow current", "search warrant", "flow current", "details", "details",
                "compact", "schedule abc", "compare", "algorithms",
                "my property was seized without any information or prior notice", "flow current",
                "theft", "flow current", "identity theft", "flow current",
                "zxqv-unmatched-token-8841", "my wife hits me", "exit", "");
        try (OutputStream stream = process.getOutputStream()) {
            stream.write(input.getBytes(StandardCharsets.UTF_8));
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        if (!process.waitFor(40, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            check(false, "CLI process exits within 40 seconds");
            return;
        }
        String text = new String(output.toByteArray(), StandardCharsets.UTF_8);
        check(text.contains("No search results are active yet"), "flow current before search reports no active session");
        check(text.contains("CITATION LINKS FOR CURRENT RESULTS"), "flow current after search dispatches to citation-link analysis");
        check(text.contains("TERM MENTIONED IN TEXT"), "keyword mention is not mislabeled as direct legal relevance");
        check(text.contains("Excerpt    :") && text.contains("convicted of murder"), "fuzzy term result displays context showing why it appeared");
        check(text.contains("They may not relate to the search topic"), "citation links clearly warn that corpus links may be unrelated to the query");
        check(!text.contains("Query type: TEXT_SEARCH\n  Retrieval"), "flow current is not sent to ordinary query search");
        check(text.contains("Type / year: Judgment | 1965"), "compact results show the judgment year without internal identifiers");
        check(count(text, "Output mode changed to: DETAILED") >= 2,
                "repeating details keeps detailed mode enabled instead of toggling it off");
        check(count(text, "Output mode changed to: COMPACT") == 1,
                "compact mode changes only when the explicit compact command is used");
        check(text.contains("Usage: schedule | schedule H"), "invalid schedule argument receives command usage instead of a legal search");
        check(!text.contains("STANDALONE ALGORITHM DEMONSTRATIONS"), "standalone algorithm demonstrations are hidden from search interface");
        check(text.contains("DOCUMENT SIMILARITY") || text.contains("comparison needs at least two results"), "optional compare command is handled");
        check(text.contains("Yanala Malleshwari") || text.contains("Bandi Kotayya"), "corpus citation overview resolves document IDs to readable titles");
        check(text.contains("NO DIRECT MATCH ESTABLISHED; RELATED/BACKGROUND RESULTS ONLY"), "unsupported prior-notice fact pattern is clearly reported as background-only rather than a confirmed answer");
        check(text.contains("Interpret related documents as research leads, not answers to the described facts."), "background results are explicitly qualified for the user");
        check(text.contains("Bharatiya Nyaya Sanhita, 2023") && text.contains("Information Technology Act, 2000") && text.contains("Original baseline   : 242") && text.contains("Statutory excerpts  : 7"), "production search loads separate official statutory tracks without changing baseline count");
        check(text.contains("No document in the loaded corpus matched the full query."), "nonsensical multi-token query produces a clear no-match instead of unrelated weak leads");
        check(text.contains("possible domestic or physical abuse") && text.contains("Results are limited to evidence in the loaded corpus"),
                "recognized abuse situation is shown to the user with a cautious corpus-coverage explanation");
        check(process.exitValue() == 0, "CLI exits successfully");
        System.out.println("CLI REGRESSION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("CLI regression failed.");
    }

    private static int count(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }

    private static void check(boolean condition, String message) {
        if (condition) System.out.println("PASS | " + message);
        else {
            failures++;
            System.out.println("FAIL | " + message);
        }
    }
}

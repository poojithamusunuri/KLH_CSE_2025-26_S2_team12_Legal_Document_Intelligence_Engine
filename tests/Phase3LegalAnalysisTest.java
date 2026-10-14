package tests;

import analysis.LegalProblemAnalyzer;
import structures.SimpleList;

public class Phase3LegalAnalysisTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        verifyPositive("My employer has not paid my salary for three months.",
                "Employment-related dispute");
        verifyPositive("My boss has not paid my wages.",
                "Employment-related dispute");
        verifyPositive("I was discharged from work without notice.",
                "Employment-related dispute");
        verifyPositive("I am not being paid for the work I did.",
                "Employment-related dispute");
        verifyPositive("We had a verbal deal and now they are not honoring it.",
                "Contract-related issue");
        verifyPositive("I signed an agreement but the other party refused to perform its obligations.",
                "Contract-related issue");
        verifyPositive("Someone used my bank account without permission.",
                "Banking / unauthorized transaction matter");
        verifyPositive("The owner refuses to return my rental deposit.",
                "Tenancy-related matter");
        verifyPositive("The product I bought is defective and the seller refuses a refund.",
                "Consumer / goods or services dispute");
        verifyPositive("Someone copied my photo and used it on a website without permission.",
                "Intellectual property matter");
        verifyPositive("A person copied my logo and is using it commercially.",
                "Intellectual property matter");
        verifyPositive("A driver hit my car and caused injuries.",
                "Motor vehicle / accident-related matter");
        verifyPositive("My spouse and I are disputing the inheritance of our parents' property.",
                "Family / inheritance matter");
        verifyPositive("conspiracy", "Criminal / conspiracy-related issue");
        verifyPositive("conspiraci", "Criminal / conspiracy-related issue");
        verifyPositive("ragging", "Education / student issue");

        verifyNegative("I paid my friend back for a personal loan.");
        verifyNegative("Our team completed the project.");
        verifyNegative("The agreement was discussed during a family meeting.");
        verifyNegative("I work on a vehicle project.");
        verifyNegative("The person paid the amount voluntarily.");
        verifyNegative("I have a problem with money.");
        verifyNegative("There is an issue with my agreement.");
        verifyNegative("Someone is troubling me.");

        System.out.println("SUMMARY | passed=" + passed + " failed=" + failed);

        if (failed > 0) {
            throw new AssertionError("Phase 3 legal-analysis test failed.");
        }
    }

    private static void verifyPositive(String query, String expectedIssue) {
        LegalProblemAnalyzer.AnalysisResult result =
                new LegalProblemAnalyzer().analyze(query);
        boolean success = contains(result.getIssues(), expectedIssue)
                && !result.getMissingInformation().isEmpty()
                && !"LOW".equals(result.getSituation().getConfidence());
        report(success, "POSITIVE", query, result);
    }

    private static void verifyNegative(String query) {
        LegalProblemAnalyzer.AnalysisResult result =
                new LegalProblemAnalyzer().analyze(query);
        boolean success = result.getIssues().isEmpty()
                && "LOW".equals(result.getSituation().getConfidence());
        report(success, "NEGATIVE", query, result);
    }

    private static boolean contains(SimpleList<String> values, String expected) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static void report(
            boolean success,
            String type,
            String query,
            LegalProblemAnalyzer.AnalysisResult result) {

        if (success) {
            passed++;
        } else {
            failed++;
        }

        System.out.println((success ? "PASS" : "FAIL")
                + " | " + type
                + " | " + query
                + " | issues=" + values(result.getIssues())
                + " | objects=" + values(result.getSituation().getObjects())
                + " | missing=" + values(result.getMissingInformation())
                + " | confidence=" + result.getSituation().getConfidence());
    }

    private static String values(SimpleList<String> values) {
        StringBuilder output = new StringBuilder("[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                output.append(", ");
            }
            output.append(values.get(i));
        }

        output.append("]");
        return output.toString();
    }
}

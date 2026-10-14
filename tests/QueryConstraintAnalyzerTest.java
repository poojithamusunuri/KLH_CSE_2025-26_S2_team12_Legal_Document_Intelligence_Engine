package tests;

import java.util.List;
import search.QueryConstraintAnalyzer;

/** Regression tests for domain-independent negative-condition handling. */
public class QueryConstraintAnalyzerTest {
    private static int failures;
    private static int checks;

    public static void main(String[] args) {
        check("missing-notice query extracts a generic condition",
                !QueryConstraintAnalyzer.extract(
                        "my property was seized without any information or prior notice").isEmpty());
        check("a matching negative condition is recognized",
                supported("my property was seized without prior notice",
                        "The property was seized without prior notice."));
        check("a nearby unrelated 'without' phrase does not validate notice absence",
                !supported("my property was seized without prior notice",
                        "The notice was issued without jurisdiction after the property was seized."));
        check("positive notice evidence does not validate an absent-notice claim",
                !supported("my property was seized without prior notice",
                        "The property was seized after prior notice was served."));
        check("warrant condition is handled by the same generic mechanism",
                supported("Police searched the premises without a warrant",
                        "Police searched the premises without a warrant."));
        check("salary condition is handled without a salary-specific production rule",
                supported("Salary was not paid", "Salary was not paid."));
        check("deposit condition is handled without a tenancy-specific production rule",
                supported("The landlord did not return the deposit",
                        "The landlord did not return the deposit."));
        System.out.println("QUERY CONSTRAINT ANALYZER SUMMARY | checks=" + checks + " failures=" + failures);
        if (failures > 0) throw new AssertionError("Query constraint tests failed");
    }

    private static boolean supported(String query, String body) {
        List<QueryConstraintAnalyzer.Constraint> constraints = QueryConstraintAnalyzer.extract(query);
        return QueryConstraintAnalyzer.isSupportedNearEvent(body, constraints,
                null, null, null, 260);
    }

    private static void check(String label, boolean ok) {
        checks++;
        if (ok) System.out.println("PASS | " + label);
        else { failures++; System.out.println("FAIL | " + label); }
    }
}

package tests;

import analysis.LegalProblemAnalyzer;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

public class ScenarioEvaluation {

    private static class Scenario {
        private final String id;
        private final String query;
        private final String[] expectedDomains;
        private final String[] expectedConcepts;
        private final boolean shouldBeUncertain;

        public Scenario(
                String id,
                String query,
                String[] expectedDomains,
                String[] expectedConcepts,
                boolean shouldBeUncertain) {
            this.id = id;
            this.query = query;
            this.expectedDomains = expectedDomains;
            this.expectedConcepts = expectedConcepts;
            this.shouldBeUncertain = shouldBeUncertain;
        }
    }

    public static void main(String[] args) {
        try {
            LegalCorpusLoader loader = new LegalCorpusLoader("data/corpus");
            SimpleList<LegalDocument> documents = loader.loadDocuments();
            SearchEngine engine = new SearchEngine(documents);

            runScenarios(engine);
        } catch (Exception e) {
            System.out.println("EVALUATION_ERROR | " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private static void runScenarios(SearchEngine engine) throws Exception {
        Scenario[] scenarios = new Scenario[] {
                new Scenario("P01", "My employer has not paid my salary for three months.", new String[]{"Employment-related dispute", "Salary"}, new String[]{"salary", "employer"}, false),
                new Scenario("P02", "The owner refuses to return my rental deposit.", new String[]{"Tenancy-related matter", "Money / debt / recovery matter"}, new String[]{"deposit", "tenant", "landlord"}, false),
                new Scenario("P03", "Someone used my bank account without permission.", new String[]{"Money / debt / recovery matter"}, new String[]{"account", "bank", "unauthorized"}, false),
                new Scenario("P04", "The product I bought is defective and the seller refuses a refund.", new String[]{"Consumer / goods or services dispute"}, new String[]{"product", "refund", "seller"}, false),
                new Scenario("P05", "I signed an agreement but the other party refused to perform its obligations.", new String[]{"Contract-related issue"}, new String[]{"agreement", "perform", "obligation"}, false),
                new Scenario("P06", "My landlord is not returning my security deposit after I vacated the house.", new String[]{"Tenancy-related matter"}, new String[]{"deposit", "landlord", "house"}, false),
                new Scenario("P07", "A driver hit my car and caused injuries.", new String[]{"Motor vehicle / accident-related matter"}, new String[]{"vehicle", "accident", "injury"}, false),
                new Scenario("P08", "My spouse and I are disputing the inheritance of our parents' property.", new String[]{"Family / inheritance matter"}, new String[]{"inheritance", "property", "spouse"}, false),
                new Scenario("P09", "Someone copied my photo and used it on a website without permission.", new String[]{"Intellectual property matter"}, new String[]{"photo", "copy", "unauthorized"}, false),
                new Scenario("P10", "I borrowed money from a friend and they now refuse repayment.", new String[]{"Money / debt / recovery matter"}, new String[]{"borrow", "money", "repayment"}, false),
                new Scenario("P11", "My boss has not paid my wages for two months.", new String[]{"Employment-related dispute"}, new String[]{"boss", "wages"}, false),
                new Scenario("P12", "The seller delivered a faulty product and refused a remedy.", new String[]{"Consumer / goods or services dispute"}, new String[]{"faulty", "seller", "remedy"}, false),
                new Scenario("P13", "The tenant has not paid rent for six months.", new String[]{"Tenancy-related matter"}, new String[]{"tenant", "rent"}, false),
                new Scenario("P14", "The company dismissed me after I raised a complaint.", new String[]{"Employment-related dispute"}, new String[]{"company", "dismissed", "complaint"}, false),
                new Scenario("P15", "I need help with a legal issue.", new String[]{}, new String[]{}, true),
                new Scenario("F01", "I paid my friend back for a personal loan.", new String[]{}, new String[]{}, true),
                new Scenario("F02", "Our team completed the project.", new String[]{}, new String[]{}, true),
                new Scenario("F03", "The agreement was discussed during a family meeting.", new String[]{}, new String[]{}, true),
                new Scenario("F04", "I work on a vehicle project.", new String[]{}, new String[]{}, true),
                new Scenario("F05", "The person paid the amount voluntarily.", new String[]{}, new String[]{}, true),
                new Scenario("F06", "I have a problem with money.", new String[]{}, new String[]{}, true),
                new Scenario("F07", "There is an issue with my agreement.", new String[]{}, new String[]{}, true),
                new Scenario("F08", "Someone is troubling me.", new String[]{}, new String[]{}, true),
                new Scenario("N01", "My boss hasn't given me my wages.", new String[]{"Employment-related dispute"}, new String[]{"boss", "wages"}, false),
                new Scenario("N02", "The owner refuses to return my rental deposit.", new String[]{"Tenancy-related matter"}, new String[]{"owner", "deposit"}, false),
                new Scenario("N03", "Someone used my bank account without permission.", new String[]{"Money / debt / recovery matter"}, new String[]{"bank", "unauthorized"}, false),
                new Scenario("N04", "The product I bought is defective and the seller refuses a refund.", new String[]{"Consumer / goods or services dispute"}, new String[]{"defective", "refund"}, false),
                new Scenario("N05", "We had a verbal deal and now they are not honoring it.", new String[]{"Contract-related issue"}, new String[]{"deal", "honoring"}, false),
                new Scenario("N06", "My house is being occupied by someone claiming ownership.", new String[]{"Property / ownership dispute"}, new String[]{"house", "ownership", "claim"}, false),
                new Scenario("N07", "I was discharged from work without notice.", new String[]{"Employment-related dispute"}, new String[]{"work", "dismissed"}, false),
                new Scenario("N08", "The company withheld my salary after I quit.", new String[]{"Employment-related dispute"}, new String[]{"withheld", "salary"}, false),
                new Scenario("N09", "I am not being paid for the work I did.", new String[]{"Employment-related dispute"}, new String[]{"paid", "work"}, false),
                new Scenario("N10", "A person copied my logo and is using it commercially.", new String[]{"Intellectual property matter"}, new String[]{"logo", "copied", "commercially"}, false),
                new Scenario("N11", "My college is harassing me and I want help.", new String[]{"Employment-related dispute"}, new String[]{"college", "harassment"}, false),
                new Scenario("R01", "conspiracy", new String[]{"Contract-related issue"}, new String[]{"conspiracy"}, false),
                new Scenario("R02", "conspiraci", new String[]{"Contract-related issue"}, new String[]{"conspiracy"}, false),
                new Scenario("R03", "agreement between parties", new String[]{"Contract-related issue"}, new String[]{"agreement"}, false),
                new Scenario("R04", "ragging", new String[]{"Employment-related dispute"}, new String[]{"ragging"}, false),
                new Scenario("R05", "Defamation online and someone published my photo.", new String[]{"Intellectual property matter"}, new String[]{"photo", "defamation"}, false)
        };

        int pass = 0;
        int partial = 0;
        int fail = 0;
        int uncertain = 0;

        for (int i = 0; i < scenarios.length; i++) {
            Scenario scenario = scenarios[i];
            LegalProblemAnalyzer analyzer = new LegalProblemAnalyzer();
            LegalProblemAnalyzer.AnalysisResult result = analyzer.analyze(scenario.query);
            SearchEngine.SearchRankedOutcome rankedOutcome = engine.searchRankedWithMetadata(
                    scenario.query,
                    SearchEngine.Algorithm.Z_ALGORITHM,
                    5,
                    result
            );

            String[] issues = listToArray(result.getIssues());
            boolean expectedMatch = expectedExists(issues, scenario.expectedDomains);
            boolean conceptMatch = conceptsPresent(result, scenario.expectedConcepts);
            boolean ambiguousOutcome = scenario.shouldBeUncertain && isUncertain(result, issues);
            boolean falsePositiveBlocked = !scenario.shouldBeUncertain && !containsGenericFalsePositive(issues);

            String status;
            if (scenario.shouldBeUncertain) {
                if (ambiguousOutcome) {
                    status = "PASS";
                    uncertain++;
                } else {
                    status = "FAIL";
                    fail++;
                }
            } else if (expectedMatch && conceptMatch && falsePositiveBlocked) {
                status = "PASS";
                pass++;
            } else if (expectedMatch || conceptMatch) {
                status = "PARTIAL";
                partial++;
            } else {
                status = "FAIL";
                fail++;
            }

            System.out.println(
                    scenario.id + " | " + status + " | " + scenario.query
            );
            System.out.println("  issues=" + join(issues) + " | confidence=" + result.getSituation().getConfidence());
                System.out.println("  objects=" + joinList(result.getSituation().getObjects()) + " | ranked=" + rankedOutcome.getCandidateCount());
            System.out.println();
        }

        System.out.println("SUMMARY | pass=" + pass + " partial=" + partial + " fail=" + fail + " uncertain=" + uncertain);
    }

    private static String[] listToArray(SimpleList<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private static boolean expectedExists(String[] issues, String[] expectedDomains) {
        if (expectedDomains == null || expectedDomains.length == 0) {
            return issues == null || issues.length == 0 || issues[0].trim().length() == 0;
        }

        for (int i = 0; i < expectedDomains.length; i++) {
            String expected = expectedDomains[i];
            for (int j = 0; j < issues.length; j++) {
                if (issues[j] != null && issues[j].contains(expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean conceptsPresent(LegalProblemAnalyzer.AnalysisResult result, String[] expectedConcepts) {
        if (expectedConcepts == null || expectedConcepts.length == 0) {
            return true;
        }

        String text = joinList(result.getSituation().getActors()) + " "
                + joinList(result.getSituation().getActions()) + " "
                + joinList(result.getSituation().getObjects()) + " "
                + joinList(result.getSituation().getRelationships());

        for (int i = 0; i < expectedConcepts.length; i++) {
            String expected = expectedConcepts[i].toLowerCase();
            if (text.toLowerCase().contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUncertain(LegalProblemAnalyzer.AnalysisResult result, String[] issues) {
        if (issues == null || issues.length == 0) {
            return true;
        }
        if ("LOW".equalsIgnoreCase(result.getSituation().getConfidence())) {
            return true;
        }
        return false;
    }

    private static boolean containsGenericFalsePositive(String[] issues) {
        if (issues == null || issues.length == 0) {
            return false;
        }
        for (int i = 0; i < issues.length; i++) {
            String issue = issues[i];
            if (issue != null && (
                    issue.toLowerCase().contains("employment") ||
                    issue.toLowerCase().contains("contract") ||
                    issue.toLowerCase().contains("consumer") ||
                    issue.toLowerCase().contains("property") ||
                    issue.toLowerCase().contains("tenancy") ||
                    issue.toLowerCase().contains("intellectual property")
            )) {
                return true;
            }
        }
        return false;
    }

    private static String join(String[] values) {
        if (values == null || values.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    private static String joinList(SimpleList<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values.get(i));
        }
        builder.append("]");
        return builder.toString();
    }
}

package tests;

import analysis.InformationDependencyMap;
import analysis.LegalProblemAnalyzer;
import analysis.ResearchPlanner;
import analysis.ResearchTaskScheduler;
import structures.SimpleList;

public class Phase4ResearchPipelineTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testEmployment();
        testTenancy();
        testBanking();
        testContract();
        testIp();
        testConspiracy();

        System.out.println();
        System.out.println("PHASE 4 SUMMARY | passed=" + passed
                + " failed=" + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEmployment() {
        checkPipeline(
                "My employer has not paid my salary for three months.",
                "Employment-related dispute",
                "Salary or payment records",
                "salary-records"
        );
    }

    private static void testTenancy() {
        checkPipeline(
                "The owner refuses to return my rental deposit.",
                "Tenancy-related matter",
                "Deposit or payment proof",
                "deposit-proof"
        );
    }

    private static void testBanking() {
        checkPipeline(
                "Someone used my bank account without permission.",
                "Banking / unauthorized transaction matter",
                "Bank transaction details",
                "bank-transaction"
        );
    }

    private static void testContract() {
        checkPipeline(
                "I signed an agreement but the other party refused to perform its obligations.",
                "Contract-related issue",
                "Contract or agreement",
                "contract-document"
        );
    }

    private static void testIp() {
        checkPipeline(
                "Someone copied my photo and used it on a website without permission.",
                "Intellectual property matter",
                "Ownership or registration records",
                "ip-ownership"
        );
    }

    private static void testConspiracy() {
        checkPipeline(
                "conspiracy",
                "Criminal / conspiracy-related issue",
                "Alleged conspiracy details",
                "alleged-conduct"
        );
    }

    private static void checkPipeline(
            String query,
            String expectedIssue,
            String expectedInformation,
            String expectedTaskId) {

        LegalProblemAnalyzer analyzer = new LegalProblemAnalyzer();
        LegalProblemAnalyzer.AnalysisResult analysis = analyzer.analyze(query);

        boolean issueFound = contains(analysis.getIssues(), expectedIssue);

        InformationDependencyMap map = new InformationDependencyMap();
        map.build(analysis);

        boolean dependencyFound = false;
        for (int i = 0; i < map.getDependencies().size(); i++) {
            InformationDependencyMap.Dependency dependency =
                    map.getDependencies().get(i);

            if (dependency.getRequiredInformation().equals(expectedInformation)) {
                dependencyFound = true;
                break;
            }
        }

        ResearchPlanner planner = new ResearchPlanner();
        SimpleList<String> plan = planner.createPlan(analysis, map);
        boolean planFound = containsFragment(plan, expectedInformation);

        ResearchTaskScheduler scheduler = new ResearchTaskScheduler();
        ResearchTaskScheduler.ScheduleResult schedule =
                scheduler.scheduleFromAnalysis(analysis, map, 8);

        boolean taskFound = false;
        for (int i = 0; i < schedule.getScheduledTasks().size(); i++) {
            if (schedule.getScheduledTasks().get(i).getId().equals(expectedTaskId)) {
                taskFound = true;
                break;
            }
        }

        if (issueFound && dependencyFound && planFound && taskFound) {
            passed++;
            System.out.println("PASS | " + query);
        } else {
            failed++;
            System.out.println("FAIL | " + query);
            System.out.println("  issue=" + issueFound);
            System.out.println("  dependency=" + dependencyFound);
            System.out.println("  plan=" + planFound);
            System.out.println("  scheduledTask=" + taskFound);
        }
    }

    private static boolean contains(SimpleList<String> values, String expected) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsFragment(SimpleList<String> values, String expected) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).contains(expected)) {
                return true;
            }
        }
        return false;
    }
}

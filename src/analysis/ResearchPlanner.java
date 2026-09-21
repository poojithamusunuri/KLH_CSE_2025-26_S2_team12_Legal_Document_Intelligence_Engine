package analysis;

import structures.SimpleList;

public class ResearchPlanner {

    public SimpleList<String> createPlan(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        SimpleList<String> plan =
                new SimpleList<>();

        SimpleList<String> missing =
                analysis.getMissingInformation();

        /*
         * High priority:
         * Information that can directly establish
         * the central facts of the problem.
         */

        for (int i = 0;
                i < missing.size();
                i++) {

            String information =
                    missing.get(i);

            if (isHighPriority(information)) {
            addUniquePlanStep(plan,
                "HIGH: Investigate "
                    + information
                    + " first.");
            }
        }

        /*
         * Medium priority.
         */

        for (int i = 0;
                i < missing.size();
                i++) {

            String information =
                    missing.get(i);

            if (!isHighPriority(information)) {
                addUniquePlanStep(plan,
                        "MEDIUM: Investigate "
                                + information
                                + " next.");
            }
        }

        return plan;
    }

    private void addUniquePlanStep(SimpleList<String> plan, String step) {
        if (step == null || step.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < plan.size(); i++) {
            if (plan.get(i).equals(step)) {
                return;
            }
        }

        plan.add(step);
    }

    private boolean isHighPriority(
            String information) {

        if (information.equals(
                "Ownership history")) {

            return true;
        }

        if (information.equals(
                "Sale or transfer document")) {

            return true;
        }

        if (information.equals(
                "Employment contract")) {

            return true;
        }

        if (information.equals(
                "Termination communication")) {

            return true;
        }

        if (information.equals(
                "Complete contract or agreement")) {

            return true;
        }

        return false;
    }
}

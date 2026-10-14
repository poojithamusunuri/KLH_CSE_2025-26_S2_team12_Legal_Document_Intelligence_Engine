package analysis;

import structures.SimpleList;

public class ResearchPlanner {

    public SimpleList<String> createPlan(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        InformationDependencyMap dependencyMap =
                new InformationDependencyMap();

        dependencyMap.build(analysis);

        return createPlan(analysis, dependencyMap);
    }

    public SimpleList<String> createPlan(
            LegalProblemAnalyzer.AnalysisResult analysis,
            InformationDependencyMap dependencyMap) {

        SimpleList<String> plan = new SimpleList<>();

        if (analysis == null) {
            return plan;
        }

        if (dependencyMap != null
                && !dependencyMap.getDependencies().isEmpty()) {

            SimpleList<InformationDependencyMap.Dependency> dependencies =
                    dependencyMap.getDependencies();

            for (int i = 0; i < dependencies.size(); i++) {
                InformationDependencyMap.Dependency dependency =
                        dependencies.get(i);

                String priority =
                        isHighPriority(dependency.getRequiredInformation())
                                ? "HIGH"
                                : "MEDIUM";

                addUniquePlanStep(
                        plan,
                        priority + ": Investigate "
                                + dependency.getRequiredInformation()
                                + " to answer: "
                                + dependency.getQuestion()
                                + "."
                );
            }

            return plan;
        }

        // Safe fallback for an empty dependency map.
        SimpleList<String> missing = analysis.getMissingInformation();

        for (int i = 0; i < missing.size(); i++) {
            String information = missing.get(i);
            addUniquePlanStep(
                    plan,
                    (isHighPriority(information) ? "HIGH" : "MEDIUM")
                            + ": Investigate "
                            + information
                            + "."
            );
        }

        return plan;
    }

    private void addUniquePlanStep(
            SimpleList<String> plan,
            String step) {

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

    private boolean isHighPriority(String information) {

        if (information == null) {
            return false;
        }

        return information.equals("Property ownership documents")
                || information.equals("Ownership history")
                || information.equals("Employment relationship details")
                || information.equals("Employment agreement or appointment terms")
                || information.equals("Employment contract")
                || information.equals("Salary or payment records")
                || information.equals("Contract or agreement")
                || information.equals("Complete contract or agreement")
                || information.equals("Tenancy agreement")
                || information.equals("Product purchase records")
                || information.equals("Bank transaction details")
                || information.equals("Ownership or registration records")
                || information.equals("Family relationship details")
                || information.equals("Estate, will, or property records")
                || information.equals("Accident or incident details")
                || information.equals("Alleged conspiracy details")
                || information.equals("Institutional complaint or incident records");
    }
}

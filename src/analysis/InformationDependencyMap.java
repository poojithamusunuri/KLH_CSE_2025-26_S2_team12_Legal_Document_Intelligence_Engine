package analysis;

import structures.SimpleList;

public class InformationDependencyMap {

    public static class Dependency {

        private final String question;
        private final String requiredInformation;
        private final String reason;

        public Dependency(
                String question,
                String requiredInformation,
                String reason) {

            this.question = question;
            this.requiredInformation = requiredInformation;
            this.reason = reason;
        }

        public String getQuestion() {
            return question;
        }

        public String getRequiredInformation() {
            return requiredInformation;
        }

        public String getReason() {
            return reason;
        }
    }

    private final SimpleList<Dependency> dependencies;

    public InformationDependencyMap() {
        dependencies = new SimpleList<>();
    }

    public void build(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        dependencies.clear();

        SimpleList<String> issues =
                analysis.getIssues();

        for (int i = 0; i < issues.size(); i++) {

            String issue = issues.get(i);

            if (issue.contains("Property")) {

                addDependency(
                        "Who legally owns the property?",
                        "Ownership history",
                        "Previous ownership must be established before evaluating the current claim."
                );

                addDependency(
                        "Was the property legally transferred?",
                        "Sale or transfer document",
                        "The transfer document provides evidence of the transaction."
                );

                addDependency(
                        "Was the transfer properly recorded?",
                        "Property registration details",
                        "Registration information helps establish the recorded transfer."
                );

                addDependency(
                        "Does another party have a competing claim?",
                        "Evidence supporting the opposing claim",
                        "The competing evidence must be examined to understand the ownership conflict."
                );
            }

            if (issue.contains("Employment")) {

                addDependency(
                        "What terms governed the employment?",
                        "Employment contract",
                        "The contract establishes the relevant employment terms."
                );

                addDependency(
                        "Why was the employment terminated?",
                        "Termination communication",
                        "The termination communication provides the stated reason and circumstances."
                );

                addDependency(
                        "What workplace rule is involved?",
                        "Applicable company policy",
                        "The relevant policy must be identified before evaluating the alleged violation."
                );
            }

            if (issue.contains("Contract")) {

                addDependency(
                        "What obligations were agreed?",
                        "Complete contract or agreement",
                        "The full agreement defines the parties' contractual obligations."
                );

                addDependency(
                        "Were the obligations performed?",
                        "Evidence of contractual obligations",
                        "Evidence is required to compare the agreed obligations with what actually occurred."
                );
            }
        }
    }

    private void addDependency(
            String question,
            String information,
            String reason) {

        if (!containsInformation(information)) {

            dependencies.add(
                    new Dependency(
                            question,
                            information,
                            reason
                    )
            );
        }
    }

    private boolean containsInformation(
            String information) {

        for (int i = 0;
                i < dependencies.size();
                i++) {

            if (dependencies.get(i)
                    .getRequiredInformation()
                    .equals(information)) {

                return true;
            }
        }

        return false;
    }

    public SimpleList<Dependency> getDependencies() {
        return dependencies;
    }
}

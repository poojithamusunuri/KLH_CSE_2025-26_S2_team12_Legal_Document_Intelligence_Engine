package analysis;

import structures.SimpleList;

public class ResearchTaskScheduler {

    public static class ResearchTask {

        private final String id;
        private final String title;
        private final int duration;
        private final int value;
        private final int coverage;
        private final int priority;
        private final SimpleList<String> dependencies;

        public ResearchTask(
                String id,
                String title,
                int duration,
                int value,
                int coverage,
                int priority,
                SimpleList<String> dependencies) {

            this.id = id;
            this.title = title;
            this.duration = duration;
            this.value = value;
            this.coverage = coverage;
            this.priority = priority;
            this.dependencies = dependencies;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public int getDuration() {
            return duration;
        }

        public int getValue() {
            return value;
        }

        public int getCoverage() {
            return coverage;
        }

        public int getPriority() {
            return priority;
        }

        public SimpleList<String> getDependencies() {
            return dependencies;
        }
    }

    public static class ScheduleResult {

        private final SimpleList<ResearchTask> scheduledTasks;
        private final SimpleList<String> skippedTasks;
        private final int totalDuration;
        private final int totalValue;
        private final int totalCoverage;

        public ScheduleResult(
                SimpleList<ResearchTask> scheduledTasks,
                SimpleList<String> skippedTasks,
                int totalDuration,
                int totalValue,
                int totalCoverage) {

            this.scheduledTasks = scheduledTasks;
            this.skippedTasks = skippedTasks;
            this.totalDuration = totalDuration;
            this.totalValue = totalValue;
            this.totalCoverage = totalCoverage;
        }

        public SimpleList<ResearchTask> getScheduledTasks() {
            return scheduledTasks;
        }

        public SimpleList<String> getSkippedTasks() {
            return skippedTasks;
        }

        public int getTotalDuration() {
            return totalDuration;
        }

        public int getTotalValue() {
            return totalValue;
        }

        public int getTotalCoverage() {
            return totalCoverage;
        }
    }

    public ScheduleResult scheduleFromAnalysis(
            LegalProblemAnalyzer.AnalysisResult analysis,
            InformationDependencyMap dependencyMap,
            int capacityHours) {

        SimpleList<ResearchTask> tasks =
                buildTasksFromAnalysis(
                        analysis,
                        dependencyMap
                );

        return scheduleTasks(tasks, capacityHours);
    }

    public ScheduleResult scheduleTasks(
            SimpleList<ResearchTask> tasks,
            int capacityHours) {

        if (tasks == null || tasks.isEmpty() || capacityHours <= 0) {
            return new ScheduleResult(
                    new SimpleList<ResearchTask>(),
                    new SimpleList<String>(),
                    0,
                    0,
                    0
            );
        }

        SimpleList<ResearchTask> allTasks = cloneTasks(tasks);
        boolean[] selected = new boolean[allTasks.size()];

        int totalDuration = 0;
        int totalValue = 0;
        int totalCoverage = 0;

        while (true) {

            ResearchTask bestTask = null;
            int bestIndex = -1;
            double bestScore = -1;

            for (int i = 0; i < allTasks.size(); i++) {

                if (selected[i]) {
                    continue;
                }

                ResearchTask task = allTasks.get(i);

                if (totalDuration + task.getDuration() > capacityHours) {
                    continue;
                }

                if (!dependencySatisfied(task, allTasks, selected)) {
                    continue;
                }

                double score = computeScore(task);

                if (score > bestScore) {
                    bestScore = score;
                    bestTask = task;
                    bestIndex = i;
                }
            }

            if (bestTask == null) {
                break;
            }

            selected[bestIndex] = true;
            totalDuration += bestTask.getDuration();
            totalValue += bestTask.getValue();
            totalCoverage += bestTask.getCoverage();
        }

        SimpleList<ResearchTask> scheduledTasks =
                new SimpleList<>();

        for (int i = 0; i < allTasks.size(); i++) {

            if (selected[i]) {
                scheduledTasks.add(allTasks.get(i));
            }
        }

        SimpleList<String> skippedTasks =
                new SimpleList<>();

        for (int i = 0; i < allTasks.size(); i++) {

            if (!selected[i]) {
                skippedTasks.add(allTasks.get(i).getTitle());
            }
        }

        return new ScheduleResult(
                scheduledTasks,
                skippedTasks,
                totalDuration,
                totalValue,
                totalCoverage
        );
    }

    private SimpleList<ResearchTask> buildTasksFromAnalysis(
            LegalProblemAnalyzer.AnalysisResult analysis,
            InformationDependencyMap dependencyMap) {

        SimpleList<ResearchTask> tasks = new SimpleList<>();

        if (analysis == null) {
            return tasks;
        }

        /*
         * The dependency map is now the source of the research order.
         * This prevents the scheduler from silently ignoring the questions
         * generated by the analysis layer.
         */
        if (dependencyMap != null
                && !dependencyMap.getDependencies().isEmpty()) {

            SimpleList<InformationDependencyMap.Dependency> dependencies =
                    dependencyMap.getDependencies();

            for (int i = 0; i < dependencies.size(); i++) {
                String information =
                        dependencies.get(i).getRequiredInformation();

                ResearchTask task = createTaskForInformation(information);

                if (task != null && !containsTaskId(tasks, task.getId())) {
                    tasks.add(task);
                }
            }
        }

        /*
         * Safe fallback: preserve any analyzer information that was not
         * represented in the dependency map.
         */
        SimpleList<String> missingInformation =
                analysis.getMissingInformation();

        for (int i = 0; i < missingInformation.size(); i++) {
            ResearchTask task =
                    createTaskForInformation(missingInformation.get(i));

            if (task != null && !containsTaskId(tasks, task.getId())) {
                tasks.add(task);
            }
        }

        if (tasks.isEmpty()) {
            tasks.add(
                    new ResearchTask(
                            "general-review",
                            "General legal fact review",
                            2,
                            18,
                            6,
                            3,
                            new SimpleList<String>()
                    )
            );
        }

        return tasks;
    }

    private ResearchTask createTaskForInformation(
            String information) {

        if (information == null || information.trim().isEmpty()) {
            return null;
        }

        // Criminal procedure / search warrant
        if (matches(information,
                "Warrant, search record, or other authority relied upon")) {
            return new ResearchTask(
                    "warrant-authority",
                    "Review warrant or other search authority and its scope",
                    2, 30, 10, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Relevant police, court, or case records")) {
            return new ResearchTask(
                    "police-search-records",
                    "Review police and court records about the search",
                    1, 22, 7, 4,
                    taskList("warrant-authority")
            );
        }

        // Secured lending / mortgage
        if (matches(information,
                "Loan agreement and sanction documents",
                "Loan agreement",
                "Loan sanction documents")) {
            return new ResearchTask(
                    "loan-terms",
                    "Review loan agreement and sanction terms",
                    2, 25, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Mortgage, hypothecation, or security documents",
                "Mortgage or security documents",
                "Property security documents")) {
            return new ResearchTask(
                    "security-documents",
                    "Verify mortgage or property-security documents",
                    2, 28, 9, 5,
                    taskList("loan-terms")
            );
        }

        if (matches(information,
                "Repayment and outstanding-balance records",
                "Loan repayment records",
                "Outstanding loan balance")) {
            return new ResearchTask(
                    "repayment-history",
                    "Check repayment history and outstanding balance",
                    1, 22, 7, 4,
                    taskList("loan-terms")
            );
        }

        // Property
        if (matches(information,
                "Property ownership documents",
                "Ownership history")) {
            return new ResearchTask(
                    "property-ownership",
                    "Property ownership documents",
                    3, 30, 10, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Possession or transfer records",
                "Sale or transfer document")) {
            return new ResearchTask(
                    "property-transfer",
                    "Possession or transfer records",
                    2, 26, 8, 5,
                    taskList("property-ownership")
            );
        }

        if (matches(information,
                "Property registration details")) {
            return new ResearchTask(
                    "property-registration",
                    "Property registration details",
                    1, 16, 5, 4,
                    taskList("property-transfer")
            );
        }

        if (matches(information,
                "Evidence supporting the opposing claim")) {
            return new ResearchTask(
                    "opposing-claim-evidence",
                    "Evidence supporting the opposing claim",
                    2, 20, 7, 4,
                    taskList("property-ownership")
            );
        }

        // Employment
        if (matches(information,
                "Employment relationship details",
                "Employment contract",
                "Employment agreement or appointment terms")) {
            return new ResearchTask(
                    "employment-terms",
                    "Employment agreement or appointment terms",
                    2, 24, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Salary or payment records",
                "Payment records or salary slips")) {
            return new ResearchTask(
                    "salary-records",
                    "Salary or payment records",
                    1, 22, 8, 5,
                    taskList("employment-terms")
            );
        }

        if (matches(information,
                "Dismissal or termination details",
                "Termination communication",
                "Termination or employment communication")) {
            return new ResearchTask(
                    "termination-details",
                    "Dismissal or termination details",
                    2, 22, 7, 4,
                    taskList("employment-terms")
            );
        }

        if (matches(information,
                "Applicable company policy or employment records",
                "Applicable company policy")) {
            return new ResearchTask(
                    "employment-policy",
                    "Applicable company policy or employment records",
                    1, 14, 5, 3,
                    taskList("employment-terms")
            );
        }

        // Contract
        if (matches(information,
                "Contract or agreement",
                "Complete contract or agreement")) {
            return new ResearchTask(
                    "contract-document",
                    "Contract or agreement",
                    2, 25, 9, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Performance obligation details",
                "Evidence of contractual obligations")) {
            return new ResearchTask(
                    "contract-obligations",
                    "Performance obligation details",
                    2, 18, 6, 4,
                    taskList("contract-document")
            );
        }

        if (matches(information,
                "Evidence of performance, non-performance, or breach",
                "Evidence of breach or non-performance")) {
            return new ResearchTask(
                    "breach-evidence",
                    "Evidence of performance, non-performance, or breach",
                    2, 21, 7, 4,
                    taskList("contract-document")
            );
        }

        if (matches(information, "Identity of parties")) {
            return new ResearchTask(
                    "contract-parties",
                    "Identity of parties",
                    1, 12, 4, 3,
                    taskList("contract-document")
            );
        }

        // Consumer
        if (matches(information, "Product purchase records", "Purchase invoice or receipt")) {
            return new ResearchTask(
                    "purchase-records",
                    "Product purchase records",
                    1, 15, 5, 4,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Payment or refund records")) {
            return new ResearchTask(
                    "payment-refund-records",
                    "Payment or refund records",
                    1, 15, 5, 4,
                    taskList("purchase-records")
            );
        }

        if (matches(information, "Evidence of defect, deficiency, or refusal", "Evidence of defect or deficiency")) {
            return new ResearchTask(
                    "defect-evidence",
                    "Evidence of defect, deficiency, or refusal",
                    2, 19, 7, 4,
                    taskList("purchase-records")
            );
        }

        // Tenancy
        if (matches(information, "Tenancy agreement", "Lease or tenancy agreement")) {
            return new ResearchTask(
                    "tenancy-agreement",
                    "Tenancy agreement",
                    2, 22, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Deposit or payment proof", "Payment and deposit records")) {
            return new ResearchTask(
                    "deposit-proof",
                    "Deposit or payment proof",
                    1, 18, 6, 4,
                    taskList("tenancy-agreement")
            );
        }

        // Intellectual property
        if (matches(information, "Ownership or registration records", "Ownership or registration record")) {
            return new ResearchTask(
                    "ip-ownership",
                    "Ownership or registration records",
                    2, 22, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Evidence of use, copying, or authorization")) {
            return new ResearchTask(
                    "ip-use-evidence",
                    "Evidence of use, copying, or authorization",
                    2, 20, 7, 4,
                    taskList("ip-ownership")
            );
        }

        // Banking
        if (matches(information, "Bank transaction details", "Bank statement or transaction record")) {
            return new ResearchTask(
                    "bank-transaction",
                    "Bank transaction details",
                    1, 22, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Authorization status")) {
            return new ResearchTask(
                    "transaction-authorization",
                    "Authorization status",
                    1, 18, 6, 4,
                    taskList("bank-transaction")
            );
        }

        // Motor vehicle
        if (matches(information, "Accident or incident details", "Accident details")) {
            return new ResearchTask(
                    "accident-details",
                    "Accident or incident details",
                    2, 20, 7, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Damage, injury, or witness records", "Medical or damage records")) {
            return new ResearchTask(
                    "damage-injury-records",
                    "Damage, injury, or witness records",
                    2, 20, 7, 4,
                    taskList("accident-details")
            );
        }

        // Family / inheritance
        if (matches(information, "Family relationship details", "Marriage details")) {
            return new ResearchTask(
                    "family-relationship",
                    "Family relationship details",
                    2, 18, 6, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information,
                "Estate, will, or property records",
                "Will or testamentary document",
                "Estate or property records")) {
            return new ResearchTask(
                    "estate-records",
                    "Estate, will, or property records",
                    2, 22, 8, 4,
                    taskList("family-relationship")
            );
        }

        // Criminal / conspiracy
        if (matches(information, "Alleged conspiracy details", "Details of the alleged offence")) {
            return new ResearchTask(
                    "alleged-conduct",
                    "Alleged conspiracy details",
                    2, 22, 8, 5,
                    new SimpleList<String>()
            );
        }

        if (matches(information, "Identity of involved parties", "Names or identities of persons involved")) {
            return new ResearchTask(
                    "involved-parties",
                    "Identity of involved parties",
                    1, 15, 5, 4,
                    taskList("alleged-conduct")
            );
        }

        if (matches(information,
                "Relevant communications or records",
                "Available evidence",
                "Available evidence such as messages, photographs, videos, or witness statements")) {
            return new ResearchTask(
                    "supporting-evidence",
                    "Relevant communications or records",
                    2, 20, 7, 4,
                    taskList("alleged-conduct")
            );
        }

        // Education / student issue
        if (matches(information,
                "Institutional complaint or incident records",
                "College or institutional complaint details")) {
            return new ResearchTask(
                    "institutional-records",
                    "Institutional complaint or incident records",
                    2, 20, 7, 5,
                    new SimpleList<String>()
            );
        }

        return null;
    }

    private boolean matches(String value, String... candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (value.equals(candidates[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTaskId(
            SimpleList<ResearchTask> tasks,
            String id) {

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    private boolean dependencySatisfied(
            ResearchTask task,
            SimpleList<ResearchTask> tasks,
            boolean[] selected) {

        SimpleList<String> dependencies =
                task.getDependencies();

        for (int i = 0; i < dependencies.size(); i++) {

            String dependencyId = dependencies.get(i);
            boolean found = false;

            for (int j = 0; j < tasks.size(); j++) {

                if (selected[j]) {
                    ResearchTask current = tasks.get(j);

                    if (current.getId().equals(dependencyId)) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    private double computeScore(ResearchTask task) {

        double density =
                (task.getValue()
                        + (task.getCoverage() * 3)
                        + (task.getPriority() * 10))
                / (double) task.getDuration();

        return density;
    }

    private SimpleList<String> taskList(String dependencyId) {

        SimpleList<String> result =
                new SimpleList<>();

        result.add(dependencyId);

        return result;
    }

    private SimpleList<ResearchTask> cloneTasks(
            SimpleList<ResearchTask> tasks) {

        SimpleList<ResearchTask> clone =
                new SimpleList<>();

        for (int i = 0; i < tasks.size(); i++) {
            clone.add(tasks.get(i));
        }

        return clone;
    }
}

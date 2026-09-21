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

        SimpleList<ResearchTask> tasks =
                new SimpleList<>();

        SimpleList<String> missingInformation =
                analysis.getMissingInformation();

        for (int i = 0; i < missingInformation.size(); i++) {

            String information =
                    missingInformation.get(i);

            ResearchTask task = createTaskForInformation(information);

            if (task != null) {
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

        if (information.equals("Ownership history")) {
            return new ResearchTask(
                    "ownership-history",
                    "Ownership history",
                    3,
                    30,
                    10,
                    5,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Sale or transfer document")) {
            return new ResearchTask(
                    "sale-transfer-document",
                    "Sale or transfer document",
                    2,
                    26,
                    8,
                    5,
                    taskList("ownership-history")
            );
        }

        if (information.equals("Property registration details")) {
            return new ResearchTask(
                    "property-registration-details",
                    "Property registration details",
                    1,
                    16,
                    5,
                    4,
                    taskList("sale-transfer-document")
            );
        }

        if (information.equals("Evidence supporting the opposing claim")) {
            return new ResearchTask(
                    "opposing-claim-evidence",
                    "Evidence supporting the opposing claim",
                    2,
                    20,
                    7,
                    4,
                    taskList("ownership-history")
            );
        }

        if (information.equals("Employment contract")) {
            return new ResearchTask(
                    "employment-contract",
                    "Employment contract",
                    2,
                    24,
                    8,
                    5,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Termination communication")) {
            return new ResearchTask(
                    "termination-communication",
                    "Termination communication",
                    2,
                    22,
                    7,
                    4,
                    taskList("employment-contract")
            );
        }

        if (information.equals("Applicable company policy")) {
            return new ResearchTask(
                    "company-policy",
                    "Applicable company policy",
                    1,
                    14,
                    5,
                    3,
                    taskList("employment-contract")
            );
        }

        if (information.equals("Complete contract or agreement")) {
            return new ResearchTask(
                    "complete-contract",
                    "Complete contract or agreement",
                    2,
                    25,
                    9,
                    5,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Evidence of contractual obligations")) {
            return new ResearchTask(
                    "contractual-obligations-evidence",
                    "Evidence of contractual obligations",
                    2,
                    18,
                    6,
                    4,
                    taskList("complete-contract")
            );
        }

        if (information.equals("Evidence of breach or non-performance")) {
            return new ResearchTask(
                    "breach-evidence",
                    "Evidence of breach or non-performance",
                    2,
                    21,
                    7,
                    4,
                    taskList("complete-contract")
            );
        }

        if (information.equals("Purchase invoice or receipt")) {
            return new ResearchTask(
                    "purchase-invoice",
                    "Purchase invoice or receipt",
                    1,
                    15,
                    5,
                    3,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Warranty or service documents")) {
            return new ResearchTask(
                    "warranty-documents",
                    "Warranty or service documents",
                    1,
                    14,
                    5,
                    3,
                    taskList("purchase-invoice")
            );
        }

        if (information.equals("Evidence of defect or deficiency")) {
            return new ResearchTask(
                    "defect-evidence",
                    "Evidence of defect or deficiency",
                    2,
                    17,
                    6,
                    3,
                    taskList("purchase-invoice")
            );
        }

        if (information.equals("Details of the alleged offence")) {
            return new ResearchTask(
                    "offence-details",
                    "Details of the alleged offence",
                    2,
                    18,
                    7,
                    4,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Police complaint or FIR details")) {
            return new ResearchTask(
                    "complaint-details",
                    "Police complaint or FIR details",
                    1,
                    15,
                    5,
                    3,
                    taskList("offence-details")
            );
        }

        if (information.equals("Available evidence")) {
            return new ResearchTask(
                    "available-evidence",
                    "Available evidence",
                    2,
                    17,
                    6,
                    3,
                    taskList("offence-details")
            );
        }

        if (information.equals("Death certificate or relevant records")) {
            return new ResearchTask(
                    "death-records",
                    "Death certificate or relevant records",
                    1,
                    14,
                    5,
                    3,
                    new SimpleList<String>()
            );
        }

        if (information.equals("Will or testamentary document")) {
            return new ResearchTask(
                    "will-document",
                    "Will or testamentary document",
                    2,
                    19,
                    7,
                    4,
                    taskList("death-records")
            );
        }

        if (information.equals("Details of legal heirs")) {
            return new ResearchTask(
                    "legal-heirs",
                    "Details of legal heirs",
                    1,
                    13,
                    4,
                    3,
                    taskList("death-records")
            );
        }

        if (information.equals("Estate or property records")) {
            return new ResearchTask(
                    "estate-records",
                    "Estate or property records",
                    2,
                    16,
                    6,
                    3,
                    taskList("death-records")
            );
        }

        return null;
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

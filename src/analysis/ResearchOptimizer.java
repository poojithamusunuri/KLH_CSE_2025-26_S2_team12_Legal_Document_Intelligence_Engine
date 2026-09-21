package analysis;

import model.LegalDocument;
import structures.SimpleList;

public class ResearchOptimizer {

    public static class OptimizationResult {

        private final SimpleList<LegalDocument> selectedDocuments;
        private final SimpleList<String> coveredQuestions;
        private final SimpleList<String> uncoveredQuestions;
        private final SimpleList<String> coverageBreakdown;
        private final int totalCoverage;
        private final int budget;

        public OptimizationResult(
                SimpleList<LegalDocument> selectedDocuments,
                SimpleList<String> coveredQuestions,
                SimpleList<String> uncoveredQuestions,
                SimpleList<String> coverageBreakdown,
                int totalCoverage,
                int budget) {

            this.selectedDocuments = selectedDocuments;
            this.coveredQuestions = coveredQuestions;
            this.uncoveredQuestions = uncoveredQuestions;
            this.coverageBreakdown = coverageBreakdown;
            this.totalCoverage = totalCoverage;
            this.budget = budget;
        }

        public SimpleList<LegalDocument> getSelectedDocuments() {
            return selectedDocuments;
        }

        public SimpleList<String> getCoveredQuestions() {
            return coveredQuestions;
        }

        public SimpleList<String> getUncoveredQuestions() {
            return uncoveredQuestions;
        }

        public SimpleList<String> getCoverageBreakdown() {
            return coverageBreakdown;
        }

        public int getTotalCoverage() {
            return totalCoverage;
        }

        public int getBudget() {
            return budget;
        }
    }

    private static final String[] STOP_WORDS = {
            "the",
            "a",
            "an",
            "of",
            "to",
            "in",
            "on",
            "for",
            "and",
            "or",
            "is",
            "are",
            "was",
            "were",
            "what",
            "who",
            "why",
            "how",
            "when",
            "where",
            "which",
            "did",
            "does",
            "has",
            "have",
            "had",
            "with",
            "from",
            "by",
            "into",
            "there",
            "their",
            "them",
            "this",
            "that",
            "these",
            "those",
            "can",
            "should",
            "must",
            "be",
            "it",
            "its",
            "as",
            "at",
            "about",
            "after",
            "before",
            "over",
            "under",
            "between",
            "through",
            "during",
            "without",
            "within",
            "using",
            "such",
            "more",
            "most"
    };

    public OptimizationResult optimize(
            LegalProblemAnalyzer.AnalysisResult analysis,
            SimpleList<LegalDocument> candidateDocuments,
            int budget) {

        if (analysis == null
                || candidateDocuments == null
                || candidateDocuments.isEmpty()
                || budget <= 0) {

            return new OptimizationResult(
                    new SimpleList<LegalDocument>(),
                    new SimpleList<String>(),
                    new SimpleList<String>(),
                    new SimpleList<String>(),
                    0,
                    budget
            );
        }

        SimpleList<String> questions =
                analysis.getQuestions();

        if (questions.isEmpty()) {
            questions = analysis.getMissingInformation();
        }

        if (questions.isEmpty()) {
            return new OptimizationResult(
                    new SimpleList<LegalDocument>(),
                    new SimpleList<String>(),
                    new SimpleList<String>(),
                    new SimpleList<String>(),
                    0,
                    budget
            );
        }

        SimpleList<String> uncoveredQuestions =
                cloneStrings(questions);

        SimpleList<String> coveredQuestions =
                new SimpleList<>();

        SimpleList<LegalDocument> selectedDocuments =
                new SimpleList<>();

        SimpleList<String> coverageBreakdown =
                new SimpleList<>();

        int totalCoverage = 0;
        boolean[] selectedFlags =
                new boolean[candidateDocuments.size()];

        int selectionsMade = 0;

        while (selectionsMade < budget
                && uncoveredQuestions.size() > 0) {

            int bestIndex = -1;
            int bestScore = -1;
            int bestNewCoverage = 0;
            int bestTitleMatchCount = 0;
            SimpleList<String> bestCoveredQuestions =
                    new SimpleList<>();

            for (int i = 0; i < candidateDocuments.size(); i++) {

                if (selectedFlags[i]) {
                    continue;
                }

                LegalDocument document =
                        candidateDocuments.get(i);

                CoverageInfo coverage =
                        computeCoverage(
                                document,
                                uncoveredQuestions
                        );

                if (coverage.getAdditionalCoverage() <= 0) {
                    continue;
                }

                int score =
                        coverage.getAdditionalCoverage()
                                + coverage.getTitleMatchCount();

                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                    bestNewCoverage =
                            coverage.getAdditionalCoverage();
                    bestTitleMatchCount =
                            coverage.getTitleMatchCount();
                    bestCoveredQuestions =
                            coverage.getCoveredQuestions();
                } else if (score == bestScore) {

                    if (coverage.getTitleMatchCount()
                            > bestTitleMatchCount) {

                        bestIndex = i;
                        bestNewCoverage =
                                coverage.getAdditionalCoverage();
                        bestTitleMatchCount =
                                coverage.getTitleMatchCount();
                        bestCoveredQuestions =
                                coverage.getCoveredQuestions();
                    }
                }
            }

            if (bestIndex == -1) {
                break;
            }

            LegalDocument chosenDocument =
                    candidateDocuments.get(bestIndex);

            selectedFlags[bestIndex] = true;
            selectedDocuments.add(chosenDocument);
            selectionsMade++;

            totalCoverage += bestNewCoverage;

            coverageBreakdown.add(
                    "Selected: "
                            + chosenDocument.getTitle()
                            + " | new coverage: "
                            + bestNewCoverage
                            + " | title matches: "
                            + bestTitleMatchCount
            );

            addCoveredQuestions(
                    coveredQuestions,
                    uncoveredQuestions,
                    bestCoveredQuestions
            );

            if (uncoveredQuestions.isEmpty()) {
                break;
            }
        }

        SimpleList<String> uncoveredAfterSelection =
                new SimpleList<>();

        for (int i = 0; i < questions.size(); i++) {

            String question = questions.get(i);
            boolean alreadyCovered = false;

            for (int j = 0; j < coveredQuestions.size(); j++) {

                if (coveredQuestions.get(j).equals(question)) {
                    alreadyCovered = true;
                    break;
                }
            }

            if (!alreadyCovered) {
                uncoveredAfterSelection.add(question);
            }
        }

        return new OptimizationResult(
                selectedDocuments,
                coveredQuestions,
                uncoveredAfterSelection,
                coverageBreakdown,
                totalCoverage,
                budget
        );
    }

    private CoverageInfo computeCoverage(
            LegalDocument document,
            SimpleList<String> uncoveredQuestions) {

        String title =
                normalize(document.getTitle());

        String content =
                normalize(document.getContent());

        String documentText =
                title + " " + content;

        int additionalCoverage = 0;
        int titleMatchCount = 0;
        SimpleList<String> coveredQuestions =
                new SimpleList<>();

        for (int i = 0; i < uncoveredQuestions.size(); i++) {

            String question = uncoveredQuestions.get(i);

            int questionCoverage =
                    countQuestionMatches(
                            question,
                            documentText
                    );

            if (questionCoverage > 0) {
                additionalCoverage += questionCoverage;
                coveredQuestions.add(question);

                int questionTitleMatches =
                        countQuestionMatches(
                                question,
                                title
                        );

                if (questionTitleMatches > 0) {
                    titleMatchCount++;
                }
            }
        }

        return new CoverageInfo(
                additionalCoverage,
                titleMatchCount,
                coveredQuestions
        );
    }

    private int countQuestionMatches(
            String question,
            String documentText) {

        SimpleList<String> questionTerms =
                extractMeaningfulTerms(question);

        int matchedTerms = 0;

        for (int i = 0; i < questionTerms.size(); i++) {

            String term = questionTerms.get(i);

            if (documentText.contains(term)) {
                matchedTerms++;
            }
        }

        return matchedTerms;
    }

    private SimpleList<String> extractMeaningfulTerms(
            String text) {

        SimpleList<String> terms =
                new SimpleList<>();

        String normalized =
                normalize(text);

        String[] tokens =
                normalized.split(" ");

        for (int i = 0; i < tokens.length; i++) {

            String token = tokens[i].trim();

            if (token.length() <= 2) {
                continue;
            }

            if (isStopWord(token)) {
                continue;
            }

            if (!terms.contains(token)) {
                terms.add(token);
            }
        }

        return terms;
    }

    private boolean isStopWord(String token) {

        for (int i = 0; i < STOP_WORDS.length; i++) {
            if (STOP_WORDS[i].equals(token)) {
                return true;
            }
        }

        return false;
    }

    private void addCoveredQuestions(
            SimpleList<String> coveredQuestions,
            SimpleList<String> uncoveredQuestions,
            SimpleList<String> newlyCoveredQuestions) {

        for (int i = 0; i < newlyCoveredQuestions.size(); i++) {

            String question = newlyCoveredQuestions.get(i);

            if (!coveredQuestions.contains(question)) {
                coveredQuestions.add(question);
            }
        }

        SimpleList<String> remaining =
                new SimpleList<>();

        for (int i = 0; i < uncoveredQuestions.size(); i++) {

            String question = uncoveredQuestions.get(i);
            boolean stillUncovered = true;

            for (int j = 0; j < newlyCoveredQuestions.size(); j++) {

                if (newlyCoveredQuestions.get(j).equals(question)) {
                    stillUncovered = false;
                    break;
                }
            }

            if (stillUncovered) {
                remaining.add(question);
            }
        }

        uncoveredQuestions.clear();

        for (int i = 0; i < remaining.size(); i++) {
            uncoveredQuestions.add(remaining.get(i));
        }
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private SimpleList<String> cloneStrings(
            SimpleList<String> source) {

        SimpleList<String> clone =
                new SimpleList<>();

        for (int i = 0; i < source.size(); i++) {
            clone.add(source.get(i));
        }

        return clone;
    }

    private static class CoverageInfo {

        private final int additionalCoverage;
        private final int titleMatchCount;
        private final SimpleList<String> coveredQuestions;

        public CoverageInfo(
                int additionalCoverage,
                int titleMatchCount,
                SimpleList<String> coveredQuestions) {

            this.additionalCoverage = additionalCoverage;
            this.titleMatchCount = titleMatchCount;
            this.coveredQuestions = coveredQuestions;
        }

        public int getAdditionalCoverage() {
            return additionalCoverage;
        }

        public int getTitleMatchCount() {
            return titleMatchCount;
        }

        public SimpleList<String> getCoveredQuestions() {
            return coveredQuestions;
        }
    }
}

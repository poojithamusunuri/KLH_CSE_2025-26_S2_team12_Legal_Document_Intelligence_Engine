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
            "the", "a", "an", "of", "to", "in", "on", "for",
            "and", "or", "is", "are", "was", "were", "what", "who",
            "why", "how", "when", "where", "which", "did", "does",
            "has", "have", "had", "with", "from", "by", "into",
            "there", "their", "them", "this", "that", "these", "those",
            "can", "should", "must", "be", "it", "its", "as", "at",
            "about", "after", "before", "over", "under", "between",
            "through", "during", "without", "within", "using", "such",
            "more", "most", "issue", "issues", "problem", "problems",
            "question", "questions", "information", "details", "documents",
            "records", "evidence", "support", "supported", "available",
            "happened", "handled", "specific", "legal", "law", "facts",
            "fact", "answer", "answered", "help", "needed", "need", "find",
            "case", "supporting", "document", "relevant", "agreed", "created",
            "terms", "arrangement"
    };

    public OptimizationResult optimize(
            LegalProblemAnalyzer.AnalysisResult analysis,
            SimpleList<LegalDocument> candidateDocuments,
            int budget) {

        return optimize(analysis, candidateDocuments, budget, null);
    }

    /*
     * tierWeight, when supplied, must have one entry per candidateDocuments
     * (same order): a caller-supplied relevance tier (for example, 2 for a
     * search engine's DIRECT match, 1 for RELATED, 0 for WEAK/unknown). It
     * dominates the internal keyword-density score below, so a document
     * already established as directly relevant to the specific situation
     * cannot be passed over by a merely keyword-dense but weaker match -
     * the same failure this project's ranking redesign fixed in SearchEngine,
     * which would otherwise resurface here through this separate scoring
     * path. Pass null (or use the three-argument overload) when no such
     * ranking is available, e.g. the standalone algorithm demonstration.
     */
    public OptimizationResult optimize(
            LegalProblemAnalyzer.AnalysisResult analysis,
            SimpleList<LegalDocument> candidateDocuments,
            int budget,
            int[] tierWeight) {

        if (candidateDocuments != null
            && tierWeight != null
            && tierWeight.length != candidateDocuments.size()) {
            throw new IllegalArgumentException(
                "tierWeight must contain one entry per candidate document.");
        }

        if (analysis == null
                || candidateDocuments == null
                || candidateDocuments.isEmpty()
                || budget <= 0) {

            return emptyResult(budget);
        }

        SimpleList<String> questions =
                analysis.getQuestions();

        if (questions.isEmpty()) {
            questions = analysis.getMissingInformation();
        }

        if (questions.isEmpty()) {
            return emptyResult(budget);
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

        /*
         * Greedy maximum-coverage approximation:
         *
         * At every step choose the document that gives the largest
         * amount of NEW research-question coverage.
         */
        while (selectionsMade < budget
                && selectionsMade < candidateDocuments.size()) {

            int bestIndex = -1;
            int bestScore = -1;
            int bestTierWeight = Integer.MIN_VALUE;
            int bestCoverage = 0;
            int bestTitleMatches = 0;
            SimpleList<String> bestCoveredQuestions =
                    new SimpleList<>();

            for (int i = 0; i < candidateDocuments.size(); i++) {

                if (selectedFlags[i]) {
                    continue;
                }

                LegalDocument document =
                        candidateDocuments.get(i);
                        
                if (containsDocumentId(
                        selectedDocuments, document.getDocumentId())) {
                    continue;
                }

                /*
                 * candidateDocuments is supplied already ranked by the search
                 * engine's relevance model (index 0 = most relevant to the
                 * specific situation, not merely to one research question).
                 * A small, bounded rank bonus keeps this greedy question-
                 * coverage step from overriding that ranking on the strength
                 * of raw keyword density alone - the same failure mode the
                 * ranking redesign fixed in SearchEngine, which would
                 * otherwise resurface here through a separate scoring path.
                 */
                int rankBonus =
                        (candidateDocuments.size() - i) * RANK_BONUS_WEIGHT;

                if (tierWeight != null && i < tierWeight.length) {
                    rankBonus += tierWeight[i] * TIER_BONUS_WEIGHT;
                }

                CoverageInfo coverage =
                        computeCoverage(
                                document,
                                uncoveredQuestions,
                                rankBonus,
                                tierWeight == null ? -1 : tierWeight[i]
                        );

                if (coverage.getAdditionalCoverage() <= 0) {
                    continue;
                }

                int score = coverage.getScore();

int candidateTierWeight =
        tierWeight == null ? 0 : tierWeight[i];

boolean betterCandidate;

if (bestIndex == -1) {
    betterCandidate = true;
} else if (tierWeight != null
        && candidateTierWeight != bestTierWeight) {
    betterCandidate =
            candidateTierWeight > bestTierWeight;
} else {
    betterCandidate = score > bestScore;
}

if (betterCandidate) {
    bestScore = score;
    bestTierWeight = candidateTierWeight;
    bestIndex = i;
    bestCoverage =
            coverage.getAdditionalCoverage();
    bestTitleMatches =
            coverage.getTitleMatchCount();
    bestCoveredQuestions =
            coverage.getCoveredQuestions();
}
            }

            if (bestIndex == -1) {
                // No remaining document adds new heuristic question coverage.
                // Still honor the user's requested document budget by filling
                // remaining slots from the already relevance-ranked result set.
                int fallbackIndex = -1;
                int fallbackTier = Integer.MIN_VALUE;
                for (int i = 0; i < candidateDocuments.size(); i++) {
                    if (selectedFlags[i]) continue;
                    LegalDocument candidate = candidateDocuments.get(i);
                    if (containsDocumentId(selectedDocuments, candidate.getDocumentId())) continue;
                    int candidateTier = tierWeight == null ? 0 : tierWeight[i];
                    if (fallbackIndex == -1 || candidateTier > fallbackTier) {
                        fallbackIndex = i;
                        fallbackTier = candidateTier;
                    }
                }
                if (fallbackIndex == -1) break;
                LegalDocument fallbackDocument = candidateDocuments.get(fallbackIndex);
                selectedFlags[fallbackIndex] = true;
                selectedDocuments.add(fallbackDocument);
                selectionsMade++;
                coverageBreakdown.add("Selected as an additional ranked document: "
                        + fallbackDocument.getTitle()
                        + " | no additional question coverage identified by the heuristic");
                continue;
            }

            LegalDocument chosenDocument =
                    candidateDocuments.get(bestIndex);

            selectedFlags[bestIndex] = true;
            selectedDocuments.add(chosenDocument);
            selectionsMade++;

            totalCoverage += bestCoverage;

            coverageBreakdown.add(
                    "Selected: "
                            + chosenDocument.getTitle()
                            + " | question coverage: "
                            + bestCoverage
                            + " | title matches: "
                            + bestTitleMatches
                            + " | score: "
                            + bestScore
            );

            addCoveredQuestions(
                    coveredQuestions,
                    uncoveredQuestions,
                    bestCoveredQuestions
            );
        }

        SimpleList<String> uncoveredAfterSelection =
                new SimpleList<>();

        for (int i = 0; i < questions.size(); i++) {

            String question = questions.get(i);

            if (!coveredQuestions.contains(question)) {
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

    private static final int RANK_BONUS_WEIGHT = 3;
    private static final int TIER_BONUS_WEIGHT = 100;

    private CoverageInfo computeCoverage(
            LegalDocument document,
            SimpleList<String> uncoveredQuestions,
            int rankBonus,
            int relevanceTier) {

        String title =
                normalize(document.getTitle());

        String content =
                normalize(document.getContent());

        String documentText =
                title + " " + content;

        int additionalCoverage = 0;
        int titleMatchCount = 0;
        int totalScore = 0;

        SimpleList<String> coveredQuestions =
                new SimpleList<>();

        for (int i = 0; i < uncoveredQuestions.size(); i++) {

            String question =
                    uncoveredQuestions.get(i);

            QuestionMatch match =
                    evaluateQuestion(
                            question,
                            title,
                            documentText
                    );

            // Tier 0 (weak) and tier 1 (related/background) results cannot
            // substantiate an integrated question-coverage claim. Only direct
            // or title-level matches may contribute; when no tier metadata was
            // supplied by a standalone demo, retain its clearly labelled heuristic.
            boolean credibleCandidateSignal =
                    relevanceTier < 0 || relevanceTier >= 2;

            if (match.getScore() > 0 && credibleCandidateSignal) {

                /*
                 * A research question counts as one newly covered
                 * question. Individual concept matches affect the
                 * score, not the number of questions.
                 */
                additionalCoverage++;

                totalScore += match.getScore();

                if (match.getTitleMatches() > 0) {
                    titleMatchCount++;
                }

                coveredQuestions.add(question);
            }
        }

        if (additionalCoverage > 0) {
            totalScore += rankBonus;
        }

        return new CoverageInfo(
                additionalCoverage,
                titleMatchCount,
                totalScore,
                coveredQuestions
        );
    }

    /*
     * Evaluate a complete research question instead of simply counting
     * arbitrary words.
     */
    private QuestionMatch evaluateQuestion(
            String question,
            String title,
            String documentText) {

        SimpleList<String> terms =
                extractMeaningfulTerms(question);

        SimpleList<String> aliases =
                expandLegalTerms(question);

        int directMatches = 0;
        int aliasMatches = 0;
        int titleMatches = 0;

        for (int i = 0; i < terms.size(); i++) {

            String term = terms.get(i);

            if (containsWholeWord(documentText, term)) {
                directMatches++;

                if (containsWholeWord(title, term)) {
                    titleMatches++;
                }
            }
        }

        for (int i = 0; i < aliases.size(); i++) {

            String alias = aliases.get(i);

            if (containsWholeWord(documentText, alias)) {
                aliasMatches++;

                if (containsWholeWord(title, alias)) {
                    titleMatches++;
                }
            }
        }

        /*
         * A document should not become relevant from one accidental
         * generic word such as "record" or "payment".
         *
         * Require either:
         *   - multiple direct concepts
         *   - a direct concept + legal alias
         *   - a strong title match
         */
        // Do not treat generic grammatical overlap or a large synonym group
        // as proof of coverage. A question is only considered potentially
        // covered when multiple question-specific terms are present, or when
        // the title and body provide corroborating signals.
        int localTermSupport = countTermsInBestWindow(documentText, terms, 320);
        boolean relevant =
                localTermSupport >= 2
                        || (directMatches >= 2 && titleMatches >= 1)
                        || (terms.size() <= 2 && directMatches >= 1 && aliasMatches >= 1)
                        || (directMatches >= 3 && aliasMatches >= 1);

        if (!relevant || terms.size() < 2) {
            return new QuestionMatch(0, 0);
        }

        // Require meaningful overlap, not just terms such as "agreed",
        // "created", "terms", "question", or "information".
        int score = (directMatches * 5)
                + (Math.min(aliasMatches, 2) * 2)
                + (titleMatches * 7);
        return new QuestionMatch(
                score,
                titleMatches
        );
    }

    /** Counts distinct question terms co-occurring in any 320-character window. */
    private int countTermsInBestWindow(
            String documentText,
            SimpleList<String> terms,
            int windowSize) {
        if (documentText == null || terms == null || terms.isEmpty()) return 0;
        int best = 0;
        int step = Math.max(40, windowSize / 4);
        for (int start = 0; start < documentText.length(); start += step) {
            int end = Math.min(documentText.length(), start + windowSize);
            String window = documentText.substring(start, end);
            int found = 0;
            for (int i = 0; i < terms.size(); i++) {
                if (containsWholeWord(window, terms.get(i))) found++;
            }
            if (found > best) best = found;
            if (best >= 3) return best;
        }
        return best;
    }

    /*
     * Legal vocabulary expansion.
     *
     * These are deliberately grouped by concepts rather than individual
     * spelling variants. This makes the optimizer useful across different
     * wording in judgments and legal documents.
     */
    private SimpleList<String> expandLegalTerms(
            String question) {

        SimpleList<String> aliases =
                new SimpleList<>();

        String normalized =
                normalize(question);

        addGroupIfPresent(
                aliases,
                normalized,
                "salary",
                "wage",
                "wages",
                "remuneration",
                "pay",
                "payment",
                "arrears",
                "compensation",
                "payslip",
                "payslips",
                "salary slip",
                "salary slips"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "employment",
                "employer",
                "employee",
                "service",
                "appointment",
                "occupation"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "contract",
                "agreement",
                "obligation",
                "performance",
                "breach",
                "default"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "rent",
                "rental",
                "tenancy",
                "tenant",
                "landlord",
                "lease",
                "deposit"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "bank",
                "banking",
                "account",
                "transaction",
                "withdrawal",
                "debit",
                "credit",
                "authorization",
                "unauthorized"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "product",
                "purchase",
                "consumer",
                "seller",
                "buyer",
                "defect",
                "defective",
                "refund",
                "deficiency"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "photo",
                "image",
                "copyright",
                "copying",
                "copied",
                "website",
                "publication",
                "authorization"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "property",
                "ownership",
                "possession",
                "transfer",
                "registration"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "vehicle",
                "motor",
                "accident",
                "collision",
                "damage",
                "injury"
        );

        addGroupIfPresent(
                aliases,
                normalized,
                "family",
                "marriage",
                "inheritance",
                "estate",
                "will",
                "succession"
        );

        /*
         * Avoid returning aliases that are already ordinary question
         * terms. This keeps the scoring from artificially inflating
         * repeated vocabulary.
         */
        SimpleList<String> filtered =
                new SimpleList<>();

        for (int i = 0; i < aliases.size(); i++) {

            String alias = aliases.get(i);

            if (!containsWholeWord(
                    normalized,
                    alias)) {

                filtered.add(alias);
            }
        }

        return filtered;
    }

    private void addGroupIfPresent(
            SimpleList<String> aliases,
            String question,
            String... group) {

        boolean present = false;

        for (int i = 0; i < group.length; i++) {

            if (containsWholeWord(
                    question,
                    group[i])) {

                present = true;
                break;
            }
        }

        if (!present) {
            return;
        }

        for (int i = 0; i < group.length; i++) {

            if (!aliases.contains(group[i])) {
                aliases.add(group[i]);
            }
        }
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

            String token =
                    tokens[i].trim();

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

    private boolean containsWholeWord(
            String text,
            String term) {

        if (text == null
                || term == null
                || term.trim().isEmpty()) {

            return false;
        }

        String normalizedText =
                " " + normalize(text) + " ";

        String normalizedTerm =
                normalize(term);

        return normalizedText.contains(
                " " + normalizedTerm + " "
        );
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

        for (int i = 0;
             i < newlyCoveredQuestions.size();
             i++) {

            String question =
                    newlyCoveredQuestions.get(i);

            if (!coveredQuestions.contains(question)) {
                coveredQuestions.add(question);
            }
        }

        SimpleList<String> remaining =
                new SimpleList<>();

        for (int i = 0;
             i < uncoveredQuestions.size();
             i++) {

            String question =
                    uncoveredQuestions.get(i);

            if (!newlyCoveredQuestions.contains(question)) {
                remaining.add(question);
            }
        }

        uncoveredQuestions.clear();

        for (int i = 0;
             i < remaining.size();
             i++) {

            uncoveredQuestions.add(
                    remaining.get(i)
            );
        }
    }

    private static boolean containsDocumentId(
            SimpleList<LegalDocument> documents,
            String documentId) {
        for (int i = 0; i < documents.size(); i++) {
            if (documentId.equals(documents.get(i).getDocumentId())) {
                return true;
            }
        }
        return false;
    }

    private OptimizationResult emptyResult(
            int budget) {

        return new OptimizationResult(
                new SimpleList<LegalDocument>(),
                new SimpleList<String>(),
                new SimpleList<String>(),
                new SimpleList<String>(),
                0,
                budget
        );
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

        for (int i = 0;
             i < source.size();
             i++) {

            clone.add(source.get(i));
        }

        return clone;
    }

    private static class QuestionMatch {

        private final int score;
        private final int titleMatches;

        public QuestionMatch(
                int score,
                int titleMatches) {

            this.score = score;
            this.titleMatches = titleMatches;
        }

        public int getScore() {
            return score;
        }

        public int getTitleMatches() {
            return titleMatches;
        }
    }

    private static class CoverageInfo {

        private final int additionalCoverage;
        private final int titleMatchCount;
        private final int score;
        private final SimpleList<String> coveredQuestions;

        public CoverageInfo(
                int additionalCoverage,
                int titleMatchCount,
                int score,
                SimpleList<String> coveredQuestions) {

            this.additionalCoverage =
                    additionalCoverage;

            this.titleMatchCount =
                    titleMatchCount;

            this.score = score;

            this.coveredQuestions =
                    coveredQuestions;
        }

        public int getAdditionalCoverage() {
            return additionalCoverage;
        }

        public int getTitleMatchCount() {
            return titleMatchCount;
        }

        public int getScore() {
            return score;
        }

        public SimpleList<String> getCoveredQuestions() {
            return coveredQuestions;
        }
    }
}

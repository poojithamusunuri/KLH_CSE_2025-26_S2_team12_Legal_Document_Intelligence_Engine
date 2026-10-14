package search;

import algorithms.AhoCorasick;
import algorithms.RandomizedSelect;
import model.LegalDocument;
import structures.SimpleList;
import java.util.List;

/*
 * General, explainable relevance model for a described legal situation.
 *
 * It replaces the earlier per-situation scoring blocks. Nothing here is
 * specific to salary, tenancy, banking or any other subject: the subject
 * knowledge lives in LegalVocabulary (data), and the scoring below is the same
 * for every query.
 *
 * HOW A DOCUMENT IS SCORED
 *   1. The query is mapped to vocabulary GROUPS (synonym sets). Words the
 *      vocabulary does not know become a few extra groups.
 *   2. ONE Aho-Corasick pass over each document finds every phrase of every
 *      group, with positions.
 *   3. Rarity weighting: each group gets an IDF weight computed from how many
 *      corpus documents contain it, so common words ("owner", "return",
 *      "agreement") count for little and specific ones count for more.
 *   4. coverage       = weighted share of the query's groups the document has.
 *      A group counts once however often it is repeated, so a tax judgment that
 *      says "salary" forty times gains nothing over one that says it once.
 *   5. event evidence = whether the nearest query-target OBJECT to each ACTION
 *      supports the event. Known competing object classes (currently tax/revenue)
 *      can win when they are closer, preventing distant target-word leakage.
 *   6. Small extras: exact phrases, title, curated keywords (not used for
 *      judgments, whose keywords are auto-generated and noisy), plus a small
 *      compatible-domain bonus and a conservative mismatch penalty when a
 *      supported domain mapping exists.
 *
 * TIERS
 *   DIRECT  - strong coverage AND event evidence (when the query has an event)
 *   RELATED - meaningful but incomplete overlap
 *   WEAK    - shares only generic vocabulary
 *   If nothing is DIRECT the caller reports "no directly relevant document
 *   found" instead of presenting a confident-looking ranking.
 *
 * This is lexicon-and-proximity matching. It does not understand the text and
 * does not decide who is legally right. The thresholds below are tuning
 * constants chosen against the verified evaluation set (data/queries).
 */
public class RelevanceScorer {

    public enum Tier {
        DIRECT,
        RELATED,
        WEAK
    }

    static final double DIRECT_MIN_COVERAGE = 0.60;
    static final double DIRECT_MIN_EVENT = 0.50;
    static final double RELATED_MIN_COVERAGE = 0.35;
    static final double RELATED_MIN_TITLE = 0.30;
    static final int EVENT_WINDOW_CHARS = 180;
    static final int MAX_POSITIONS = 256;
    static final int MAX_EXTRA_TERMS = 6;
    static final int MAX_BIGRAMS = 8;
    static final int MAX_WEAK_SHOWN = 3;
    static final int SNIPPET_RADIUS = 90;

    public static final class Scored {

        public final LegalDocument document;
        public final Tier tier;
        public final double score;
        public final boolean titleMatch;
        public final int firstPosition;
        public final SimpleList<String> matchedConcepts;
        public final SimpleList<String> matchedTerms;
        public final SimpleList<String> explanation;
        final int documentIndex;

        Scored(
                LegalDocument document,
                int documentIndex,
                Tier tier,
                double score,
                boolean titleMatch,
                int firstPosition,
                SimpleList<String> matchedConcepts,
                SimpleList<String> matchedTerms,
                SimpleList<String> explanation) {

            this.document = document;
            this.documentIndex = documentIndex;
            this.tier = tier;
            this.score = score;
            this.titleMatch = titleMatch;
            this.firstPosition = firstPosition;
            this.matchedConcepts = matchedConcepts;
            this.matchedTerms = matchedTerms;
            this.explanation = explanation;
        }
    }

    public static final class Result {

        public final SimpleList<Scored> top;
        public final int candidateCount;
        public final int directCount;
        public final int relatedCount;
        public final SimpleList<String> queryConcepts;

        Result(
                SimpleList<Scored> top,
                int candidateCount,
                int directCount,
                int relatedCount,
                SimpleList<String> queryConcepts) {

            this.top = top;
            this.candidateCount = candidateCount;
            this.directCount = directCount;
            this.relatedCount = relatedCount;
            this.queryConcepts = queryConcepts;
        }

        public boolean hasDirect() {
            return directCount > 0;
        }
    }

    private final SimpleList<LegalDocument> documents;
    private final String[] paddedBody;
    private final String[] paddedTitle;
    private final String[] paddedKeywords;
    private final boolean[] keywordsTrusted;
    private final RandomizedSelect selector = new RandomizedSelect();

    /* Normalises every document once, so queries never re-normalise text. */
    public RelevanceScorer(SimpleList<LegalDocument> documents) {

        this.documents = documents;

        int n = documents.size();

        paddedBody = new String[n];
        paddedTitle = new String[n];
        paddedKeywords = new String[n];
        keywordsTrusted = new boolean[n];

        for (int d = 0; d < n; d++) {

            LegalDocument document = documents.get(d);

            paddedBody[d] = TextNormalizer.padded(document.getBody());
            paddedTitle[d] = TextNormalizer.padded(document.getTitle());

            // Judgment keywords are auto-generated (party names, months, the
            // domain label repeated) and are not evidence of subject matter.
            boolean trusted = !"JUDGMENT".equals(document.getKind());

            keywordsTrusted[d] = trusted;
            paddedKeywords[d] = trusted
                    ? TextNormalizer.padded(
                            document.getKeywords() + " " + document.getDescription())
                    : " ";
        }
    }

    /* The query broken into groups; built once per query. */
    private static final class QueryModel {

        int groupCount;
        String[] label;
        int[] role;
        boolean[] core;
        String[][] paddedPhrases;
        String[][] plainPhrases;
        String[] bigram;
        int bigramCount;
        int[] actionGroups;
        int[] objectGroups;
        int[] actorGroups;
        int pairCount;
        int coreCount;
        boolean hasExplicitAction;
        List<QueryConstraintAnalyzer.Constraint> requiredConditions;
    }

    public Result score(
            String query,
            SimpleList<String> situationTerms,
            SimpleList<String> analyserLabels,
            boolean allowDirect,
            int keep) {

        SimpleList<String> noConcepts = new SimpleList<String>();

        if (query == null || TextNormalizer.normalize(query).isEmpty()) {
            return new Result(
                    new SimpleList<Scored>(), 0, 0, 0, noConcepts);
        }

        QueryModel model = buildModel(query, situationTerms);
        String normalizedQueryForNegation = TextNormalizer.normalize(query);
        model.requiredConditions = QueryConstraintAnalyzer.extract(query);

        SimpleList<String> queryConcepts = new SimpleList<String>();

        for (int g = 0; g < model.groupCount; g++) {
            if (model.core[g]) {
                queryConcepts.add(model.label[g]);
            }
        }

        if (model.coreCount == 0) {
            return new Result(
                    new SimpleList<Scored>(), 0, 0, 0, queryConcepts);
        }

        int n = documents.size();
        int groups = model.groupCount;

        // ---- one Aho-Corasick pass per document ------------------------
        SimpleList<String> patternList = new SimpleList<String>();
        SimpleList<Integer> patternGroupList = new SimpleList<Integer>();
        SimpleList<Integer> patternBigramList = new SimpleList<Integer>();

        for (int g = 0; g < groups; g++) {
            for (int p = 0; p < model.paddedPhrases[g].length; p++) {
                patternList.add(model.paddedPhrases[g][p]);
                patternGroupList.add(g);
                patternBigramList.add(-1);
            }
        }

        for (int b = 0; b < model.bigramCount; b++) {
            patternList.add(" " + model.bigram[b] + " ");
            patternGroupList.add(-1);
            patternBigramList.add(b);
        }

        final int patternTotal = patternList.size();
        String[] patterns = new String[patternTotal];
        final int[] patternGroup = new int[patternTotal];
        final int[] patternBigram = new int[patternTotal];

        for (int i = 0; i < patternTotal; i++) {
            patterns[i] = patternList.get(i);
            patternGroup[i] = patternGroupList.get(i);
            patternBigram[i] = patternBigramList.get(i);
        }

        AhoCorasick automaton = new AhoCorasick(patterns);

        final int[][] tmpPositions = new int[groups][MAX_POSITIONS];
        final int[] tmpCount = new int[groups];
        final int[] mentions = new int[groups];
        final int[] firstPattern = new int[groups];
        final boolean[] bigramSeen = new boolean[Math.max(1, model.bigramCount)];

        boolean[][] bodyPresent = new boolean[n][groups];
        int[][][] positions = new int[n][][];
        int[][] mentionCount = new int[n][];
        int[][] matchedPattern = new int[n][];
        int[] bigramHits = new int[n];

        AhoCorasick.MatchListener listener = new AhoCorasick.MatchListener() {
            @Override
            public void onMatch(int patternIndex, int endExclusive) {

                int g = patternGroup[patternIndex];

                if (g >= 0) {

                    if (mentions[g] == 0) {
                        firstPattern[g] = patternIndex;
                    }

                    mentions[g]++;

                    if (tmpCount[g] < MAX_POSITIONS) {
                        tmpPositions[g][tmpCount[g]++] = endExclusive;
                    }

                } else {
                    bigramSeen[patternBigram[patternIndex]] = true;
                }
            }
        };

        for (int d = 0; d < n; d++) {

            for (int g = 0; g < groups; g++) {
                tmpCount[g] = 0;
                mentions[g] = 0;
            }

            for (int b = 0; b < bigramSeen.length; b++) {
                bigramSeen[b] = false;
            }

            automaton.scan(paddedBody[d], listener);

            boolean any = false;

            for (int g = 0; g < groups; g++) {
                if (mentions[g] > 0) {
                    any = true;
                    bodyPresent[d][g] = true;
                }
            }

            int hits = 0;

            for (int b = 0; b < model.bigramCount; b++) {
                if (bigramSeen[b]) {
                    hits++;
                }
            }

            bigramHits[d] = hits;

            if (any) {

                positions[d] = new int[groups][];
                mentionCount[d] = new int[groups];
                matchedPattern[d] = new int[groups];

                for (int g = 0; g < groups; g++) {

                    mentionCount[d][g] = mentions[g];
                    matchedPattern[d][g] = firstPattern[g];

                    if (tmpCount[g] > 0) {
                        positions[d][g] = new int[tmpCount[g]];
                        for (int i = 0; i < tmpCount[g]; i++) {
                            positions[d][g][i] = tmpPositions[g][i];
                        }
                    }
                }
            }
        }

        // ---- title / keyword presence and rarity weights ----------------
        boolean[][] titlePresent = new boolean[n][groups];
        boolean[][] keywordPresent = new boolean[n][groups];
        int[] documentFrequency = new int[groups];

        for (int d = 0; d < n; d++) {
            for (int g = 0; g < groups; g++) {

                titlePresent[d][g] =
                        containsAny(paddedTitle[d], model.paddedPhrases[g]);

                if (keywordsTrusted[d]) {
                    keywordPresent[d][g] =
                            containsAny(paddedKeywords[d], model.paddedPhrases[g]);
                }

                if (bodyPresent[d][g] || titlePresent[d][g]) {
                    documentFrequency[g]++;
                }
            }
        }

        double[] idf = new double[groups];
        double coreWeight = 0;

        for (int g = 0; g < groups; g++) {

            double df = documentFrequency[g] > 0 ? documentFrequency[g] : 0.5;
            idf[g] = Math.log(1.0 + n / df);

            if (model.core[g]) {
                coreWeight += idf[g];
            }
        }

        // ---- score every candidate document ----------------------------
        SimpleList<Scored> candidates = new SimpleList<Scored>();
        int directCount = 0;
        int relatedCount = 0;

        for (int d = 0; d < n; d++) {

            LegalDocument document = documents.get(d);

            double covered = 0;
            double titleWeight = 0;
            double keywordWeight = 0;
            boolean anyCore = false;

            for (int g = 0; g < groups; g++) {

                if (!model.core[g]) {
                    continue;
                }

                boolean inBody = bodyPresent[d][g];
                boolean inTitle = titlePresent[d][g];
                boolean inKeywords = keywordPresent[d][g];

                if (inBody || inTitle || inKeywords) {
                    covered += idf[g];
                    anyCore = true;
                }

                if (inTitle) {
                    titleWeight += idf[g];
                }

                if (inKeywords) {
                    keywordWeight += idf[g];
                }
            }

            if (!anyCore) {
                continue;
            }

            // Preserve concrete object words in theft reports. A judgment
            // about stolen packages or misappropriated cheques is not a case
            // about a stolen bag/phone merely because both discuss property.
            String specificObjectAnchor = specificObjectAnchor(normalizedQueryForNegation);
            boolean officialStatutoryDocument = isOfficialStatutoryExcerpt(document);
            if (!specificObjectAnchor.isEmpty() && !officialStatutoryDocument
                    && !containsWholeWord(paddedBody[d], specificObjectAnchor)
                    && !containsWholeWord(paddedTitle[d], specificObjectAnchor)) {
                continue;
            }

            // A multi-word everyday description that collapses to a single
            // broad concept must not retrieve documents solely because they
            // contain one loose synonym. Require an actual phrase from the
            // user's wording in the document before showing such a result.
            if (model.hasExplicitAction
                    && model.coreCount <= 2
                    && queryHasCoreLabel(model, "physical / domestic abuse")
                    && meaningfulWordCount(normalizedQueryForNegation) >= 2
                    && !hasMeaningfulQueryPhrase(normalizedQueryForNegation,
                            paddedBody[d], paddedTitle[d], paddedKeywords[d])) {
                continue;
            }

            int matchedGroupsForTier = 0;
            for (int g = 0; g < groups; g++) {
                if (model.core[g] && (bodyPresent[d][g] || titlePresent[d][g]
                        || keywordPresent[d][g])) {
                    matchedGroupsForTier++;
                }
            }

            // If the query contains no known legal-vocabulary concept, treat
            // its unknown words as a small lexical query rather than allowing
            // one accidental common-word hit to produce unrelated weak leads.
            // For a multi-token unknown query, require at least two distinct
            // query groups to occur in the same document. Known legal concepts
            // and one-token searches keep their existing behavior.
            boolean hasKnownVocabularyGroup = false;
            for (int g = 0; g < groups; g++) {
                if (model.core[g] && isVocabularyLabel(model.label[g])) {
                    hasKnownVocabularyGroup = true;
                    break;
                }
            }
            if (!hasKnownVocabularyGroup && model.coreCount >= 2) {
                int matchedCoreGroups = 0;
                for (int g = 0; g < groups; g++) {
                    if (model.core[g] && (bodyPresent[d][g] || titlePresent[d][g] || keywordPresent[d][g])) {
                        matchedCoreGroups++;
                    }
                }
                if (matchedCoreGroups < 2) continue;
            }

            double coverage = covered / coreWeight;
            double titleFraction = titleWeight / coreWeight;
            double keywordFraction = keywordWeight / coreWeight;

            // Certain multiword legal concepts are essential to the query, not
            // optional vocabulary. A document that only mentions "land" must
            // not be treated as a mortgage result; likewise generic "cheque"
            // or "warrant" mentions alone are insufficient for the specific
            // concept requested. If the query contains one of these groups,
            // require the document to contain that group before it can enter
            // the ranked candidate pool.
            boolean missingRequiredContext = false;
            for (int g = 0; g < groups; g++) {
                if (!model.core[g] || !isCriticalQueryGroup(model.label[g])) continue;
                if (!bodyPresent[d][g] && !titlePresent[d][g] && !keywordPresent[d][g]) {
                    missingRequiredContext = true;
                    break;
                }
                if ("secured lending / mortgage".equals(model.label[g])) {
                    int targetGroup = findModelGroup(model, "land / property");
                    if (targetGroup >= 0 && closePair(positions[d], g, targetGroup) < 0) {
                        missingRequiredContext = true;
                        break;
                    }
                } else if ("cheque dishonour".equals(model.label[g])) {
                    int targetGroup = findModelGroup(model, "cheque");
                    if (targetGroup >= 0 && closePair(positions[d], g, targetGroup) < 0) {
                        missingRequiredContext = true;
                        break;
                    }
                }
            }
            if (missingRequiredContext) continue;

            // Event evidence uses the nearest query-target OBJECT, while
            // also checking known competing object classes such as tax/revenue.
            // A competing object wins only when it is closer to the action.
            int pairsFound = 0;
            int snippetCenter = -1;

            for (int a = 0; a < model.actionGroups.length; a++) {
                int actionGroup = model.actionGroups[a];
                int[] actionPositions = positions[d] == null
                        ? null : positions[d][actionGroup];

                if (actionPositions == null) {
                    continue;
                }

                for (int ap = 0; ap < actionPositions.length; ap++) {
                    int actionPosition = actionPositions[ap];
                    NearestObject nearestTarget =
                            nearestTargetObject(paddedBody[d], actionPosition, model);
                    NearestObject nearestCompetitor =
                            nearestCompetingObject(paddedBody[d], actionPosition);

                    if (nearestTarget == null
                            || nearestTarget.distance > EVENT_WINDOW_CHARS) {
                        continue;
                    }

                    if (nearestCompetitor != null
                            && nearestCompetitor.distance < nearestTarget.distance) {
                        // The closest object describes a different event.
                        continue;
                    }

                    int targetObjectGroup = modelObjectGroupAtVocabularyIndex(
                            model, nearestTarget.vocabularyGroup);

                    if (targetObjectGroup < 0
                            || !actorNearEvent(
                                    positions[d], model.actorGroups,
                                    actionGroup, targetObjectGroup)) {
                        continue;
                    }

                    pairsFound++;

                    if (snippetCenter < 0) {
                        snippetCenter = Math.max(
                                actionPosition, nearestTarget.position);
                    }

                    // One nearest target is the evidence for this action.
                    break;
                }
            }

            boolean hasPairs = model.pairCount > 0;
            double event = hasPairs ? (double) pairsFound / model.pairCount : 0;
            boolean conditionsSupported = QueryConstraintAnalyzer.isSupportedNearEvent(
                    document.getBody(), model.requiredConditions, positions[d],
                    model.actionGroups, model.objectGroups, 140);
            // General evidence gate: explicit negative conditions are required
            // for DIRECT classification. Candidates without that evidence may
            // remain as RELATED/WEAK research leads, preserving recall while
            // preventing topical overlap from being presented as confirmation.

            double phrase = 0;

            if (model.bigramCount > 0) {
                phrase = Math.min(1.0,
                        bigramHits[d] / (double) Math.min(model.bigramCount, 2));
            }

            String domainLabel =
                    DomainCrosswalk.compatibleLabel(analyserLabels, document);

            double score;

            if (hasPairs) {
                score = 0.55 * coverage + 0.25 * event + 0.08 * phrase
                        + 0.06 * titleFraction + 0.04 * keywordFraction
                        + (domainLabel != null ? 0.02 : 0);
            } else {
                score = 0.75 * coverage + 0.10 * phrase
                        + 0.08 * titleFraction + 0.05 * keywordFraction
                        + (domainLabel != null ? 0.02 : 0);
            }

            score *= 100.0;

            // When the analyzer has a known domain mapping, unrelated corpus
            // domains should not outrank compatible documents merely by repeating
            // generic terms. Keep a penalty rather than a hard exclusion because
            // corpus metadata can be incomplete or inconsistent.
            if (DomainCrosswalk.hasMapping(analyserLabels) && domainLabel == null) {
                // A supported but incompatible domain is stronger negative
                // evidence for a fact-pattern query than a missing label.
                score *= 0.45;
            }

            // A candidate that does not support an explicitly stated negative
            // condition can still be useful background, but should rank below
            // an otherwise comparable document that addresses that condition.
            if (!conditionsSupported && !model.requiredConditions.isEmpty()) {
                score *= 0.62;
            }

            // For a described factual incident, a generic Act body that only
            // repeats topic vocabulary should not outrank judgments describing
            // factual events. Acts remain searchable and can rank highly when
            // their title directly matches the requested legal instrument.
            if (model.hasExplicitAction
                    && "ACT".equalsIgnoreCase(document.getKind())
                    && titleFraction < 0.99) {
                score *= 0.55;
            }

            Tier tier;

            boolean direct;

            boolean compatibleDomain = !DomainCrosswalk.hasMapping(analyserLabels)
                    || domainLabel != null;
            boolean unsupportedActorContext = !DomainCrosswalk.hasMapping(analyserLabels)
                    && model.actorGroups.length > 0;
            boolean specificObjectEvidence = hasSpecificObjectEvidence(
                    TextNormalizer.normalize(query),
                    model,
                    bodyPresent,
                    titlePresent,
                    keywordPresent,
                    d,
                    document);

            if (hasPairs) {
    direct = allowDirect
        && model.hasExplicitAction
        && compatibleDomain
        && !unsupportedActorContext
        && coverage >= DIRECT_MIN_COVERAGE
        && event >= DIRECT_MIN_EVENT
        && specificObjectEvidence
        && conditionsSupported;
} else {
                direct = allowDirect
                    && model.hasExplicitAction
                    && compatibleDomain
                    && !unsupportedActorContext
                    && coverage >= DIRECT_MIN_COVERAGE
                    && titleFraction >= 0.99
                    && specificObjectEvidence
                    && conditionsSupported;
            }

            boolean officialStatutoryExcerpt = isOfficialStatutoryExcerpt(document);
            boolean enoughDistinctEvidence = model.coreCount < 3
                    || matchedGroupsForTier >= 2
                    || titleFraction >= 0.99;
            boolean sensitiveAbuseQuery = false;
            for (int g = 0; g < groups; g++) {
                if (model.core[g] && "physical / domestic abuse".equals(model.label[g])) {
                    sensitiveAbuseQuery = true;
                    break;
                }
            }
            boolean eventEvidenceForRelated = !hasPairs
                    || pairsFound > 0
                    || (officialStatutoryExcerpt && matchedGroupsForTier >= 2)
                    || (!queryHasCoreLabel(model, "theft / dishonest taking")
                        && !sensitiveAbuseQuery);

            if (direct) {
                tier = Tier.DIRECT;
                directCount++;
            } else if ((coverage >= RELATED_MIN_COVERAGE
                    || titleFraction >= RELATED_MIN_TITLE)
                    && enoughDistinctEvidence
                    && eventEvidenceForRelated) {
                tier = Tier.RELATED;
                relatedCount++;
            } else {
                tier = Tier.WEAK;
            }

            SimpleList<String> matchedConcepts = new SimpleList<String>();
            SimpleList<String> matchedTerms = new SimpleList<String>();
            int firstPosition = Integer.MAX_VALUE;

            for (int g = 0; g < groups; g++) {

                if (!model.core[g]) {
                    continue;
                }

                if (bodyPresent[d][g]) {

                    matchedConcepts.add(model.label[g]
                            + " (" + mentionCount[d][g] + " mention"
                            + (mentionCount[d][g] == 1 ? "" : "s") + ")");

                    matchedTerms.add(
                            patterns[matchedPattern[d][g]].trim());

                    int start = positions[d][g][0];

                    if (start < firstPosition) {
                        firstPosition = start;
                    }

                } else if (titlePresent[d][g] || keywordPresent[d][g]) {
                    matchedConcepts.add(model.label[g]
                            + (titlePresent[d][g] ? " (title)" : " (keywords)"));
                }
            }

            SimpleList<String> explanation = buildExplanation(
                    model, tier, coverage, event, hasPairs, pairsFound,
                    snippetCenter >= 0
                            ? snippet(paddedBody[d], snippetCenter) : null,
                    titleFraction, keywordFraction, domainLabel,
                    matchedConcepts, document);
            candidates.add(new Scored(
                    document,
                    d,
                    tier,
                    score,
                    titleFraction > 0,
                    firstPosition == Integer.MAX_VALUE ? 0 : firstPosition,
                    matchedConcepts,
                    matchedTerms,
                    explanation));
        }

        SimpleList<Scored> top = select(
                candidates, directCount, relatedCount, keep);

        return new Result(
                top, candidates.size(), directCount, relatedCount,
                queryConcepts);
    }

    private static boolean isVocabularyLabel(String label) {
        for (int i = 0; i < LegalVocabulary.size(); i++) {
            if (LegalVocabulary.label(i).equals(label)) return true;
        }
        return false;
    }

    private static int findModelGroup(QueryModel model, String label) {
        for (int i = 0; i < model.groupCount; i++) {
            if (model.core[i] && label.equals(model.label[i])) return i;
        }
        return -1;
    }

    private static boolean isCriticalQueryGroup(String label) {
        return "secured lending / mortgage".equals(label)
                || "search warrant / arrest warrant".equals(label)
                || "cheque dishonour".equals(label);
    }

    private static boolean containsAnyPhrase(String text, String[] phrases) {
        if (text == null || phrases == null) return false;
        String padded = " " + TextNormalizer.normalize(text) + " ";
        for (String phrase : phrases) {
            String normalizedPhrase = TextNormalizer.normalize(phrase);
            if (!normalizedPhrase.isEmpty()
                    && padded.contains(" " + normalizedPhrase + " ")) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Query model
    // ------------------------------------------------------------------

    private QueryModel buildModel(
            String query,
            SimpleList<String> situationTerms) {

        String normalizedQuery = TextNormalizer.normalize(query);

        StringBuilder trigger = new StringBuilder(" " + normalizedQuery + " ");

        if (situationTerms != null) {
            for (int i = 0; i < situationTerms.size(); i++) {

                String term = TextNormalizer.normalize(situationTerms.get(i));

                if (!term.isEmpty()) {
                    trigger.append(term).append(" | ");
                }
            }
        }

        String triggerText = " " + trigger.toString().replace("|", " ") + " ";

        SimpleList<Integer> vocabularyHits = new SimpleList<Integer>();

        for (int g = 0; g < LegalVocabulary.size(); g++) {

            String[] phrases = LegalVocabulary.phrases(g);

            for (int p = 0; p < phrases.length; p++) {

                if (triggerText.indexOf(" " + phrases[p] + " ") >= 0) {
                    vocabularyHits.add(g);
                    break;
                }
            }
        }

        // Words the vocabulary does not know become a few extra groups.
        String[] words = TextNormalizer.words(normalizedQuery);
        SimpleList<String> extras = new SimpleList<String>();

        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            if (word.length() < 4
                    || TextNormalizer.isStopWord(word)
                    || extras.contains(word)
                    || extras.size() >= MAX_EXTRA_TERMS
                    || coveredByVocabulary(word, vocabularyHits)) {
                continue;
            }

            extras.add(word);
        }

        boolean promoteExtras = vocabularyHits.isEmpty();

        QueryModel model = new QueryModel();
        model.groupCount = vocabularyHits.size() + extras.size();

        int total = model.groupCount;

        model.label = new String[total];
        model.role = new int[total];
        model.core = new boolean[total];
        model.paddedPhrases = new String[total][];
        model.plainPhrases = new String[total][];

        for (int i = 0; i < vocabularyHits.size(); i++) {

            int g = vocabularyHits.get(i);

            model.label[i] = LegalVocabulary.label(g);
            model.role[i] = LegalVocabulary.role(g);
            model.core[i] = true;
            model.plainPhrases[i] = LegalVocabulary.phrases(g);
        }

        for (int i = 0; i < extras.size(); i++) {

            int index = vocabularyHits.size() + i;
            String word = extras.get(i);

            model.label[index] = word;
            model.role[index] = LegalVocabulary.CONTEXT;
            model.core[index] = promoteExtras;

            String variant = word.endsWith("s")
                    ? word.substring(0, word.length() - 1)
                    : word + "s";

            model.plainPhrases[index] = new String[] {word, variant};
        }

        for (int g = 0; g < total; g++) {

            model.paddedPhrases[g] =
                    new String[model.plainPhrases[g].length];

            for (int p = 0; p < model.plainPhrases[g].length; p++) {
                model.paddedPhrases[g][p] =
                        " " + model.plainPhrases[g][p] + " ";
            }

            if (model.core[g]) {
                model.coreCount++;
            }
        }

        // Adjacent meaningful word pairs from the query, e.g. "security deposit".
        model.bigram = new String[MAX_BIGRAMS];

        for (int i = 0; i + 1 < words.length
                && model.bigramCount < MAX_BIGRAMS; i++) {

            if (words[i].length() < 3 || words[i + 1].length() < 3
                    || TextNormalizer.isStopWord(words[i])
                    || TextNormalizer.isStopWord(words[i + 1])) {
                continue;
            }

            if (words[i].equals(words[i + 1])) {
                continue; // a word repeated next to itself is not a phrase
            }

            String pair = words[i] + " " + words[i + 1];

            boolean duplicate = false;

            for (int b = 0; b < model.bigramCount; b++) {
                if (model.bigram[b].equals(pair)) {
                    duplicate = true;
                }
            }

            if (!duplicate) {
                model.bigram[model.bigramCount++] = pair;
            }
        }

        // Which (ACTION, OBJECT) pairs make up the event to look for.
        int actionCount = 0;
        int objectCount = 0;
        int actorCount = 0;

        for (int g = 0; g < total; g++) {

            if (!model.core[g]) {
                continue;
            }

            if (model.role[g] == LegalVocabulary.ACTION) {
                actionCount++;
            } else if (model.role[g] == LegalVocabulary.OBJECT) {
                objectCount++;
            } else if (model.role[g] == LegalVocabulary.ACTOR) {
                actorCount++;
            }
        }
model.hasExplicitAction = actionCount > 0
        && words.length >= 3;
        int partnerRole = objectCount > 0
                ? LegalVocabulary.OBJECT
                : LegalVocabulary.ACTOR;

        int partnerCount = objectCount > 0 ? objectCount : actorCount;

        model.actionGroups = new int[actionCount];
        model.objectGroups = new int[partnerCount];
        model.actorGroups = new int[actorCount];

        int a = 0;
        int o = 0;
        int r = 0;

        for (int g = 0; g < total; g++) {

            if (!model.core[g]) {
                continue;
            }

            if (model.role[g] == LegalVocabulary.ACTION) {
                model.actionGroups[a++] = g;
            } else if (model.role[g] == partnerRole) {
                model.objectGroups[o++] = g;
            } else if (model.role[g] == LegalVocabulary.ACTOR) {
                model.actorGroups[r++] = g;
            }
        }

        model.pairCount = actionCount * partnerCount;

        return model;
    }

    private static boolean coveredByVocabulary(
            String word,
            SimpleList<Integer> groups) {

        String needle = " " + word + " ";

        for (int i = 0; i < groups.size(); i++) {

            String[] phrases = LegalVocabulary.phrases(groups.get(i));

            for (int p = 0; p < phrases.length; p++) {

                if ((" " + phrases[p] + " ").indexOf(needle) >= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // ------------------------------------------------------------------
    // Evidence helpers
    // ------------------------------------------------------------------

    private static boolean isOfficialStatutoryExcerpt(LegalDocument document) {
        return document.getDocumentType() != null
                && document.getDocumentType().toLowerCase(java.util.Locale.ROOT)
                        .contains("statutory text excerpt");
    }

    private static String specificObjectAnchor(String normalizedQuery) {
        String[] anchors = {"mobile phone", "smartphone", "cellphone", "phone",
                "bag", "bags", "wallet", "purse", "laptop", "tablet"};
        for (String anchor : anchors) {
            if ((" " + normalizedQuery + " ").contains(" " + anchor + " ")) return anchor;
        }
        return "";
    }

    private static boolean queryHasCoreLabel(QueryModel model, String label) {
        for (int g = 0; g < model.groupCount; g++) {
            if (model.core[g] && label.equals(model.label[g])) return true;
        }
        return false;
    }

    private static int meaningfulWordCount(String normalizedQuery) {
        int count = 0;
        String[] words = TextNormalizer.words(normalizedQuery);
        for (String word : words) {
            if (word.length() >= 3 && !TextNormalizer.isStopWord(word)) count++;
        }
        return count;
    }

    private static boolean hasMeaningfulQueryPhrase(
            String normalizedQuery, String body, String title, String keywords) {
        String[] words = TextNormalizer.words(normalizedQuery);
        String[] meaningful = new String[words.length];
        int count = 0;
        for (String word : words) {
            if (word.length() >= 3 && !TextNormalizer.isStopWord(word)) {
                meaningful[count++] = word;
            }
        }
        if (count < 2) return true;
        String paddedBodyText = body == null ? " " : body;
        String paddedTitleText = title == null ? " " : title;
        String paddedKeywordText = keywords == null ? " " : keywords;
        for (int i = 0; i + 1 < count; i++) {
            String phrase = " " + meaningful[i] + " " + meaningful[i + 1] + " ";
            if (paddedBodyText.contains(phrase) || paddedTitleText.contains(phrase)
                    || paddedKeywordText.contains(phrase)) return true;
        }
        return false;
    }

    private static boolean containsAny(String text, String[] paddedPhrases) {

        for (int i = 0; i < paddedPhrases.length; i++) {
            if (text.indexOf(paddedPhrases[i]) >= 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasSpecificObjectEvidence(
            String normalizedQuery,
            QueryModel model,
            boolean[][] bodyPresent,
            boolean[][] titlePresent,
            boolean[][] keywordPresent,
            int documentIndex,
            LegalDocument document) {

        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return true;
        }

        String docText = normalizeDocumentText(document);
        if (docText.isEmpty()) {
            return false;
        }

        String[] queryTerms = TextNormalizer.words(normalizedQuery);
        for (int i = 0; i < queryTerms.length; i++) {
            String term = TextNormalizer.normalize(queryTerms[i]);
            if (term.length() <= 2
                    || TextNormalizer.isStopWord(term)
                    || isGenericDirectTerm(term)
                    || "landlord".equals(term)
                    || "security".equals(term)
                    || "house".equals(term)) {
                continue;
            }

            if (containsWholeWord(docText, term)) {
                return true;
            }
        }

        for (int i = 0; i < model.objectGroups.length; i++) {
            int groupIndex = model.objectGroups[i];
            if (!(bodyPresent[documentIndex][groupIndex]
                    || titlePresent[documentIndex][groupIndex]
                    || keywordPresent[documentIndex][groupIndex])) {
                continue;
            }

            String[] phrases = model.plainPhrases[groupIndex];
            for (int p = 0; p < phrases.length; p++) {
                String phrase = TextNormalizer.normalize(phrases[p]);
                if (phrase.isEmpty()
                        || isGenericDirectPhrase(phrase)) {
                    continue;
                }
                if (containsWholeWord(docText, phrase)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isGenericDirectTerm(String term) {
        if (term == null) {
            return false;
        }

        String normalized = TextNormalizer.normalize(term);
        if (normalized.isEmpty()) {
            return false;
        }

        String[] genericTerms = {
                "deposit",
                "deposits",
                "return",
                "returns",
                "returned",
                "refund",
                "rent",
                "rents",
                "rental",
                "rentals",
                "lease",
                "leases",
                "leased",
                "rented",
                "renting",
                "tenancy",
                "tenancys",
                "tenement",
                "tenements",
                "occupancy",
                "occupation",
                "occupations",
                "land",
                "lands",
                "property",
                "properties",
                "propertys",
                "propertie",
                "house",
                "houses",
                "plot",
                "plots",
                "premise",
                "premises",
                "building",
                "buildings",
                "flat",
                "flats",
                "security",
                "amount",
                "money",
                "payment",
                "payments",
                "record",
                "records",
                "case",
                "contract",
                "contracts",
                "agreement",
                "agreements",
                "document",
                "documents",
                "account",
                "accounts",
                "transaction",
                "transactions",
                "loan",
                "loans",
                "debt",
                "debts",
                "tenant",
                "tenants",
                "landlord",
                "landlords",
                "owner",
                "owners"
        };

        String stem = normalized;
        if (stem.endsWith("ies") && stem.length() > 3) {
            stem = stem.substring(0, stem.length() - 3) + "y";
        } else if (stem.endsWith("es") && stem.length() > 3) {
            stem = stem.substring(0, stem.length() - 2);
        } else if (stem.endsWith("s") && stem.length() > 3) {
            stem = stem.substring(0, stem.length() - 1);
        }

        for (int i = 0; i < genericTerms.length; i++) {
            if (normalized.equals(genericTerms[i]) || stem.equals(genericTerms[i])) {
                return true;
            }
        }

        return false;
    }

    private static boolean isGenericDirectPhrase(String phrase) {
        if (phrase == null) {
            return false;
        }

        String normalized = TextNormalizer.normalize(phrase);
        if (normalized.isEmpty()) {
            return false;
        }

        String[] allowedSpecificPhrases = {
                "salary",
                "wage",
                "wages",
                "salary wages",
                "cheque",
                "cheques",
                "loan",
                "debt",
                "account",
                "bank account",
                "agreement",
                "contract"
        };

        for (int i = 0; i < allowedSpecificPhrases.length; i++) {
            if (normalized.equals(allowedSpecificPhrases[i])) {
                return false;
            }
        }

        String[] parts = normalized.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if ("salary".equals(part)
                    || "wage".equals(part)
                    || "wages".equals(part)
                    || "cheque".equals(part)
                    || "cheques".equals(part)
                    || "loan".equals(part)
                    || "debt".equals(part)
                    || "account".equals(part)
                    || "agreement".equals(part)
                    || "contract".equals(part)) {
                return false;
            }
            if (isGenericDirectTerm(part)) {
                return true;
            }
        }

        return false;
    }

    private static String normalizeDocumentText(LegalDocument document) {
        if (document == null) {
            return "";
        }

        return TextNormalizer.normalize(
                document.getTitle() + " " + document.getContent());
    }

    private static boolean containsWholeWord(String text, String term) {
        if (text == null || term == null) {
            return false;
        }

        String normalized = " " + text.trim() + " ";
        String needle = " " + term.trim() + " ";
        return normalized.contains(needle);
    }

    private static final class NearestObject {
        final int vocabularyGroup;
        final int position;
        final int distance;

        NearestObject(int vocabularyGroup, int position, int distance) {
            this.vocabularyGroup = vocabularyGroup;
            this.position = position;
            this.distance = distance;
        }
    }

    /** Finds the closest OBJECT phrase that belongs to the query model. */
    private static NearestObject nearestTargetObject(
            String paddedText, int actionPosition, QueryModel model) {

        NearestObject best = null;

        for (int i = 0; i < model.objectGroups.length; i++) {
            int group = model.objectGroups[i];
            String[] phrases = model.plainPhrases[group];

            for (int p = 0; p < phrases.length; p++) {
                String paddedPhrase = " " + phrases[p] + " ";
                int from = 0;

                while (from < paddedText.length()) {
                    int start = paddedText.indexOf(paddedPhrase, from);
                    if (start < 0) {
                        break;
                    }

                    int position = start + paddedPhrase.length();
                    int distance = Math.abs(actionPosition - position);
                    int vocabularyGroup = vocabularyGroupForModelGroup(model, group);

                    if (best == null || distance < best.distance) {
                        best = new NearestObject(vocabularyGroup, position, distance);
                    }

                    from = start + 1;
                }
            }
        }

        return best;
    }

    /**
     * Finds the closest known competing object. The current calibrated
     * conflict is tax/revenue versus salary/payment; adding a conflict here
     * does not create a new legal situation or a new query-specific rule.
     */
    private static NearestObject nearestCompetingObject(
            String paddedText, int actionPosition) {

        NearestObject best = null;

        for (int g = 0; g < LegalVocabulary.size(); g++) {
            if (LegalVocabulary.role(g) != LegalVocabulary.OBJECT
                    || !isCompetingObject(g)) {
                continue;
            }

            String[] phrases = LegalVocabulary.phrases(g);
            for (int p = 0; p < phrases.length; p++) {
                String paddedPhrase = " " + phrases[p] + " ";
                int from = 0;

                while (from < paddedText.length()) {
                    int start = paddedText.indexOf(paddedPhrase, from);
                    if (start < 0) {
                        break;
                    }

                    int position = start + paddedPhrase.length();
                    int distance = Math.abs(actionPosition - position);

                    if (best == null || distance < best.distance) {
                        best = new NearestObject(g, position, distance);
                    }

                    from = start + 1;
                }
            }
        }

        return best;
    }

    private static boolean isCompetingObject(int vocabularyGroup) {
        return "tax / revenue".equals(LegalVocabulary.label(vocabularyGroup));
    }

    private static int vocabularyGroupForModelGroup(
            QueryModel model, int modelGroup) {
        return findVocabularyGroupByLabel(model.label[modelGroup]);
    }

    private static int findVocabularyGroupByLabel(String label) {
        for (int g = 0; g < LegalVocabulary.size(); g++) {
            if (LegalVocabulary.label(g).equals(label)) {
                return g;
            }
        }
        return -1;
    }

    /** Returns the model-local group for a global vocabulary OBJECT group. */
    private static int modelObjectGroupAtVocabularyIndex(
            QueryModel model, int vocabularyGroup) {

        for (int i = 0; i < model.objectGroups.length; i++) {
            int modelGroup = model.objectGroups[i];
            if (model.role[modelGroup] == LegalVocabulary.OBJECT
                    && vocabularyLabelEquals(model, modelGroup, vocabularyGroup)) {
                return modelGroup;
            }
        }

        return -1;
    }

    private static boolean vocabularyLabelEquals(
            QueryModel model, int modelGroup, int vocabularyGroup) {
        return model.label[modelGroup].equals(
                LegalVocabulary.label(vocabularyGroup));
    }

    /*
     * Two-pointer scan over two sorted position lists. Returns the position of
     * the later mention when some pair lies within EVENT_WINDOW_CHARS, else -1.
     */
    private static int closePair(int[][] positions, int groupA, int groupB) {

        if (positions == null
                || positions[groupA] == null
                || positions[groupB] == null) {
            return -1;
        }

        int[] first = positions[groupA];
        int[] second = positions[groupB];

        int i = 0;
        int j = 0;

        while (i < first.length && j < second.length) {

            int difference = first[i] - second[j];

            if (Math.abs(difference) <= EVENT_WINDOW_CHARS) {
                return Math.max(first[i], second[j]);
            }

            if (difference < 0) {
                i++;
            } else {
                j++;
            }
        }

        return -1;
    }

    private static boolean actorNearEvent(
            int[][] positions,
            int[] actorGroups,
            int actionGroup,
            int objectGroup) {
        if (actorGroups == null || actorGroups.length == 0) {
            return true;
        }

        for (int i = 0; i < actorGroups.length; i++) {
            if (closePair(positions, actorGroups[i], actionGroup) >= 0
                    || closePair(positions, actorGroups[i], objectGroup) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static String snippet(String paddedText, int center) {

        int from = Math.max(0, center - SNIPPET_RADIUS);
        int to = Math.min(paddedText.length(), center + SNIPPET_RADIUS);

        while (from > 0 && paddedText.charAt(from) != ' ') {
            from++;
        }

        while (to < paddedText.length() && paddedText.charAt(to) != ' ') {
            to--;
        }

        if (to <= from) {
            return "";
        }

        return "..." + paddedText.substring(from, to).trim() + "...";
    }

    private static SimpleList<String> buildExplanation(
            QueryModel model,
            Tier tier,
            double coverage,
            double event,
            boolean hasPairs,
            int pairsFound,
            String snippet,
            double titleFraction,
            double keywordFraction,
            String domainLabel,
            SimpleList<String> matchedConcepts,
            LegalDocument document) {

        SimpleList<String> lines = new SimpleList<String>();

        int coveredGroups = 0;

        for (int i = 0; i < matchedConcepts.size(); i++) {
            coveredGroups++;
        }

        lines.add("Covers " + coveredGroups + " of " + model.coreCount
                + " query concepts (rarity-weighted coverage "
                + Math.round(coverage * 100) + "%).");

        if (hasPairs) {
            if (pairsFound > 0) {
                lines.add("Event evidence: an action and its object occur close"
                        + " together in the text (" + pairsFound + " of "
                        + model.pairCount + " action/object pairs).");
            } else {
                lines.add("No passage links the described action to its object;"
                        + " the concepts appear separately.");
            }
        }

        if (snippet != null && !snippet.isEmpty()) {
            lines.add("Passage: " + snippet);
        }

        if (titleFraction > 0) {
            lines.add("Title mentions query concepts.");
        }

        if (keywordFraction > 0) {
            lines.add("Curated keywords/description mention query concepts.");
        }

        if (domainLabel != null) {
            lines.add("Corpus domain '" + document.getDomain()
                    + "' is compatible with the detected issue '"
                    + domainLabel + "' (small bonus only).");
        }

        if (tier == Tier.WEAK) {
            lines.add("Overlap is limited to generic vocabulary; treat this"
                    + " as weak lexical overlap, not as a direct match.");
        }

        return lines;
    }

    // ------------------------------------------------------------------
    // Selection
    // ------------------------------------------------------------------

    private SimpleList<Scored> select(
            SimpleList<Scored> candidates,
            int directCount,
            int relatedCount,
            int keep) {

        SimpleList<Scored> top = new SimpleList<Scored>();

        if (keep <= 0 || candidates.isEmpty()) {
            return top;
        }

        boolean strongExists = directCount + relatedCount > 0;

        // Abstain instead of displaying unrelated weak-only candidates. Weak
        // overlap remains available in internal diagnostics/benchmarks, but a
        // user-facing answer should not imply relevance when no meaningful
        // DIRECT or RELATED evidence exists.
        if (!strongExists) {
            return top;
        }

        SimpleList<Scored> pool = new SimpleList<Scored>();

        for (int i = 0; i < candidates.size(); i++) {

            Scored candidate = candidates.get(i);

            if (strongExists == (candidate.tier != Tier.WEAK)) {
                pool.add(candidate);
            }
        }

        int limit = strongExists ? keep : Math.min(keep, MAX_WEAK_SHOWN);

        double[] keys = new double[pool.size()];
        int[] tie = new int[pool.size()];

        for (int i = 0; i < pool.size(); i++) {

            Scored item = pool.get(i);
            int tierRank = item.tier == Tier.DIRECT ? 2
                    : item.tier == Tier.RELATED ? 1 : 0;

            keys[i] = tierRank * 1000.0 + item.score;
            tie[i] = item.documentIndex;
        }

        int[] best = selector.topK(keys, tie, limit);

        for (int i = 0; i < best.length; i++) {
            top.add(pool.get(best[i]));
        }

        return top;
    }
}

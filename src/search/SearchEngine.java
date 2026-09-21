package search;

import structures.SimpleList;

import analysis.LegalProblemAnalyzer;

import algorithms.KMP;
import algorithms.RabinKarp;
import algorithms.ZAlgorithm;

import model.LegalDocument;

public class SearchEngine {

    public static final int DEFAULT_TOP_K = 10;

    private static final String[] STOP_WORDS = {
            "a",
            "an",
            "the",
            "my",
            "me",
            "i",
            "is",
            "are",
            "was",
            "were",
            "has",
            "have",
            "had",
            "to",
            "of",
            "in",
            "on",
            "for",
            "and",
            "or",
            "but",
            "that",
            "this",
            "it",
            "them",
            "they",
            "their",
            "with",
            "from",
            "as",
            "what",
            "who",
            "how",
            "why",
            "when",
            "where",
            "want",
            "know",
            "not",
            "someone",
            "claiming",
            "claims",
            "says",
            "said",
            "belong",
            "belongs",
            "belonged",
            "other",
            "party",
            "three",
            "months",
            "legal",
            "issues",
            "issue",
            "arise",
            "arises",
            "follow",
            "refusing",
            "refuse",
            "signed",
            "bought",
            "our",
            "your",
            "their",
            "mine",
            "person",
            "people",
            "until",
            "after",
            "before",
            "because",
            "while",
            "about",
            "under",
            "over",
            "against",
            "across",
            "around",
            "through",
            "without",
            "within",
            "between",
            "during",
            "into",
            "throughout"
    };

    private static final String[] LEGAL_CONCEPT_NAMES = {
            "Employment",
            "Salary",
            "Agreement",
            "Contract",
            "Breach of Contract",
            "Specific Performance",
            "Consumer",
            "Goods / Services",
            "Compensation",
            "Damages",
            "Property",
            "Ownership",
            "Debt / Money Recovery",
            "Motor Vehicle",
            "Accident",
            "Negligence",
            "Tenancy",
            "Family / Inheritance",
            "Intellectual Property"
    };

    private static final String[][] LEGAL_CONCEPT_KEYWORDS = {
            {"employer", "employee", "employment", "workplace", "job", "work", "company", "boss", "manager", "termination", "terminated", "dismissed", "fired", "sacked", "withhold", "withheld", "unpaid"},
            {"salary", "wage", "wages", "pay", "paid", "payment", "payments", "remuneration", "income", "earning", "earnings", "dues", "unpaid", "non payment", "non-payment"},
            {"agreement", "deal", "arrangement", "understanding", "undertaking", "promise", "promised", "signed", "executed", "parties", "terms"},
            {"contract", "agreement", "deal", "obligation", "obligations", "promise", "promised", "contractual", "undertaking", "terms", "non performance", "non-performance"},
            {"breach", "breached", "breach of contract", "refuse", "refusing", "refused", "non-performance", "non performance", "violate", "violated", "default", "defaulted", "honour", "honor", "breach of agreement"},
            {"specific performance", "performance", "perform", "enforce", "enforcement", "obligation", "obligations", "honour", "honor", "follow", "followed"},
            {"consumer", "customer", "buyer", "shopper", "purchaser", "retailer", "seller", "shop", "store", "purchase", "purchased", "bought", "buy"},
            {"goods", "services", "product", "products", "item", "mobile", "phone", "device", "faulty", "defective", "damaged", "repair", "replacement", "exchange", "warranty"},
            {"compensation", "refund", "reimburse", "reimbursement", "replace", "replacement", "exchange", "payback", "settlement", "remedy"},
            {"damages", "loss", "injury", "liability", "compensation", "claims", "settlement", "damage", "damaged"},
            {"property", "house", "home", "land", "plot", "estate", "real estate", "ownership", "possession", "title", "claim", "claimed", "occupy", "occupied", "transfer", "sale"},
            {"ownership", "owner", "title", "belongs", "belong", "belonged", "possession", "rights", "claim", "claimed", "own", "owned"},
            {"money", "amount", "funds", "loan", "debt", "borrow", "borrowed", "borrower", "lend", "lent", "lender", "owe", "owed", "owing", "repay", "repaid", "repayment", "return", "returned", "returning", "recover", "recovery", "payback", "cash"},
            {"motor vehicle", "vehicle", "car", "cars", "motorcycle", "bike", "truck", "automobile", "driver", "road"},
            {"accident", "hit", "collision", "collided", "crash", "crashed", "road accident", "damage", "damages", "injury", "injured"},
            {"negligence", "careless", "fault", "wrongful", "driver", "driving", "reckless", "negligent"},
            {"tenant", "landlord", "rent", "rental", "lease", "tenancy", "deposit", "security deposit", "eviction", "evicted", "premises"},
            {"father", "mother", "brother", "sister", "husband", "wife", "spouse", "child", "children", "inheritance", "inherited", "estate", "will", "share", "heir", "heirs", "succession"},
            {"photo", "image", "logo", "brand", "copy", "copied", "copyright", "trademark", "patent", "infringement", "unauthorized"}
    };

    public enum Algorithm {
        KMP,
        RABIN_KARP,
        Z_ALGORITHM
    }

    public enum MatchType {
        TITLE_MATCH,
        CONTENT_REFERENCE
    }

    public static class SearchResult {

        private final LegalDocument document;
        private final int position;
        private final Algorithm algorithm;
        private final MatchType matchType;
        private final int relevanceScore;
        private final SimpleList<String> matchedTerms;
        private final SimpleList<String> matchedConcepts;

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType) {

            this(
                    document,
                    position,
                    algorithm,
                    matchType,
                    0,
                    new SimpleList<String>(),
                    new SimpleList<String>()
            );
        }

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType,
                int relevanceScore,
                SimpleList<String> matchedTerms) {

            this(
                    document,
                    position,
                    algorithm,
                    matchType,
                    relevanceScore,
                    matchedTerms,
                    new SimpleList<String>()
            );
        }

        public SearchResult(
                LegalDocument document,
                int position,
                Algorithm algorithm,
                MatchType matchType,
                int relevanceScore,
                SimpleList<String> matchedTerms,
                SimpleList<String> matchedConcepts) {

            this.document = document;
            this.position = position;
            this.algorithm = algorithm;
            this.matchType = matchType;
            this.relevanceScore = relevanceScore;
            this.matchedTerms = matchedTerms;
            this.matchedConcepts = matchedConcepts;
        }

        public LegalDocument getDocument() {
            return document;
        }

        public int getPosition() {
            return position;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public MatchType getMatchType() {
            return matchType;
        }

        public int getRelevanceScore() {
            return relevanceScore;
        }

        public SimpleList<String> getMatchedTerms() {
            return matchedTerms;
        }

        public SimpleList<String> getMatchedConcepts() {
            return matchedConcepts;
        }
    }

    public static class SearchRankedOutcome {

        private final SimpleList<SearchResult> results;
        private final int candidateCount;
        private final int displayedCount;
        private final SimpleList<String> legalConcepts;

        public SearchRankedOutcome(
                SimpleList<SearchResult> results,
                int candidateCount,
                int displayedCount,
                SimpleList<String> legalConcepts) {

            this.results = results;
            this.candidateCount = candidateCount;
            this.displayedCount = displayedCount;
            this.legalConcepts = legalConcepts;
        }

        public SimpleList<SearchResult> getResults() {
            return results;
        }

        public int getCandidateCount() {
            return candidateCount;
        }

        public int getDisplayedCount() {
            return displayedCount;
        }

        public SimpleList<String> getLegalConcepts() {
            return legalConcepts;
        }
    }

    private static class RankedCandidate {

        private final LegalDocument document;
        private final SimpleList<String> matchedTerms;
        private final SimpleList<String> matchedConcepts;
        private int titleMatchCount;
        private int phraseMatchCount;
        private int titleConceptMatchCount;
        private int contentConceptMatchCount;
        private int position;
        private MatchType matchType;

        public RankedCandidate(LegalDocument document) {
            this.document = document;
            this.matchedTerms = new SimpleList<String>();
            this.matchedConcepts = new SimpleList<String>();
            this.titleMatchCount = 0;
            this.phraseMatchCount = 0;
            this.titleConceptMatchCount = 0;
            this.contentConceptMatchCount = 0;
            this.position = -1;
            this.matchType = MatchType.CONTENT_REFERENCE;
        }

        public LegalDocument getDocument() {
            return document;
        }

        public SimpleList<String> getMatchedTerms() {
            return matchedTerms;
        }

        public SimpleList<String> getMatchedConcepts() {
            return matchedConcepts;
        }

        public int getTitleMatchCount() {
            return titleMatchCount;
        }

        public int getPhraseMatchCount() {
            return phraseMatchCount;
        }

        public int getTitleConceptMatchCount() {
            return titleConceptMatchCount;
        }

        public int getContentConceptMatchCount() {
            return contentConceptMatchCount;
        }

        public int getPosition() {
            return position;
        }

        public MatchType getMatchType() {
            return matchType;
        }
    }

    private final SimpleList<LegalDocument> documents;
    private final LegalTermCatalog legalTermCatalog;

    public SearchEngine(SimpleList<LegalDocument> documents) {
        this.documents = documents;
        this.legalTermCatalog = new LegalTermCatalog();
    }

    /*
     * ============================================================
     * AUTOMATIC ALGORITHM SELECTION
     * ============================================================
     *
     * The user does not select an algorithm.
     * The system selects one based on the query type.
     */

    public static Algorithm chooseAlgorithm(
            QueryProcessor.QueryType queryType) {

        if (queryType
                == QueryProcessor.QueryType.EXACT_LEGAL_TERM) {

            return Algorithm.KMP;

        } else if (queryType
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            return Algorithm.RABIN_KARP;

        } else {

            return Algorithm.Z_ALGORITHM;
        }
    }

    /*
     * Gives a human-readable explanation for the
     * automatically selected algorithm.
     */

    public static String getAlgorithmReason(
            QueryProcessor.QueryType queryType,
            Algorithm algorithm) {

        if (algorithm == Algorithm.KMP) {

            return "Exact legal-term query; KMP provides predictable O(n + m) pattern matching.";

        } else if (algorithm == Algorithm.RABIN_KARP) {

            return "Fuzzy-resolved legal term; Rabin-Karp uses hashing for efficient pattern comparison.";

        } else {

            return "General text query; Z Algorithm provides efficient prefix-based pattern matching.";
        }
    }

    public SimpleList<String> extractMeaningfulTerms(String query) {

        SimpleList<String> meaningfulTerms =
                new SimpleList<String>();

        if (query == null || query.trim().isEmpty()) {
            return meaningfulTerms;
        }

        String normalized =
                normalizeText(query);

        if (normalized.isEmpty()) {
            return meaningfulTerms;
        }

        String[] words =
                normalized.split(" ");

        for (int i = 0; i < words.length; i++) {

            String word = words[i].trim();
            String normalizedWord = normalizeForComparison(word);

            if (word.isEmpty()
                    || normalizedWord.isEmpty()
                    || !isMeaningfulTerm(word)
                    || meaningfulTerms.contains(normalizedWord)) {
                continue;
            }

            meaningfulTerms.add(normalizedWord);
        }

        return meaningfulTerms;
    }

    public SimpleList<SearchResult> searchRanked(
            String query,
            Algorithm algorithm,
            int topK) {

        return searchRankedWithMetadata(
                query,
                algorithm,
                topK
        ).getResults();
    }

    public SearchRankedOutcome searchRankedWithMetadata(
            String query,
            Algorithm algorithm,
            int topK) {

        return searchRankedWithMetadata(
            query,
            algorithm,
            topK,
            null
        );
        }

        public SearchRankedOutcome searchRankedWithMetadata(
            String query,
            Algorithm algorithm,
            int topK,
            LegalProblemAnalyzer.AnalysisResult analysis) {

        SimpleList<SearchResult> results =
                new SimpleList<SearchResult>();

        if (query == null || query.trim().isEmpty()) {
            return new SearchRankedOutcome(
                    results,
                    0,
                    0,
                    new SimpleList<String>()
            );
        }

        SimpleList<String> meaningfulTerms =
                extractMeaningfulTerms(query);

        if (meaningfulTerms.isEmpty()) {
            return new SearchRankedOutcome(
                    results,
                    0,
                    0,
                    new SimpleList<String>()
            );
        }

        SimpleList<String> legalConcepts =
            extractLegalConcepts(query);

        if (analysis != null) {
            legalConcepts = mergeConcepts(
                legalConcepts,
                analysis.getSituation().getLegalDomains()
            );

            legalConcepts = mergeConcepts(
                legalConcepts,
                extractSituationTerms(analysis)
            );
        }

        SimpleList<String> contextualTerms =
            buildContextualTerms(query, analysis);

        SimpleList<RankedCandidate> candidates =
                new SimpleList<RankedCandidate>();

        for (int i = 0; i < contextualTerms.size(); i++) {

            String term = contextualTerms.get(i);

            if (term == null || term.trim().isEmpty()) {
            continue;
            }

            SimpleList<SearchResult> termResults =
                    search(term, algorithm);

            for (int j = 0; j < termResults.size(); j++) {

                SearchResult termResult =
                        termResults.get(j);

                LegalDocument document =
                        termResult.getDocument();

                int candidateIndex =
                        findCandidateIndex(
                                candidates,
                                document.getDocumentId()
                        );

                RankedCandidate candidate;

                if (candidateIndex == -1) {

                    candidate =
                            new RankedCandidate(document);

                    candidates.add(candidate);

                } else {

                    candidate = candidates.get(candidateIndex);
                }

                if (!candidate.getMatchedTerms().contains(term)) {
                    candidate.getMatchedTerms().add(term);
                }

                if (termResult.getMatchType() == MatchType.TITLE_MATCH) {
                    candidate.titleMatchCount++;
                }

                if (candidate.position == -1) {
                    candidate.position = termResult.getPosition();
                }

                if (candidate.matchType == MatchType.CONTENT_REFERENCE
                        && termResult.getMatchType() == MatchType.TITLE_MATCH) {
                    candidate.matchType = MatchType.TITLE_MATCH;
                }
            }
        }

        SimpleList<String> phraseCandidates =
            buildPhraseCandidates(contextualTerms);

        for (int i = 0; i < candidates.size(); i++) {

            RankedCandidate candidate =
                    candidates.get(i);

            String candidateText =
                    normalizeText(
                            candidate.getDocument().getTitle()
                                    + " "
                                    + candidate.getDocument().getContent()
                    );

            for (int j = 0; j < phraseCandidates.size(); j++) {

                if (containsPhrase(
                        candidateText,
                        phraseCandidates.get(j)
                )) {
                    candidate.phraseMatchCount++;
                }
            }

            applyConceptMatches(
                    candidate,
                    legalConcepts,
                    candidateText,
                    normalizeText(candidate.getDocument().getTitle())
            );
        }

        SearchResult[] scoredResults =
                new SearchResult[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {

            RankedCandidate candidate =
                    candidates.get(i);

            int score = calculateScore(candidate);

            scoredResults[i] =
                    new SearchResult(
                            candidate.getDocument(),
                            candidate.getPosition(),
                            algorithm,
                            candidate.getTitleMatchCount() > 0
                                    ? MatchType.TITLE_MATCH
                                    : MatchType.CONTENT_REFERENCE,
                            score,
                            cloneTerms(candidate.getMatchedTerms()),
                            cloneTerms(candidate.getMatchedConcepts())
                    );
        }

        insertionSort(scoredResults);

        int candidateCount = scoredResults.length;

        int limit = topK;

        if (limit <= 0) {
            limit = DEFAULT_TOP_K;
        }

        if (limit > candidateCount) {
            limit = candidateCount;
        }

        for (int i = 0; i < limit; i++) {
            results.add(scoredResults[i]);
        }

        return new SearchRankedOutcome(
                results,
                candidateCount,
                results.size(),
                legalConcepts
        );
    }

    private SimpleList<String> buildContextualTerms(
            String query,
            LegalProblemAnalyzer.AnalysisResult analysis) {

        SimpleList<String> contextualTerms =
                new SimpleList<String>();

        SimpleList<String> meaningfulTerms =
                extractMeaningfulTerms(query);

        for (int i = 0; i < meaningfulTerms.size(); i++) {
            String term = meaningfulTerms.get(i);
            if (!contextualTerms.contains(term)) {
                contextualTerms.add(term);
            }
        }

        if (analysis != null) {
            LegalProblemAnalyzer.LegalSituation situation =
                    analysis.getSituation();

            addSituationTokens(contextualTerms, situation.getActors());
            addSituationTokens(contextualTerms, situation.getActions());
            addSituationTokens(contextualTerms, situation.getObjects());
            addSituationTokens(contextualTerms, situation.getRelationships());
            addSituationTokens(contextualTerms, situation.getPossibleObjectives());
        }

        return contextualTerms;
    }

    private void addSituationTokens(
            SimpleList<String> target,
            SimpleList<String> source) {

        for (int i = 0; i < source.size(); i++) {
            String token = source.get(i);
            if (token != null && !token.trim().isEmpty()) {
                String normalizedToken = normalizeForComparison(token);
                if (!normalizedToken.isEmpty() && !target.contains(normalizedToken)) {
                    target.add(normalizedToken);
                }
            }
        }
    }

    private SimpleList<String> extractSituationTerms(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        SimpleList<String> terms =
                new SimpleList<String>();

        if (analysis == null) {
            return terms;
        }

        LegalProblemAnalyzer.LegalSituation situation =
                analysis.getSituation();

        addSituationTokens(terms, situation.getActors());
        addSituationTokens(terms, situation.getActions());
        addSituationTokens(terms, situation.getObjects());
        addSituationTokens(terms, situation.getRelationships());

        return terms;
    }

    private SimpleList<String> mergeConcepts(
            SimpleList<String> base,
            SimpleList<String> extra) {

        SimpleList<String> merged =
                new SimpleList<String>();

        for (int i = 0; i < base.size(); i++) {
            String value = base.get(i);
            if (!merged.contains(value)) {
                merged.add(value);
            }
        }

        for (int i = 0; i < extra.size(); i++) {
            String value = extra.get(i);
            if (!merged.contains(value)) {
                merged.add(value);
            }
        }

        return merged;
    }

    public SimpleList<SearchResult> search(
            String query,
            Algorithm algorithm) {

        SimpleList<SearchResult> results =
                new SimpleList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery =
                query.toLowerCase();

        for (int i = 0; i < documents.size(); i++) {

            LegalDocument document =
                    documents.get(i);

            String content =
                    document.getContent().toLowerCase();

            int position;

            if (algorithm == Algorithm.KMP) {

                position = KMP.search(
                        content,
                        normalizedQuery
                );

            } else if (algorithm == Algorithm.RABIN_KARP) {

                position = RabinKarp.search(
                        content,
                        normalizedQuery
                );

            } else {

                position = ZAlgorithm.search(
                        content,
                        normalizedQuery
                );
            }

            if (position != -1) {

                MatchType matchType;

                String title =
                        document.getTitle().toLowerCase();

                if (title.contains(normalizedQuery)) {

                    matchType =
                            MatchType.TITLE_MATCH;

                } else {

                    matchType =
                            MatchType.CONTENT_REFERENCE;
                }

                results.add(
                        new SearchResult(
                                document,
                                position,
                                algorithm,
                                matchType
                        )
                );
            }
        }

        return results;
    }

    private int calculateScore(RankedCandidate candidate) {

        int score = 0;

        score += candidate.getMatchedConcepts().size() * 50;
        score += candidate.getTitleConceptMatchCount() * 20;
        score += candidate.getContentConceptMatchCount() * 10;
        score += candidate.getTitleMatchCount() * 8;
        score += candidate.getPhraseMatchCount() * 4;
        score += candidate.getMatchedTerms().size() * 3;

        return score;
    }

    private SimpleList<String> buildPhraseCandidates(
            SimpleList<String> meaningfulTerms) {

        SimpleList<String> phrases =
                new SimpleList<String>();

        for (int i = 0; i < meaningfulTerms.size(); i++) {

            if (i + 1 < meaningfulTerms.size()) {
                phrases.add(
                        meaningfulTerms.get(i)
                                + " "
                                + meaningfulTerms.get(i + 1)
                );
            }

            if (i + 2 < meaningfulTerms.size()) {
                phrases.add(
                        meaningfulTerms.get(i)
                                + " "
                                + meaningfulTerms.get(i + 1)
                                + " "
                                + meaningfulTerms.get(i + 2)
                );
            }
        }

        return phrases;
    }

    private int findCandidateIndex(
            SimpleList<RankedCandidate> candidates,
            String documentId) {

        for (int i = 0; i < candidates.size(); i++) {

            RankedCandidate candidate =
                    candidates.get(i);

            if (candidate.getDocument()
                    .getDocumentId()
                    .equals(documentId)) {
                return i;
            }
        }

        return -1;
    }

    private SimpleList<String> cloneTerms(
            SimpleList<String> source) {

        SimpleList<String> clone =
                new SimpleList<String>();

        for (int i = 0; i < source.size(); i++) {
            clone.add(source.get(i));
        }

        return clone;
    }

    private void insertionSort(SearchResult[] results) {

        for (int i = 1; i < results.length; i++) {

            SearchResult current = results[i];
            int j = i - 1;

            while (j >= 0
                    && shouldSwap(results[j], current)) {

                results[j + 1] = results[j];
                j--;
            }

            results[j + 1] = current;
        }
    }

    private boolean shouldSwap(
            SearchResult left,
            SearchResult right) {

        if (left.getRelevanceScore() < right.getRelevanceScore()) {
            return true;
        }

        if (left.getRelevanceScore() == right.getRelevanceScore()) {

            String leftTitle =
                    left.getDocument().getTitle();
            String rightTitle =
                    right.getDocument().getTitle();

            if (leftTitle.compareTo(rightTitle) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isMeaningfulTerm(String word) {

        if (word == null || word.isEmpty()) {
            return false;
        }

        String normalizedWord = normalizeForComparison(word);

        if (normalizedWord.isEmpty()) {
            return false;
        }

        if (isCatalogTerm(normalizedWord)) {
            return true;
        }

        if (word.length() <= 1) {
            return false;
        }

        for (int i = 0; i < STOP_WORDS.length; i++) {

            if (STOP_WORDS[i].equals(word)) {
                return false;
            }
        }

        return isKnownContextWord(normalizedWord);
    }

    private boolean isCatalogTerm(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        SimpleList<String> catalogTerms = legalTermCatalog.getTerms();

        for (int i = 0; i < catalogTerms.size(); i++) {
            String catalogTerm = normalizeForComparison(catalogTerms.get(i));
            if (!catalogTerm.isEmpty() && catalogTerm.equals(word)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKnownContextWord(String word) {

        String normalizedWord =
                normalizeForComparison(word);

        if (normalizedWord.isEmpty()) {
            return false;
        }

        for (int i = 0; i < LEGAL_CONCEPT_NAMES.length; i++) {

            String[] keywords =
                    LEGAL_CONCEPT_KEYWORDS[i];

            for (int j = 0; j < keywords.length; j++) {

                if (normalizedWord.equals(
                        normalizeForComparison(keywords[j]))) {

                    return true;
                }
            }
        }

        return false;
    }

    public SimpleList<String> extractLegalConcepts(String query) {

        SimpleList<String> concepts =
                new SimpleList<String>();

        String normalizedQuery =
                normalizeText(query);

        if (normalizedQuery.isEmpty()) {
            return concepts;
        }

        boolean hasEmploymentContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "employer",
                                "employee",
                                "employment",
                                "company",
                                "boss",
                                "manager",
                                "dismissed",
                                "fired",
                                "sacked",
                                "terminated"
                        }
                );

        boolean hasContractContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "contract",
                                "agreement",
                                "deal",
                                "terms",
                                "obligation",
                                "obligations",
                                "promise",
                                "promised",
                                "signed"
                        }
                );

        boolean hasConsumerContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "consumer",
                                "customer",
                                "buyer",
                                "seller",
                                "shop",
                                "retailer",
                                "purchase",
                                "purchased",
                                "bought",
                                "product",
                                "products",
                                "phone",
                                "laptop",
                                "item",
                                "goods",
                                "services"
                        }
                );

        boolean hasPropertyContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "property",
                                "house",
                                "home",
                                "land",
                                "estate",
                                "ownership",
                                "owner",
                                "title",
                                "claim",
                                "claimed",
                                "occupied",
                                "occupy",
                                "possession"
                        }
                );

        boolean hasVehicleContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "vehicle",
                                "car",
                                "cars",
                                "motorcycle",
                                "bike",
                                "truck",
                                "automobile",
                                "driver",
                                "road"
                        }
                );

        boolean hasFamilyContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "father",
                                "mother",
                                "brother",
                                "sister",
                                "husband",
                                "wife",
                                "spouse",
                                "child",
                                "children",
                                "heir",
                                "heirs",
                                "estate",
                                "inheritance"
                        }
                );

        boolean hasIPContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "photo",
                                "photograph",
                                "image",
                                "logo",
                                "copyright",
                                "trademark",
                                "patent",
                                "infringement",
                                "unauthorized",
                                "copy",
                                "copied",
                                "use"
                        }
                );

        boolean hasSalaryContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "salary",
                                "wage",
                                "wages",
                                "pay",
                                "paid",
                                "payment",
                                "payments",
                                "remuneration",
                                "income",
                                "earnings",
                                "dues",
                                "unpaid"
                        }
                );

        boolean hasLoanContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "borrow",
                                "borrowed",
                                "lend",
                                "lent",
                                "loan",
                                "owe",
                                "owed",
                                "owing",
                                "repay",
                                "repaid",
                                "repayment",
                                "return",
                                "returned",
                                "returning",
                                "payback"
                        }
                );

        boolean hasMoneyContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "money",
                                "cash",
                                "amount",
                                "funds",
                                "loan",
                                "debt"
                        }
                );

        boolean hasDefectContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "defect",
                                "defective",
                                "faulty",
                                "damaged",
                                "wrong product",
                                "wrong goods",
                                "stopped working"
                        }
                );

        boolean hasRemedyContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "refund",
                                "reimburse",
                                "replacement",
                                "replace",
                                "exchange",
                                "repair",
                                "remedy"
                        }
                );

        boolean hasBreachContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "breach",
                                "breached",
                                "refuse",
                                "refusing",
                                "refused",
                                "non-performance",
                                "non performance",
                                "violate",
                                "violated",
                                "defaulted"
                        }
                );

        boolean hasPerformanceContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "perform",
                                "performance",
                                "enforce",
                                "enforcement",
                                "follow",
                                "followed",
                                "honour",
                                "honor",
                                "obligation",
                                "obligations"
                        }
                );

        boolean hasOwnershipContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "ownership",
                                "owner",
                                "title",
                                "claim",
                                "claimed",
                                "possession",
                                "belongs",
                                "belong",
                                "belonged",
                                "occupied",
                                "occupy"
                        }
                );

        boolean hasVehicleAccidentContext =
                hasAnyKeyword(
                        normalizedQuery,
                        new String[] {
                                "accident",
                                "hit",
                                "collision",
                                "crash",
                                "crashed",
                                "injury",
                                "injured",
                                "damage",
                                "damaged"
                        }
                );

        if (hasEmploymentContext) {
            addConceptIfMissing(concepts, "Employment");
        }

        if (hasSalaryContext) {
            addConceptIfMissing(concepts, "Salary");
        }

        if (hasContractContext) {
            addConceptIfMissing(concepts, "Agreement");
            addConceptIfMissing(concepts, "Contract");
        }

        if (hasContractContext && hasBreachContext) {
            addConceptIfMissing(concepts, "Breach of Contract");
        }

        if (hasContractContext && hasPerformanceContext) {
            addConceptIfMissing(concepts, "Specific Performance");
        }

        if (hasConsumerContext && (hasDefectContext || hasRemedyContext)) {
            addConceptIfMissing(concepts, "Consumer");
            addConceptIfMissing(concepts, "Goods / Services");
        }

        if (hasConsumerContext && hasRemedyContext) {
            addConceptIfMissing(concepts, "Compensation");
        }

        if (hasPropertyContext) {
            addConceptIfMissing(concepts, "Property");
        }

        if (hasPropertyContext && hasOwnershipContext) {
            addConceptIfMissing(concepts, "Ownership");
        }

        if (hasMoneyContext && hasLoanContext) {
            addConceptIfMissing(concepts, "Debt / Money Recovery");
        }

        if (hasVehicleContext && hasVehicleAccidentContext) {
            addConceptIfMissing(concepts, "Motor Vehicle");
            addConceptIfMissing(concepts, "Accident");
        }

        if (hasVehicleContext && hasAnyKeyword(normalizedQuery, new String[] {"negligence", "careless", "fault", "wrongful", "reckless"})) {
            addConceptIfMissing(concepts, "Negligence");
        }

        if (hasAnyKeyword(
                normalizedQuery,
                new String[] {
                        "tenant",
                        "landlord",
                        "rent",
                        "rental",
                        "lease",
                        "security deposit",
                        "deposit"
                }
        )) {
            addConceptIfMissing(concepts, "Tenancy");
        }

        if (hasFamilyContext
                && (hasAnyKeyword(normalizedQuery, new String[] {"property", "estate", "inheritance", "share"}))) {
            addConceptIfMissing(concepts, "Family / Inheritance");
        }

        if (hasIPContext) {
            addConceptIfMissing(concepts, "Intellectual Property");
        }

        return concepts;
    }

    private void addConceptIfMissing(
            SimpleList<String> concepts,
            String concept) {

        if (!concepts.contains(concept)) {
            concepts.add(concept);
        }
    }

    private boolean hasAnyKeyword(
            String text,
            String[] keywords) {

        for (int i = 0; i < keywords.length; i++) {
            if (queryContainsKeyword(text, keywords[i])) {
                return true;
            }
        }

        return false;
    }

    private void applyConceptMatches(
            RankedCandidate candidate,
            SimpleList<String> legalConcepts,
            String candidateText,
            String titleText) {

        if (legalConcepts.isEmpty()) {
            return;
        }

        for (int i = 0; i < LEGAL_CONCEPT_NAMES.length; i++) {

            String conceptName = LEGAL_CONCEPT_NAMES[i];

            if (!legalConcepts.contains(conceptName)) {
                continue;
            }

            if (conceptMatchesText(candidateText, i)) {
                candidate.getMatchedConcepts().add(conceptName);

                if (conceptMatchesText(titleText, i)) {
                    candidate.titleConceptMatchCount++;
                } else {
                    candidate.contentConceptMatchCount++;
                }
            }
        }
    }

    private boolean conceptMatchesText(
            String text,
            int conceptIndex) {

        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String normalizedText =
                normalizeText(text);

        String[] keywords = LEGAL_CONCEPT_KEYWORDS[conceptIndex];

        for (int i = 0; i < keywords.length; i++) {

            String keyword = keywords[i];

            if (keywordMatchesText(normalizedText, keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean queryContainsKeyword(
            String normalizedQuery,
            String keyword) {

        return keywordMatchesText(normalizedQuery, keyword);
    }

    private boolean keywordMatchesText(
            String normalizedText,
            String keyword) {

        if (normalizedText == null || normalizedText.trim().isEmpty()) {
            return false;
        }

        String normalizedKeyword =
                normalizeForComparison(keyword);

        if (normalizedKeyword.isEmpty()) {
            return false;
        }

        if (normalizedKeyword.indexOf(' ') >= 0) {
            return containsPhrase(
                    normalizedText,
                    normalizedKeyword
            );
        }

        String[] tokens =
                splitWords(normalizedText);

        for (int i = 0; i < tokens.length; i++) {

            if (normalizedTermMatches(tokens[i], normalizedKeyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean normalizedTermMatches(
            String term,
            String keyword) {

        String normalizedTerm =
                normalizeForComparison(term);
        String normalizedKeyword =
                normalizeForComparison(keyword);

        if (normalizedTerm.isEmpty()
                || normalizedKeyword.isEmpty()) {
            return false;
        }

        if (normalizedTerm.equals(normalizedKeyword)) {
            return true;
        }

        if (normalizedTerm.length() > 1
                && normalizedTerm.endsWith("s")
                && !normalizedTerm.endsWith("ss")) {

            String singularTerm =
                    normalizedTerm.substring(0, normalizedTerm.length() - 1);

            if (singularTerm.equals(normalizedKeyword)) {
                return true;
            }
        }

        if (normalizedTerm.length() > 2
                && normalizedTerm.endsWith("es")
                && !normalizedTerm.endsWith("ses")) {

            String singularTerm =
                    normalizedTerm.substring(0, normalizedTerm.length() - 2);

            if (singularTerm.equals(normalizedKeyword)) {
                return true;
            }
        }

        if (normalizedTerm.endsWith("ies")
                && normalizedTerm.length() > 3) {

            String singularTerm =
                    normalizedTerm.substring(0, normalizedTerm.length() - 3)
                            + "y";

            if (singularTerm.equals(normalizedKeyword)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeForComparison(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                value.toLowerCase();

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9 ]",
                        " "
                );

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        String[] words =
                splitWords(normalized);

        StringBuilder builder =
                new StringBuilder();

        for (int i = 0; i < words.length; i++) {

            String word =
                    singularizeToken(words[i]);

            if (word.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(word);
        }

        return builder.toString().trim();
    }

    private String singularizeToken(String token) {

        if (token == null || token.isEmpty()) {
            return "";
        }

        String normalized = token.trim();

        if (normalized.length() <= 3) {
            return normalized;
        }

        if (normalized.endsWith("ies")
                && normalized.length() > 4) {
            return normalized.substring(0, normalized.length() - 3) + "y";
        }

        if (normalized.endsWith("sses")
                || normalized.endsWith("xes")
                || normalized.endsWith("zes")
                || normalized.endsWith("ches")
                || normalized.endsWith("shes")) {
            return normalized.substring(0, normalized.length() - 2);
        }

        if (normalized.endsWith("s")
                && !normalized.endsWith("ss")) {
            return normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        String normalized =
                text.toLowerCase();

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9 ]",
                        " "
                );

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        return normalized.trim();
    }

    private boolean containsPhrase(
            String text,
            String phrase) {

        if (text == null || phrase == null) {
            return false;
        }

        String[] textWords =
                splitWords(text);
        String[] phraseWords =
                splitWords(phrase);

        if (phraseWords.length == 0
                || phraseWords.length > textWords.length) {
            return false;
        }

        for (int i = 0;
                i <= textWords.length - phraseWords.length;
                i++) {

            boolean match = true;

            for (int j = 0; j < phraseWords.length; j++) {

                if (!textWords[i + j].equals(phraseWords[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }

    private String[] splitWords(String text) {

        String[] rawWords =
                text.split(" ");

        int count = 0;

        for (int i = 0; i < rawWords.length; i++) {

            if (!rawWords[i].trim().isEmpty()) {
                count++;
            }
        }

        String[] words = new String[count];
        int index = 0;

        for (int i = 0; i < rawWords.length; i++) {

            String word = rawWords[i].trim();

            if (!word.isEmpty()) {
                words[index] = word;
                index++;
            }
        }

        return words;
    }
}

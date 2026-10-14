package tests;

import search.QueryProcessor;
import search.FuzzyLegalSearch;
import search.LegalTermCatalog;
import search.SearchEngine;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import structures.SimpleList;

import java.nio.file.Files;
import java.nio.file.Path;

public class SearchSafetyRegressionTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        QueryProcessor processor = new QueryProcessor();
        check(processor.analyze("rape").getType() != QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "real word rape is never rewritten as a fuzzy legal term");
        check(processor.analyze("dowry").getType() != QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "real word dowry is never rewritten as a fuzzy legal term");
        check(processor.analyze("stole").getType() != QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "real word stole is never rewritten as a fuzzy legal term");
        check(processor.analyze("murde").getType() == QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "small single-token typo remains correctable");
        check(processor.analyze("my husband beats me").getType() != QueryProcessor.QueryType.FUZZY_LEGAL_TERM,
                "multi-word sentence is never rewritten as one catalog term");

        SimpleList<LegalDocument> docs = new SimpleList<>();
        Path baseline = Path.of("data/corpus");
        Path statutes = Path.of("data/statutory_additions");
        if (Files.isDirectory(baseline)) {
            docs = new LegalCorpusLoader(baseline.toString()).loadDocuments();
            SimpleList<LegalDocument> additions = new LegalCorpusLoader(statutes.toString()).loadDocuments();
            for (int i = 0; i < additions.size(); i++) docs.add(additions.get(i));
        }
        if (!docs.isEmpty()) {
            SearchEngine engine = new SearchEngine(docs);
            SearchEngine.SearchRankedOutcome theft = engine.searchRankedWithMetadata(
                    "someone stole my phone", SearchEngine.Algorithm.Z_ALGORITHM, 10);
            check(hasStatute(theft, "STATUTE_BNS_2023_S303"),
                    "everyday stolen-phone wording reaches the BNS theft excerpt");
            SearchEngine.SearchRankedOutcome busBag = engine.searchRankedWithMetadata(
                    "bus stolen bag", SearchEngine.Algorithm.Z_ALGORITHM, 10);
            check(hasStatute(busBag, "STATUTE_BNS_2023_S303"),
                    "bus/bag theft query places the BNS theft excerpt first among legal leads");
            boolean bagJudgmentWithoutBag = false;
            for (int i = 0; i < busBag.getResults().size(); i++) {
                LegalDocument d = busBag.getResults().get(i).getDocument();
                if (d != null && "JUDGMENT".equalsIgnoreCase(d.getKind())
                        && !d.getBody().toLowerCase(java.util.Locale.ROOT).matches("(?s).*\\bbags?\\b.*")) {
                    bagJudgmentWithoutBag = true;
                }
            }
            check(!bagJudgmentWithoutBag,
                    "bus/bag theft does not return judgments that never mention a bag");
            SearchEngine.SearchRankedOutcome cyber = engine.searchRankedWithMetadata(
                    "someone hacked my bank account and stole money online",
                    SearchEngine.Algorithm.Z_ALGORITHM, 10);
            check(hasStatute(cyber, "STATUTE_IT_ACT_2000_S66C_66D"),
                    "bank-account compromise query reaches IT Act identity-theft/personation excerpt");
            SearchEngine.SearchRankedOutcome noise = engine.searchRankedWithMetadata(
                    "my neighbour plays loud music at night", SearchEngine.Algorithm.Z_ALGORITHM, 10);
            check(noise.getResults().isEmpty(),
                    "unrelated weak-only neighbour/noise results are suppressed");
        } else {
            check(false, "test corpus available for query-level regression checks");
        }

        System.out.println("SEARCH SAFETY REGRESSION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Search safety regression failed.");
    }

    private static boolean hasStatute(SearchEngine.SearchRankedOutcome outcome, String id) {
        for (int i = 0; i < outcome.getResults().size(); i++) {
            LegalDocument d = outcome.getResults().get(i).getDocument();
            if (d != null && id.equals(d.getDocumentId())) return true;
        }
        return false;
    }

    private static void check(boolean ok, String message) {
        System.out.println((ok ? "PASS" : "FAIL") + " | " + message);
        if (!ok) failures++;
    }
}

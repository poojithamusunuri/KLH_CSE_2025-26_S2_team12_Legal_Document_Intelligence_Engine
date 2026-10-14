package tests;

import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

import java.util.HashSet;
import java.util.Set;

/** Ensures the accepted baseline stays separate while official excerpts join production search. */
public class CorpusTrackIntegrationTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        SimpleList<LegalDocument> baseline = new LegalCorpusLoader("data/corpus").loadDocuments();
        SimpleList<LegalDocument> additions = new LegalCorpusLoader("data/statutory_additions").loadDocuments();
        check(baseline.size() == 242, "original baseline remains exactly 242 documents");
        check(additions.size() == 7, "separate statutory track contains seven text excerpts");

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < baseline.size(); i++) ids.add(baseline.get(i).getDocumentId());
        for (int i = 0; i < additions.size(); i++) {
            LegalDocument d = additions.get(i);
            check(ids.add(d.getDocumentId()), "unique document ID: " + d.getDocumentId());
            check(d.getSource().contains("India Code"), d.getDocumentId() + " identifies India Code provenance");
            check(d.getContent().contains("SOURCE_URL:"), d.getDocumentId() + " retains official source URL in raw content");
        }

        SimpleList<LegalDocument> combined = new SimpleList<>();
        for (int i = 0; i < baseline.size(); i++) combined.add(baseline.get(i));
        for (int i = 0; i < additions.size(); i++) combined.add(additions.get(i));
        check(combined.size() == 249, "combined production corpus contains 242 baseline + 7 additions");

        SearchEngine engine = new SearchEngine(combined);
        SimpleList<SearchEngine.SearchResult> theft = engine.search("theft", SearchEngine.Algorithm.Z_ALGORITHM);
        check(hasId(theft, "STATUTE_BNS_2023_S303"), "statutory theft excerpt is searchable");
        SimpleList<SearchEngine.SearchResult> contract = engine.search("lawful consideration", SearchEngine.Algorithm.Z_ALGORITHM);
        check(hasId(contract, "STATUTE_ICA_1872_S10"), "contract section 10 excerpt is searchable");
        SimpleList<SearchEngine.SearchResult> cyber = engine.search("identity theft", SearchEngine.Algorithm.Z_ALGORITHM);
        check(hasId(cyber, "STATUTE_IT_ACT_2000_S66C_66D"), "IT Act identity-theft and personation provisions are searchable");
        SimpleList<SearchEngine.SearchResult> noMatch = engine.search("zxqv-unmatched-token-8841", SearchEngine.Algorithm.Z_ALGORITHM);
        check(noMatch.isEmpty(), "unmatched query remains a true no-match");

        System.out.println("CORPUS TRACK INTEGRATION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Corpus track integration failed");
    }

    private static boolean hasId(SimpleList<SearchEngine.SearchResult> results, String id) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getDocument().getDocumentId().equals(id)) return true;
        }
        return false;
    }

    private static void check(boolean ok, String label) {
        if (ok) System.out.println("PASS | " + label);
        else { failures++; System.out.println("FAIL | " + label); }
    }
}

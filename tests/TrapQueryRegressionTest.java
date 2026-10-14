package tests;

import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.SearchEngine;
import structures.SimpleList;

/** Trap queries: wrong-topic results, missing theft cues, seizure vs theft, section ordering. */
public final class TrapQueryRegressionTest {
    private static int failures;
    private static SearchEngine engine;

    public static void main(String[] args) throws Exception {
        SimpleList<LegalDocument> docs = new LegalCorpusLoader("data/corpus").loadDocuments();
        SimpleList<LegalDocument> extra = new LegalCorpusLoader("data/statutory_additions").loadDocuments();
        for (int i = 0; i < extra.size(); i++) docs.add(extra.get(i));
        engine = new SearchEngine(docs);

        // Theft cue works for any object, not only a fixed list.
        expectHas("my car was stolen", "STATUTE_BNS_2023_S303");
        expectHas("my scooter was stolen", "STATUTE_BNS_2023_S303");
        expectHas("a thief took my wallet", "STATUTE_BNS_2023_S303");
        expectHas("my bike was taken without my permission", "STATUTE_BNS_2023_S303");

        // Object words or loss alone are not theft.
        expectNoTheft("my phone fell and the screen broke");
        expectNoTheft("my phone has a virus");
        expectNoTheft("I lost my wallet");
        expectNoTheft("I lost my bag in the train");

        // Seizure by authorities is not theft.
        expectNoTheft("My property was seized without prior notice");
        expectNoTheft("my land was seized by the government");

        // Explicit section number comes first.
        expectTop("section 318", "STATUTE_BNS_2023_S318");
        expectTop("section 303", "STATUTE_BNS_2023_S303");
        expectTop("section 73 contract act damages", "STATUTE_ICA_1872_S73");

        // Personal incidents the corpus cannot support return nothing.
        expectEmpty("he threatened to kill me");
        expectEmpty("I forgot my bag on the bus");
        expectEmpty("my wife hits me");
        expectEmpty("asdf jkl");

        // Cyber wording stays with the IT Act, not the theft rule.
        expectTop("Someone hacked my bank account and stole money online", "STATUTE_IT_ACT_2000_S66C_66D");

        // Legal-term lookups still work.
        expectTop("theft", "STATUTE_BNS_2023_S303");
        expectHas("cheating", "STATUTE_BNS_2023_S318");

        System.out.println("TRAP QUERY REGRESSION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Trap query regressions failed");
    }

    private static SearchEngine.SearchRankedOutcome run(String q) {
        return engine.searchRankedWithMetadata(q, SearchEngine.Algorithm.Z_ALGORITHM, 10);
    }

    private static boolean has(SearchEngine.SearchRankedOutcome o, String id) {
        for (int i = 0; i < o.getResults().size(); i++) {
            LegalDocument d = o.getResults().get(i).getDocument();
            if (d != null && id.equals(d.getDocumentId())) return true;
        }
        return false;
    }

    private static void expectHas(String q, String id) {
        check(has(run(q), id), "'" + q + "' returns " + id);
    }

    private static void expectTop(String q, String id) {
        SearchEngine.SearchRankedOutcome o = run(q);
        boolean ok = !o.getResults().isEmpty()
                && id.equals(o.getResults().get(0).getDocument().getDocumentId());
        check(ok, "'" + q + "' ranks " + id + " first");
    }

    private static void expectNoTheft(String q) {
        check(!has(run(q), "STATUTE_BNS_2023_S303"), "'" + q + "' does not return the theft section");
    }

    private static void expectEmpty(String q) {
        check(run(q).getResults().isEmpty(), "'" + q + "' returns no results");
    }

    private static void check(boolean ok, String label) {
        System.out.println((ok ? "PASS" : "FAIL") + " | " + label);
        if (!ok) failures++;
    }
}

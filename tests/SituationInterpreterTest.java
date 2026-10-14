package tests;

import search.SituationInterpreter;
import search.SearchEngine;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import structures.SimpleList;

/** Checks concept-level generalization beyond one memorized sentence per issue. */
public class SituationInterpreterTest {
    private static int failures;

    public static void main(String[] args) {
        expectCue("Someone picked up my mobile and walked away", "theft");
        expectCue("My handbag disappeared after another person carried it away", "theft");
        expectCue("A person tricked me into sending money", "cheating");
        expectCue("Someone broke into my online banking account", "unauthorized access");
        expectCue("My spouse slapped and kicked me at home", "physical abuse");
        expectCue("I worked all month but my employer withheld my wages", "non-payment");
        expectCue("The landlord kept my rental deposit after I moved out", "return / refund");
        expectCue("My neighbour blares music every night and I cannot sleep", "nuisance");
        expectCue("A seller gave me a faulty item and refuses a replacement", "defect / deficiency");
        expectCue("Someone created an account in my name without asking", "identity theft");

        String vague = SituationInterpreter.expandForRetrieval("What is the law?");
        check(vague.equals("What is the law?"), "vague query is not assigned an invented legal concept");
        check(SituationInterpreter.concepts("What is the law?").isEmpty(),
                "no concept is claimed without multiple situation cues");

        try {
            SimpleList<LegalDocument> corpus = new LegalCorpusLoader("data/corpus").loadDocuments();
            SimpleList<LegalDocument> additions = new LegalCorpusLoader("data/statutory_additions").loadDocuments();
            for (int i = 0; i < additions.size(); i++) corpus.add(additions.get(i));
            SearchEngine engine = new SearchEngine(corpus);
            check(hasDocument(engine.searchRankedWithMetadata("Someone picked up my mobile and walked away", SearchEngine.Algorithm.Z_ALGORITHM, 10), "STATUTE_BNS_2023_S303"),
                    "unseen theft paraphrase retrieves the relevant statutory excerpt");
            check(hasDocument(engine.searchRankedWithMetadata("A person tricked me into sending money", SearchEngine.Algorithm.Z_ALGORITHM, 10), "STATUTE_BNS_2023_S318"),
                    "fraud paraphrase retrieves the cheating provision");
            check(hasDocument(engine.searchRankedWithMetadata("Someone broke into my online banking account", SearchEngine.Algorithm.Z_ALGORITHM, 10), "STATUTE_IT_ACT_2000_S43_EXCERPT"),
                    "account-intrusion paraphrase retrieves the computer-access provision");
        } catch (Exception e) {
            check(false, "semantic retrieval integration test can load corpus: " + e.getMessage());
        }
        System.out.println("SITUATION INTERPRETER SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Situation interpreter failed");
    }

    private static boolean hasDocument(SearchEngine.SearchRankedOutcome outcome, String id) {
        for (int i = 0; i < outcome.getResults().size(); i++) {
            LegalDocument d = outcome.getResults().get(i).getDocument();
            if (d != null && id.equals(d.getDocumentId())) return true;
        }
        return false;
    }

    private static void expectCue(String query, String cue) {
        String expanded = SituationInterpreter.expandForRetrieval(query);
        check(expanded.toLowerCase().contains(cue.toLowerCase()),
                "generalized situation cues: " + query + " -> " + cue);
    }

    private static void check(boolean ok, String label) {
        if (ok) System.out.println("PASS | " + label);
        else { failures++; System.out.println("FAIL | " + label); }
    }
}

package tests;

import search.SituationInterpreter;
import search.SearchEngine;
import loader.LegalCorpusLoader;
import model.LegalDocument;
import structures.SimpleList;

/** Adversarial positive and negative cases for event-level interpretation. */
public final class ConceptGeneralizationRegressionTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        expectConcept("My car was stolen", "possible theft");
        expectConcept("My bike was taken without my permission", "possible theft");
        expectConcept("Someone stole my phone", "possible theft");
        expectConcept("My wife hits me", "possible domestic or physical abuse");
        expectConcept("Someone hacked my bank account without my knowledge", "possible unauthorised account access");
        expectConcept("I worked all month and my employer withheld my wages", "possible unpaid wages");
        expectConcept("The landlord kept my security deposit after I moved out", "possible unreturned tenancy deposit");
        expectConcept("The government seized my land", "possible government land acquisition or seizure");

        expectNoConcept("My phone fell and the screen broke");
        expectNoConcept("My phone has a virus");
        expectNoConcept("I lost my bag in the train");
        expectNoConcept("My laptop went missing");
        expectNoConcept("I accessed my account online");
        expectNoConcept("I paid the bank account fee online");
        expectNoConcept("The bank refused to pay the cheque");
        expectNoConcept("The landlord kept the flat key");
        expectConcept("My neighbour plays loud music at night", "possible neighbourhood noise");
        expectNoConcept("What is the law?");
        check(SituationInterpreter.isUnclassifiedPersonalIncident("My phone fell and the screen broke"),
                "unclassified personal incident is recognized as such, not mislabeled theft");
        check(SituationInterpreter.isUnclassifiedPersonalIncident("I accessed my account online"),
                "ordinary account use is left unclassified rather than called hacking");
        check(!SituationInterpreter.isUnclassifiedPersonalIncident("theft"),
                "legal-term searches are not treated as personal incidents");

        check(!SituationInterpreter.expandForRetrieval("My phone fell and the screen broke").toLowerCase().contains("theft"),
                "object mention alone does not add theft retrieval cue");
        check(!SituationInterpreter.expandForRetrieval("I accessed my account online").toLowerCase().contains("unauthorized access"),
                "ordinary authorized account use does not add cyber-intrusion cue");
        check(SituationInterpreter.expandForRetrieval("My car was stolen").toLowerCase().contains("theft"),
                "explicit stolen-car statement adds a theft retrieval cue");

        SimpleList<LegalDocument> docs = new LegalCorpusLoader("data/corpus").loadDocuments();
        SimpleList<LegalDocument> additions = new LegalCorpusLoader("data/statutory_additions").loadDocuments();
        for (int i = 0; i < additions.size(); i++) docs.add(additions.get(i));
        SearchEngine engine = new SearchEngine(docs);
        check(engine.searchRankedWithMetadata("my phone fell and the screen broke", SearchEngine.Algorithm.Z_ALGORITHM, 5).getResults().isEmpty(),
                "unclassified phone accident returns no coincidental legal leads");
        SearchEngine.SearchRankedOutcome stolenCar = engine.searchRankedWithMetadata("my car was stolen", SearchEngine.Algorithm.Z_ALGORITHM, 10);
        check(hasDoc(stolenCar, "STATUTE_BNS_2023_S303"),
                "stolen car can retrieve theft statute using the canonical legal concept");
        check(noUnrelatedJudgments(stolenCar),
                "stolen-car query does not display judgments that omit the described item");

        System.out.println("CONCEPT GENERALIZATION SUMMARY | failures=" + failures);
        if (failures > 0) throw new AssertionError("Concept generalization regressions failed");
    }

    private static boolean topIs(SearchEngine engine, String query, String id) {
        SearchEngine.SearchRankedOutcome outcome = engine.searchRankedWithMetadata(query, SearchEngine.Algorithm.Z_ALGORITHM, 5);
        if (outcome.getResults().isEmpty()) return false;
        LegalDocument d = outcome.getResults().get(0).getDocument();
        return d != null && id.equals(d.getDocumentId());
    }

    private static boolean hasDoc(SearchEngine.SearchRankedOutcome outcome, String id) {
        for (int i = 0; i < outcome.getResults().size(); i++) {
            LegalDocument d = outcome.getResults().get(i).getDocument();
            if (d != null && id.equals(d.getDocumentId())) return true;
        }
        return false;
    }

    private static boolean noUnrelatedJudgments(SearchEngine.SearchRankedOutcome outcome) {
        for (int i = 0; i < outcome.getResults().size(); i++) {
            LegalDocument d = outcome.getResults().get(i).getDocument();
            if (d != null && "JUDGMENT".equalsIgnoreCase(d.getKind())
                    && !SituationInterpreter.judgmentSupportsTheftObject("my car was stolen", d.getTitle(), d.getBody())) return false;
        }
        return true;
    }

    private static void expectConcept(String query, String prefix) {
        SimpleList<String> concepts = SituationInterpreter.concepts(query);
        boolean found = false;
        for (int i = 0; i < concepts.size(); i++) if (concepts.get(i).startsWith(prefix)) found = true;
        check(found, "expected concept for: " + query + " -> " + conceptList(concepts));
    }

    private static void expectNoConcept(String query) {
        check(SituationInterpreter.concepts(query).isEmpty(), "no false event concept: " + query);
    }

    private static String conceptList(SimpleList<String> concepts) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < concepts.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(concepts.get(i));
        }
        return out.append("]").toString();
    }

    private static void check(boolean condition, String label) {
        System.out.println((condition ? "PASS" : "FAIL") + " | " + label);
        if (!condition) failures++;
    }
}


import loader.LegalCorpusLoader;
import algorithms.MillerRabin;
import algorithms.RandomizedSelect;
import analysis.ResearchPlanner;
import analysis.LegalProblemAnalyzer;
import analysis.CitationFlowAnalyzer;
import analysis.InformationDependencyMap;
import analysis.ResearchTaskScheduler;
import analysis.ResearchOptimizer;
import model.LegalDocument;
import search.QueryProcessor;
import search.SearchEngine;
import search.DocumentSimilarity;
import structures.SimpleList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    /* Compact mode is the professional default; detailed mode is opt-in. */
    private static boolean detailedMode = false;

    // Query-scoped research state: advanced commands must use the last
    // natural-language case, not a separate hard-coded demonstration.
    private static String currentResearchQuery = "";
    private static LegalProblemAnalyzer.AnalysisResult currentResearchAnalysis;
    private static InformationDependencyMap currentDependencyMap;
    private static SimpleList<SearchEngine.SearchResult> currentSearchResults = new SimpleList<>();
    private static SimpleList<SearchEngine.SearchResult> currentDisplayedSearchResults = new SimpleList<>();
    private static boolean currentQueryHasResearchContext = false;
    private static String currentResolvedSearchTerm = "";
    private static boolean currentTermOnlyQuery = false;
    private static SimpleList<LegalDocument> allCorpusDocuments = new SimpleList<>();

    private static final String LINE =
            "------------------------------------------------------------";

    private static final String DOUBLE_LINE =
            "============================================================";


    // ============================================================
    // GENERAL UI HELPERS
    // ============================================================

    private static void printSection(String title) {

        System.out.println();
        System.out.println(DOUBLE_LINE);
        System.out.println("  " + title);
        System.out.println(DOUBLE_LINE);
        System.out.println();
    }

    private static void printSubSection(String title) {

        System.out.println();
        System.out.println(LINE);
        System.out.println("  " + title);
        System.out.println(LINE);
        System.out.println();
    }

    private static void printFooter() {

        System.out.println();
        System.out.println(LINE);
        System.out.println();
    }


    // ============================================================
    // HEADER / WELCOME
    // ============================================================

    private static void printHeader() {

        System.out.println();
        System.out.println("+============================================================+");
        System.out.println("|                                                            |");
        System.out.println("|          LEGAL DOCUMENT INTELLIGENCE ENGINE                |");
        System.out.println("|              Indian Legal Research System                  |");
        System.out.println("|                                                            |");
        System.out.println("+============================================================+");
        System.out.println();
        System.out.println("  PURPOSE");
        System.out.println("    Search, analyze and organize an Indian legal corpus using");
        System.out.println("    DSA-based pattern matching and research-analysis methods.");
        System.out.println();
    }

    private static void printWelcome(int documentCount, int baselineCount, int statutoryCount) {

        printSection("SYSTEM STATUS");

        System.out.println("  Corpus status       : READY");
        System.out.println("  Documents available : " + documentCount);
        System.out.println("  Original baseline   : " + baselineCount);
        System.out.println("  Statutory excerpts  : " + statutoryCount);
        System.out.println("  Jurisdiction        : India");
        System.out.println("  Output mode         : " + (detailedMode ? "DETAILED" : "COMPACT"));

        System.out.println();
        System.out.println("  SUPPORTED INPUT");
        System.out.println("    • Recognized legal terms and phrases");
        System.out.println("    • Fuzzy / misspelled legal terms");
        System.out.println("    • Natural-language legal situations");

        System.out.println();
        System.out.println("  PIPELINE");
        System.out.println("    Query → Legal Analysis → Search → Ranking → Research Planning");

        System.out.println();
        System.out.println("  Type 'help' for the system guide.");
        System.out.println("  Type 'exit' to close the application.");
        printFooter();
    }

    // ============================================================
    // HELP
    // ============================================================

    private static void printHelp() {

        printSection("SYSTEM GUIDE");

        System.out.println("  WHAT CAN I SEARCH?");
        System.out.println();
        System.out.println("  1. Legal terms / phrases");
        System.out.println("     breach of contract");
        System.out.println("     search warrant");
        System.out.println("     cheque dishonour");
        System.out.println("     bail");
        System.out.println("     mortgage");
        System.out.println();
        System.out.println("  2. Natural-language situations");
        System.out.println("     my bank gave me a loan against my property");
        System.out.println("     my employer has not paid my salary for three months");
        System.out.println("     the owner refuses to return my rental deposit");
        System.out.println();
        System.out.println("  WHAT DOES THE ENGINE DO?");
        System.out.println();
        System.out.println("     1. Interpret the search request");
        System.out.println("     2. Identify possible legal concepts");
        System.out.println("     3. Search and rank corpus documents");
        System.out.println("     4. Explain why a result was returned");
        System.out.println("     5. Explore recorded case and Act links");
        System.out.println("     6. Organize follow-up research tasks");
        System.out.println();
        System.out.println("  RESEARCH TOOLS (run after a query)");
        System.out.println("     flow          Explore citation relationships across the corpus");
        System.out.println("     flow current  Show citation links connected to this query's results");
        System.out.println("     optimize      Choose how many retrieved documents to prioritize");
        System.out.println("     optimize N    Prioritize N documents (1-20) without a prompt");
        System.out.println("     schedule      Choose the available research time in hours");
        System.out.println("     schedule H    Build a plan within H hours (1-24) without a prompt");
        System.out.println("     session       Review the active query and research context");
        System.out.println("     compare       Compare text overlap between current results (optional)");
        System.out.println();
        System.out.println("  OUTPUT DETAIL");
        System.out.println("     compact       Concise results; recommended for normal use");
        System.out.println("     detailed      Expanded analysis and document evidence");
        System.out.println("     details       Alias for 'detailed'; show expanded results");
        System.out.println();
        System.out.println("  APPLICATION");
        System.out.println("     help       Show this guide");
        System.out.println("     exit       Close the engine");
        System.out.println();
        System.out.println("  Current mode: " + (detailedMode ? "DETAILED" : "COMPACT"));

        printFooter();
    }



    // ============================================================
    // MODE CONTROL
    // ============================================================

    private static void printModeChanged() {

        System.out.println();

        if (detailedMode) {
            System.out.println(
                    "  Output mode changed to: DETAILED"
            );
            System.out.println(
                    "  Full research and algorithmic pipeline is visible."
            );
        } else {
            System.out.println(
                    "  Output mode changed to: COMPACT"
            );
            System.out.println(
                    "  Search results are presented in concise form."
            );
        }

        System.out.println();
    }


    // ============================================================
    // QUERY ANALYSIS
    // ============================================================

    private static void printQueryAnalysis(
            QueryProcessor.QueryAnalysis analysis,
            LegalProblemAnalyzer.AnalysisResult problemAnalysis) {

        printSection("1. QUERY UNDERSTANDING");

        String queryType = friendlyQueryType(analysis, problemAnalysis);

        System.out.println("  INPUT");
        System.out.println("    " + analysis.getOriginalQuery());

        if (!analysis.getOriginalQuery()
                .equals(analysis.getNormalizedQuery())) {
            System.out.println();
            System.out.println("  NORMALIZED INPUT");
            System.out.println("    " + analysis.getNormalizedQuery());
        }

        System.out.println();
        System.out.println("  CLASSIFICATION");
        System.out.println("    " + queryType);
        if (!analysis.getResolvedTerms().isEmpty()) {

            System.out.println();
            System.out.println("  RESOLVED LEGAL CONCEPT");

            for (int i = 0; i < analysis.getResolvedTerms().size(); i++) {
                System.out.println(
                        "    • " + analysis.getResolvedTerms().get(i)
                );
            }
        }

        if (analysis.getType()
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            System.out.println();
            System.out.println("  FUZZY RESOLUTION");
            System.out.println("    Edit distance : " + analysis.getDistance());
            System.out.println(
                    "    Meaning       : The input is this many character-level edits"
            );
            System.out.println(
                    "                    away from the recognized legal term."
            );
        }

        System.out.println();
        System.out.println("  INTERPRETATION");

        if (analysis.getType()
                == QueryProcessor.QueryType.EXACT_LEGAL_TERM) {

            System.out.println(
                    "    The input directly matches a legal term in the system catalog."
            );

        } else if (analysis.getType()
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            System.out.println(
                    "    The input was interpreted as a likely spelling variation"
            );
            System.out.println(
                    "    of the resolved legal concept shown above."
            );

        } else if (problemAnalysis != null
                && !problemAnalysis.getIssues().isEmpty()) {

            System.out.println(
                    "    The input describes a legal situation rather than only"
            );
            System.out.println(
                    "    a catalog term, so situation-level analysis is applied."
            );

        } else {

            System.out.println(
                    "    The input is treated as a general text-search query."
            );
        }

        printFooter();
    }


    private static String friendlyQueryType(
            QueryProcessor.QueryAnalysis analysis,
            LegalProblemAnalyzer.AnalysisResult problemAnalysis) {

        if (analysis.getType()
                == QueryProcessor.QueryType.EXACT_LEGAL_TERM) {
            return "EXACT LEGAL TERM";
        }

        if (analysis.getType()
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {
            return "FUZZY LEGAL TERM";
        }

        if (problemAnalysis != null
                && !problemAnalysis.getIssues().isEmpty()) {
            return "NATURAL-LANGUAGE LEGAL SITUATION";
        }

        return "GENERAL TEXT SEARCH";
    }


    // ============================================================
    // SEARCH STRATEGY
    // ============================================================

    private static void printSearchStrategy(
            QueryProcessor.QueryType queryType,
            SearchEngine.Algorithm algorithm,
            int documentCount) {

        printSection("3. SEARCH OVERVIEW");
        System.out.println("  Searching " + documentCount + " legal documents.");
        System.out.println("  Documents are ordered by how closely their content matches your search.");
    }

    // ============================================================
    // UTILITY
    // ============================================================

    private static String stringifyList(
            SimpleList<String> values) {

        if (values == null || values.isEmpty()) {
            return "None";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                builder.append(", ");
            }

            builder.append(values.get(i));
        }

        return builder.toString();
    }


    // ============================================================
    // LEGAL PROBLEM ANALYSIS
    // ============================================================

    private static void printProblemAnalysis(
            LegalProblemAnalyzer.AnalysisResult analysis,
            ResearchPlanner planner,
            InformationDependencyMap dependencyMap) {

        printSection("2. LEGAL PROBLEM ANALYSIS");

        System.out.println("  REPORTED SITUATION");
        System.out.println("    " + analysis.getOriginalProblem());

        LegalProblemAnalyzer.LegalSituation situation =
                analysis.getSituation();

        if (analysis.getIssues().isEmpty()) {

            System.out.println();
            System.out.println("  ISSUE CLASSIFICATION");
            SimpleList<String> concepts = search.SituationInterpreter.concepts(analysis.getOriginalProblem());
            if (concepts.isEmpty()) {
                System.out.println("    No specific legal issue was confidently identified.");
            } else {
                System.out.println("    Possible situation (not a legal finding):");
                for (int i = 0; i < concepts.size(); i++) {
                    System.out.println("      • " + concepts.get(i));
                }
            }

            System.out.println();
            System.out.println("  INTERPRETATION");
            System.out.println("    The engine searches the loaded corpus for supporting material.");
            System.out.println("    If the topic is not covered, it will say so rather than invent an answer.");

        } else {

            System.out.println();
            System.out.println("  POTENTIAL LEGAL DOMAINS");

            for (int i = 0; i < analysis.getIssues().size(); i++) {
                System.out.println("    • " + analysis.getIssues().get(i));
            }

            System.out.println();
            System.out.println("  SITUATION SUMMARY");

            printLabeledList("Actors / entities", situation.getActors());
            printLabeledList("Actions / events", situation.getActions());
            printLabeledList("Objects / subjects", situation.getObjects());
            printLabeledList("Relationships", situation.getRelationships());
            printLabeledList("Legal domains", situation.getLegalDomains());
            printLabeledList("Possible objective", situation.getPossibleObjectives());

            System.out.println();
            System.out.println("  Problem event");
            System.out.println("    " + situation.getProblemEvent());

            System.out.println();
            printLabeledList("Evidence indicators", situation.getEvidenceIndicators());

            System.out.println();
            System.out.println("  Analysis confidence");
            System.out.println("    " + situation.getConfidence());
        }

        if (!analysis.getMissingInformation().isEmpty()) {

            System.out.println();
            System.out.println("  INFORMATION STILL NEEDED");

            for (int i = 0; i < analysis.getMissingInformation().size(); i++) {
                System.out.println(
                        "    [" + (i + 1) + "] "
                                + analysis.getMissingInformation().get(i)
                );
            }
        }

        if (!dependencyMap.getDependencies().isEmpty()) {

            System.out.println();
            System.out.println("  INFORMATION DEPENDENCY MAP");
            System.out.println(
                    "    Each dependency identifies information that would"
            );
            System.out.println(
                    "    strengthen or verify the research analysis."
            );

            for (int i = 0; i < dependencyMap.getDependencies().size(); i++) {

                InformationDependencyMap.Dependency dependency =
                        dependencyMap.getDependencies().get(i);

                System.out.println();
                System.out.println(
                        "    [" + (i + 1) + "] "
                                + dependency.getRequiredInformation()
                );
                System.out.println(
                        "        Research question : "
                                + dependency.getQuestion()
                );
                System.out.println(
                        "        Why it matters   : "
                                + dependency.getReason()
                );
            }
        }

        // The dependency map above already explains the next research steps.
        // Do not print a second near-identical plan here; the integrated
        // scheduler below presents the actionable sequence once.

        printFooter();
    }

    private static void printLabeledList(
            String label,
            SimpleList<String> values) {

        System.out.println(
                "    " + String.format("%-20s: %s", label, stringifyList(values))
        );
    }



    // ============================================================
    // LEGAL CONCEPTS
    // ============================================================

    private static void printConcepts(
            SimpleList<String> concepts) {

        printSubSection("LEGAL CONCEPTS IDENTIFIED");

        if (concepts == null || concepts.isEmpty()) {

            System.out.println("  - None");

        } else {

            for (int i = 0;
                 i < concepts.size();
                 i++) {

                System.out.println(
                        "  - " + concepts.get(i)
                );
            }
        }
    }


    // ============================================================
    // MEANINGFUL TERMS
    // ============================================================

    private static void printMeaningfulTerms(
            SimpleList<String> terms) {

        printSubSection("MEANINGFUL QUERY TERMS");

        if (terms == null || terms.isEmpty()) {

            System.out.println("  - None");

        } else {

            for (int i = 0;
                 i < terms.size();
                 i++) {

                System.out.println(
                        "  - " + terms.get(i)
                );
            }
        }
    }


    // ============================================================
    // SEARCH RESULTS
    // ============================================================

    private static void printSearchResultsHeader(
            int candidateCount,
            int displayedCount,
            boolean hasDirectMatch) {

        printSection("5. SEARCH RESULTS");

        System.out.println("  Documents ranked by relevance to your search.");
    }

    private static void printResult(
            SearchEngine.SearchResult result,
            int rank,
            boolean relevanceRankedRoute) {

        LegalDocument document =
                result.getDocument();

        System.out.println();
        System.out.println(
                "  [" + String.format("%02d", rank) + "] "
                        + document.getTitle()
        );
        System.out.println("  " + LINE);

        System.out.println(
                "    Match classification : "
                        + friendlyMatchType(result.getMatchType())
        );

        System.out.println(
                "    Ranking signal       : "
                        + result.getRelevanceScore()
        );

        System.out.println(
                "    Document ID          : "
                        + document.getDocumentId()
        );

        System.out.println(
                "    Document type        : "
                        + document.getDocumentType()
        );

        System.out.println(
                "    Jurisdiction         : "
                        + document.getJurisdiction()
        );

        System.out.println(
                "    Year                 : "
                        + document.getYear()
        );

        System.out.println(
                "    Source               : "
                        + document.getSource()
        );

        if (result.getMatchType() == SearchEngine.MatchType.CONTENT_REFERENCE) {
            System.out.println();
            System.out.println("    Why it appears       : A searched term occurs in the document text.");
            System.out.println("                           This alone does not mean the judgment decides that issue.");
        }
        printTermEvidenceExcerpt(result, "    Evidence excerpt     : ");

        SimpleList<String> matchedConcepts =
                result.getMatchedConcepts();

        if (matchedConcepts != null && !matchedConcepts.isEmpty()) {
            System.out.println();
            System.out.println("    Matched concepts");

            java.util.Set<String> printedConcepts = new java.util.LinkedHashSet<>();
            for (int i = 0; i < matchedConcepts.size(); i++) {
                String concept = matchedConcepts.get(i);
                if (concept != null && printedConcepts.add(concept.trim().toLowerCase(java.util.Locale.ROOT))) {
                    System.out.println("      • " + concept);
                }
            }
        }

        SimpleList<String> matchedTerms =
                result.getMatchedTerms();

        if (matchedTerms != null && !matchedTerms.isEmpty()) {
            System.out.println();
            System.out.println("    Matched terms");

            java.util.Set<String> printedTerms = new java.util.LinkedHashSet<>();
            for (int i = 0; i < matchedTerms.size(); i++) {
                String term = matchedTerms.get(i);
                if (term != null && printedTerms.add(term.trim().toLowerCase(java.util.Locale.ROOT))) {
                    System.out.println("      • " + term);
                }
            }
        }

        SimpleList<String> explanation =
                result.getExplanation();

        if (explanation != null && !explanation.isEmpty()) {
            System.out.println();
            System.out.println("    WHY THIS DOCUMENT APPEARS");

            for (int i = 0; i < explanation.size(); i++) {
                System.out.println("      • " + explanation.get(i));
            }
        }

        // Algorithm and character-offset diagnostics are internal details,
        // not part of a normal document search result.
    }

    private static void printTermEvidenceExcerpt(
            SearchEngine.SearchResult result,
            String label) {
        if (!currentTermOnlyQuery || currentResolvedSearchTerm == null
                || currentResolvedSearchTerm.trim().isEmpty()
                || result == null || result.getDocument() == null) {
            return;
        }
        String body = result.getDocument().getBody();
        if (body == null || body.isEmpty()) return;
        String term = currentResolvedSearchTerm.trim();
        int index = body.toLowerCase(java.util.Locale.ROOT)
                .indexOf(term.toLowerCase(java.util.Locale.ROOT));
        if (index < 0) {
            // Some corpus documents contain only a variant of the resolved term.
            SimpleList<String> terms = result.getMatchedTerms();
            if (terms != null) {
                for (int i = 0; i < terms.size() && index < 0; i++) {
                    String candidate = terms.get(i);
                    if (candidate == null || candidate.trim().length() < 4) continue;
                    term = candidate.trim();
                    index = body.toLowerCase(java.util.Locale.ROOT)
                            .indexOf(term.toLowerCase(java.util.Locale.ROOT));
                }
            }
        }
        if (index < 0) return;
        int start = Math.max(0, index - 75);
        int end = Math.min(body.length(), index + term.length() + 100);
        String excerpt = body.substring(start, end).replaceAll("\\s+", " ").trim();
        if (start > 0) excerpt = "…" + excerpt;
        if (end < body.length()) excerpt = excerpt + "…";
        System.out.println(label + excerpt);
    }

    private static String friendlyMatchType(
            SearchEngine.MatchType type) {

        // Exact and typo-corrected term searches should describe an incidental
        // body occurrence consistently, rather than exposing different labels
        // merely because one query took the fuzzy route.
        if (currentTermOnlyQuery && type != SearchEngine.MatchType.TITLE_MATCH) {
            return "TERM MENTIONED IN TEXT";
        }

        switch (type) {
            case TITLE_MATCH:
                return "DIRECT / TITLE";
            case CONTENT_REFERENCE:
                return "TERM MENTIONED IN TEXT";
            case DIRECT_MATCH:
                return "DIRECT";
            case RELATED_MATCH:
                return "RELATED";
            case WEAK_MATCH:
                return "WEAK / BACKGROUND";
            default:
                return type.toString();
        }
    }

    private static void printInterpretation(
            SearchEngine.SearchRankedOutcome rankedOutcome,
            SimpleList<SearchEngine.SearchResult> results,
            String query,
            boolean directMatch) {

        printSection("9. RESULT INTERPRETATION");

        if (results.isEmpty()) {
            System.out.println(
                    "  The current corpus did not return a document for this query."
            );
            System.out.println();
            System.out.println(
                    "  This does NOT establish that no applicable law exists."
            );
            System.out.println(
                    "  It only means that no qualifying document was found"
            );
            System.out.println(
                    "  in the currently loaded corpus/search configuration."
            );
            printFooter();
            return;
        }

        boolean direct = directMatch;

        if (direct) {
            System.out.println(
                    "  The engine identified at least one document with a"
            );
            System.out.println(
                    "  direct relevance signal for the requested query."
            );
        } else {
            System.out.println(
                    "  No direct legal-document match was established."
            );
            System.out.println(
                    "  The returned material should be treated as related"
            );
            System.out.println(
                    "  or background research rather than a confirmed answer."
            );
        }

        System.out.println();
        System.out.println("  IMPORTANT");
        System.out.println(
                "    Relevance scores are internal ranking signals."
        );
        System.out.println(
                "    They are not probabilities and do not establish"
        );
        System.out.println(
                "    legal correctness, liability, or a final legal conclusion."
        );

        System.out.println();
        System.out.println(
                "  Corpus scope"
        );
        System.out.println(
                "    Results are limited to the documents currently loaded"
        );
        System.out.println(
                "    by this application."
        );

        System.out.println();
        System.out.println(
                "  Research-use notice"
        );
        System.out.println(
                "    The engine supports legal research and document discovery;"
        );
        System.out.println(
                "    it does not replace professional legal advice."
        );

        printFooter();
    }



    // ============================================================
    // OPTIMIZED RESEARCH PACK
    // ============================================================

    private static void printIntegratedOptimizationResult(
            ResearchOptimizer.OptimizationResult result,
            LegalProblemAnalyzer.AnalysisResult analysis,
            int budget) {

        printSection("6. RESEARCH OPTIMIZATION");

        System.out.println("  PURPOSE");
        System.out.println(
                "    Select a small set of documents that covers as many"
        );
        System.out.println(
                "    identified research questions as possible within a"
        );
        System.out.println(
                "    fixed review budget."
        );

        System.out.println();
        System.out.println("  DOCUMENT REVIEW BUDGET : " + budget);
        System.out.println(
                "  RESEARCH QUESTIONS     : "
                        + analysis.getQuestions().size()
        );
        System.out.println(
                "  POTENTIAL QUESTION MATCHES : "
                        + result.getCoveredQuestions().size() + "/"
                        + analysis.getQuestions().size()
                        + " (textual signals only)"
        );

        System.out.println();
        System.out.println("  SELECTED DOCUMENTS");

        if (result.getSelectedDocuments().isEmpty()) {
            System.out.println("    None selected from the retrieved set.");
        } else {
            for (int i = 0; i < result.getSelectedDocuments().size(); i++) {
                System.out.println(
                        "    [" + (i + 1) + "] "
                                + result.getSelectedDocuments().get(i).getTitle()
                );
            }
        }

        System.out.println();
        System.out.println("  POTENTIAL COVERAGE");

        if (result.getCoveredQuestions().isEmpty()) {
            System.out.println("    No research question received a qualifying keyword match.");
        } else {
            for (int i = 0; i < result.getCoveredQuestions().size(); i++) {
                System.out.println(
                        "    Potentially covered • " + result.getCoveredQuestions().get(i)
                );
            }
        }

        if (!result.getUncoveredQuestions().isEmpty()) {
            System.out.println();
            System.out.println("    No qualifying match found");

            for (int i = 0; i < result.getUncoveredQuestions().size(); i++) {
                System.out.println(
                        "      • " + result.getUncoveredQuestions().get(i)
                );
            }
        }

        System.out.println();
        System.out.println("  Note: coverage is a textual relevance signal, not proof that a document answers a question.");
    }



    // ============================================================
    // RESEARCH SCHEDULE
    // ============================================================

    private static void printIntegratedScheduleResult(
            ResearchTaskScheduler.ScheduleResult result,
            int capacityHours) {

        printSection("7. RESEARCH SCHEDULING");

        System.out.println("  PURPOSE");
        System.out.println(
                "    Arrange identified research tasks within the available"
        );
        System.out.println(
                "    research-time capacity."
        );

        System.out.println();
        System.out.println("  AVAILABLE TIME : " + capacityHours + " hours");
        System.out.println(
                "  PLANNED TIME   : "
                        + result.getTotalDuration()
                        + " hours"
        );

        System.out.println();
        System.out.println("  SCHEDULE");

        if (result.getScheduledTasks().isEmpty()) {
            System.out.println("    No research task fits the current capacity.");
        } else {
            for (int i = 0; i < result.getScheduledTasks().size(); i++) {

                ResearchTaskScheduler.ResearchTask task =
                        result.getScheduledTasks().get(i);

                System.out.println(
                        "    [" + (i + 1) + "] "
                                + task.getTitle()
                                + "  • "
                                + task.getDuration()
                                + "h"
                );
            }
        }
    }



    // ============================================================
    // DOCUMENT SIMILARITY
    // ============================================================

    private static void printDocumentSimilarity(
            SimpleList<SearchEngine.SearchResult> results) {

        printSection("8. DOCUMENT SIMILARITY");

        System.out.println("  METHOD");
        System.out.println("    Suffix Array + LCP (Kasai) over substantive document bodies");
        System.out.println("  NOTE");
        System.out.println("    Similarity measures shared text, not legal relevance or whether the documents answer the query.");

        System.out.println();
        if (results.size() < 2) {
            System.out.println();
            System.out.println(
                    "  At least two retrieved documents are required for comparison."
            );
            return;
        }

        LegalDocument baseDocument =
                results.get(0).getDocument();

        System.out.println();
        System.out.println("  REFERENCE DOCUMENT");
        System.out.println("    " + baseDocument.getTitle());

        int comparisonLimit = Math.min(results.size(), 4); // reference + 3 comparisons
        for (int i = 1; i < comparisonLimit; i++) {

            LegalDocument otherDocument =
                    results.get(i).getDocument();

            double similarity =
                    DocumentSimilarity.similarity(
                            baseDocument.getBody(),
                            otherDocument.getBody()
                    );

            int longestShared =
                    DocumentSimilarity.longestCommonSequence(
                            baseDocument.getBody(),
                            otherDocument.getBody()
                    );

            System.out.println();
            System.out.println(
                    "  COMPARED DOCUMENT [" + i + "]"
            );
            System.out.println(
                    "    " + otherDocument.getTitle()
            );
            System.out.println(
                    "    Similarity : "
                            + String.format("%.4f%%", similarity * 100.0)
            );
            System.out.println(
                    "    Longest shared sequence : "
                            + longestShared
                            + " characters"
            );
            System.out.println(
                    "    Interpretation : "
                            + similarityInterpretation(similarity)
            );
        }
        if (results.size() > comparisonLimit) {
            System.out.println();
            System.out.println("  Compared the top 3 alternatives; remaining results omitted to keep the report focused.");
        }
    }

    private static String similarityInterpretation(double similarity) {
        double percentage = similarity * 100.0;

        if (percentage >= 50.0) {
            return "HIGH textual overlap";
        }
        if (percentage >= 20.0) {
            return "MODERATE textual overlap";
        }
        if (percentage >= 5.0) {
            return "LOW textual overlap";
        }
        return "VERY LOW textual overlap";
    }



    // ============================================================
    // CITATION FLOW
    // ============================================================

    private static String friendlyCorpusDocumentName(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) return rawValue;
        String rawId = rawValue.trim();
        String countSuffix = "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\s*\\((\\d+)\\)\\s*$").matcher(rawId);
        if (matcher.find()) {
            countSuffix = " (" + matcher.group(1) + " references)";
            rawId = rawId.substring(0, matcher.start()).trim();
        }
        String target = normalizeForFlow(rawId).replace("judgment", "judg");
        for (int i = 0; i < allCorpusDocuments.size(); i++) {
            LegalDocument document = allCorpusDocuments.get(i);
            String candidate = normalizeForFlow(document.getDocumentId()).replace("judgment", "judg");
            if (target.equals(candidate)) {
                return document.getTitle() + " (" + document.getDocumentId() + ")" + countSuffix;
            }
        }
        // External Acts may exist in the citation metadata without a full
        // document in the loaded corpus. Use the recorded Act title rather
        // than exposing an opaque EXTACT identifier when possible.
        java.nio.file.Path csvPath = java.nio.file.Paths.get("results/judgment_act_relationships.csv");
        if (java.nio.file.Files.exists(csvPath)) {
            try (java.io.BufferedReader csv = java.nio.file.Files.newBufferedReader(csvPath)) {
                csv.readLine();
                String line;
                while ((line = csv.readLine()) != null) {
                    String[] row = parseFlowCsvLine(line);
                    if (row.length >= 3
                            && target.equals(normalizeForFlow(row[1]).replace("judgment", "judg"))) {
                        return row[2] + " (" + rawId + ")" + countSuffix;
                    }
                }
            } catch (Exception ignored) { }
        }
        return rawValue;
    }

    private static void printCitationFlowResult(
            CitationFlowAnalyzer.CitationFlowResult result) {

        printSection("CORPUS CITATION NETWORK");

        System.out.println("  What this shows: recorded links between judgments and Acts in the corpus.");
        System.out.println("  Why it helps: it can reveal which statutes are frequently cited and which judgments refer to them.");
        System.out.println("  How to use it: use this as a research-navigation aid, then open the underlying documents.");
        System.out.println("  Scope: this is corpus-wide structure, not a claim that every linked Act applies to your facts.");
        System.out.println();
        System.out.println(
                "  Judgments loaded      : "
                        + result.getJudgmentCount()
        );

        System.out.println(
                "  Acts loaded           : "
                        + result.getActCount()
        );

        System.out.println(
                "  Relationships loaded  : "
                        + result.getEdgeCount()
        );

        System.out.println(
                "  Citation mentions     : "
                        + result.getTotalReferenceCount()
        );

        System.out.println(
                "  Maximum matching      : "
                        + result.getMaximumMatching()
                        + " judgment-Act pairs"
        );

        System.out.println();
        System.out.println(
                "  Interpretation:"
        );

        System.out.println(
                "    Largest one-to-one pairing of judgments "
                        + "and Acts"
        );

        System.out.println(
                "    connected by a citation relationship."
        );

        System.out.println();
        System.out.println(
                "  Note:"
        );

        System.out.println(
                "    Raw citation counts describe volume;"
        );

        System.out.println(
                "    matching is the structure-dependent "
                        + "graph result."
        );

        System.out.println();
        System.out.println(
                "  Top judgments by outgoing references:"
        );

        if (result.getTopJudgments().isEmpty()) {

            System.out.println(
                    "    - None"
            );

        } else {

            for (int i = 0;
                 i < result.getTopJudgments().size();
                 i++) {

                System.out.println(
                        "    - "
                                + friendlyCorpusDocumentName(result.getTopJudgments().get(i))
                );
            }
        }

        System.out.println();
        System.out.println(
                "  Top Acts by incoming references:"
        );

        if (result.getTopActs().isEmpty()) {

            System.out.println(
                    "    - None"
            );

        } else {

            for (int i = 0;
                 i < result.getTopActs().size();
                 i++) {

                System.out.println(
                        "    - "
                                + friendlyCorpusDocumentName(result.getTopActs().get(i))
                );
            }
        }
    }


    // ============================================================
    private static String[] parseFlowCsvLine(String line) {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else field.append(c);
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }

    private static String normalizeForFlow(String value) {
        if (value == null) return "";
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "").trim();
    }

    private static boolean containsResultDocumentId(String id) {
        if (id == null || id.isEmpty()) return false;
        String normalizedId = normalizeForFlow(id);
        for (int i = 0; i < currentDisplayedSearchResults.size(); i++) {
            LegalDocument document = currentDisplayedSearchResults.get(i).getDocument();
            String docId = normalizeForFlow(document.getDocumentId());
            String canonicalDocId = docId.replace("judgment", "judg");
            String canonicalInputId = normalizedId.replace("judgment", "judg");
            if (!docId.isEmpty() && (canonicalDocId.equals(canonicalInputId)
                    || canonicalDocId.endsWith(canonicalInputId)
                    || canonicalInputId.endsWith(canonicalDocId))) return true;
            String title = normalizeForFlow(document.getTitle());
            if (title.equals(normalizedId) && !title.isEmpty()) return true;
        }
        return false;
    }

    private static String getRetrievedJudgmentTitle(String judgmentId) {
        String target = normalizeForFlow(judgmentId).replace("judgment", "judg");
        for (int i = 0; i < currentDisplayedSearchResults.size(); i++) {
            LegalDocument document = currentDisplayedSearchResults.get(i).getDocument();
            String candidate = normalizeForFlow(document.getDocumentId()).replace("judgment", "judg");
            if (target.equals(candidate)) return document.getTitle();
        }
        return "";
    }

    private static boolean matchesRetrievedActTitle(String actTitle) {
        String normalizedAct = normalizeForFlow(actTitle);
        if (normalizedAct.length() < 8) return false;
        for (int i = 0; i < currentDisplayedSearchResults.size(); i++) {
            String title = normalizeForFlow(currentDisplayedSearchResults.get(i).getDocument().getTitle());
            if (title.length() >= 8 && (title.contains(normalizedAct) || normalizedAct.contains(title))) return true;
        }
        return false;
    }

    private static boolean hasTextSupportForActLink(
            String judgmentId,
            String actTitle,
            String aliases) {
        String target = normalizeForFlow(judgmentId).replace("judgment", "judg");
        for (int i = 0; i < currentDisplayedSearchResults.size(); i++) {
            LegalDocument document = currentDisplayedSearchResults.get(i).getDocument();
            String candidate = normalizeForFlow(document.getDocumentId()).replace("judgment", "judg");
            if (!target.equals(candidate)) continue;
            String body = normalizeForFlow(document.getBody());
            if (body.isEmpty()) return false;
            if (containsNormalizedPhrase(body, actTitle)) return true;
            if (aliases != null && !aliases.trim().isEmpty()) {
                String[] aliasParts = aliases.split("[;|]");
                for (String alias : aliasParts) {
                    if (containsNormalizedPhrase(body, alias)) return true;
                }
            }
            // A relationship row without a matching Act title/alias in the
            // judgment text is metadata-only, so do not present it as a
            // verified citation for the current result.
            return false;
        }
        return true;
    }

    private static boolean containsNormalizedPhrase(String normalizedBody, String phrase) {
        String normalizedPhrase = normalizeForFlow(phrase);
        return normalizedPhrase.length() >= 8 && normalizedBody.contains(normalizedPhrase);
    }

    private static void printCurrentCitationFlow() {
        printSection("CITATION LINKS FOR CURRENT RESULTS");
        if (currentResearchQuery == null || currentResearchQuery.trim().isEmpty()
                || currentSearchResults.isEmpty()) {
            System.out.println("  No search results are active yet. Enter a query, then run 'flow current'.");
            System.out.println("  Use 'flow' at any time to explore the wider corpus citation network.");
            return;
        }

        System.out.println("  Query: " + currentResearchQuery);
        System.out.println("  Visible search results considered: " + currentDisplayedSearchResults.size());
        System.out.println("  These are relationships recorded in the corpus metadata for the displayed documents.");
        System.out.println("  They may not relate to the search topic; verify the judgment text before relying on a link.");
        boolean hasStrongResult = false;
        for (int i = 0; i < currentDisplayedSearchResults.size(); i++) {
            SearchEngine.MatchType type = currentDisplayedSearchResults.get(i).getMatchType();
            if (type == SearchEngine.MatchType.DIRECT_MATCH
                    || type == SearchEngine.MatchType.TITLE_MATCH) {
                hasStrongResult = true;
                break;
            }
        }
        if (!hasStrongResult) {
            System.out.println();
            System.out.println("  No case-specific citation links are shown because the current results are only term mentions or related/background material.");
            System.out.println("  Use 'flow' to explore the corpus-wide citation network, or search for a more specific legal term.");
            return;
        }
        System.out.println();

        java.nio.file.Path path = java.nio.file.Paths.get("results/judgment_act_relationships.csv");
        if (!java.nio.file.Files.exists(path)) {
            System.out.println("  Citation relationship data is unavailable: " + path.toAbsolutePath());
            return;
        }

        int shown = 0;
        int matchingRows = 0;
        int metadataOnlyRows = 0;
        try (java.io.BufferedReader csv = java.nio.file.Files.newBufferedReader(path)) {
            String line = csv.readLine();
            while ((line = csv.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] row = parseFlowCsvLine(line);
                if (row.length < 7) continue;
                String judgmentId = row[0].trim();
                String actTitle = row[2].trim();
                boolean retrievedJudgment = containsResultDocumentId(judgmentId);
                boolean retrievedAct = matchesRetrievedActTitle(actTitle);
                if (!retrievedJudgment && !retrievedAct) continue;
                if (retrievedJudgment && !hasTextSupportForActLink(judgmentId, actTitle, row[5])) {
                    metadataOnlyRows++;
                    continue;
                }
                matchingRows++;
                if (shown < 10) {
                    String endpointLabel = retrievedJudgment && retrievedAct
                            ? "both ends shown" : (retrievedJudgment ? "judgment shown" : "Act shown");
                    String judgmentTitle = getRetrievedJudgmentTitle(judgmentId);
                    String judgmentLabel = judgmentTitle == null || judgmentTitle.isEmpty()
                            ? judgmentId : judgmentTitle + " (" + judgmentId + ")";
                    System.out.print("  " + (shown + 1) + ". " + judgmentLabel + " -> " + actTitle
                            + " [" + endpointLabel + "]");
                    if (!row[6].trim().isEmpty()) System.out.print(" | recorded sections: " + row[6].trim());
                    System.out.println();
                    shown++;
                }
            }
        } catch (Exception e) {
            System.out.println("  Could not read citation relationships: " + e.getMessage());
            return;
        }

        if (matchingRows == 0) {
            System.out.println("  No text-supported citation links were found for the displayed results.");
            if (metadataOnlyRows > 0) {
                System.out.println("  " + metadataOnlyRows + " metadata relationship(s) were hidden because the Act title/alias was not found in the judgment text.");
            }
            System.out.println("  This does not prove that no legal relationship exists; the loaded text or metadata may be incomplete.");
        } else {
            if (matchingRows > shown) System.out.println("  Showing " + shown + " of " + matchingRows + " matching relationship rows.");
            System.out.println();
            System.out.println("  Why it helps: follow a cited Act from a displayed judgment, or explore judgments linked to a displayed Act.");
            System.out.println("  Important: a citation is a navigation clue, not proof that the Act applies to the facts. Verify the source text and current law.");
        }
    }

    private static int promptNumber(BufferedReader reader, String prompt, int min, int max) throws IOException {
        while (true) {
            System.out.print(prompt);
            String value = readLine(reader);
            if (value == null || value.trim().equalsIgnoreCase("cancel")) return -1;
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed >= min && parsed <= max) return parsed;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Enter a whole number from " + min + " to " + max + ", or type 'cancel'.");
        }
    }

    private static int parseCommandNumber(String command, int fallback, int min, int max) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length < 2) return fallback;
        try {
            int value = Integer.parseInt(parts[1]);
            if (value < min || value > max) {
                System.out.println("  Value must be between " + min + " and " + max + ". Using default " + fallback + ".");
                return fallback;
            }
            return value;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static void printCurrentResearchSession() {
        printSection("CURRENT RESEARCH SESSION");
        if (currentResearchQuery.isEmpty()) {
            System.out.println("  No query has been processed yet. Enter a legal situation first.");
            return;
        }
        System.out.println("  Query: " + currentResearchQuery);
        System.out.println("  Research context: " + (currentQueryHasResearchContext ? "AVAILABLE" : "TERM/GENERAL SEARCH"));
        System.out.println("  Retrieved documents: " + currentSearchResults.size());
        if (currentQueryHasResearchContext && currentResearchAnalysis != null) {
            System.out.println("  Research questions: " + currentResearchAnalysis.getQuestions().size());
            for (int i = 0; i < currentResearchAnalysis.getQuestions().size(); i++) {
                System.out.println("    [" + (i + 1) + "] " + currentResearchAnalysis.getQuestions().get(i));
            }
            System.out.println("  Commands: optimize, schedule, flow current");
        } else {
            System.out.println("  Advanced research planning is available after a situation-level query is detected.");
        }
    }

    private static void runCurrentOptimization(int budget) {
        if (!currentQueryHasResearchContext || currentResearchAnalysis == null) {
            System.out.println("  No active legal-situation session. Enter a natural-language legal situation before running optimize.");
            return;
        }
        SimpleList<LegalDocument> candidates = new SimpleList<>();
        int[] tiers = new int[currentSearchResults.size()];
        for (int i = 0; i < currentSearchResults.size(); i++) {
            SearchEngine.SearchResult result = currentSearchResults.get(i);
            candidates.add(result.getDocument());
            SearchEngine.MatchType type = result.getMatchType();
            if (type == SearchEngine.MatchType.DIRECT_MATCH || type == SearchEngine.MatchType.TITLE_MATCH) tiers[i] = 2;
            else if (type == SearchEngine.MatchType.CONTENT_REFERENCE || type == SearchEngine.MatchType.RELATED_MATCH) tiers[i] = 1;
            else tiers[i] = 0;
        }
        ResearchOptimizer.OptimizationResult result = new ResearchOptimizer().optimize(currentResearchAnalysis, candidates, budget, tiers);
        printSection("CURRENT-CASE RESEARCH OPTIMIZATION");
        System.out.println("  Query: " + currentResearchQuery);
        System.out.println("  Review budget: up to " + budget + " documents");
        System.out.println("  Selected: " + result.getSelectedDocuments().size());
        for (int i = 0; i < result.getSelectedDocuments().size(); i++) {
            LegalDocument selected = result.getSelectedDocuments().get(i);
            String matchSummary = "candidate; relevance not independently verified";
            for (int j = 0; j < currentSearchResults.size(); j++) {
                SearchEngine.SearchResult sourceResult = currentSearchResults.get(j);
                if (sourceResult.getDocument().getDocumentId().equals(selected.getDocumentId())) {
                    matchSummary = sourceResult.getMatchType() + " | relevance signal " + sourceResult.getRelevanceScore();
                    break;
                }
            }
            System.out.println("    [" + (i + 1) + "] " + selected.getTitle());
            System.out.println("        " + selected.getDocumentType() + " | " + matchSummary);
        }
        System.out.println("  Heuristic question-coverage signals: " + result.getCoveredQuestions().size() + "/" + currentResearchAnalysis.getQuestions().size());
        for (int i = 0; i < result.getCoveredQuestions().size(); i++) System.out.println("    Potential signal: " + result.getCoveredQuestions().get(i));
        for (int i = 0; i < result.getUncoveredQuestions().size(); i++) System.out.println("    No qualifying match: " + result.getUncoveredQuestions().get(i));
        System.out.println("  Note: textual coverage is heuristic, not verification that a document answers a question.");
    }

    private static void runCurrentSchedule(int hours) {
        if (!currentQueryHasResearchContext || currentResearchAnalysis == null || currentDependencyMap == null) {
            System.out.println("  No active legal-situation session. Enter a natural-language legal situation before running schedule.");
            return;
        }
        ResearchTaskScheduler.ScheduleResult result = new ResearchTaskScheduler().scheduleFromAnalysis(currentResearchAnalysis, currentDependencyMap, hours);
        printSection("CURRENT-CASE RESEARCH SCHEDULE");
        System.out.println("  Query: " + currentResearchQuery);
        System.out.println("  Available time: " + hours + " hours | Planned: " + result.getTotalDuration() + " hours");
        for (int i = 0; i < result.getScheduledTasks().size(); i++) {
            ResearchTaskScheduler.ResearchTask task = result.getScheduledTasks().get(i);
            System.out.println("    [" + (i + 1) + "] " + task.getTitle() + " — " + task.getDuration() + "h");
        }
        for (int i = 0; i < result.getSkippedTasks().size(); i++) System.out.println("    Deferred: " + result.getSkippedTasks().get(i));
        System.out.println("  Durations are planning estimates, not guaranteed real-world timings.");
    }

    private static void runStandaloneAlgorithmDemo() {
        printSection("STANDALONE ALGORITHM DEMONSTRATIONS");
        System.out.println("  Miller-Rabin primality test (deterministic witness set for 64-bit inputs)");
        System.out.println("    97  -> " + (MillerRabin.isPrime(97) ? "prime" : "composite"));
        System.out.println("    91  -> " + (MillerRabin.isPrime(91) ? "prime" : "composite"));

        double[] scores = {42.0, 91.0, 67.0, 91.0};
        int[] stableTieBreak = {0, 1, 2, 3};
        int[] selected = new RandomizedSelect(20261011L).topK(scores, stableTieBreak, 2);
        System.out.println();
        System.out.println("  Randomized Quickselect / top-K selection");
        System.out.println("    Scores: [42, 91, 67, 91]");
        System.out.print("    Selected indices (best first): ");
        for (int i = 0; i < selected.length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(selected[i]);
        }
        System.out.println();
        System.out.println("    The same top-K selector is used by relevance ranking to retain the best candidates.");
        System.out.println();
        System.out.println("  Execution note");
        System.out.println("    Corpus scanning is sequential in this version; parallel search/reduction is not claimed.");
        printFooter();
    }

    // MAIN QUERY PIPELINE
    // ============================================================

    private static void performQuery(
            SearchEngine engine,
            QueryProcessor processor,
            String originalQuery,
            CitationFlowAnalyzer analyzer,
            SimpleList<LegalDocument> documents,
            BufferedReader reader)
            throws IOException {

        String query = originalQuery.trim();

        // --------------------------------------------------------
        // DSA / RESEARCH COMMANDS
        // --------------------------------------------------------

        if (query.equalsIgnoreCase("session")) {
            printCurrentResearchSession();
            return;
        }

        if (query.equalsIgnoreCase("flow current")) {
            printCurrentCitationFlow();
            return;
        }

        if (query.toLowerCase().startsWith("flow ")
                && !query.equalsIgnoreCase("flow current")) {
            System.out.println("  Usage: flow | flow current");
            return;
        }

        if (query.equalsIgnoreCase("flow")) {

            try {
                CitationFlowAnalyzer.CitationFlowResult result =
                        analyzer.analyze(
                                "results/judgment_act_relationships.csv"
                        );

                printCitationFlowResult(result);

            } catch (Exception e) {
                System.out.println();
                System.out.println("  Citation-flow analysis could not be completed.");
                System.out.println("  Reason: " + e.getMessage());
                printFooter();
            }

            return;
        }

        if (query.equalsIgnoreCase("compare")) {
            if (currentSearchResults == null || currentSearchResults.size() < 2) {
                System.out.println("  Search for documents first; comparison needs at least two results.");
            } else {
                printDocumentSimilarity(currentSearchResults);
            }
            return;
        }
        if (query.equalsIgnoreCase("algorithms")) {
            System.out.println("  Algorithm demonstrations are available in the source code and test suite, not the search interface.");
            return;
        }

        if (query.toLowerCase().startsWith("schedule ")
                && !query.toLowerCase().matches("schedule\\s+\\d+")) {
            System.out.println("  Usage: schedule | schedule H (H must be 1-24)");
            return;
        }

        if (query.toLowerCase().startsWith("optimize ")
                && !query.toLowerCase().matches("optimize\\s+\\d+")) {
            System.out.println("  Usage: optimize | optimize N (N must be 1-20)");
            return;
        }

        if (query.toLowerCase().matches("schedule(?:\\s+\\d+)?")) {
            if (!currentQueryHasResearchContext || currentResearchAnalysis == null || currentDependencyMap == null) {
                runCurrentSchedule(6);
                return;
            }
            int hours;
            if (query.trim().equalsIgnoreCase("schedule")) {
                hours = promptNumber(reader, "  How many hours can you spend on this research? (1-24, or cancel): ", 1, 24);
                if (hours < 0) return;
            } else {
                hours = parseCommandNumber(query, 6, 1, 24);
            }
            runCurrentSchedule(hours);
            return;
        }

        if (query.toLowerCase().matches("optimize(?:\\s+\\d+)?")) {
            if (!currentQueryHasResearchContext || currentResearchAnalysis == null) {
                runCurrentOptimization(3);
                return;
            }
            int budget;
            if (query.trim().equalsIgnoreCase("optimize")) {
                budget = promptNumber(reader, "  How many documents should be prioritized? (1-20, or cancel): ", 1, 20);
                if (budget < 0) return;
            } else {
                budget = parseCommandNumber(query, 3, 1, 20);
            }
            runCurrentOptimization(budget);
            return;
        }

        if (query.isEmpty()) {
            System.out.println();
            System.out.println("  Query cannot be empty. Type 'help' for examples.");
            printFooter();
            return;
        }

        // --------------------------------------------------------
        // QUERY UNDERSTANDING
        // --------------------------------------------------------

        QueryProcessor.QueryAnalysis analysis =
                processor.analyze(query);

        if (analysis.getType()
                == QueryProcessor.QueryType.INVALID) {

            System.out.println();
            System.out.println("  The query could not be processed.");
            printFooter();
            return;
        }

        LegalProblemAnalyzer problemAnalyzer =
                new LegalProblemAnalyzer();

        LegalProblemAnalyzer.AnalysisResult problemAnalysis =
                problemAnalyzer.analyze(query);

        SearchEngine.Algorithm selectedAlgorithm =
                SearchEngine.chooseAlgorithm(
                        analysis.getType()
                );

        if (!detailedMode) {
            runCompactSearch(
                    engine,
                    analysis,
                    selectedAlgorithm,
                    problemAnalysis
            );
            return;
        }

        printQueryAnalysis(
                analysis,
                problemAnalysis
        );

        /*
         * A catalog term is a search request, not automatically a
         * factual legal dispute. For example, "ragging" should show
         * its fuzzy resolution and corpus result without inventing
         * a detailed incident analysis.
         */
        boolean catalogSearch =
                analysis.getType()
                        == QueryProcessor.QueryType.EXACT_LEGAL_TERM
                        || analysis.getType()
                        == QueryProcessor.QueryType.FUZZY_LEGAL_TERM;

        boolean scenarioAnalysis =
                !catalogSearch
                        && !problemAnalysis.getIssues().isEmpty();

        if (scenarioAnalysis) {

            InformationDependencyMap dependencyMap =
                    new InformationDependencyMap();

            dependencyMap.build(problemAnalysis);

            ResearchPlanner researchPlanner =
                    new ResearchPlanner();

            printProblemAnalysis(
                    problemAnalysis,
                    researchPlanner,
                    dependencyMap
            );

            printSearchStrategy(
                    analysis.getType(),
                    selectedAlgorithm,
                    documents.size()
            );

            // Search strategy is intentionally placed after the
            // problem interpretation for a natural-language query.

            runDetailedSearchPipeline(
                    engine,
                    query,
                    analysis,
                    problemAnalysis,
                    selectedAlgorithm,
                    documents,
                    dependencyMap,
                    true
            );

        } else {

            if (catalogSearch) {
                printCatalogSearchInterpretation(analysis);
            } else {
                printProblemAnalysisNotice(problemAnalysis);
            }

            printSearchStrategy(
                    analysis.getType(),
                    selectedAlgorithm,
                    documents.size()
            );

            runDetailedSearchPipeline(
                    engine,
                    query,
                    analysis,
                    problemAnalysis,
                    selectedAlgorithm,
                    documents,
                    null,
                    false
            );
        }
    }


    private static void printCatalogSearchInterpretation(
            QueryProcessor.QueryAnalysis analysis) {

        printSection("2. SEARCH INTERPRETATION");

        System.out.println("  This input is being treated as a legal-term search.");

        if (analysis.getType()
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            System.out.println();
            System.out.println("  The engine first resolved the entered term");
            System.out.println(
                    "  and then searches the corpus using the resolved concept."
            );

        } else {

            System.out.println();
            System.out.println(
                    "  No factual incident has been inferred from the term alone."
            );
            System.out.println(
                    "  This avoids inventing legal facts that the user did not provide."
            );
        }

        printFooter();
    }


    private static void printProblemAnalysisNotice(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        printSection("2. LEGAL PROBLEM ANALYSIS");

        SimpleList<String> recognizedConcepts =
                search.SituationInterpreter.concepts(analysis.getOriginalProblem());
        if (!recognizedConcepts.isEmpty()) {
            System.out.println("  POSSIBLE SITUATION (interpretation, not a legal finding)");
            for (int i = 0; i < recognizedConcepts.size(); i++) {
                System.out.println("    • " + recognizedConcepts.get(i));
            }
            System.out.println("  The interpretation is tentative; the loaded documents may not cover this topic.");
        } else {
            System.out.println("  No specific legal situation was confidently identified.");
            System.out.println("  The engine will treat the input primarily as a general corpus-search request.");
        }

        if (!analysis.getMissingInformation().isEmpty()) {
            System.out.println();
            System.out.println("  To enable deeper situation analysis, useful information includes:");

            for (int i = 0; i < analysis.getMissingInformation().size(); i++) {
                System.out.println(
                        "    • " + analysis.getMissingInformation().get(i)
                );
            }
        }

        printFooter();
    }


    private static void runDetailedSearchPipeline(
            SearchEngine engine,
            String query,
            QueryProcessor.QueryAnalysis analysis,
            LegalProblemAnalyzer.AnalysisResult problemAnalysis,
            SearchEngine.Algorithm selectedAlgorithm,
            SimpleList<LegalDocument> documents,
            InformationDependencyMap dependencyMap,
            boolean includeResearchPipeline) {

        SimpleList<SearchEngine.SearchResult> results =
                new SimpleList<>();

        SearchEngine.SearchRankedOutcome rankedOutcome =
                null;

        SimpleList<String> legalConcepts =
                new SimpleList<>();

        int candidateCount;

        // --------------------------------------------------------
        // CORPUS SEARCH
        // --------------------------------------------------------

        printSection("4. CORPUS SEARCH");

        System.out.println("  Corpus");
        System.out.println("    Indian Legal Corpus");

        System.out.println();
        System.out.println("  Documents searched");
        System.out.println("    " + documents.size());

        System.out.println();
        System.out.println("  Search input");
        System.out.println("    " + query);

        System.out.println();
        if (detailedMode) {
            System.out.println("  Search approach: ranked matching across document titles and content");
        }

        SimpleList<String> resolvedTerms =
                analysis.getResolvedTerms();
        currentTermOnlyQuery = analysis.getType() == QueryProcessor.QueryType.EXACT_LEGAL_TERM
                || analysis.getType() == QueryProcessor.QueryType.FUZZY_LEGAL_TERM;
        currentResolvedSearchTerm = resolvedTerms.isEmpty()
                ? query : resolvedTerms.get(0);

        if (!resolvedTerms.isEmpty()) {

            for (int i = 0; i < resolvedTerms.size(); i++) {

                SimpleList<SearchEngine.SearchResult> termResults =
                        engine.search(
                                resolvedTerms.get(i),
                                selectedAlgorithm
                        );

                addUniqueDocuments(results, termResults);
            }

            candidateCount = results.size();

            results =
                    engine.limitResults(
                            results,
                            SearchEngine.DEFAULT_TOP_K
                    );

        } else {

            SimpleList<String> meaningfulTerms =
                    engine.extractMeaningfulTerms(query);

            rankedOutcome =
                    engine.searchRankedWithMetadata(
                            query,
                            selectedAlgorithm,
                            SearchEngine.DEFAULT_TOP_K,
                            problemAnalysis
                    );

            results =
                    rankedOutcome.getResults();

            candidateCount =
                    rankedOutcome.getCandidateCount();

            legalConcepts =
                    rankedOutcome.getLegalConcepts();

            if (!legalConcepts.isEmpty()) {
                printConcepts(legalConcepts);
            }

            if (!meaningfulTerms.isEmpty()) {
                printMeaningfulTerms(meaningfulTerms);
            }
        }

        currentResearchQuery = query;
        currentResearchAnalysis = problemAnalysis;
        currentDependencyMap = dependencyMap;
        currentSearchResults = results;
        currentQueryHasResearchContext = includeResearchPipeline
                && problemAnalysis != null
                && !problemAnalysis.getIssues().isEmpty();

        boolean directMatch =
                hasDirectEvidence(results)
                        || (rankedOutcome != null
                        && rankedOutcome.hasDirectMatch());

        System.out.println();
        System.out.println(
                "  Search status      : COMPLETED"
        );
        System.out.println(
                "  Retrieved candidates: " + results.size()
        );
        System.out.println(
                "  Candidate documents: " + candidateCount
        );
        System.out.println(
                "  Displayed documents: " + Math.min(detailedMode ? 5 : 3, results.size())
        );
        System.out.println(
                "  Direct relevance   : "
                        + (directMatch ? "FOUND" : "NOT ESTABLISHED")
        );

        // --------------------------------------------------------
        // NO RESULTS
        // --------------------------------------------------------

        if (results.isEmpty()) {

            printSection("5. SEARCH RESULTS");

            System.out.println(
                    "  No matching document was found in the current corpus."
            );

            System.out.println();
            System.out.println("  HOW TO INTERPRET THIS");

            System.out.println(
                    "    The engine searched the currently loaded corpus,"
            );
            System.out.println(
                    "    but did not retrieve a qualifying document."
            );

            System.out.println();
            System.out.println("  IMPORTANT");
            System.out.println(
                    "    This does not mean that no applicable Indian law exists."
            );
            System.out.println(
                    "    It only means that this corpus/search configuration"
            );
            System.out.println(
                    "    did not return a matching document."
            );

            printFooter();
            return;
        }

        printSearchResultsHeader(
                candidateCount,
                results.size(),
                directMatch
        );

        if (!directMatch) {

            System.out.println();
            System.out.println("  No close match found for the complete situation. Showing potentially related documents for further research.");
        }

        // --------------------------------------------------------
        // ADVANCED RESEARCH PIPELINE
        // --------------------------------------------------------

        if (includeResearchPipeline && dependencyMap != null) {

            SimpleList<LegalDocument> candidateDocuments =
                    new SimpleList<>();

            int[] tierWeight =
                    new int[results.size()];

            for (int i = 0; i < results.size(); i++) {

                candidateDocuments.add(
                        results.get(i).getDocument()
                );

                SearchEngine.MatchType matchType =
                        results.get(i).getMatchType();

                if (matchType == SearchEngine.MatchType.DIRECT_MATCH
                        || matchType == SearchEngine.MatchType.TITLE_MATCH) {
                    tierWeight[i] = 2;
                } else if (matchType == SearchEngine.MatchType.CONTENT_REFERENCE
                        || matchType == SearchEngine.MatchType.RELATED_MATCH) {
                    // A statutory/content mention is not equivalent to a
                    // direct match to the user's reported situation.
                    tierWeight[i] = 1;
                } else {
                    tierWeight[i] = 0;
                }
            }

            // Optimization and scheduling are user-directed commands. This
            // keeps each search report focused and lets the user set a budget.
            // Similarity is available on demand using 'compare'.
        }

        // --------------------------------------------------------
        // DETAILED DOCUMENT RECORDS
        // --------------------------------------------------------

        printSection("RETRIEVED LEGAL DOCUMENTS");

        int detailedDocumentLimit = detailedMode ? Math.min(5, results.size()) : Math.min(3, results.size());
        currentDisplayedSearchResults = new SimpleList<>();
        for (int i = 0; i < detailedDocumentLimit; i++) currentDisplayedSearchResults.add(results.get(i));
        for (int i = 0; i < detailedDocumentLimit; i++) {
            printResult(results.get(i), i + 1, resolvedTerms.isEmpty());
        }
        if (results.size() > detailedDocumentLimit) {
            System.out.println();
            System.out.println("  Showing top " + detailedDocumentLimit + " of " + results.size()
                    + " retrieved documents to avoid repeating low-value detail.");
            System.out.println("  Use a narrower query or compact mode for a shorter report.");
        }

        printInterpretation(
                rankedOutcome,
                results,
                query,
                directMatch
        );

        System.out.println(
                "  Search pipeline completed successfully."
        );
        System.out.println(
                "  Type another query, 'help', or 'exit'."
        );

        printFooter();
    }


    private static boolean hasDirectEvidence(
            SimpleList<SearchEngine.SearchResult> results) {

        for (int i = 0; i < results.size(); i++) {

            SearchEngine.MatchType type =
                    results.get(i).getMatchType();

            if (type == SearchEngine.MatchType.DIRECT_MATCH) {
                return true;
            }
        }

        return false;
    }

    private static void addUniqueDocuments(
            SimpleList<SearchEngine.SearchResult> target,
            SimpleList<SearchEngine.SearchResult> source) {

        for (int i = 0; i < source.size(); i++) {

            SearchEngine.SearchResult candidate =
                    source.get(i);

            boolean exists = false;

            for (int j = 0; j < target.size(); j++) {

                if (target.get(j)
                        .getDocument()
                        .getDocumentId()
                        .equals(
                                candidate.getDocument()
                                        .getDocumentId()
                        )) {

                    exists = true;
                    break;
                }
            }

            if (!exists) {
                target.add(candidate);
            }
        }
    }



    // ============================================================
    // COMPACT MODE
    // ============================================================

    private static void runCompactSearch(
            SearchEngine engine,
            QueryProcessor.QueryAnalysis analysis,
            SearchEngine.Algorithm algorithm,
            LegalProblemAnalyzer.AnalysisResult problemAnalysis) {

        currentTermOnlyQuery = analysis.getType() == QueryProcessor.QueryType.EXACT_LEGAL_TERM
                || analysis.getType() == QueryProcessor.QueryType.FUZZY_LEGAL_TERM;
        currentResolvedSearchTerm = analysis.getResolvedTerms().isEmpty()
                ? analysis.getOriginalQuery() : analysis.getResolvedTerms().get(0);

        SearchEngine.SearchRankedOutcome outcome =
                engine.searchRankedWithMetadata(
                        analysis.getOriginalQuery(),
                        algorithm,
                        SearchEngine.DEFAULT_TOP_K,
                        problemAnalysis
                );

        SimpleList<SearchEngine.SearchResult> results =
                outcome.getResults();

        currentResearchQuery = analysis.getOriginalQuery();
        currentResearchAnalysis = problemAnalysis;
        currentSearchResults = results;
        currentQueryHasResearchContext = analysis.getType() != QueryProcessor.QueryType.EXACT_LEGAL_TERM
                && analysis.getType() != QueryProcessor.QueryType.FUZZY_LEGAL_TERM
                && problemAnalysis != null
                && !problemAnalysis.getIssues().isEmpty();
        if (currentQueryHasResearchContext) {
            currentDependencyMap = new InformationDependencyMap();
            currentDependencyMap.build(problemAnalysis);
        } else {
            currentDependencyMap = null;
        }

        if (results.isEmpty()
                && !analysis.getResolvedTerms().isEmpty()) {

            results =
                    engine.limitResults(
                            engine.search(
                                    analysis.getResolvedTerms().get(0),
                                    algorithm
                            ),
                            SearchEngine.DEFAULT_TOP_K
                    );
        }

        // Keep the active session aligned with the final result list, including fallback retrieval.
        currentSearchResults = results;
        currentDisplayedSearchResults = new SimpleList<>();
        for (int i = 0; i < Math.min(3, results.size()); i++) currentDisplayedSearchResults.add(results.get(i));

        printSubSection("SEARCH RESULTS");

        System.out.println(
                "  Query     : "
                        + analysis.getOriginalQuery()
        );

        System.out.println(
                "  Search type: "
                        + friendlyQueryType(analysis, problemAnalysis).toLowerCase(java.util.Locale.ROOT)
        );
        SimpleList<String> interpretedConcepts =
                search.SituationInterpreter.concepts(analysis.getOriginalQuery());
        if (!interpretedConcepts.isEmpty()) {
            System.out.println("  Possible situation (not a legal finding):");
            for (int i = 0; i < interpretedConcepts.size(); i++) {
                System.out.println("    • " + interpretedConcepts.get(i));
            }
            System.out.println("  Results are limited to evidence in the loaded corpus; no match does not mean no law applies.");
        }
        if (analysis.getType() == QueryProcessor.QueryType.FUZZY_LEGAL_TERM
                && !analysis.getResolvedTerms().isEmpty()) {
            System.out.println("  Spelling correction: " + analysis.getResolvedTerms().get(0));
        }
        System.out.println("  Search method: Ranked document search");
        System.out.println();


        if (results.isEmpty()) {

            System.out.println(
                    "  No document in the loaded corpus matched the full query."
            );
            System.out.println(
                    "  This does not establish that no applicable law or remedy exists."
            );
            System.out.println(
                    "  Try a broader legal term or a fuller description of the event."
            );

            return;
        }


        int compactLimit = Math.min(3, results.size());
        boolean directEvidence = outcome.hasDirectMatch();
        boolean termOnlyQuery = analysis.getType() == QueryProcessor.QueryType.EXACT_LEGAL_TERM
                || analysis.getType() == QueryProcessor.QueryType.FUZZY_LEGAL_TERM;
        if (termOnlyQuery) {
            System.out.println("  Evidence status: LEGAL-TERM RESULTS FOUND; no specific fact pattern was evaluated");
        } else {
            System.out.println("  Evidence status: " + (directEvidence
                    ? "DIRECT MATCH CANDIDATE(S) FOUND"
                    : "NO DIRECT MATCH ESTABLISHED; RELATED/BACKGROUND RESULTS ONLY"));
            if (!directEvidence) {
                System.out.println("  Interpret related documents as research leads, not answers to the described facts.");
            }
        }
        System.out.println("  Results: " + results.size());
        System.out.println("  Showing top " + compactLimit + " to keep the report focused.");
        System.out.println();

        for (int i = 0;
             i < compactLimit;
             i++) {

            LegalDocument document =
                    results.get(i).getDocument();

            SearchEngine.SearchResult result =
                    results.get(i);

            System.out.println(
                    "  [" + (i + 1) + "] "
                            + document.getTitle()
            );

            System.out.println(
                    "      Match      : "
                            + friendlyMatchType(result.getMatchType())
            );
            System.out.println(
                    "      Type / year: "
                            + document.getDocumentType() + " | " + document.getYear()
            );
            if (result.getMatchType() == SearchEngine.MatchType.CONTENT_REFERENCE) {
                System.out.println("      Why shown  : The term occurs in the text; this is not proof the case decides that issue.");
            }
            printTermEvidenceExcerpt(result, "      Excerpt    : ");
            System.out.println();
        }

        System.out.println(
                "  Tip: type 'details' to return to the "
                        + "full research pipeline."
        );

        System.out.println();
    }


    // ============================================================
    // INPUT
    // ============================================================

    private static String readLine(
            BufferedReader reader)
            throws IOException {

        String line =
                reader.readLine();

        return line;
    }


    // ============================================================
    // MAIN
    // ============================================================

    private static String resolveProjectDataPath(String relativePath) throws Exception {
        java.nio.file.Path relative = java.nio.file.Paths.get(relativePath);
        java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath().normalize();
        java.nio.file.Path cursor = cwd;
        for (int i = 0; i < 8 && cursor != null; i++, cursor = cursor.getParent()) {
            java.nio.file.Path candidate = cursor.resolve(relative).normalize();
            if (java.nio.file.Files.isDirectory(candidate)) return candidate.toString();
        }
        try {
            java.nio.file.Path classes = java.nio.file.Paths.get(
                    Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath().normalize();
            cursor = java.nio.file.Files.isDirectory(classes) ? classes : classes.getParent();
            for (int i = 0; i < 8 && cursor != null; i++, cursor = cursor.getParent()) {
                java.nio.file.Path candidate = cursor.resolve(relative).normalize();
                if (java.nio.file.Files.isDirectory(candidate)) return candidate.toString();
            }
        } catch (Exception ignored) { }
        throw new java.io.IOException("Could not locate '" + relativePath
                + "'. Run from the project folder or keep the compiled 'out' directory inside it.");
    }

    public static void main(String[] args) {

        printHeader();

        try {

            // Keep the accepted 242-document corpus as an independent baseline.
            // Official statutory excerpts live in a separate, provenance-tracked
            // directory so they can be audited without changing that baseline.
            LegalCorpusLoader loader =
                    new LegalCorpusLoader(resolveProjectDataPath("data/corpus"));

            SimpleList<LegalDocument> baselineDocuments = loader.loadDocuments();
            LegalCorpusLoader statutoryLoader =
                    new LegalCorpusLoader(resolveProjectDataPath("data/statutory_additions"));
            SimpleList<LegalDocument> statutoryDocuments =
                    statutoryLoader.loadDocuments();

            SimpleList<LegalDocument> documents = new SimpleList<LegalDocument>();
            for (int i = 0; i < baselineDocuments.size(); i++) {
                documents.add(baselineDocuments.get(i));
            }
            for (int i = 0; i < statutoryDocuments.size(); i++) {
                documents.add(statutoryDocuments.get(i));
            }
            allCorpusDocuments = documents;

            printWelcome(
                    documents.size(), baselineDocuments.size(), statutoryDocuments.size()
            );


            QueryProcessor processor =
                    new QueryProcessor();

            SearchEngine engine =
                    new SearchEngine(
                            documents
                    );

            CitationFlowAnalyzer citationFlowAnalyzer =
                    new CitationFlowAnalyzer();


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    System.in
                            )
                    );


            while (true) {

                System.out.print(
                        "Legal Engine > "
                );

                String input =
                        readLine(reader);

                if (input == null) {
                    System.out.println();
                    System.out.println("  Input stream closed. Exiting the engine.");
                    System.out.println();
                    reader.close();
                    return;
                }

                String command =
                        input.trim();


                // ------------------------------------------------
                // EMPTY INPUT
                // ------------------------------------------------

                if (command.isEmpty()) {

                    System.out.println(
                            "  Please enter a query or type 'help'."
                    );

                    System.out.println();

                    continue;
                }


                // ------------------------------------------------
                // LEGACY CONTINUE / EXIT INPUT
                // ------------------------------------------------

                if (command.equalsIgnoreCase("n")
                        || command.equalsIgnoreCase("no")) {

                    System.out.println();
                    System.out.println("  Closing Legal Document Intelligence Engine.");
                    System.out.println();
                    reader.close();
                    return;
                }

                if (command.equalsIgnoreCase("y")
                        || command.equalsIgnoreCase("yes")) {
                    continue;
                }

                // ------------------------------------------------
                // EXIT
                // ------------------------------------------------

                if (command.equalsIgnoreCase("exit")
                        || command.equalsIgnoreCase("quit")) {

                    System.out.println();

                    System.out.println(
                            "  Closing Legal Document Intelligence Engine."
                    );

                    System.out.println(
                            "  Thank you for using the system."
                    );

                    System.out.println();

                    reader.close();

                    return;
                }


                // ------------------------------------------------
                // HELP
                // ------------------------------------------------

                if (command.equalsIgnoreCase("help")) {

                    printHelp();

                    continue;
                }


                // ------------------------------------------------
                // DETAILS
                // ------------------------------------------------

                if (command.equalsIgnoreCase("details")) {

                    // Inspection should never unexpectedly hide detail.
                    detailedMode = true;
                    printModeChanged();
                    continue;
                }


                // ------------------------------------------------
                // EXPLICIT DETAILED MODE
                // ------------------------------------------------

                if (command.equalsIgnoreCase("detailed")) {

                    detailedMode = true;

                    printModeChanged();

                    continue;
                }


                // ------------------------------------------------
                // EXPLICIT COMPACT MODE
                // ------------------------------------------------

                if (command.equalsIgnoreCase("compact")) {

                    detailedMode = false;

                    printModeChanged();

                    continue;
                }


                // ------------------------------------------------
                // QUERY
                // ------------------------------------------------

                try {

                    performQuery(
                            engine,
                            processor,
                            command,
                            citationFlowAnalyzer,
                            documents,
                            reader
                    );

                } catch (Exception e) {

                    System.out.println();

                    System.out.println(
                            "  Could not complete the request:"
                    );

                    System.out.println(
                            "    " + e.getMessage()
                    );

                    System.out.println();

                    System.out.println(
                            "  Type 'help' for available commands."
                    );

                    System.out.println();
                }
            }


        } catch (Exception e) {

            System.err.println();

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );

            System.err.println("  Start the application from the extracted project directory, "
                    + "or keep the compiled 'out' directory inside that project.");
        }
    }
}

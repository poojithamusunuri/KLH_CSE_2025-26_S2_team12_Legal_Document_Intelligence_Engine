import loader.LegalCorpusLoader;
import analysis.ResearchPlanner;
import analysis.LegalProblemAnalyzer;
import analysis.CitationFlowAnalyzer;
import analysis.InformationDependencyMap;
import analysis.ResearchTaskScheduler;
import analysis.ResearchOptimizer;
import algorithms.MillerRabin;
import model.LegalDocument;
import search.QueryProcessor;
import search.SearchEngine;
import search.DocumentSimilarity;
import structures.SimpleList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    private static void printHeader() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("           LEGAL DOCUMENT INTELLIGENCE ENGINE");
        System.out.println("============================================================");
        System.out.println();

        System.out.println(
                "Search Indian legal documents using exact and"
        );
        System.out.println(
                "fuzzy legal-term recognition and efficient pattern search."
        );

        System.out.println();
    }

    private static void printQueryAnalysis(
            QueryProcessor.QueryAnalysis analysis) {

        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("QUERY ANALYSIS");
        System.out.println("------------------------------------------------------------");

        System.out.println();
        System.out.println(
                "Original query: " + analysis.getOriginalQuery()
        );

        System.out.println(
                "Query type: " + analysis.getType()
        );

        if (!analysis.getResolvedTerms().isEmpty()) {

            System.out.println();
            System.out.println("Resolved legal terms:");

            for (int i = 0; i < analysis.getResolvedTerms().size(); i++) {
                String term = analysis.getResolvedTerms().get(i);

                System.out.println("  • " + term);
            }
        }

        if (analysis.getType()
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            System.out.println();
            System.out.println(
                    "Edit distance: " + analysis.getDistance()
            );
        }

        System.out.println();
    }

    private static void printResult(
            SearchEngine.SearchResult result,
            int rank) {

        LegalDocument document =
                result.getDocument();

        System.out.println();
        System.out.println("[" + rank + "] " + document.getTitle());

        if (result.getRelevanceScore() > 0) {
            System.out.println();
            System.out.println(
                    "    Relevance score: "
                            + result.getRelevanceScore()
            );

            SimpleList<String> matchedConcepts =
                    result.getMatchedConcepts();

            if (!matchedConcepts.isEmpty()) {
                System.out.println(
                        "    Matched legal concepts:"
                );

                for (int i = 0; i < matchedConcepts.size(); i++) {
                    System.out.println(
                            "       " + matchedConcepts.get(i)
                    );
                }
            }

            SimpleList<String> matchedTerms =
                    result.getMatchedTerms();

            if (!matchedTerms.isEmpty()) {
                System.out.println(
                        "    Matched terms: "
                                + matchedTerms.get(0)
                );

                for (int i = 1; i < matchedTerms.size(); i++) {
                    System.out.println(
                            "                   "
                                    + matchedTerms.get(i)
                    );
                }
            }
        }

        System.out.println();
        System.out.println(
                "    Document ID : "
                        + document.getDocumentId()
        );

        System.out.println(
                "    Type        : "
                        + document.getDocumentType()
        );

        System.out.println(
                "    Jurisdiction: "
                        + document.getJurisdiction()
        );

        System.out.println(
                "    Year        : "
                        + document.getYear()
        );

        System.out.println(
                "    Source      : "
                        + document.getSource()
        );

        System.out.println();

        switch (result.getMatchType()) {

            case TITLE_MATCH:

                System.out.println(
                        "    Match       : Primary legal document"
                );

                System.out.println(
                        "    Why relevant: The queried legal term is "
                                + "the document title."
                );

                break;

            case CONTENT_REFERENCE:

                System.out.println(
                        "    Match       : Legal reference"
                );

                System.out.println(
                        "    Why relevant: The queried term appears "
                                + "within the document."
                );

                break;

            default:

                System.out.println(
                        "    Match       : Document match"
                );
        }

        System.out.println(
                "    Search      : "
                        + result.getAlgorithm()
        );

        System.out.println(
                "    Position    : "
                        + result.getPosition()
        );

        System.out.println();
        System.out.println(
                "------------------------------------------------------------"
        );
    }

    private static String stringifyList(SimpleList<String> values) {
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

    private static void printProblemAnalysis(
            LegalProblemAnalyzer.AnalysisResult analysis,
            ResearchPlanner planner) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("LEGAL PROBLEM ANALYSIS");
        System.out.println("============================================================");

        System.out.println();
        System.out.println("Problem:");
        System.out.println("  " + analysis.getOriginalProblem());

        System.out.println();
        System.out.println("Identified issues:");

        if (analysis.getIssues().isEmpty()) {
            System.out.println("  • No specific legal issue detected.");
        } else {
            for (int i = 0; i < analysis.getIssues().size(); i++) {
                System.out.println(
                        "  • " + analysis.getIssues().get(i)
                );
            }
        }

        LegalProblemAnalyzer.LegalSituation situation =
                analysis.getSituation();

        System.out.println();
        System.out.println("Legal situation summary:");
        System.out.println("  Actors/entities: " + stringifyList(situation.getActors()));
        System.out.println("  Actions/events: " + stringifyList(situation.getActions()));
        System.out.println("  Objects/subjects: " + stringifyList(situation.getObjects()));
        System.out.println("  Relationships: " + stringifyList(situation.getRelationships()));
        System.out.println("  Legal domains: " + stringifyList(situation.getLegalDomains()));
        System.out.println("  Possible objective: " + stringifyList(situation.getPossibleObjectives()));
        System.out.println("  Problem event: " + situation.getProblemEvent());
        System.out.println("  Evidence indicators: " + stringifyList(situation.getEvidenceIndicators()));
        System.out.println("  Confidence: " + situation.getConfidence());

        System.out.println();
        System.out.println("Missing information:");

        if (analysis.getMissingInformation().isEmpty()) {
            System.out.println("  • No specific missing information identified.");
        } else {
            for (
                    int i = 0;
                    i < analysis.getMissingInformation().size();
                    i++
            ) {
                System.out.println(
                        "  [" + (i + 1) + "] "
                                + analysis.getMissingInformation().get(i)
                );
            }
        }

        SimpleList<String> plan =
                planner.createPlan(analysis);

        System.out.println();
        System.out.println("RESEARCH PLAN:");

        if (plan.isEmpty()) {
            System.out.println("  • No specific research steps generated.");
        } else {
            for (int i = 0; i < plan.size(); i++) {
                System.out.println(
                        "  " + plan.get(i)
                );
            }
        }

        System.out.println();
        System.out.println("============================================================");
    }

    private static SearchEngine.Algorithm chooseAlgorithm(
            QueryProcessor.QueryType queryType) {

        /*
         * Automatic DSA search-strategy selection.
         *
         * EXACT_LEGAL_TERM:
         * KMP provides predictable O(n + m) exact pattern matching.
         *
         * FUZZY_LEGAL_TERM:
         * Levenshtein first resolves the spelling variation.
         * KMP then performs the exact corpus search.
         *
         * TEXT_SEARCH:
         * Z Algorithm performs linear-time pattern matching
         * using the Z-array.
         */

        if (queryType
                == QueryProcessor.QueryType.TEXT_SEARCH) {
            return SearchEngine.Algorithm.Z_ALGORITHM;
        }

        return SearchEngine.Algorithm.KMP;
    }

    private static String getAlgorithmReason(
            QueryProcessor.QueryType queryType,
            SearchEngine.Algorithm algorithm) {

        if (queryType
                == QueryProcessor.QueryType.EXACT_LEGAL_TERM) {

            return "Exact legal-term query; KMP provides predictable "
                    + "O(n + m) pattern matching.";

        }

        if (queryType
                == QueryProcessor.QueryType.FUZZY_LEGAL_TERM) {

            return "The query was resolved using Levenshtein distance; "
                    + "KMP performs the final exact corpus search efficiently.";

        }

        if (algorithm
                == SearchEngine.Algorithm.Z_ALGORITHM) {

            return "General text query; Z Algorithm performs linear-time "
                    + "pattern matching using the Z-array.";

        }

        if (algorithm
                == SearchEngine.Algorithm.RABIN_KARP) {

            return "General text query; Rabin-Karp uses hash-based "
                    + "pattern matching for unrestricted text.";

        }

        return "Automatic search strategy selected for the query type.";
    }

    private static void printSearchStrategy(
            QueryProcessor.QueryType queryType,
            SearchEngine.Algorithm algorithm) {

        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("SEARCH STRATEGY");
        System.out.println("------------------------------------------------------------");
        System.out.println();
        System.out.println(
                "Algorithm : " + algorithm
        );
        System.out.println(
                "Reason    : "
                        + getAlgorithmReason(
                                queryType,
                                algorithm
                        )
        );
        System.out.println();
    }

    private static void printCitationFlowResult(
            CitationFlowAnalyzer.CitationFlowResult result) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("CITATION FLOW ANALYSIS");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Judgments loaded     : " + result.getJudgmentCount());
        System.out.println("Acts loaded          : " + result.getActCount());
        System.out.println("Relationships loaded : " + result.getEdgeCount());
        System.out.println("Total references     : " + result.getTotalReferenceCount());
        System.out.println("Max-flow value       : " + result.getMaxFlow());

        System.out.println();
        System.out.println("Top judgments by outgoing references:");

        if (result.getTopJudgments().isEmpty()) {
            System.out.println("  • No judgment references found.");
        } else {
            for (int i = 0; i < result.getTopJudgments().size(); i++) {
                System.out.println("  • " + result.getTopJudgments().get(i));
            }
        }

        System.out.println();
        System.out.println("Top acts by incoming references:");

        if (result.getTopActs().isEmpty()) {
            System.out.println("  • No act references found.");
        } else {
            for (int i = 0; i < result.getTopActs().size(); i++) {
                System.out.println("  • " + result.getTopActs().get(i));
            }
        }

        System.out.println();
        System.out.println("============================================================");
    }

    private static void printScheduleResult(
            ResearchTaskScheduler.ScheduleResult result,
            int capacityHours) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("LEGAL RESEARCH SCHEDULING DEMO");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Capacity hours: " + capacityHours);
        System.out.println("Scheduled hours: " + result.getTotalDuration());
        System.out.println("Total value: " + result.getTotalValue());
        System.out.println("Total coverage: " + result.getTotalCoverage());

        System.out.println();
        System.out.println("Selected research tasks:");

        if (result.getScheduledTasks().isEmpty()) {
            System.out.println("  • No tasks could be scheduled within the available capacity.");
        } else {
            for (int i = 0; i < result.getScheduledTasks().size(); i++) {
                ResearchTaskScheduler.ResearchTask task =
                        result.getScheduledTasks().get(i);

                System.out.println(
                        "  • " + task.getTitle()
                                + " [" + task.getDuration()
                                + "h, value=" + task.getValue()
                                + ", coverage=" + task.getCoverage()
                                + "]"
                );
            }
        }

        System.out.println();
        System.out.println("Skipped tasks:");

        if (result.getSkippedTasks().isEmpty()) {
            System.out.println("  • None");
        } else {
            for (int i = 0; i < result.getSkippedTasks().size(); i++) {
                System.out.println("  • " + result.getSkippedTasks().get(i));
            }
        }

        System.out.println();
        System.out.println("============================================================");
    }

    private static void runPrimeDemo() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("MILLER-RABIN PRIMALITY TEST DEMO");
        System.out.println("============================================================");

        long[] primes = {
                2,
                3,
                5,
                7,
                11,
                1000000007L,
                1000000009L
        };

        long[] composites = {
                1,
                4,
                9,
                15,
                21,
                25,
                1000000007L * 1000000009L,
                2147483647L,
                2147483649L,
                999999937L * 999999929L
        };

        System.out.println();
        System.out.println("Small known primes:");

        for (int i = 0; i < primes.length; i++) {
            long value = primes[i];
            System.out.println(
                    "  • " + value + " -> "
                            + (MillerRabin.isPrime(value)
                            ? "prime"
                            : "composite")
            );
        }

        System.out.println();
        System.out.println("Representative composites:");

        for (int i = 0; i < composites.length; i++) {
            long value = composites[i];
            System.out.println(
                    "  • " + value + " -> "
                            + (MillerRabin.isPrime(value)
                            ? "prime"
                            : "composite")
            );
        }

        System.out.println();
        System.out.println("============================================================");
    }

    private static void printOptimizationResult(
            ResearchOptimizer.OptimizationResult result,
            LegalProblemAnalyzer.AnalysisResult analysis,
            int budget) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("RESEARCH OPTIMIZATION DEMO");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Budget: " + budget);
        System.out.println("Questions analyzed: " + analysis.getQuestions().size());
        System.out.println("Total additional coverage captured: " + result.getTotalCoverage());

        System.out.println();
        System.out.println("Covered questions:");

        if (result.getCoveredQuestions().isEmpty()) {
            System.out.println("  • None");
        } else {
            for (int i = 0; i < result.getCoveredQuestions().size(); i++) {
                System.out.println("  • " + result.getCoveredQuestions().get(i));
            }
        }

        System.out.println();
        System.out.println("Selected documents:");

        if (result.getSelectedDocuments().isEmpty()) {
            System.out.println("  • No documents selected.");
        } else {
            for (int i = 0; i < result.getSelectedDocuments().size(); i++) {
                LegalDocument document =
                        result.getSelectedDocuments().get(i);

                System.out.println(
                        "  • [" + (i + 1) + "] "
                                + document.getTitle()
                );
            }
        }

        System.out.println();
        System.out.println("Coverage breakdown:");

        if (result.getCoverageBreakdown().isEmpty()) {
            System.out.println("  • No coverage breakdown available.");
        } else {
            for (int i = 0; i < result.getCoverageBreakdown().size(); i++) {
                System.out.println("  • " + result.getCoverageBreakdown().get(i));
            }
        }

        System.out.println();
        System.out.println("Uncovered questions:");

        if (result.getUncoveredQuestions().isEmpty()) {
            System.out.println("  • None");
        } else {
            for (int i = 0; i < result.getUncoveredQuestions().size(); i++) {
                System.out.println("  • " + result.getUncoveredQuestions().get(i));
            }
        }

        System.out.println();
        System.out.println("============================================================");
    }

    private static void runOptimizationDemo(
            SimpleList<LegalDocument> documents) {

        String sampleProblem =
                "Property ownership dispute involving a transfer and competing claims";

        LegalProblemAnalyzer problemAnalyzer =
                new LegalProblemAnalyzer();

        LegalProblemAnalyzer.AnalysisResult analysis =
                problemAnalyzer.analyze(sampleProblem);

        ResearchOptimizer optimizer =
                new ResearchOptimizer();

        ResearchOptimizer.OptimizationResult result =
                optimizer.optimize(
                        analysis,
                        documents,
                        3
                );

        System.out.println();
        System.out.println("Problem: " + sampleProblem);

        System.out.println();
        System.out.println("Research questions:");

        if (analysis.getQuestions().isEmpty()) {
            System.out.println("  • No research questions detected.");
        } else {
            for (int i = 0; i < analysis.getQuestions().size(); i++) {
                System.out.println(
                        "  [" + (i + 1) + "] "
                                + analysis.getQuestions().get(i)
                );
            }
        }

        printOptimizationResult(result, analysis, 3);
    }

    private static void runScheduleDemo() {

        String sampleProblem =
                "Property ownership dispute involving a transfer and competing claims";

        LegalProblemAnalyzer problemAnalyzer =
                new LegalProblemAnalyzer();

        LegalProblemAnalyzer.AnalysisResult analysis =
                problemAnalyzer.analyze(sampleProblem);

        InformationDependencyMap dependencyMap =
                new InformationDependencyMap();

        dependencyMap.build(analysis);

        ResearchTaskScheduler scheduler =
                new ResearchTaskScheduler();

        ResearchTaskScheduler.ScheduleResult result =
                scheduler.scheduleFromAnalysis(
                        analysis,
                        dependencyMap,
                        6
                );

        System.out.println();
        System.out.println("Problem: " + sampleProblem);

        System.out.println();
        System.out.println("Detected issues:");

        if (analysis.getIssues().isEmpty()) {
            System.out.println("  • No legal issues detected.");
        } else {
            for (int i = 0; i < analysis.getIssues().size(); i++) {
                System.out.println("  • " + analysis.getIssues().get(i));
            }
        }

        System.out.println();
        System.out.println("Missing information:");

        if (analysis.getMissingInformation().isEmpty()) {
            System.out.println("  • No missing information identified.");
        } else {
            for (int i = 0; i < analysis.getMissingInformation().size(); i++) {
                System.out.println(
                        "  [" + (i + 1) + "] "
                                + analysis.getMissingInformation().get(i)
                );
            }
        }

        printScheduleResult(result, 6);
    }

    private static void performQuery(
            SearchEngine engine,
            QueryProcessor processor,
            BufferedReader reader,
            CitationFlowAnalyzer analyzer,
            SimpleList<LegalDocument> documents) throws IOException {

        System.out.println();
        System.out.print("Enter your legal query (or type 'flow' for citation-flow, 'schedule' for a scheduling demo, 'prime' for Miller-Rabin, or 'optimize' for research optimization): ");

        String query =
                readLine(reader).trim();

        if (query.equalsIgnoreCase("flow")) {
            try {
                CitationFlowAnalyzer.CitationFlowResult result =
                        analyzer.analyze("results/judgment_act_relationships.csv");

                printCitationFlowResult(result);

            } catch (Exception e) {
                System.out.println();
                System.out.println("Could not run citation-flow analysis: " + e.getMessage());
            }

            return;
        }

        if (query.equalsIgnoreCase("schedule")) {
            runScheduleDemo();
            return;
        }

        if (query.equalsIgnoreCase("prime")) {
            runPrimeDemo();
            return;
        }

        if (query.equalsIgnoreCase("optimize")) {
            runOptimizationDemo(documents);
            return;
        }

        if (query.isEmpty()) {
            System.out.println();
            System.out.println(
                    "Query cannot be empty."
            );
            return;
        }

        QueryProcessor.QueryAnalysis analysis =
                processor.analyze(query);

        if (analysis.getType()
                == QueryProcessor.QueryType.INVALID) {

            System.out.println();
            System.out.println(
                    "Invalid query."
            );
            return;
        }

        printQueryAnalysis(analysis);

        /*
         * Automatically select the most appropriate
         * pattern-search algorithm.
         *
         * The user does not select an algorithm.
         */
        SearchEngine.Algorithm selectedAlgorithm =
                chooseAlgorithm(analysis.getType());

        printSearchStrategy(
                analysis.getType(),
                selectedAlgorithm
        );

        /*
         * Project-level legal problem analysis.
         */
        LegalProblemAnalyzer problemAnalyzer =
                new LegalProblemAnalyzer();

        String analysisProblem = query;

        if (!analysis.getResolvedTerms().isEmpty()) {
            analysisProblem =
                    analysis.getResolvedTerms().get(0);
        }

        LegalProblemAnalyzer.AnalysisResult problemAnalysis =
                problemAnalyzer.analyze(analysisProblem);

        ResearchPlanner researchPlanner =
                new ResearchPlanner();

        printProblemAnalysis(
                problemAnalysis,
                researchPlanner
        );

        /*
         * Search the corpus.
         *
         * Exact and fuzzy legal-term queries continue to use the
         * existing query-resolution workflow.
         *
         * General natural-language queries now use a ranked,
         * relevance-based retrieval flow.
         */
        String searchQuery = query;

        SimpleList<String> resolvedTerms =
                analysis.getResolvedTerms();

        SimpleList<SearchEngine.SearchResult> results =
                new SimpleList<>();

        int candidateCount = 0;
        int displayedCount = 0;
        SimpleList<String> legalConcepts =
                new SimpleList<String>();

        if (!resolvedTerms.isEmpty()) {

            for (int i = 0; i < resolvedTerms.size(); i++) {

                String term =
                        resolvedTerms.get(i);

                SimpleList<SearchEngine.SearchResult> termResults =
                        engine.search(
                                term,
                                selectedAlgorithm
                        );

                for (int j = 0; j < termResults.size(); j++) {

                    SearchEngine.SearchResult result =
                            termResults.get(j);

                    boolean alreadyPresent = false;

                    for (int k = 0; k < results.size(); k++) {

                        SearchEngine.SearchResult existing =
                                results.get(k);

                        if (existing.getDocument()
                                .getDocumentId()
                                .equals(
                                        result.getDocument()
                                                .getDocumentId()
                                )) {

                            alreadyPresent = true;
                            break;
                        }
                    }

                    if (!alreadyPresent) {
                        results.add(result);
                    }
                }
            }

        } else {

            SimpleList<String> meaningfulTerms =
                    engine.extractMeaningfulTerms(query);

            LegalProblemAnalyzer.AnalysisResult situationAnalysis =
                    problemAnalyzer.analyze(query);

            SearchEngine.SearchRankedOutcome rankedOutcome =
                    engine.searchRankedWithMetadata(
                            query,
                            selectedAlgorithm,
                            SearchEngine.DEFAULT_TOP_K,
                            situationAnalysis
                    );

            results = rankedOutcome.getResults();
            candidateCount = rankedOutcome.getCandidateCount();
            displayedCount = rankedOutcome.getDisplayedCount();
            legalConcepts = rankedOutcome.getLegalConcepts();

            System.out.println();
            System.out.println(
                    "============================================================"
            );
            System.out.println(
                    "LEGAL CONCEPTS IDENTIFIED"
            );
            System.out.println(
                    "============================================================"
            );

            if (legalConcepts.isEmpty()) {
                System.out.println("  • None");
            } else {
                for (int i = 0; i < legalConcepts.size(); i++) {
                    System.out.println(
                            "  • " + legalConcepts.get(i)
                    );
                }
            }

            System.out.println();
            System.out.println(
                    "============================================================"
            );
            System.out.println(
                    "MEANINGFUL QUERY TERMS"
            );
            System.out.println(
                    "============================================================"
            );

            if (meaningfulTerms.isEmpty()) {
                System.out.println("  • None");
            } else {
                for (int i = 0; i < meaningfulTerms.size(); i++) {
                    System.out.println(
                            "  • " + meaningfulTerms.get(i)
                    );
                }
            }
        }

        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println("SEARCH RESULTS");
        System.out.println(
                "============================================================"
        );

        if (results.isEmpty()) {

            System.out.println();

            if (analysis.getType() == QueryProcessor.QueryType.TEXT_SEARCH) {
                System.out.println(
                        "Matching candidate documents: 0"
                );
                System.out.println(
                        "Top ranked documents shown: 0"
                );
            } else {
                System.out.println(
                        "Matching documents: 0"
                );
            }

            System.out.println();

            return;
        }

        System.out.println();

        if (analysis.getType() == QueryProcessor.QueryType.TEXT_SEARCH) {
            System.out.println(
                    "Matching candidate documents: " + candidateCount
            );
            System.out.println(
                    "Top ranked documents shown: " + displayedCount
            );
        } else {
            System.out.println(
                    "Matching documents: " + results.size()
            );
        }

        /*
         * Compare matching documents using
         * Suffix Array + LCP (Kasai).
         */
        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println(
                "DOCUMENT SIMILARITY ANALYSIS"
        );
        System.out.println(
                "============================================================"
        );
        System.out.println();

        System.out.println(
                "Using Suffix Array + LCP (Kasai) to compare matching documents."
        );

        if (results.size() < 2) {

            System.out.println(
                    "Similarity comparison requires at least 2 matching documents."
            );

        } else {

            SearchEngine.SearchResult baseResult =
                    results.get(0);

            LegalDocument baseDocument =
                    baseResult.getDocument();

            System.out.println(
                    "Reference document: "
                            + baseDocument.getTitle()
            );

            for (int i = 1; i < results.size(); i++) {

                SearchEngine.SearchResult otherResult =
                        results.get(i);

                LegalDocument otherDocument =
                        otherResult.getDocument();

                double similarity =
                        DocumentSimilarity.similarity(
                                baseDocument.getContent(),
                                otherDocument.getContent()
                        );

                int longestShared =
                        DocumentSimilarity.longestCommonSequence(
                                baseDocument.getContent(),
                                otherDocument.getContent()
                        );

                System.out.println();

                System.out.println(
                        "Compared with: "
                                + otherDocument.getTitle()
                );

                System.out.println(
                        "Similarity score: "
                                + similarity
                );

                System.out.println(
                        "Longest shared textual sequence: "
                                + longestShared
                                + " characters"
                );
            }
        }

        System.out.println();

        int rank = 1;

        for (int i = 0; i < results.size(); i++) {

            SearchEngine.SearchResult result =
                    results.get(i);

            printResult(result, rank);

            rank++;
        }
    }

    private static String readLine(
            BufferedReader reader) throws IOException {

        String line = reader.readLine();

        if (line == null) {
            return "";
        }

        return line;
    }

    public static void main(String[] args) {

        printHeader();

        try {

            LegalCorpusLoader loader =
                    new LegalCorpusLoader(
                            "data/corpus"
                    );

            SimpleList<LegalDocument> documents =
                    loader.loadDocuments();

            System.out.println(
                    "Corpus loaded successfully."
            );

            System.out.println(
                    "Documents available: "
                            + documents.size()
            );

            QueryProcessor processor =
                    new QueryProcessor();

            SearchEngine engine =
                    new SearchEngine(documents);

            CitationFlowAnalyzer citationFlowAnalyzer =
                    new CitationFlowAnalyzer();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(System.in)
                    );

            while (true) {

                performQuery(
                        engine,
                        processor,
                        reader,
                        citationFlowAnalyzer,
                        documents
                );

                System.out.println();
                System.out.print(
                        "Search again? (y/n): "
                );

                String answer =
                        readLine(reader)
                                .trim()
                                .toLowerCase();

                if (!answer.equals("y")) {

                    System.out.println();
                    System.out.println(
                            "Exiting Legal Document "
                                    + "Intelligence Engine."
                    );

                    reader.close();

                    return;
                }
            }

        } catch (Exception e) {

            System.err.println();
            System.err.println(
                    "ERROR: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}

import loader.LegalCorpusLoader;
import model.LegalDocument;
import search.QueryProcessor;
import search.SearchEngine;

import java.util.List;
import java.util.Scanner;

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

            for (String term : analysis.getResolvedTerms()) {

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

    private static void performQuery(
            SearchEngine engine,
            QueryProcessor processor,
            Scanner scanner) {

        System.out.println();
        System.out.print("Enter your legal query: ");

        String query =
                scanner.nextLine().trim();

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
         * For Module 2 + Module 3:
         *
         * The system uses KMP for the actual corpus
         * pattern search.
         *
         * Levenshtein is used inside QueryProcessor
         * when fuzzy legal-term resolution is required.
         */

        String searchQuery = query;

        /*
         * If the query was resolved to one or more legal
         * terms, search using the resolved terms.
         *
         * For multiple equally good fuzzy matches,
         * search each resolved term.
         */

        List<String> resolvedTerms =
                analysis.getResolvedTerms();

        List<SearchEngine.SearchResult> results =
                new java.util.ArrayList<>();

        if (!resolvedTerms.isEmpty()) {

            for (String term : resolvedTerms) {

                List<SearchEngine.SearchResult> termResults =
                        engine.search(
                                term,
                                SearchEngine.Algorithm.KMP
                        );

                for (SearchEngine.SearchResult result
                        : termResults) {

                    boolean alreadyPresent = false;

                    for (SearchEngine.SearchResult existing
                            : results) {

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

            results =
                    engine.search(
                            searchQuery,
                            SearchEngine.Algorithm.KMP
                    );
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("SEARCH RESULTS");
        System.out.println("============================================================");

        if (results.isEmpty()) {

            System.out.println();
            System.out.println(
                    "No matching legal documents were found."
            );

            System.out.println();
            return;
        }

        System.out.println();
        System.out.println(
                "Matching documents: " + results.size()
        );

        int rank = 1;

        for (SearchEngine.SearchResult result : results) {

            printResult(result, rank);

            rank++;
        }
    }

    public static void main(String[] args) {

        printHeader();

        try {

            LegalCorpusLoader loader =
                    new LegalCorpusLoader(
                            "data/corpus"
                    );

            List<LegalDocument> documents =
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

            Scanner scanner =
                    new Scanner(System.in);

            while (true) {

                performQuery(
                        engine,
                        processor,
                        scanner
                );

                System.out.println();
                System.out.print(
                        "Search again? (y/n): "
                );

                String answer =
                        scanner.nextLine()
                                .trim()
                                .toLowerCase();

                if (!answer.equals("y")) {

                    System.out.println();
                    System.out.println(
                            "Exiting Legal Document "
                                    + "Intelligence Engine."
                    );

                    scanner.close();

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

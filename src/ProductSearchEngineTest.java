import java.util.*;

public class ProductSearchEngineTest {

    public static void main(String[] args) {

        try {

            CorpusLoader loader =
                new CorpusLoader("data/corpus");

            ProductDocument[] corpus =
                loader.load();

            ProductSearchEngine engine =
                new ProductSearchEngine(
                    corpus
                );

            Scanner scanner =
                new Scanner(System.in);

            System.out.println(
                "========================================"
            );

            System.out.println(
                "       PRODUCT SEARCH SYSTEM"
            );

            System.out.println(
                "========================================"
            );

            System.out.println(
                "Corpus files loaded: "
                + corpus.length
            );

            while (true) {

                System.out.println();

                System.out.println(
                    "1. Search Products"
                );

                System.out.println(
                    "2. Prefix Suggestions"
                );

                System.out.println(
                    "3. Exit"
                );

                System.out.print(
                    "Enter choice: "
                );

                String choice =
                    scanner.nextLine();

                if (choice.equals("1")) {

                    System.out.print(
                        "Enter search query: "
                    );

                    String query =
                        scanner.nextLine();

                    ArrayList<
                        ProductRanker.RankedProduct
                    > results =
                        engine.search(query);

                    System.out.println();

                    System.out.println(
                        "========================================"
                    );

                    System.out.println(
                        "         SEARCH RESULTS"
                    );

                    System.out.println(
                        "========================================"
                    );

                    System.out.println(
                        "Query: "
                        + query
                    );

                    if (results.isEmpty()) {

                        System.out.println();

                        System.out.println(
                            "No matching products found."
                        );

                    } else {

                        int count = 1;

                        for (
                            ProductRanker.RankedProduct
                            result :
                            results
                        ) {

                            System.out.println();

                            System.out.println(
                                count
                                + ". "
                                + result.product
                                    .getProductName()
                            );

                            System.out.println(
                                "   File  : "
                                + result.product
                                    .getFileName()
                            );

                            System.out.println(
                                "   Score : "
                                + String.format(
                                    "%.0f",
                                    result.score
                                )
                            );

                            count++;

                            /*
                             * Display top 10 only.
                             */
                            if (count > 10) {
                                break;
                            }
                        }
                    }

                    System.out.println(
                        "========================================"
                    );
                }

                else if (choice.equals("2")) {

                    System.out.print(
                        "Enter prefix: "
                    );

                    String prefix =
                        scanner.nextLine();

                    ArrayList<String>
                        suggestions =
                            engine.getSuggestions(
                                prefix
                            );

                    System.out.println();

                    System.out.println(
                        "========================================"
                    );

                    System.out.println(
                        "        PRODUCT SUGGESTIONS"
                    );

                    System.out.println(
                        "========================================"
                    );

                    if (suggestions.isEmpty()) {

                        System.out.println(
                            "No suggestions found."
                        );

                    } else {

                        int count = 1;

                        for (
                            String suggestion :
                            suggestions
                        ) {

                            System.out.println(
                                count
                                + ". "
                                + suggestion
                            );

                            count++;

                            if (count > 10) {
                                break;
                            }
                        }
                    }

                    System.out.println(
                        "========================================"
                    );
                }

                else if (choice.equals("3")) {

                    System.out.println(
                        "Exiting Product Search System."
                    );

                    break;
                }

                else {

                    System.out.println(
                        "Invalid choice."
                    );
                }
            }

            scanner.close();

        } catch (Exception e) {

            System.out.println(
                "Error: "
                + e.getMessage()
            );
        }
    }
}
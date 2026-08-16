import java.util.ArrayList;
import java.util.Scanner;

public class RankingTest {

    public static void main(String[] args) {

        try {

            CorpusLoader loader =
                new CorpusLoader("data/corpus");

            ProductDocument[] corpus =
                loader.load();

            TrieSearch trieSearch =
                new TrieSearch();

            trieSearch.build(corpus);

            Scanner scanner =
                new Scanner(System.in);

            System.out.println(
                "========================================"
            );

            System.out.println(
                "       PRODUCT RELEVANCE RANKING"
            );

            System.out.println(
                "========================================"
            );

            System.out.println(
                "Corpus files loaded: "
                + corpus.length
            );

            System.out.print(
                "Enter search query: "
            );

            String query =
                scanner.nextLine();

            /*
             * Trie generates candidates.
             */
            ArrayList<ProductDocument> candidates =
                trieSearch.searchMultiWord(query);

            /*
             * Priority Queue ranks candidates.
             */
            ArrayList<ProductRanker.RankedProduct>
                results =
                ProductRanker.rank(
                    candidates,
                    query
                );

            System.out.println();

            System.out.println(
                "========================================"
            );

            System.out.println(
                "        RANKED SEARCH RESULTS"
            );

            System.out.println(
                "========================================"
            );

            System.out.println(
                "Query: " + query
            );

            if (results.isEmpty()) {

                System.out.println();

                System.out.println(
                    "No matching products found."
                );

            } else {

                int count = 1;

                for (
                    ProductRanker.RankedProduct result :
                    results
                ) {

                    System.out.println();

                    System.out.println(
                        count + ". "
                        + result.product.getProductName()
                    );

                    System.out.println(
                        "   File  : "
                        + result.product.getFileName()
                    );

                    System.out.println(
                        "   Score : "
                        + String.format(
                            "%.0f",
                            result.score
                        )
                    );

                    count++;

                    if (count > 10) {
                        break;
                    }
                }
            }

            System.out.println();

            System.out.println(
                "========================================"
            );

            scanner.close();

        } catch (Exception e) {

            System.out.println(
                "Error: " + e.getMessage()
            );
        }
    }
}
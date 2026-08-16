import java.util.*;

public class TrieCorpusTest {

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
                "         TRIE PREFIX SEARCH"
            );
            System.out.println(
                "========================================"
            );

            System.out.println(
                "Corpus files loaded: "
                + corpus.length
            );

            System.out.print(
                "Enter prefix: "
            );

            String prefix =
                scanner.nextLine().trim();

            ArrayList<ProductDocument> results =
                trieSearch.searchProducts(prefix);

            System.out.println();
            System.out.println(
                "========================================"
            );
            System.out.println(
                "          PRODUCT SUGGESTIONS"
            );
            System.out.println(
                "========================================"
            );

            System.out.println();
            System.out.println(
                "Prefix: " + prefix
            );

            if (results.isEmpty()) {

                System.out.println(
                    "No matching products found."
                );

            } else {

                int count = 1;

                for (ProductDocument product : results) {

                    System.out.println();

                    System.out.println(
                        count + ". "
                        + product.getProductName()
                    );

                    System.out.println(
                        "   File : "
                        + product.getFileName()
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

            scanner.close();

        } catch (Exception e) {

            System.out.println(
                "Error: " + e.getMessage()
            );
        }
    }
}
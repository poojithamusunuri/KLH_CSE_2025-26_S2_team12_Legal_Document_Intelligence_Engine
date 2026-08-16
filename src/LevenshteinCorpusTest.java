import java.util.*;

public class LevenshteinCorpusTest {

    public static void main(String[] args) {

        try {

            CorpusLoader loader =
                new CorpusLoader("data/corpus");

            ProductDocument[] corpus =
                loader.load();

            Scanner scanner =
                new Scanner(System.in);

            System.out.println(
                "========================================"
            );
            System.out.println(
                "      LEVENSHTEIN TYPO SEARCH"
            );
            System.out.println(
                "========================================"
            );

            System.out.println(
                "Corpus files loaded: "
                + corpus.length
            );

            System.out.print(
                "Enter product/search term: "
            );

            String query =
                scanner.nextLine();

            ArrayList<LevenshteinSearch.Result> results =
                LevenshteinSearch.search(
                    corpus,
                    query
                );

            System.out.println();
            System.out.println(
                "========================================"
            );
            System.out.println(
                "         TYPO SEARCH RESULTS"
            );
            System.out.println(
                "========================================"
            );

            System.out.println(
                "Query: " + query
            );

            if (results.isEmpty()) {

                System.out.println(
                    "No close product matches found."
                );

            } else {

                int count = 1;

                for (LevenshteinSearch.Result result : results) {

                    System.out.println();
                    System.out.println(
                        count + ". "
                        + result.productName
                    );

                    System.out.println(
                        "   File            : "
                        + result.fileName
                    );

                    System.out.println(
                        "   Matched Terms   : "
                        + result.matchedTerms
                        + "/"
                        + result.totalTerms
                    );

                    System.out.println(
                        "   Matched Text    : "
                        + result.matchedTerm
                    );

                    System.out.println(
                        "   Total Edit Distance : "
                        + result.distance
                    );

                    System.out.println(
                        "   Match Score     : "
                        + String.format("%.0f", result.matchScore)
                        + "%"
                    );

                    count++;
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
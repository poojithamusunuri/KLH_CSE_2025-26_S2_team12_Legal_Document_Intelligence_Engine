public class SearchIndexTest {

    public static void main(String[] args) {

        try {

            CorpusLoader loader =
                new CorpusLoader("data/corpus");

            ProductDocument[] corpus =
                loader.load();

            System.out.println(
                "========================================"
            );

            System.out.println(
                "       SEARCH INDEX INSPECTION"
            );

            System.out.println(
                "========================================"
            );

            for (ProductDocument product : corpus) {

                String content =
                    product.getContent();

                String[] lines =
                    content.split("\\R");

                boolean insideIndex = false;

                System.out.println();
                System.out.println(
                    product.getProductName()
                );

                System.out.println(
                    "----------------------------------------"
                );

                for (String line : lines) {

                    String trimmed =
                        line.trim();

                    if (trimmed.equalsIgnoreCase(
                            "Search Index:")) {

                        insideIndex = true;

                        System.out.println(
                            "[Search Index]"
                        );

                        continue;
                    }

                    if (insideIndex &&
                        trimmed.equalsIgnoreCase(
                            "Customer Search Queries:")) {

                        break;
                    }

                    if (insideIndex) {

                        System.out.println(
                            line
                        );
                    }
                }
            }

            System.out.println();
            System.out.println(
                "========================================"
            );

        } catch (Exception e) {

            System.out.println(
                "Error: " + e.getMessage()
            );
        }
    }
}
import java.util.*;

public class TrieSearch {

    private final Trie trie;

    private final Map<String, ArrayList<ProductDocument>>
        termToProducts;

    public TrieSearch() {

        trie = new Trie();

        termToProducts =
            new HashMap<>();
    }

    /*
     * Build Trie and term-to-product mapping
     * from the 20-product corpus.
     */
    public void build(ProductDocument[] corpus) {

        for (ProductDocument product : corpus) {

            /*
             * Product name
             */
            addTerms(
                product.getProductName(),
                product
            );

            /*
             * Search Index only
             */
            String searchIndex =
                extractSearchIndex(
                    product.getContent()
                );

            addTerms(
                searchIndex,
                product
            );
        }
    }

    private void addTerms(
            String text,
            ProductDocument product) {

        if (text == null) {
            return;
        }

        String cleaned =
            text.toLowerCase()
                .replaceAll(
                    "[^a-z0-9]+",
                    " "
                )
                .trim();

        if (cleaned.isEmpty()) {
            return;
        }

        String[] words =
            cleaned.split("\\s+");

        for (String word : words) {

            if (word.length() < 2) {
                continue;
            }

            trie.insert(word);

            termToProducts
                .computeIfAbsent(
                    word,
                    k -> new ArrayList<>()
                );

            ArrayList<ProductDocument> products =
                termToProducts.get(word);

            if (!products.contains(product)) {
                products.add(product);
            }
        }
    }

    /*
     * Single-prefix search.
     *
     * Example:
     * "sam" -> samsung, ...
     */
    public ArrayList<String> searchPrefix(
            String prefix) {

        return trie.getWordsWithPrefix(prefix);
    }

    /*
     * Single-prefix product search.
     */
    public ArrayList<ProductDocument>
        searchProducts(String prefix) {

        ArrayList<ProductDocument> results =
            new ArrayList<>();

        ArrayList<String> matchingTerms =
            trie.getWordsWithPrefix(prefix);

        LinkedHashSet<ProductDocument>
            uniqueProducts =
            new LinkedHashSet<>();

        for (String term : matchingTerms) {

            ArrayList<ProductDocument> products =
                termToProducts.get(term);

            if (products != null) {
                uniqueProducts.addAll(products);
            }
        }

        results.addAll(uniqueProducts);

        return results;
    }

    /*
     * ------------------------------------------------
     * MULTI-WORD SEARCH
     * ------------------------------------------------
     *
     * Example:
     *
     * "galaxy s24 ultra"
     *
     * becomes:
     *
     * galaxy
     * s24
     * ultra
     *
     * Each term is searched through the Trie.
     */
    public ArrayList<ProductDocument>
        searchMultiWord(String query) {

        ArrayList<ProductDocument> results =
            new ArrayList<>();

        if (query == null ||
            query.trim().isEmpty()) {

            return results;
        }

        String cleaned =
            query.toLowerCase()
                 .replaceAll(
                     "[^a-z0-9]+",
                     " "
                 )
                 .trim();

        if (cleaned.isEmpty()) {
            return results;
        }

        String[] queryTerms =
            cleaned.split("\\s+");

        /*
         * Keep track of how many query terms
         * matched each product.
         */
        Map<ProductDocument, Integer>
            matchCount =
            new HashMap<>();

        for (String queryTerm : queryTerms) {

            if (queryTerm.length() < 2) {
                continue;
            }

            /*
             * Trie prefix matching for this term.
             *
             * Example:
             * "gal" -> galaxy
             * "sam" -> samsung
             */
            ArrayList<String> matchingTerms =
                trie.getWordsWithPrefix(
                    queryTerm
                );

            /*
             * Find products associated with
             * the matching Trie terms.
             */
            for (String term : matchingTerms) {

                ArrayList<ProductDocument> products =
                    termToProducts.get(term);

                if (products == null) {
                    continue;
                }

                for (ProductDocument product :
                        products) {

                    matchCount.put(
                        product,
                        matchCount.getOrDefault(
                            product,
                            0
                        ) + 1
                    );
                }
            }
        }

        /*
         * Only return products that matched
         * at least one query term.
         *
         * Ranking will be handled separately
         * by ProductRanker.
         */
        results.addAll(
            matchCount.keySet()
        );

        return results;
    }

    /*
     * Extract Search Index only.
     */
    private String extractSearchIndex(
            String content) {

        if (content == null) {
            return "";
        }

        String[] lines =
            content.split("\\R");

        StringBuilder index =
            new StringBuilder();

        boolean insideIndex = false;

        for (String line : lines) {

            String trimmed =
                line.trim();

            if (trimmed.equalsIgnoreCase(
                    "Search Index:")) {

                insideIndex = true;
                continue;
            }

            if (insideIndex &&
                trimmed.equalsIgnoreCase(
                    "Customer Search Queries:")) {

                break;
            }

            if (insideIndex) {

                index.append(line)
                     .append(" ");
            }
        }

        return index.toString();
    }
}
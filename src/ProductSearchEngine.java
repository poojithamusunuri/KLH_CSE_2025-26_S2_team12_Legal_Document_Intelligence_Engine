import java.util.*;

public class ProductSearchEngine {

    private final ProductDocument[] corpus;
    private final TrieSearch trieSearch;

    public ProductSearchEngine(
            ProductDocument[] corpus) {

        this.corpus = corpus;

        trieSearch =
            new TrieSearch();

        trieSearch.build(corpus);
    }

    /*
     * Main product-search function.
     *
     * The user does not need to know which
     * DSA algorithm is being used internally.
     */
    public ArrayList<ProductRanker.RankedProduct>
        search(String query) {

        ArrayList<ProductDocument> candidates =
            new ArrayList<>();

        if (query == null ||
            query.trim().isEmpty()) {

            return new ArrayList<>();
        }

        /*
         * -----------------------------------------
         * STEP 1
         * -----------------------------------------
         * Try multi-word Trie candidate generation.
         */
        candidates.addAll(
            trieSearch.searchMultiWord(query)
        );

        /*
         * Remove duplicates.
         */
        LinkedHashSet<ProductDocument>
            uniqueCandidates =
                new LinkedHashSet<>(
                    candidates
                );

        candidates =
            new ArrayList<>(
                uniqueCandidates
            );

        /*
         * -----------------------------------------
         * STEP 2
         * -----------------------------------------
         * If Trie found nothing, use
         * Levenshtein typo search.
         */
        if (candidates.isEmpty()) {

            ArrayList<LevenshteinSearch.Result>
                typoResults =
                    LevenshteinSearch.search(
                        corpus,
                        query
                    );

            for (
                LevenshteinSearch.Result result :
                typoResults
            ) {

                for (
                    ProductDocument product :
                    corpus
                ) {

                    if (
                        product
                            .getFileName()
                            .equals(
                                result.fileName
                            )
                    ) {

                        candidates.add(
                            product
                        );

                        break;
                    }
                }
            }
        }

        /*
         * -----------------------------------------
         * STEP 3
         * -----------------------------------------
         * If still nothing found, return empty.
         */
        if (candidates.isEmpty()) {

            return new ArrayList<>();
        }

        /*
         * -----------------------------------------
         * STEP 4
         * -----------------------------------------
         * Rank candidates.
         */
        return ProductRanker.rank(
            candidates,
            query
        );
    }

    /*
     * Prefix suggestions.
     */
    public ArrayList<String>
        getSuggestions(String prefix) {

        return trieSearch.searchPrefix(
            prefix
        );
    }
}
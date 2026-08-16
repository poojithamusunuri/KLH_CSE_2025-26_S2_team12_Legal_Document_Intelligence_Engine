import java.util.*;

public class ProductRanker {

    public static class RankedProduct {

        ProductDocument product;
        double score;

        RankedProduct(
                ProductDocument product,
                double score) {

            this.product = product;
            this.score = score;
        }
    }

    private static final Set<String> STOP_WORDS =
        new HashSet<>(Arrays.asList(
            "a", "an", "and", "are",
            "for", "from", "has", "have",
            "in", "is", "it", "of",
            "on", "or", "the", "to",
            "with", "this", "that"
        ));

    public static ArrayList<RankedProduct> rank(
            ArrayList<ProductDocument> products,
            String query) {

        ArrayList<RankedProduct> results =
            new ArrayList<>();

        if (products == null ||
            products.isEmpty() ||
            query == null ||
            query.trim().isEmpty()) {

            return results;
        }

        String normalizedQuery =
            normalize(query);

        String[] queryTerms =
            getMeaningfulTerms(normalizedQuery);

        if (queryTerms.length == 0) {
            return results;
        }

        PriorityQueue<RankedProduct> maxHeap =
            new PriorityQueue<>(
                (a, b) -> {

                    int comparison =
                        Double.compare(
                            b.score,
                            a.score
                        );

                    if (comparison != 0) {
                        return comparison;
                    }

                    return a.product
                        .getProductName()
                        .compareToIgnoreCase(
                            b.product.getProductName()
                        );
                }
            );

        for (ProductDocument product : products) {

            double score =
                calculateScore(
                    product,
                    normalizedQuery,
                    queryTerms
                );

            if (score > 0) {

                maxHeap.offer(
                    new RankedProduct(
                        product,
                        score
                    )
                );
            }
        }

        while (!maxHeap.isEmpty()) {
            results.add(maxHeap.poll());
        }

        return results;
    }

    private static double calculateScore(
            ProductDocument product,
            String query,
            String[] queryTerms) {

        String productName =
            normalize(
                product.getProductName()
            );

        double score = 0.0;

        /*
         * =========================================
         * PRODUCT NAME
         * =========================================
         */

        if (productName.equals(query)) {

            score += 300;
        }

        else if (productName.contains(query)) {

            score += 180;
        }

        else if (productName.startsWith(query)) {

            score += 100;
        }

        /*
         * =========================================
         * SEARCH INDEX FIELDS
         * =========================================
         */

        Map<String, String> fields =
            extractSearchFields(
                product.getContent()
            );

        score += scoreField(
            fields.get("primary"),
            query,
            queryTerms,
            70
        );

        score += scoreField(
            fields.get("feature"),
            query,
            queryTerms,
            55
        );

        score += scoreField(
            fields.get("technical"),
            query,
            queryTerms,
            50
        );

        score += scoreField(
            fields.get("aliases"),
            query,
            queryTerms,
            40
        );

        score += scoreField(
            fields.get("synonyms"),
            query,
            queryTerms,
            35
        );

        score += scoreField(
            fields.get("phrases"),
            query,
            queryTerms,
            35
        );

        score += scoreField(
            fields.get("intent"),
            query,
            queryTerms,
            25
        );

        score += scoreField(
            fields.get("related"),
            query,
            queryTerms,
            15
        );

        /*
         * =========================================
         * QUERY COVERAGE
         * =========================================
         */

        int matchedTerms = 0;

        for (String term : queryTerms) {

            if (containsWholeWord(
                    productName,
                    term)) {

                matchedTerms++;
            }
        }

        if (matchedTerms == queryTerms.length) {

            score += 80;
        }

        else if (matchedTerms >= 2) {

            score += 30;
        }

        /*
         * =========================================
         * SEARCHABLE CONTENT
         * =========================================
         */

        String searchableContent =
            normalize(
                product.getSearchableContent()
            );

        for (String term : queryTerms) {

            if (containsWholeWord(
                    searchableContent,
                    term)) {

                score += 5;
            }
        }

        /*
         * =========================================
         * WEAK MULTI-TERM MATCH PENALTY
         * =========================================
         */

        if (queryTerms.length >= 2 &&
            matchedTerms <= 1) {

            score *= 0.30;
        }

        return score;
    }

    /*
     * ==================================================
     * FIELD-AWARE + PHRASE-AWARE SCORING
     * ==================================================
     */
    private static double scoreField(
            String field,
            String query,
            String[] queryTerms,
            double weight) {

        if (field == null ||
            field.trim().isEmpty()) {

            return 0.0;
        }

        String normalizedField =
            normalize(field);

        double score = 0.0;

        /*
         * ------------------------------------------
         * 1. EXACT PHRASE
         * ------------------------------------------
         *
         * Example:
         *
         * wireless charging
         *
         * appearing exactly in one field.
         */
        if (normalizedField.contains(query)) {

            score += weight * 3.0;
        }

        /*
         * ------------------------------------------
         * 2. COUNT INDIVIDUAL TERMS
         * ------------------------------------------
         */

        int matchedTerms = 0;

        for (String term : queryTerms) {

            if (containsWholeWord(
                    normalizedField,
                    term)) {

                matchedTerms++;

                score += weight;
            }

            else if (containsWordPrefix(
                    normalizedField,
                    term)) {

                matchedTerms++;

                score += weight * 0.5;
            }
        }

        /*
         * ------------------------------------------
         * 3. ALL TERMS IN SAME FIELD
         * ------------------------------------------
         */

        if (matchedTerms == queryTerms.length) {

            score += weight * 1.5;

            /*
             * --------------------------------------
             * 4. PROXIMITY BONUS
             * --------------------------------------
             *
             * If all query terms are close together,
             * this is stronger evidence that they
             * describe the same feature.
             */
            if (areTermsClose(
                    normalizedField,
                    queryTerms,
                    5)) {

                score += weight * 2.0;
            }
        }

        /*
         * ------------------------------------------
         * 5. PARTIAL MULTI-TERM MATCH
         * ------------------------------------------
         */

        else if (matchedTerms >= 2) {

            score += weight * 0.5;
        }

        return score;
    }

    /*
     * Checks whether all query terms occur within
     * a small word-distance window.
     */
    private static boolean areTermsClose(
            String text,
            String[] terms,
            int maxDistance) {

        String[] words =
            text.split("\\s+");

        ArrayList<Integer> positions =
            new ArrayList<>();

        for (String term : terms) {

            int bestPosition = -1;

            for (int i = 0; i < words.length; i++) {

                String word = words[i];

                if (word.equals(term) ||
                    word.startsWith(term)) {

                    bestPosition = i;
                    break;
                }
            }

            if (bestPosition == -1) {
                return false;
            }

            positions.add(bestPosition);
        }

        int min = Collections.min(positions);
        int max = Collections.max(positions);

        return (max - min) <= maxDistance;
    }

    /*
     * ==================================================
     * SEARCH INDEX FIELD EXTRACTION
     * ==================================================
     */
    private static Map<String, String>
        extractSearchFields(String content) {

        Map<String, String> fields =
            new HashMap<>();

        fields.put("primary", "");
        fields.put("feature", "");
        fields.put("technical", "");
        fields.put("aliases", "");
        fields.put("synonyms", "");
        fields.put("phrases", "");
        fields.put("intent", "");
        fields.put("related", "");

        if (content == null) {
            return fields;
        }

        String[] lines =
            content.split("\\R");

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

            if (!insideIndex) {
                continue;
            }

            String lower =
                trimmed.toLowerCase();

            if (lower.startsWith(
                    "primary keywords:")) {

                fields.put(
                    "primary",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "feature keywords:")) {

                fields.put(
                    "feature",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "technical keywords:")) {

                fields.put(
                    "technical",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "aliases:")) {

                fields.put(
                    "aliases",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "synonyms:")) {

                fields.put(
                    "synonyms",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "search phrases:")) {

                fields.put(
                    "phrases",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "search intent keywords:")) {

                fields.put(
                    "intent",
                    getValue(trimmed)
                );
            }

            else if (lower.startsWith(
                    "related search terms:")) {

                fields.put(
                    "related",
                    getValue(trimmed)
                );
            }
        }

        return fields;
    }

    private static String getValue(
            String line) {

        int colon =
            line.indexOf(":");

        if (colon == -1) {
            return "";
        }

        return line.substring(
            colon + 1
        );
    }

    /*
     * ==================================================
     * QUERY TOKENIZATION
     * ==================================================
     */
    private static String[] getMeaningfulTerms(
            String query) {

        String[] rawTerms =
            query.split("\\s+");

        ArrayList<String> meaningful =
            new ArrayList<>();

        for (String term : rawTerms) {

            /*
             * Keep one-character meaningful terms
             * such as "s" in "S Pen".
             */
            if (STOP_WORDS.contains(term)) {
                continue;
            }

            if (term.isEmpty()) {
                continue;
            }

            meaningful.add(term);
        }

        return meaningful.toArray(
            new String[0]
        );
    }

    private static boolean containsWholeWord(
            String text,
            String term) {

        String pattern =
            "\\b"
            + java.util.regex.Pattern.quote(term)
            + "\\b";

        return text.matches(
            "(?s).*" + pattern + ".*"
        );
    }

    private static boolean containsWordPrefix(
            String text,
            String term) {

        String pattern =
            "(?s).*\\b"
            + java.util.regex.Pattern.quote(term)
            + "[a-z0-9]*\\b.*";

        return text.matches(pattern);
    }

    private static String normalize(
            String text) {

        if (text == null) {
            return "";
        }

        return text
            .toLowerCase()
            .replaceAll(
                "[^a-z0-9]+",
                " "
            )
            .trim();
    }
}
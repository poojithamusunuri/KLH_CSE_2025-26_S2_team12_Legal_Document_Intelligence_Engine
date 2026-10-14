package search;

import algorithms.Levenshtein;
import structures.SimpleList;

public class FuzzyLegalSearch {

    public static class FuzzyResult {

        private final String term;
        private final int distance;

        public FuzzyResult(String term, int distance) {
            this.term = term;
            this.distance = distance;
        }

        public String getTerm() {
            return term;
        }

        public int getDistance() {
            return distance;
        }
    }

    private final LegalTermCatalog catalog;

    public FuzzyLegalSearch(LegalTermCatalog catalog) {
        this.catalog = catalog;
    }

    public SimpleList<FuzzyResult> search(
            String query,
            int maxDistance) {

        SimpleList<FuzzyResult> results =
                new SimpleList<FuzzyResult>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery = normalize(query);

        if (normalizedQuery.isEmpty()) {
            return results;
        }

        SimpleList<String> terms =
                catalog.getTerms();

        for (int i = 0; i < terms.size(); i++) {

            String term = terms.get(i);

            String baseTerm =
                    removeYear(term);

            String normalizedTerm =
                    normalize(baseTerm);

            if (normalizedTerm.isEmpty()) {
                continue;
            }

            int distance =
                    Levenshtein.distance(
                            normalizedQuery,
                            normalizedTerm
                    );

            /*
             * ========================================================
             * 1. DIRECT LEVENSHTEIN MATCH
             * ========================================================
             *
             * Handles small spelling mistakes.
             *
             * Examples:
             *
             * divorce  -> divorse
             * marriage -> marrage
             * murder   -> muder
             * contract -> contrct
             * conspiracy -> cnspiracy
             *
             */

            if (isAcceptableEditDistance(
                    normalizedQuery,
                    normalizedTerm,
                    distance,
                    maxDistance)) {

                results.add(
                        new FuzzyResult(
                                term,
                                distance
                        )
                );

                continue;
            }

            /*
             * ========================================================
             * 2. STRUCTURAL / PARTIAL MATCH
             * ========================================================
             *
             * Handles cases where several characters are missing
             * or corrupted but the overall word structure remains
             * recognizable.
             *
             * Example:
             *
             * divorce -> divse
             *
             */

            // Do not use structural/LCS similarity as a fallback. It can
            // rewrite valid everyday words (for example "rape" or "dowry")
            // to unrelated legal catalog entries. Bounded edit distance is
            // intentionally conservative; unknown words remain text queries.

            /*
             * ========================================================
             * 3. TRANSPOSITION MATCH
             * ========================================================
             *
             * Handles adjacent characters being swapped.
             *
             * Examples:
             *
             * marriage -> marraige
             * receive  -> recieve
             *
             */

            if (isTranspositionMatch(
                    normalizedQuery,
                    normalizedTerm,
                    maxDistance)) {

                results.add(
                        new FuzzyResult(
                                term,
                                distance
                        )
                );
            }
        }

        return results;
    }

    /*
     * ================================================================
     * ACCEPTABLE EDIT DISTANCE
     * ================================================================
     *
     * This method prevents unrelated words from becoming fuzzy
     * matches simply because their edit-distance ratio happens
     * to look acceptable.
     *
     * Example of a BAD match:
     *
     * conspiracy -> contract
     *
     * The words are legally different concepts and should not be
     * treated as spelling variations of each other.
     *
     */

    private static boolean isAcceptableEditDistance(
            String query,
            String term,
            int distance,
            int maxDistance) {

        int shorterLength =
                Math.min(
                        query.length(),
                        term.length()
                );

        int longerLength =
                Math.max(
                        query.length(),
                        term.length()
                );

        /*
         * ------------------------------------------------------------
         * Very short words need strict matching.
         *
         * This prevents examples such as:
         *
         * sale -> assault
         *
         * from becoming fuzzy matches.
         * ------------------------------------------------------------
         */

        if (shorterLength <= 5) {
            return distance <= 1;
        }

        /*
         * ------------------------------------------------------------
         * Prevent words with significantly different lengths from
         * being considered fuzzy matches.
         *
         * Example:
         *
         * conspiracy -> contract
         *
         * should not be accepted merely because of a similarity
         * calculation.
         * ------------------------------------------------------------
         */

        if (longerLength > shorterLength + 2) {
            return false;
        }

        /*
         * ------------------------------------------------------------
         * Determine allowed edit distance based on word length.
         * ------------------------------------------------------------
         */

        int allowed;

        if (shorterLength <= 7) {
            allowed = 1;
        } else {
            allowed = 2;
        }

        /*
         * Never allow more than the configured maximum distance.
         */

        allowed = Math.min(allowed, Math.max(1, maxDistance));

        /*
         * ------------------------------------------------------------
         * Direct edit-distance match.
         * ------------------------------------------------------------
         */

        if (distance <= allowed) {
            return true;
        }

        /*
         * ------------------------------------------------------------
         * Additional similarity check.
         *
         * This provides another safety layer against unrelated
         * words.
         * ------------------------------------------------------------
         */

        double similarity =
                1.0
                - ((double) distance / longerLength);

        return similarity >= 0.75
                && distance <= allowed;
    }

    /*
     * ================================================================
     * STRONG STRUCTURAL MATCH
     * ================================================================
     *
     * Uses:
     *
     * - Longest Common Subsequence
     * - Prefix similarity
     * - Suffix similarity
     * - Relative word length
     *
     */

    private static boolean isStrongStructuralMatch(
            String query,
            String term) {

        if (query.length() < 4 || term.length() < 4) {
            return false;
        }

        int shorterLength =
                Math.min(
                        query.length(),
                        term.length()
                );

        int longerLength =
                Math.max(
                        query.length(),
                        term.length()
                );

        /*
         * Do not allow extremely different word lengths.
         */

        /*
         * Do not allow words with significantly different lengths
         * to be considered structurally similar.
         *
         * Example:
         *
         * conspiracy -> contract
         *
         * must not be accepted.
         */
        if (longerLength > shorterLength + 2) {
            return false;
        }

        /*
         * Calculate Longest Common Subsequence.
         */

        int lcs =
                longestCommonSubsequence(
                        query,
                        term
                );

        double lcsSimilarity =
                (double) lcs / shorterLength;

        /*
         * Calculate prefix similarity.
         */

        int prefix =
                commonPrefixLength(
                        query,
                        term
                );

        double prefixSimilarity =
                (double) prefix / shorterLength;

        /*
         * Calculate suffix similarity.
         */

        int suffix =
                commonSuffixLength(
                        query,
                        term
                );

        double suffixSimilarity =
                (double) suffix / shorterLength;

        /*
         * LCS is the most important component because it detects
         * characters that remain in the same order even when
         * characters are removed from the middle.
         */

        if (lcsSimilarity >= 0.70) {
            return true;
        }

        if (lcsSimilarity >= 0.60
                && (prefixSimilarity >= 0.25
                || suffixSimilarity >= 0.25)) {

            return true;
        }

        return false;
    }

    /*
     * ================================================================
     * LONGEST COMMON SUBSEQUENCE
     * ================================================================
     *
     * Example:
     *
     * divorce
     * divse
     *
     * Common ordered characters include:
     *
     * d i v e
     *
     */

    private static int longestCommonSubsequence(
            String a,
            String b) {

        int m = a.length();
        int n = b.length();

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                if (a.charAt(i - 1)
                        == b.charAt(j - 1)) {

                    dp[i][j] =
                            dp[i - 1][j - 1] + 1;

                } else {

                    dp[i][j] =
                            Math.max(
                                    dp[i - 1][j],
                                    dp[i][j - 1]
                            );
                }
            }
        }

        return dp[m][n];
    }

    /*
     * ================================================================
     * TRANSPOSITION MATCH
     * ================================================================
     *
     * Detects a simple adjacent character swap.
     *
     */

    private static boolean isTranspositionMatch(
            String query,
            String term,
            int maxDistance) {

        if (query.length() != term.length()) {
            return false;
        }

        int differences = 0;

        int firstDifference = -1;
        int secondDifference = -1;

        for (int i = 0; i < query.length(); i++) {

            if (query.charAt(i)
                    != term.charAt(i)) {

                differences++;

                if (firstDifference == -1) {

                    firstDifference = i;

                } else if (secondDifference == -1) {

                    secondDifference = i;
                }
            }
        }

        /*
         * A transposition must produce exactly two
         * different positions.
         */

        if (differences != 2) {
            return false;
        }

        /*
         * The two differences must be adjacent.
         */

        if (secondDifference
                != firstDifference + 1) {

            return false;
        }

        /*
         * Verify that the two characters were actually swapped.
         */

        return query.charAt(firstDifference)
                == term.charAt(secondDifference)

                && query.charAt(secondDifference)
                == term.charAt(firstDifference)

                && maxDistance >= 1;
    }

    /*
     * ================================================================
     * PREFIX HELPER
     * ================================================================
     */

    private static int commonPrefixLength(
            String a,
            String b) {

        int length =
                Math.min(
                        a.length(),
                        b.length()
                );

        int count = 0;

        for (int i = 0; i < length; i++) {

            if (a.charAt(i)
                    != b.charAt(i)) {

                break;
            }

            count++;
        }

        return count;
    }

    /*
     * ================================================================
     * SUFFIX HELPER
     * ================================================================
     */

    private static int commonSuffixLength(
            String a,
            String b) {

        int i = a.length() - 1;
        int j = b.length() - 1;

        int count = 0;

        while (i >= 0
                && j >= 0
                && a.charAt(i)
                == b.charAt(j)) {

            count++;

            i--;
            j--;
        }

        return count;
    }

    /*
     * ================================================================
     * NORMALIZATION
     * ================================================================
     *
     * Converts text to lowercase and removes unnecessary
     * punctuation.
     *
     */

    private static String normalize(String text) {

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * ================================================================
     * REMOVE YEAR FROM ACT NAMES
     * ================================================================
     *
     * Example:
     *
     * Indian Contract Act, 1872
     *
     * becomes:
     *
     * Indian Contract Act
     *
     */

    private static String removeYear(String term) {

        return term
                .replaceAll(
                        ",?\\s*\\(?\\d{4}\\)?\\s*$",
                        ""
                )
                .trim();
    }
}
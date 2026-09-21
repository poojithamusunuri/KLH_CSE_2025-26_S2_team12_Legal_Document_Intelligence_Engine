package algorithms;

public class LCPArray {

    /*
     * Builds the LCP array using Kasai's algorithm.
     *
     * LCP[i] stores the length of the longest common prefix
     * between:
     *
     * suffixArray[i]
     * and
     * suffixArray[i - 1]
     *
     * LCP[0] is always 0 because there is no previous suffix.
     */

    public static int[] build(
            String text,
            int[] suffixArray) {

        if (text == null || suffixArray == null) {
            return new int[0];
        }

        int n = text.length();

        if (suffixArray.length != n) {
            return new int[0];
        }

        int[] lcp = new int[n];

        /*
         * inverseRank[position] tells us the position of
         * a suffix in the suffix array.
         *
         * Example:
         *
         * SA = [5, 3, 1, 0, 4, 2]
         *
         * inverseRank[5] = 0
         * inverseRank[3] = 1
         * inverseRank[1] = 2
         * ...
         */
        int[] inverseRank = new int[n];

        for (int i = 0; i < n; i++) {
            inverseRank[suffixArray[i]] = i;
        }

        /*
         * h represents the number of characters already known
         * to match from the previous comparison.
         */
        int h = 0;

        /*
         * Kasai's algorithm.
         */
        for (int i = 0; i < n; i++) {

            int rank = inverseRank[i];

            /*
             * First suffix has no previous suffix.
             */
            if (rank == 0) {
                h = 0;
                continue;
            }

            /*
             * Find the suffix immediately before this suffix
             * in lexicographical order.
             */
            int previousSuffix =
                    suffixArray[rank - 1];

            /*
             * Compare characters while they are equal.
             */
            while (
                    i + h < n
                    && previousSuffix + h < n
                    && text.charAt(i + h)
                            == text.charAt(previousSuffix + h)
            ) {
                h++;
            }

            lcp[rank] = h;

            /*
             * Kasai's key optimization:
             *
             * When moving to the next suffix, at least h-1
             * characters are already known to match.
             */
            if (h > 0) {
                h--;
            }
        }

        return lcp;
    }
}

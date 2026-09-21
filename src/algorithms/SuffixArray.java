package algorithms;

public class SuffixArray {

    /*
     * Builds the suffix array using the
     * prefix-doubling technique.
     *
     * suffixArray[i] stores the starting position of
     * the i-th lexicographically smallest suffix.
     *
     * Example:
     *
     * text = "banana"
     *
     * Sorted suffixes:
     *
     * a       -> 5
     * ana     -> 3
     * anana   -> 1
     * banana  -> 0
     * na      -> 4
     * nana    -> 2
     *
     * Result:
     *
     * [5, 3, 1, 0, 4, 2]
     *
     * The implementation uses:
     *
     * 1. Rank assignment
     * 2. Prefix doubling
     * 3. Counting sort
     *
     * This avoids using Java's built-in sorting methods.
     */

    public static int[] build(String text) {

        if (text == null || text.length() == 0) {
            return new int[0];
        }

        int n = text.length();

        /*
         * suffixArray[i] initially represents
         * suffix starting at position i.
         */
        int[] suffixArray = new int[n];

        /*
         * rank[i] represents the rank of the suffix
         * beginning at position i.
         *
         * Initially the rank is based on the character.
         *
         * We do NOT use the character's ASCII value
         * directly as an array index.
         */
        int[] rank = new int[n];

        /*
         * Compress characters into integer ranks.
         *
         * Example:
         *
         * banana
         *
         * a -> 0
         * b -> 1
         * n -> 2
         *
         * So:
         *
         * b a n a n a
         * 1 0 2 0 2 0
         */

        char[] characters = new char[n];

        for (int i = 0; i < n; i++) {
            characters[i] = text.charAt(i);
        }

        /*
         * Sort characters using simple comparison.
         *
         * This is only used to create the initial
         * compressed character ranks.
         */
        for (int i = 0; i < n - 1; i++) {

            for (int j = i + 1; j < n; j++) {

                if (characters[i] > characters[j]) {

                    char temp = characters[i];
                    characters[i] = characters[j];
                    characters[j] = temp;
                }
            }
        }

        /*
         * Remove duplicate characters while assigning
         * compressed ranks.
         */
        int uniqueCount = 0;

        for (int i = 0; i < n; i++) {

            if (i == 0 || characters[i] != characters[i - 1]) {
                uniqueCount++;
            }
        }

        char[] uniqueCharacters = new char[uniqueCount];

        int uniqueIndex = 0;

        for (int i = 0; i < n; i++) {

            if (i == 0 || characters[i] != characters[i - 1]) {

                uniqueCharacters[uniqueIndex] = characters[i];
                uniqueIndex++;
            }
        }

        /*
         * Assign compressed rank to every original
         * character.
         */
        for (int i = 0; i < n; i++) {

            char current = text.charAt(i);

            for (int j = 0; j < uniqueCount; j++) {

                if (current == uniqueCharacters[j]) {

                    rank[i] = j;
                    break;
                }
            }
        }

        /*
         * Prefix-doubling.
         *
         * First we sort suffixes according to
         * their first 1 character.
         *
         * Then first 2 characters.
         *
         * Then first 4.
         *
         * Then first 8...
         *
         * until the complete suffixes are ordered.
         */

        int[] newRank = new int[n];

        int[] firstKey = new int[n];
        int[] secondKey = new int[n];

        for (int i = 0; i < n; i++) {
            suffixArray[i] = i;
        }

        int length = 1;

        while (length < n) {

            /*
             * Build the second key.
             *
             * For suffix starting at i:
             *
             * first key  = rank[i]
             * second key = rank[i + length]
             *
             * If i + length >= n,
             * second key is -1.
             */

            for (int i = 0; i < n; i++) {

                firstKey[i] = rank[i];

                if (i + length < n) {
                    secondKey[i] = rank[i + length];
                } else {
                    secondKey[i] = -1;
                }
            }

            /*
             * Sort suffixes by second key first.
             */
            countingSort(
                    suffixArray,
                    secondKey,
                    n,
                    -1,
                    uniqueCount
            );

            /*
             * Then sort by first key.
             */
            countingSort(
                    suffixArray,
                    firstKey,
                    n,
                    0,
                    uniqueCount
            );

            /*
             * Assign new ranks after sorting.
             */
            newRank[suffixArray[0]] = 0;

            int classes = 1;

            for (int i = 1; i < n; i++) {

                int current = suffixArray[i];
                int previous = suffixArray[i - 1];

                boolean different =
                        firstKey[current] != firstKey[previous]
                        ||
                        secondKey[current] != secondKey[previous];

                if (different) {
                    classes++;
                }

                newRank[current] = classes - 1;
            }

            /*
             * Copy new ranks.
             */
            for (int i = 0; i < n; i++) {
                rank[i] = newRank[i];
            }

            uniqueCount = classes;

            /*
             * Double the prefix length.
             */
            length *= 2;
        }

        return suffixArray;
    }

    /*
     * Counting sort based on integer ranks.
     *
     * IMPORTANT:
     *
     * We use compressed ranks rather than
     * character ASCII values.
     *
     * Therefore:
     *
     * 'b' = 98
     *
     * is NEVER used as:
     *
     * count[98]
     *
     * Instead the compressed rank might be:
     *
     * b -> 1
     *
     * which is safe.
     */
    private static void countingSort(
            int[] suffixArray,
            int[] key,
            int n,
            int minimumKey,
            int numberOfClasses) {

        /*
         * Keys range from:
         *
         * minimumKey
         *
         * to
         *
         * numberOfClasses - 1
         *
         * For the second key, -1 represents
         * the end of the string.
         */

        int range = numberOfClasses;

        if (minimumKey == -1) {
            range++;
        }

        int[] count = new int[range];

        /*
         * Count occurrences.
         */
        for (int i = 0; i < n; i++) {

            int value = key[i];

            int index;

            if (minimumKey == -1) {

                /*
                 * Convert:
                 *
                 * -1 -> 0
                 *  0 -> 1
                 *  1 -> 2
                 * ...
                 */
                index = value + 1;

            } else {

                /*
                 * Normal ranks:
                 *
                 * 0 -> 0
                 * 1 -> 1
                 * 2 -> 2
                 */
                index = value;
            }

            count[index]++;
        }

        /*
         * Convert count array into
         * cumulative positions.
         */
        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }

        int[] output = new int[n];

        /*
         * Build sorted output.
         *
         * Iterate backwards to preserve stability.
         */
        for (int i = n - 1; i >= 0; i--) {

            int suffix = suffixArray[i];

            int value = key[suffix];

            int index;

            if (minimumKey == -1) {
                index = value + 1;
            } else {
                index = value;
            }

            count[index]--;

            output[count[index]] = suffix;
        }

        /*
         * Copy sorted suffix positions back.
         */
        for (int i = 0; i < n; i++) {
            suffixArray[i] = output[i];
        }
    }
}

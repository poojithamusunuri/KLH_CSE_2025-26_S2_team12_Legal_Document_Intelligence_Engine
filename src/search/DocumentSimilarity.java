package search;

import algorithms.SuffixArray;
import algorithms.LCPArray;

public class DocumentSimilarity {

    /*
     * Calculates textual similarity between two documents
     * using Suffix Array + LCP (Kasai) analysis.
     *
     * Instead of using only the single longest common sequence,
     * this implementation measures how much of each document is
     * covered by shared textual sequences discovered through the
     * LCP array.
     *
     * This produces a more meaningful document-level similarity
     * score while keeping Suffix Array + LCP as the core algorithm.
     */
    public static double similarity(
            String first,
            String second) {

        if (first == null || second == null) {
            return 0.0;
        }

        String a = normalize(first);
        String b = normalize(second);

        if (a.length() == 0 || b.length() == 0) {
            return 0.0;
        }

        String combined = a + "#" + b;

        int[] suffixArray =
                SuffixArray.build(combined);

        int[] lcp =
                LCPArray.build(
                        combined,
                        suffixArray
                );

        int boundary = a.length();

        /*
         * Difference arrays allow us to calculate the union of
         * all shared-text intervals efficiently.
         *
         * coverageA[x] = number of shared LCP intervals covering
         * position x of document A.
         *
         * coverageB[x] = same for document B.
         */
        int[] diffA = new int[a.length() + 1];
        int[] diffB = new int[b.length() + 1];

        int longestCommonLength = 0;

        /*
         * Examine neighboring suffixes in suffix-array order.
         *
         * When two neighboring suffixes belong to different
         * documents, their LCP is a shared sequence.
         */
        for (int i = 1;
                i < suffixArray.length;
                i++) {

            int current =
                    suffixArray[i];

            int previous =
                    suffixArray[i - 1];

            boolean currentInFirst =
                    current < boundary;

            boolean previousInFirst =
                    previous < boundary;

            if (currentInFirst != previousInFirst) {

                int common = lcp[i];

                /*
                 * Make sure the common sequence cannot cross the
                 * separator or the end of either document.
                 */
                if (currentInFirst) {

                    common = Math.min(
                            common,
                            a.length() - current
                    );

                    common = Math.min(
                            common,
                            b.length() - (previous - boundary - 1)
                    );

                    if (common > 0) {

                        if (common > longestCommonLength) {
                            longestCommonLength = common;
                        }

                        int startA = current;
                        int startB =
                                previous - boundary - 1;

                        addInterval(
                                diffA,
                                startA,
                                startA + common
                        );

                        addInterval(
                                diffB,
                                startB,
                                startB + common
                        );
                    }

                } else {

                    common = Math.min(
                            common,
                            a.length() - previous
                    );

                    common = Math.min(
                            common,
                            b.length() - (current - boundary - 1)
                    );

                    if (common > 0) {

                        if (common > longestCommonLength) {
                            longestCommonLength = common;
                        }

                        int startA = previous;
                        int startB =
                                current - boundary - 1;

                        addInterval(
                                diffA,
                                startA,
                                startA + common
                        );

                        addInterval(
                                diffB,
                                startB,
                                startB + common
                        );
                    }
                }
            }
        }

        /*
         * Convert difference arrays into actual coverage.
         */
        int coveredA =
                calculateCoverage(diffA);

        int coveredB =
                calculateCoverage(diffB);

        /*
         * Calculate coverage ratios for both documents.
         */
        double coverageA =
                (double) coveredA / a.length();

        double coverageB =
                (double) coveredB / b.length();

        /*
         * Use the smaller coverage as the final similarity.
         *
         * This prevents a very short document from appearing
         * highly similar merely because a small part of it occurs
         * inside a much larger document.
         */
        double similarity =
                Math.min(
                        coverageA,
                        coverageB
                );

        return similarity;
    }

    /*
     * Returns the length of the longest common textual sequence
     * between two documents using Suffix Array + LCP.
     */
    public static int longestCommonSequence(
            String first,
            String second) {

        if (first == null || second == null) {
            return 0;
        }

        String a = normalize(first);
        String b = normalize(second);

        if (a.length() == 0 || b.length() == 0) {
            return 0;
        }

        String combined =
                a + "#" + b;

        int[] suffixArray =
                SuffixArray.build(combined);

        int[] lcp =
                LCPArray.build(
                        combined,
                        suffixArray
                );

        int boundary = a.length();
        int longest = 0;

        for (int i = 1;
                i < suffixArray.length;
                i++) {

            int current =
                    suffixArray[i];

            int previous =
                    suffixArray[i - 1];

            boolean currentInFirst =
                    current < boundary;

            boolean previousInFirst =
                    previous < boundary;

            if (currentInFirst != previousInFirst) {

                int common = lcp[i];

                /*
                 * Prevent the separator from being included.
                 */
                if (currentInFirst) {

                    common = Math.min(
                            common,
                            a.length() - current
                    );

                    common = Math.min(
                            common,
                            b.length()
                                    - (previous - boundary - 1)
                    );

                } else {

                    common = Math.min(
                            common,
                            a.length() - previous
                    );

                    common = Math.min(
                            common,
                            b.length()
                                    - (current - boundary - 1)
                    );
                }

                if (common > longest) {
                    longest = common;
                }
            }
        }

        return longest;
    }

    /*
     * Adds the half-open interval [start, end) to a difference array.
     */
    private static void addInterval(
            int[] difference,
            int start,
            int end) {

        if (start < 0) {
            start = 0;
        }

        if (end > difference.length - 1) {
            end = difference.length - 1;
        }

        if (start >= end) {
            return;
        }

        difference[start]++;

        difference[end]--;
    }

    /*
     * Calculates how many positions are covered by at least one
     * shared-text interval.
     */
    private static int calculateCoverage(
            int[] difference) {

        int active = 0;
        int covered = 0;

        for (int i = 0;
                i < difference.length - 1;
                i++) {

            active += difference[i];

            if (active > 0) {
                covered++;
            }
        }

        return covered;
    }

    /*
     * Normalizes legal text so that differences in capitalization,
     * punctuation and repeated whitespace do not affect comparison.
     */
    private static String normalize(
            String text) {

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

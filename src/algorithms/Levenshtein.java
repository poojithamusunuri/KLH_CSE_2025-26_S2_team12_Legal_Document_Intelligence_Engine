package algorithms;

public class Levenshtein {

    /**
     * Calculates the standard Levenshtein edit distance.
     *
     * Allowed operations:
     * 1. Insertion
     * 2. Deletion
     * 3. Replacement
     */
    public static int distance(String a, String b) {

        if (a == null || b == null) {
            return Integer.MAX_VALUE;
        }

        a = a.toLowerCase();
        b = b.toLowerCase();

        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        // Empty string -> b
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // a -> empty string
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                if (a.charAt(i - 1) == b.charAt(j - 1)) {

                    dp[i][j] = dp[i - 1][j - 1];

                } else {

                    int insertion =
                            dp[i][j - 1];

                    int deletion =
                            dp[i - 1][j];

                    int replacement =
                            dp[i - 1][j - 1];

                    dp[i][j] =
                            1 + Math.min(
                                    insertion,
                                    Math.min(
                                            deletion,
                                            replacement
                                    )
                            );
                }
            }
        }

        return dp[m][n];
    }


    /**
     * Damerau-Levenshtein distance.
     *
     * In addition to insertion, deletion and replacement,
     * this method treats adjacent character transposition
     * as one operation.
     *
     * Example:
     *
     * "divocre" -> "divorce"
     *
     * The swapped characters are handled naturally.
     */
    public static int damerauDistance(String a, String b) {

        if (a == null || b == null) {
            return Integer.MAX_VALUE;
        }

        a = a.toLowerCase();
        b = b.toLowerCase();

        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int cost =
                        (a.charAt(i - 1) == b.charAt(j - 1))
                                ? 0
                                : 1;

                int deletion =
                        dp[i - 1][j] + 1;

                int insertion =
                        dp[i][j - 1] + 1;

                int substitution =
                        dp[i - 1][j - 1] + cost;

                dp[i][j] =
                        Math.min(
                                deletion,
                                Math.min(
                                        insertion,
                                        substitution
                                )
                        );

                // Adjacent character transposition
                if (i > 1
                        && j > 1
                        && a.charAt(i - 1)
                            == b.charAt(j - 2)
                        && a.charAt(i - 2)
                            == b.charAt(j - 1)) {

                    dp[i][j] =
                            Math.min(
                                    dp[i][j],
                                    dp[i - 2][j - 2] + 1
                            );
                }
            }
        }

        return dp[m][n];
    }
}
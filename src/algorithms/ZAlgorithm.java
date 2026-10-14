package algorithms;

public class ZAlgorithm {

    /*
     * Returns the first index where pattern occurs in text, or -1.
     *
     * z[i] = length of the longest common prefix of the pattern and
     * the substring starting at i. It is computed for the pattern first,
     * then reused while scanning the text. No separator character is
     * concatenated, so patterns/texts may contain any character
     * (including '$').
     */
    public static int search(String text, String pattern) {

        if (text == null || pattern == null) {
            return -1;
        }

        int m = pattern.length();
        int n = text.length();

        if (m == 0) {
            return 0;
        }

        if (m > n) {
            return -1;
        }

        // Z array of the pattern itself.
        int[] zPattern = new int[m];
        int left = 0;
        int right = 0;

        for (int i = 1; i < m; i++) {

            if (i < right) {
                zPattern[i] = Math.min(right - i, zPattern[i - left]);
            }

            while (i + zPattern[i] < m
                    && pattern.charAt(zPattern[i])
                            == pattern.charAt(i + zPattern[i])) {
                zPattern[i]++;
            }

            if (i + zPattern[i] > right) {
                left = i;
                right = i + zPattern[i];
            }
        }

        // Scan the text using the same Z-box idea.
        // [left, right) is the rightmost text window matching a pattern prefix.
        left = 0;
        right = 0;

        for (int i = 0; i <= n - m; i++) {

            int z = 0;

            if (i < right) {
                z = Math.min(right - i, zPattern[i - left]);
            }

            while (z < m && text.charAt(i + z) == pattern.charAt(z)) {
                z++;
            }

            if (z == m) {
                return i;
            }

            if (i + z > right) {
                left = i;
                right = i + z;
            }
        }

        return -1;
    }
}

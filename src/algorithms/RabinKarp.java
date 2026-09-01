package algorithms;

public class RabinKarp {

    private static final int PRIME = 101;

    public static int search(String text, String pattern) {

        if (text == null || pattern == null || pattern.isEmpty()) {
            return -1;
        }

        int textLength = text.length();
        int patternLength = pattern.length();

        if (patternLength > textLength) {
            return -1;
        }

        int patternHash = 0;
        int textHash = 0;
        int highestPower = 1;

        for (int i = 0; i < patternLength - 1; i++) {
            highestPower =
                    (highestPower * 256) % PRIME;
        }

        for (int i = 0; i < patternLength; i++) {

            patternHash =
                    (256 * patternHash
                    + pattern.charAt(i)) % PRIME;

            textHash =
                    (256 * textHash
                    + text.charAt(i)) % PRIME;
        }

        for (int i = 0;
             i <= textLength - patternLength;
             i++) {

            if (patternHash == textHash) {

                int j;

                for (j = 0; j < patternLength; j++) {

                    if (text.charAt(i + j)
                            != pattern.charAt(j)) {
                        break;
                    }
                }

                if (j == patternLength) {
                    return i;
                }
            }

            if (i < textLength - patternLength) {

                textHash =
                        (256 *
                        (textHash
                        - text.charAt(i) * highestPower)
                        + text.charAt(i + patternLength))
                        % PRIME;

                if (textHash < 0) {
                    textHash += PRIME;
                }
            }
        }

        return -1;
    }
}

package algorithms;

public class RabinKarp {

    private static final int BASE = 256;
    private static final int MODULUS_1 = 1_000_000_007;
    private static final int MODULUS_2 = 1_000_000_009;

    public static int search(String text, String pattern) {

        if (text == null || pattern == null || pattern.isEmpty()) {
            return -1;
        }

        int textLength = text.length();
        int patternLength = pattern.length();

        if (patternLength > textLength) {
            return -1;
        }

        long patternHash1 = 0;
        long patternHash2 = 0;
        long textHash1 = 0;
        long textHash2 = 0;

        long highestPower1 = 1;
        long highestPower2 = 1;

        for (int i = 0; i < patternLength - 1; i++) {
            highestPower1 =
                    (highestPower1 * BASE) % MODULUS_1;
            highestPower2 =
                    (highestPower2 * BASE) % MODULUS_2;
        }

        for (int i = 0; i < patternLength; i++) {

            int patternChar = pattern.charAt(i);
            int textChar = text.charAt(i);

            patternHash1 =
                    (patternHash1 * BASE + patternChar)
                            % MODULUS_1;
            patternHash2 =
                    (patternHash2 * BASE + patternChar)
                            % MODULUS_2;

            textHash1 =
                    (textHash1 * BASE + textChar)
                            % MODULUS_1;
            textHash2 =
                    (textHash2 * BASE + textChar)
                            % MODULUS_2;
        }

        for (int i = 0;
             i <= textLength - patternLength;
             i++) {

            if (patternHash1 == textHash1
                    && patternHash2 == textHash2
                    && verify(text, pattern, i)) {

                return i;
            }

            if (i < textLength - patternLength) {

                int outgoingChar = text.charAt(i);
                int incomingChar = text.charAt(i + patternLength);

                textHash1 =
                        ((textHash1
                                - outgoingChar * highestPower1)
                                * BASE
                                + incomingChar)
                                % MODULUS_1;

                textHash2 =
                        ((textHash2
                                - outgoingChar * highestPower2)
                                * BASE
                                + incomingChar)
                                % MODULUS_2;

                if (textHash1 < 0) {
                    textHash1 += MODULUS_1;
                }

                if (textHash2 < 0) {
                    textHash2 += MODULUS_2;
                }
            }
        }

        return -1;
    }

    private static boolean verify(
            String text,
            String pattern,
            int startIndex) {

        for (int i = 0; i < pattern.length(); i++) {

            if (text.charAt(startIndex + i)
                    != pattern.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}

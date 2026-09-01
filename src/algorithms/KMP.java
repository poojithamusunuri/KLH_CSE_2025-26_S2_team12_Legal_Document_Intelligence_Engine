package algorithms;

public class KMP {

    public static int search(String text, String pattern) {

        if (text == null || pattern == null || pattern.isEmpty()) {
            return -1;
        }

        int[] lps = buildLPS(pattern);

        int textIndex = 0;
        int patternIndex = 0;

        while (textIndex < text.length()) {

            if (text.charAt(textIndex) == pattern.charAt(patternIndex)) {
                textIndex++;
                patternIndex++;

                if (patternIndex == pattern.length()) {
                    return textIndex - patternIndex;
                }
            } else {

                if (patternIndex != 0) {
                    patternIndex = lps[patternIndex - 1];
                } else {
                    textIndex++;
                }
            }
        }

        return -1;
    }

    private static int[] buildLPS(String pattern) {

        int[] lps = new int[pattern.length()];

        int length = 0;
        int index = 1;

        while (index < pattern.length()) {

            if (pattern.charAt(index) == pattern.charAt(length)) {
                length++;
                lps[index] = length;
                index++;

            } else {

                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[index] = 0;
                    index++;
                }
            }
        }

        return lps;
    }
}

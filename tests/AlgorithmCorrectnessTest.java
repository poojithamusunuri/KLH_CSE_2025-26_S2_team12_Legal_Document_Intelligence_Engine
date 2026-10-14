package tests;

import algorithms.AhoCorasick;
import algorithms.KMP;
import algorithms.LCPArray;
import algorithms.Levenshtein;
import algorithms.MillerRabin;
import algorithms.RabinKarp;
import algorithms.RandomizedSelect;
import algorithms.SuffixArray;
import algorithms.ZAlgorithm;

import java.math.BigInteger;

/*
 * Verifies every hand-built algorithm against a slow but obviously correct
 * reference. Random inputs come from a tiny linear congruential generator so
 * that the test itself does not need java.util either.
 */
public class AlgorithmCorrectnessTest {

    private static int passed;
    private static int failed;
    private static long seed = 12345;

    public static void main(String[] args) {

        testPatternSearch();
        testSuffixArrayAndLcp();
        testLevenshtein();
        testMillerRabin();
        testAhoCorasick();
        testRandomizedSelect();

        System.out.println("SUMMARY | passed=" + passed + " failed=" + failed);

        if (failed > 0) {
            throw new AssertionError("Algorithm correctness test failed.");
        }
    }

    private static int random(int bound) {
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        return (int) ((seed >>> 33) % bound);
    }

    private static String randomString(String alphabet, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(random(alphabet.length())));
        }
        return builder.toString();
    }

    private static void check(boolean condition, String name) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL | " + name);
        }
    }

    private static void testPatternSearch() {

        int bad = 0;

        for (int t = 0; t < 4000; t++) {

            String text = randomString("ab$", random(30) + 1);
            String pattern = randomString("ab$", random(5) + 1);
            int expected = text.indexOf(pattern);

            if (KMP.search(text, pattern) != expected
                    || ZAlgorithm.search(text, pattern) != expected
                    || RabinKarp.search(text, pattern) != expected) {
                bad++;
            }
        }

        check(bad == 0, "KMP / Z / Rabin-Karp agree with indexOf (incl. '$' text)");
        check(ZAlgorithm.search("xx$", "$") == 2, "Z-algorithm finds '$' pattern");
        check(KMP.search("abc", "") == -1 && ZAlgorithm.search("abc", "") == 0,
                "empty-pattern conventions are stable");
        check(KMP.search(null, "a") == -1 && RabinKarp.search("a", null) == -1,
                "null inputs do not crash");
    }

    private static void testSuffixArrayAndLcp() {

        int bad = 0;

        for (int t = 0; t < 1500; t++) {

            String s = randomString("abc", random(40) + 1);
            int[] sa = SuffixArray.build(s);
            int[] lcp = LCPArray.build(s, sa);

            for (int i = 1; i < s.length(); i++) {

                String x = s.substring(sa[i - 1]);
                String y = s.substring(sa[i]);

                int h = 0;
                while (h < x.length() && h < y.length()
                        && x.charAt(h) == y.charAt(h)) {
                    h++;
                }

                if (x.compareTo(y) >= 0 || lcp[i] != h) {
                    bad++;
                    break;
                }
            }
        }

        check(bad == 0, "suffix array is sorted and Kasai LCP is exact");
        check(SuffixArray.build("banana").length == 6
                && SuffixArray.build("banana")[0] == 5, "banana example");
        check(SuffixArray.build("").length == 0, "empty text");

        // Non-ASCII characters must not break the initial character ranking.
        String unicode = "\u0915\u0916a\u0915b\u0916";
        int[] sa = SuffixArray.build(unicode);
        boolean sorted = true;
        for (int i = 1; i < sa.length; i++) {
            if (unicode.substring(sa[i - 1]).compareTo(unicode.substring(sa[i])) >= 0) {
                sorted = false;
            }
        }
        check(sorted, "suffix array handles non-ASCII characters");
    }

    private static void testLevenshtein() {

        check(Levenshtein.distance("kitten", "sitting") == 3, "Levenshtein kitten/sitting");
        check(Levenshtein.distance("", "abc") == 3, "Levenshtein empty");
        check(Levenshtein.damerauDistance("divocre", "divorce") == 1,
                "Damerau counts a transposition as one edit");
        check(Levenshtein.distance("divocre", "divorce") == 2,
                "plain Levenshtein counts a transposition as two edits");
    }

    private static void testMillerRabin() {

        int bad = 0;

        for (long n = 0; n < 200000; n++) {
            if (MillerRabin.isPrime(n) != BigInteger.valueOf(n).isProbablePrime(40)) {
                bad++;
            }
        }

        check(bad == 0, "Miller-Rabin exact for every n < 200000");

        long[] carmichaelAndPseudoprimes = {
                561, 1105, 1729, 4033, 4681, 3215031751L,
                3825123056546413051L, 9223372036854775807L
        };

        boolean allComposite = true;
        for (int i = 0; i < carmichaelAndPseudoprimes.length; i++) {
            if (MillerRabin.isPrime(carmichaelAndPseudoprimes[i])) {
                allComposite = false;
            }
        }

        check(allComposite, "Carmichael numbers and strong pseudoprimes rejected");
        check(MillerRabin.isPrime(9223372036854775783L), "largest 63-bit prime accepted");
    }

    private static void testAhoCorasick() {

        int bad = 0;

        for (int t = 0; t < 1500; t++) {

            String text = randomString("ab ", random(40) + 1);
            int count = random(4) + 1;
            String[] patterns = new String[count];

            for (int p = 0; p < count; p++) {
                patterns[p] = randomString("ab ", random(4) + 1);
            }

            int[] actual = new AhoCorasick(patterns).countMatches(text);

            for (int p = 0; p < count; p++) {

                int expected = 0;

                for (int i = 0; i + patterns[p].length() <= text.length(); i++) {
                    if (text.startsWith(patterns[p], i)) {
                        expected++;
                    }
                }

                if (actual[p] != expected) {
                    bad++;
                }
            }
        }

        check(bad == 0, "Aho-Corasick counts equal naive overlapping counts");

        int[] counts = new AhoCorasick(new String[] {" he ", " she ", " hers "})
                .countMatches(" she hers he ");
        check(counts[0] == 1 && counts[1] == 1 && counts[2] == 1,
                "whole-word patterns via space padding");
    }

    private static void testRandomizedSelect() {

        int bad = 0;

        for (int t = 0; t < 600; t++) {

            int n = random(60) + 1;
            double[] keys = new double[n];
            int[] tie = new int[n];

            for (int i = 0; i < n; i++) {
                keys[i] = random(6);   // many equal scores on purpose
                tie[i] = i;
            }

            int k = random(n + 3);
            int[] fast = new RandomizedSelect(t + 1).topK(keys, tie, k);

            // Reference: repeatedly pick the best remaining item.
            boolean[] used = new boolean[n];
            int expectedLength = Math.min(k, n);

            if (fast.length != expectedLength) {
                bad++;
                continue;
            }

            for (int r = 0; r < expectedLength; r++) {

                int best = -1;

                for (int i = 0; i < n; i++) {
                    if (used[i]) {
                        continue;
                    }
                    if (best == -1 || keys[i] > keys[best]
                            || (keys[i] == keys[best] && tie[i] < tie[best])) {
                        best = i;
                    }
                }

                used[best] = true;

                if (fast[r] != best) {
                    bad++;
                    break;
                }
            }
        }

        check(bad == 0, "randomised select matches exhaustive ranking (ties broken by id)");

        check(new RandomizedSelect(1).topK(new double[0], new int[0], 5).length == 0,
                "empty input");
        check(new RandomizedSelect(1).topK(new double[] {1}, new int[] {0}, 0).length == 0,
                "k = 0");
    }
}

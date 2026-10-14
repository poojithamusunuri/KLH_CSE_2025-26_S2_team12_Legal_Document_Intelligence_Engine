package algorithms;

/*
 * Aho-Corasick multi-pattern matching.
 *
 * Problem it solves in this project:
 *   A legal query is turned into many search phrases (synonym groups such as
 *   "not paid", "unpaid", "withheld", "salary", "wages", ...). Scanning every
 *   document once per phrase costs O(phrases x text). Aho-Corasick scans each
 *   document ONCE for ALL phrases in O(text + matches), and reports the
 *   position of every match (the positions are later used to check whether an
 *   action phrase occurs close to an object phrase).
 *
 * Construction:
 *   1. Insert every pattern into a trie (goto function).
 *   2. Breadth-first search assigns each node a failure link: the node for the
 *      longest proper suffix of its string that is also a trie prefix.
 *   3. The goto table is completed with failure transitions so scanning never
 *      backs up (one table lookup per text character).
 *
 * Complexity:
 *   build : O(total pattern length x ALPHABET)
 *   scan  : O(text length + number of matches)
 *
 * Alphabet: the engine normalises text to lower-case a-z, 0-9 and a single
 * space, so ALPHABET = 37. Any other character is treated as a space.
 * Patterns should be padded with spaces (" not paid ") to match whole words.
 */
public class AhoCorasick {

    /* Receives every match found by scan(). */
    public interface MatchListener {

        /*
         * patternIndex : index of the pattern in the array given to the constructor
         * endExclusive : index just after the last matched character in the text
         */
        void onMatch(int patternIndex, int endExclusive);
    }

    private static final int ALPHABET = 37;

    private final int patternCount;
    private final int[] patternLength;

    private int nodeCount;
    private int[] next;          // next[node * ALPHABET + c]
    private int[] fail;          // failure link
    private int[] terminal;      // first pattern ending exactly at this node, or -1
    private int[] sameTerminal;  // next pattern index ending at the same node, or -1
    private int[] dictionary;    // nearest node on the failure chain with a pattern, or -1

    public AhoCorasick(String[] patterns) {

        if (patterns == null) {
            patterns = new String[0];
        }

        patternCount = patterns.length;
        patternLength = new int[patternCount];

        int capacity = 1;

        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i] != null) {
                capacity += patterns[i].length();
            }
        }

        next = new int[capacity * ALPHABET];
        fail = new int[capacity];
        terminal = new int[capacity];
        sameTerminal = new int[Math.max(patternCount, 1)];
        dictionary = new int[capacity];

        for (int i = 0; i < next.length; i++) {
            next[i] = -1;
        }

        for (int i = 0; i < capacity; i++) {
            terminal[i] = -1;
            dictionary[i] = -1;
        }

        for (int i = 0; i < sameTerminal.length; i++) {
            sameTerminal[i] = -1;
        }

        nodeCount = 1;

        for (int p = 0; p < patterns.length; p++) {
            insert(p, patterns[p]);
        }

        buildFailureLinks();
    }

    public int patternCount() {
        return patternCount;
    }

    private void insert(int index, String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            return;
        }

        patternLength[index] = pattern.length();

        int node = 0;

        for (int i = 0; i < pattern.length(); i++) {

            int c = symbol(pattern.charAt(i));
            int slot = node * ALPHABET + c;

            if (next[slot] == -1) {
                next[slot] = nodeCount;
                nodeCount++;
            }

            node = next[slot];
        }

        if (terminal[node] == -1) {
            terminal[node] = index;
        } else {
            // Identical pattern already stored: chain it.
            int last = terminal[node];

            while (sameTerminal[last] != -1) {
                last = sameTerminal[last];
            }

            sameTerminal[last] = index;
        }
    }

    private void buildFailureLinks() {

        int[] queue = new int[nodeCount];
        int head = 0;
        int tail = 0;

        // Depth-1 nodes fail to the root; missing root transitions loop to root.
        for (int c = 0; c < ALPHABET; c++) {

            int child = next[c];

            if (child == -1) {
                next[c] = 0;
            } else {
                fail[child] = 0;
                queue[tail++] = child;
            }
        }

        while (head < tail) {

            int node = queue[head++];

            int link = fail[node];

            dictionary[node] = terminal[link] != -1 ? link : dictionary[link];

            for (int c = 0; c < ALPHABET; c++) {

                int slot = node * ALPHABET + c;
                int child = next[slot];

                if (child == -1) {
                    // Complete the automaton with the failure transition.
                    next[slot] = next[link * ALPHABET + c];
                } else {
                    fail[child] = next[link * ALPHABET + c];
                    queue[tail++] = child;
                }
            }
        }
    }

    /* Scans text once and reports every occurrence of every pattern. */
    public void scan(String text, MatchListener listener) {

        if (text == null || listener == null || patternCount == 0) {
            return;
        }

        int state = 0;

        for (int i = 0; i < text.length(); i++) {

            state = next[state * ALPHABET + symbol(text.charAt(i))];

            int node = terminal[state] != -1 ? state : dictionary[state];

            while (node != -1) {

                for (int p = terminal[node]; p != -1; p = sameTerminal[p]) {
                    listener.onMatch(p, i + 1);
                }

                node = dictionary[node];
            }
        }
    }

    /* Convenience: how many times each pattern occurs. */
    public int[] countMatches(String text) {

        final int[] counts = new int[patternCount];

        scan(text, new MatchListener() {
            @Override
            public void onMatch(int patternIndex, int endExclusive) {
                counts[patternIndex]++;
            }
        });

        return counts;
    }

    public int lengthOfPattern(int patternIndex) {
        return patternLength[patternIndex];
    }

    private static int symbol(char c) {

        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        }

        if (c >= '0' && c <= '9') {
            return 26 + (c - '0');
        }

        return 36; // space and every other character
    }
}

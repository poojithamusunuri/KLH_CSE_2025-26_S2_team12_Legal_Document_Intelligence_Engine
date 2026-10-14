package algorithms;

/*
 * Randomised quickselect / quicksort for ranking.
 *
 * Problem it solves in this project:
 *   After scoring, the engine has many candidate documents but only shows the
 *   top K. Quickselect partitions the candidates so that the K best come first
 *   in expected O(n) time; only those K are then sorted (O(K log K)). This is
 *   cheaper than sorting all n candidates and replaces the O(n^2) insertion
 *   sort that was used before.
 *
 * Randomisation:
 *   The pivot is chosen with a small hand-written xorshift generator. This is
 *   a Las Vegas algorithm: the output is always correct; only the running time
 *   is random (expected O(n) for select, expected O(n log n) for quicksort).
 *
 * Determinism of the RESULT:
 *   Items are compared by (key descending, tieBreak ascending). When tieBreak
 *   values are unique the order is a strict total order, so the output is the
 *   same no matter which pivots are drawn.
 */
public class RandomizedSelect {

    private long state;

    public RandomizedSelect() {
        this(System.nanoTime());
    }

    public RandomizedSelect(long seed) {
        state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
    }

    /*
     * Returns the indices of the k best items, best first.
     * keys     : score of every item (higher is better)
     * tieBreak : unique rank of every item; lower wins when scores are equal
     */
    public int[] topK(double[] keys, int[] tieBreak, int k) {

        int n = keys == null ? 0 : keys.length;
        int limit = Math.max(0, Math.min(k, n));

        int[] order = new int[n];

        for (int i = 0; i < n; i++) {
            order[i] = i;
        }

        if (limit == 0) {
            return new int[0];
        }

        if (limit < n) {
            select(order, keys, tieBreak, 0, n - 1, limit - 1);
        }

        quicksort(order, keys, tieBreak, 0, limit - 1);

        int[] result = new int[limit];

        for (int i = 0; i < limit; i++) {
            result[i] = order[i];
        }

        return result;
    }

    /* Sorts every item, best first. */
    public int[] sortAll(double[] keys, int[] tieBreak) {
        return topK(keys, tieBreak, keys == null ? 0 : keys.length);
    }

    /* Rearranges order[lo..hi] so that position target holds its final item. */
    private void select(
            int[] order, double[] keys, int[] tie,
            int lo, int hi, int target) {

        while (lo < hi) {

            int pivotPosition = partition(order, keys, tie, lo, hi);

            if (pivotPosition == target) {
                return;
            }

            if (target < pivotPosition) {
                hi = pivotPosition - 1;
            } else {
                lo = pivotPosition + 1;
            }
        }
    }

    private void quicksort(
            int[] order, double[] keys, int[] tie, int lo, int hi) {

        while (lo < hi) {

            int p = partition(order, keys, tie, lo, hi);

            // Recurse into the smaller side to keep the stack O(log n).
            if (p - lo < hi - p) {
                quicksort(order, keys, tie, lo, p - 1);
                lo = p + 1;
            } else {
                quicksort(order, keys, tie, p + 1, hi);
                hi = p - 1;
            }
        }
    }

    /* Lomuto partition around a random pivot; better items go left. */
    private int partition(
            int[] order, double[] keys, int[] tie, int lo, int hi) {

        int pivotIndex = lo + (int) (nextLong() % (hi - lo + 1));
        swap(order, pivotIndex, hi);

        int pivot = order[hi];
        int store = lo;

        for (int i = lo; i < hi; i++) {

            if (better(order[i], pivot, keys, tie)) {
                swap(order, i, store);
                store++;
            }
        }

        swap(order, store, hi);
        return store;
    }

    private static boolean better(int a, int b, double[] keys, int[] tie) {

        if (keys[a] != keys[b]) {
            return keys[a] > keys[b];
        }

        return tie[a] < tie[b];
    }

    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private long nextLong() {
        state ^= state << 13;
        state ^= state >>> 7;
        state ^= state << 17;
        return state >>> 1; // non-negative
    }
}

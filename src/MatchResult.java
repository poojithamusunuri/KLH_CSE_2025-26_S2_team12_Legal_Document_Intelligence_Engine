public class MatchResult {
    public final boolean found;
    public final int position;
    public final long comparisons;

    public MatchResult(boolean found, int position, long comparisons) {
        this.found = found;
        this.position = position;
        this.comparisons = comparisons;
    }
}

public class SearchResult {
    public final String algorithm;
    public final String pattern;
    public final int filesSearched;
    public final int matchingFiles;
    public final long comparisons;
    public final long timeNanos;
    public final ProductDocument[] documents;
    public final int[] positions;

    public SearchResult(String algorithm, String pattern, int filesSearched,
                        int matchingFiles, long comparisons, long timeNanos,
                        ProductDocument[] documents, int[] positions) {
        this.algorithm = algorithm;
        this.pattern = pattern;
        this.filesSearched = filesSearched;
        this.matchingFiles = matchingFiles;
        this.comparisons = comparisons;
        this.timeNanos = timeNanos;
        this.documents = documents;
        this.positions = positions;
    }
}

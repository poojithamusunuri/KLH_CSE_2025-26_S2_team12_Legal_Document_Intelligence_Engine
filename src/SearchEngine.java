public class SearchEngine {
    private SearchEngine() {}

    public static SearchResult searchKMP(ProductDocument[] corpus, String pattern) {
        return search(corpus, pattern, true);
    }

    public static SearchResult searchRabinKarp(ProductDocument[] corpus, String pattern) {
        return search(corpus, pattern, false);
    }

    private static SearchResult search(ProductDocument[] corpus, String pattern, boolean useKMP) {
        ProductDocument[] matches = new ProductDocument[corpus.length];
        int[] positions = new int[corpus.length];
        int count = 0;
        long comparisons = 0;

        long start = System.nanoTime();
        for (ProductDocument document : corpus) {
            MatchResult result = useKMP
                    ? KMP.search(document.getContent(), pattern)
                    : RabinKarp.search(document.getContent(), pattern);
            comparisons += result.comparisons;
            if (result.found) {
                matches[count] = document;
                positions[count] = result.position;
                count++;
            }
        }
        long elapsed = System.nanoTime() - start;

        ProductDocument[] resultDocs = new ProductDocument[count];
        int[] resultPositions = new int[count];
        for (int i = 0; i < count; i++) {
            resultDocs[i] = matches[i];
            resultPositions[i] = positions[i];
        }

        return new SearchResult(
                useKMP ? "KMP" : "Rabin-Karp", pattern, corpus.length,
                count, comparisons, elapsed, resultDocs, resultPositions
        );
    }

    /**
     * Runs the given algorithm `runs` times (after `warmupRuns` untimed
     * warm-up passes to let the JIT compile hot methods) and returns
     * the average execution time and average operation count.
     * Result: {avgTimeNanos, avgOperations}
     */
    public static long[] benchmark(ProductDocument[] corpus, String pattern, boolean useKMP,
                                    int runs, int warmupRuns) {
        for (int i = 0; i < warmupRuns; i++) {
            search(corpus, pattern, useKMP);
        }

        long totalTime = 0;
        long totalOperations = 0;
        for (int i = 0; i < runs; i++) {
            SearchResult r = search(corpus, pattern, useKMP);
            totalTime += r.timeNanos;
            totalOperations += r.comparisons;
        }

        return new long[] { totalTime / runs, totalOperations / runs };
    }

    public static void print(SearchResult result) {
        System.out.println("\n========================================");
        System.out.println("           SEARCH RESULTS");
        System.out.println("========================================");
        System.out.println("Algorithm       : " + result.algorithm);
        System.out.println("Search pattern  : " + result.pattern);
        System.out.println("Files searched  : " + result.filesSearched);
        System.out.println("Matching files  : " + result.matchingFiles);

        String opLabel = result.algorithm.equals("KMP")
                ? "Character Comparisons"
                : "Hash/Character Checks";
        System.out.println(opLabel + " : " + result.comparisons);
        System.out.println("Execution time  : " + result.timeNanos + " ns");
        System.out.println("----------------------------------------");

        if (result.matchingFiles == 0) {
            System.out.println("No matching product files found.");
        } else {
            for (int i = 0; i < result.matchingFiles; i++) {
                System.out.println((i + 1) + ". " + result.documents[i].getProductName());
                System.out.println("   File           : " + result.documents[i].getFileName());
                System.out.println("   Match position : " + result.positions[i]);
            }
        }
        System.out.println("========================================");
    }
}
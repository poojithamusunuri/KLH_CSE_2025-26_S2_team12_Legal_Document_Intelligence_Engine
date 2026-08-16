import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final int BENCHMARK_RUNS = 10;
    private static final int WARMUP_RUNS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CorpusLoader loader = new CorpusLoader("data/corpus");

        try {
            ProductDocument[] corpus = loader.load();

            System.out.println("========================================");
            System.out.println("   PRODUCT SEARCH ENGINE  ");
            System.out.println("========================================");
            System.out.println("Actual corpus loaded: " + corpus.length + " TXT files");

            while (true) {
                System.out.println("\n1. KMP String Search");
                System.out.println("2. Rabin-Karp String Search");
                System.out.println("3. Compare KMP and Rabin-Karp");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                String choiceText = scanner.nextLine().trim();
                int choice;
                try {
                    choice = Integer.parseInt(choiceText);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                    continue;
                }

                if (choice == 4) break;
                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                System.out.print("Enter search pattern: ");
                String pattern = scanner.nextLine().trim();
                if (pattern.isEmpty()) {
                    System.out.println("Search pattern cannot be empty.");
                    continue;
                }

                if (choice == 1) {
                    SearchEngine.print(SearchEngine.searchKMP(corpus, pattern));
                } else if (choice == 2) {
                    SearchEngine.print(SearchEngine.searchRabinKarp(corpus, pattern));
                } else {
                    printComparison(corpus, pattern);
                }
            }
        } catch (IOException e) {
            System.out.println("Corpus loading error: " + e.getMessage());
            System.out.println("Make sure you run the program from the project root so that ./data/corpus exists.");
        } finally {
            scanner.close();
        }
    }

    private static void printComparison(ProductDocument[] corpus, String pattern) {
        // Single authoritative run of each — used for match listing and consistency check
        SearchResult kmp = SearchEngine.searchKMP(corpus, pattern);
        SearchResult rk = SearchEngine.searchRabinKarp(corpus, pattern);

        // Averaged timing/operations over multiple runs
        long[] kmpBench = SearchEngine.benchmark(corpus, pattern, true, BENCHMARK_RUNS, WARMUP_RUNS);
        long[] rkBench = SearchEngine.benchmark(corpus, pattern, false, BENCHMARK_RUNS, WARMUP_RUNS);

        boolean consistent = verifyConsistency(kmp, rk);

        System.out.println("\n==================================================");
        System.out.println("             KMP VS RABIN-KARP");
        System.out.println("==================================================\n");
        System.out.println("Query              : " + pattern);
        System.out.println("Corpus Files       : " + kmp.filesSearched);
        System.out.println("Benchmark Runs     : " + BENCHMARK_RUNS);
        System.out.println("\n--------------------------------------------------");
        System.out.printf("%-22s %-12s %-12s%n", "Metric", "KMP", "Rabin-Karp");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-22s %-12d %-12d%n", "Matching Files", kmp.matchingFiles, rk.matchingFiles);
        System.out.printf("%-22s %-12s %-12s%n", "Avg. Execution Time",
                kmpBench[0] + " ns", rkBench[0] + " ns");
        System.out.printf("%-22s %-12d %-12d%n", "Operations", kmpBench[1], rkBench[1]);
        System.out.println("--------------------------------------------------");
        System.out.println("\nResult Consistency  : " + (consistent ? "PASS" : "FAIL"));
        System.out.println("==================================================");
    }

    /**
     * Verifies both algorithms found the same set of matching product files,
     * independent of the order each returned them in.
     */
    private static boolean verifyConsistency(SearchResult kmp, SearchResult rk) {
        if (kmp.matchingFiles != rk.matchingFiles) return false;

        String[] kmpFiles = new String[kmp.matchingFiles];
        for (int i = 0; i < kmp.matchingFiles; i++) kmpFiles[i] = kmp.documents[i].getFileName();

        String[] rkFiles = new String[rk.matchingFiles];
        for (int i = 0; i < rk.matchingFiles; i++) rkFiles[i] = rk.documents[i].getFileName();

        java.util.Arrays.sort(kmpFiles);
        java.util.Arrays.sort(rkFiles);
        return java.util.Arrays.equals(kmpFiles, rkFiles);
    }
}
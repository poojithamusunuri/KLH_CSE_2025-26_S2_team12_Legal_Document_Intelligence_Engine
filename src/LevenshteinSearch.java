import java.util.ArrayList;

public class LevenshteinSearch {

    public static class Result {

        String productName;
        String fileName;
        String matchedTerm;

        int distance;
        int matchedTerms;
        int totalTerms;

        double matchScore;

        Result(
            String productName,
            String fileName,
            String matchedTerm,
            int distance,
            int matchedTerms,
            int totalTerms,
            double matchScore
        ) {
            this.productName = productName;
            this.fileName = fileName;
            this.matchedTerm = matchedTerm;
            this.distance = distance;
            this.matchedTerms = matchedTerms;
            this.totalTerms = totalTerms;
            this.matchScore = matchScore;
        }
    }

    public static ArrayList<Result> search(
            ProductDocument[] corpus,
            String query) {

        ArrayList<Result> results =
            new ArrayList<>();

        String[] queryTerms =
            tokenize(query);

        if (queryTerms.length == 0) {
            return results;
        }

        for (ProductDocument product : corpus) {

            String productName =
                product.getProductName();

            String productNameText =
                productName.toLowerCase();

            String searchableText =
                product.getSearchableContent()
                       .toLowerCase();

            String[] searchableTerms =
                tokenize(searchableText);

            int matchedTerms = 0;
            int totalDistance = 0;

            StringBuilder matched =
                new StringBuilder();

            for (String queryTerm : queryTerms) {

                int bestDistance =
                    Integer.MAX_VALUE;

                String bestMatch =
                    "";

                /*
                 * First compare against the
                 * PRODUCT NAME.
                 *
                 * Product-name matches receive
                 * priority.
                 */
                String[] productNameTerms =
                    tokenize(productNameText);

                for (String productTerm :
                        productNameTerms) {

                    int distance =
                        Levenshtein.distance(
                            queryTerm,
                            productTerm
                        );

                    if (distance < bestDistance) {

                        bestDistance = distance;
                        bestMatch = productTerm;
                    }
                }

                /*
                 * Then search the complete
                 * searchable corpus fields.
                 */
                for (String searchableTerm :
                        searchableTerms) {

                    int distance =
                        Levenshtein.distance(
                            queryTerm,
                            searchableTerm
                        );

                    if (distance < bestDistance) {

                        bestDistance = distance;
                        bestMatch = searchableTerm;
                    }
                }

                int threshold =
                    getThreshold(queryTerm.length());

                if (bestDistance <= threshold) {

                    matchedTerms++;
                    totalDistance += bestDistance;

                    if (matched.length() > 0) {
                        matched.append(", ");
                    }

                    matched.append(bestMatch);
                }
            }

            int minimumMatches;

            if (queryTerms.length >= 3) {
                minimumMatches = 2;
            } else {
                minimumMatches = 1;
            }

            if (matchedTerms >= minimumMatches) {

                /*
                 * Base score:
                 * percentage of query terms matched.
                 */
                double termScore =
                    ((double) matchedTerms
                    / queryTerms.length) * 100.0;

                /*
                 * Distance penalty.
                 */
                double distancePenalty =
                    totalDistance * 5.0;

                double matchScore =
                    termScore - distancePenalty;

                /*
                 * Product-name bonus.
                 */
                if (productNameText.contains(
                        query.toLowerCase())) {

                    matchScore += 30.0;
                }

                if (matchScore > 100.0) {
                    matchScore = 100.0;
                }

                if (matchScore < 0.0) {
                    matchScore = 0.0;
                }

                results.add(
                    new Result(
                        productName,
                        product.getFileName(),
                        matched.toString(),
                        totalDistance,
                        matchedTerms,
                        queryTerms.length,
                        matchScore
                    )
                );
            }
        }

        /*
         * Highest score first.
         */
        results.sort((a, b) ->
            Double.compare(
                b.matchScore,
                a.matchScore
            )
        );

        return results;
    }

    private static String[] tokenize(
            String text) {

        String cleaned =
            text.toLowerCase()
                .replaceAll(
                    "[^a-z0-9]+",
                    " "
                )
                .trim();

        if (cleaned.isEmpty()) {
            return new String[0];
        }

        return cleaned.split("\\s+");
    }

    private static int getThreshold(
            int length) {

        if (length <= 4) {
            return 1;
        }

        if (length <= 8) {
            return 2;
        }

        return 3;
    }
}
public class ProductDocument {

    private final String fileName;
    private final String content;

    public ProductDocument(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public String getProductName() {

        String[] lines = content.split("\\R", 3);

        if (lines.length > 0 && !lines[0].trim().isEmpty()) {
            return lines[0].trim()
                    .replace("#", "")
                    .trim();
        }

        return fileName;
    }

    /*
     * Returns the content that is useful for
     * PRODUCT SIMILARITY matching (Levenshtein),
     * as opposed to the full corpus text used
     * for KMP/Rabin-Karp demonstrations.
     *
     * We intentionally exclude:
     * - Customer Search Queries (contains comparison
     *   queries like "s24 ultra vs iphone 15 pro max",
     *   which would wrongly pull in OTHER product names)
     * - Customer Questions & Answers
     * - Customer Reviews
     * - Recommendation Data (BLOCK 3)
     *
     * because those are not primary product
     * search-index fields, and some directly
     * reference other products.
     *
     * NOTE: our corpus .txt files are plain text
     * (no markdown # symbols — those were stripped
     * during the markdown-to-txt conversion). Every
     * former "### " heading was converted with a
     * trailing colon, e.g.:
     *
     *   Customer Search Queries:
     *   Customer Questions & Answers:
     *   Searchable Attributes:
     *   Search Index:
     *   Customer Reviews:
     *
     * while former "## " headings (BLOCK 1/2/3) have
     * NO trailing colon, e.g.:
     *
     *   BLOCK 3 — Recommendation Data
     *   -----------------------------
     *
     * The boundary strings below match those exact
     * forms, not generic "## "/"### " markdown.
     */
    public String getSearchableContent() {

        StringBuilder searchable =
            new StringBuilder();

        String[] lines =
            content.split("\\R");

        boolean include = true;
        boolean skipSection = false;

        for (String line : lines) {

            String trimmed =
                line.trim();

            /*
             * BLOCK 3 is recommendation data.
             * Do not use it for typo/similarity search.
             */
            if (trimmed.equalsIgnoreCase(
                    "BLOCK 3 — Recommendation Data")) {

                include = false;
                continue;
            }

            /*
             * Customer Reviews are not used for
             * product similarity.
             */
            if (trimmed.equalsIgnoreCase(
                    "Customer Reviews:")) {

                skipSection = true;
                continue;
            }

            /*
             * Customer Search Queries contain
             * comparison examples such as:
             *
             * "s24 ultra vs iphone 15 pro max"
             *
             * These must NOT become product terms
             * for a DIFFERENT product's document.
             */
            if (trimmed.equalsIgnoreCase(
                    "Customer Search Queries:")) {

                skipSection = true;
                continue;
            }

            /*
             * Customer Questions & Answers are
             * also excluded from the current
             * similarity stage.
             */
            if (trimmed.equalsIgnoreCase(
                    "Customer Questions & Answers:")) {

                skipSection = true;
                continue;
            }

            /*
             * Searchable Attributes comes after
             * the excluded Q&A section — resume here.
             */
            if (trimmed.equalsIgnoreCase(
                    "Searchable Attributes:")) {

                skipSection = false;
            }

            /*
             * Search Index begins the useful
             * corpus fields again (in case a future
             * corpus ordering puts it after an
             * excluded section).
             */
            if (trimmed.equalsIgnoreCase(
                    "Search Index:")) {

                skipSection = false;
            }

            /*
             * Resume when the next major block
             * begins.
             */
            if (trimmed.equalsIgnoreCase(
                    "BLOCK 2 — Search Data")) {

                include = true;
            }

            if (include && !skipSection) {
                searchable.append(line).append(" ");
            }
        }

        return searchable.toString();
    }
}
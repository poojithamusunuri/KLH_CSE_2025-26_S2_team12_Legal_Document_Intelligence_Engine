package loader;

import model.LegalDocument;
import structures.SimpleList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Loads the legal corpus from a directory of .txt files.
 *
 * File format (verified against the real corpus):
 *   - A header of "FIELD: value" lines, often separated by blank lines.
 *   - Some fields carry their value on the FOLLOWING lines
 *     (for example "KEYWORDS:" then the list, or a multi-line "DESCRIPTION:").
 *   - The keyword field is called SEARCH_KEYWORDS in judgments and core Acts
 *     and KEYWORDS elsewhere; legal principles even contain two KEYWORDS blocks.
 *   - Most files have a "CONTENT:" line after which the substantive text starts.
 *     Short curated documents (legal principles) have none; their DESCRIPTION
 *     is the substantive text.
 */
public class LegalCorpusLoader {

    private static final String[] KNOWN_FIELDS = {
            "DOCUMENT_ID", "TITLE", "DOCUMENT_TYPE", "CATEGORY", "DOMAIN",
            "SUBDOMAIN", "JURISDICTION", "YEAR", "AUTHORITY", "COURT_TYPE",
            "CASE_TYPE", "SOURCE", "SOURCE_URL", "SOURCE_TYPE", "KEYWORDS",
            "SEARCH_KEYWORDS", "DESCRIPTION", "CONTENT"
    };

    private final String corpusPath;
    private final SimpleList<String> skippedFiles;

    public LegalCorpusLoader(String corpusPath) {
        this.corpusPath = corpusPath;
        this.skippedFiles = new SimpleList<String>();
    }

    public SimpleList<LegalDocument> loadDocuments() throws IOException {

        SimpleList<LegalDocument> documents =
                new SimpleList<LegalDocument>();

        skippedFiles.clear();

        File corpusDirectory = new File(corpusPath);

        if (!corpusDirectory.exists()) {
            throw new IOException(
                    "Corpus directory not found: "
                            + corpusDirectory.getAbsolutePath()
            );
        }

        loadDirectory(corpusDirectory, documents);

        return documents;
    }

    /* Files that could not be read; they are skipped instead of crashing. */
    public SimpleList<String> getSkippedFiles() {
        return skippedFiles;
    }

    private void loadDirectory(
            File directory,
            SimpleList<LegalDocument> documents) {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        sortByName(files);

        for (int i = 0; i < files.length; i++) {

            File file = files[i];

            if (file.isDirectory()) {

                loadDirectory(file, documents);

            } else if (file.getName().toLowerCase().endsWith(".txt")) {

                try {
                    String content = Files.readString(file.toPath());
                    documents.add(createDocument(file, content));
                } catch (IOException | RuntimeException e) {
                    skippedFiles.add(file.getPath());
                }
            }
        }
    }

    /*
     * The operating system returns files in arbitrary order. Sorting by name
     * makes every run (and therefore every ranking tie) deterministic.
     */
    private static void sortByName(File[] files) {

        for (int i = 1; i < files.length; i++) {

            File current = files[i];
            int j = i - 1;

            while (j >= 0
                    && files[j].getName().compareTo(current.getName()) > 0) {
                files[j + 1] = files[j];
                j--;
            }

            files[j + 1] = current;
        }
    }

    private LegalDocument createDocument(File file, String content) {

        String[] lines = content.split("\\R", -1);

        int contentLine = findContentLine(lines);
        int headerEnd = contentLine == -1 ? lines.length : contentLine;

        String documentId = field(lines, headerEnd, "DOCUMENT_ID");
        String title = field(lines, headerEnd, "TITLE");
        String documentType = field(lines, headerEnd, "DOCUMENT_TYPE");
        String domain = field(lines, headerEnd, "DOMAIN");
        String subdomain = field(lines, headerEnd, "SUBDOMAIN");
        String jurisdiction = field(lines, headerEnd, "JURISDICTION");
        String year = field(lines, headerEnd, "YEAR");
        if (year.isEmpty() || year.equalsIgnoreCase("unknown")) {
            String inferredYear = yearFromExplicitTitleDate(title);
            if (!inferredYear.isEmpty()) year = inferredYear;
        }
        String source = field(lines, headerEnd, "SOURCE");
        String description = field(lines, headerEnd, "DESCRIPTION");

        if (source.isEmpty()) {
            source = field(lines, headerEnd, "SOURCE_TYPE");
        }

        String keywords = joinKeywords(
                allFields(lines, headerEnd, "SEARCH_KEYWORDS"),
                allFields(lines, headerEnd, "KEYWORDS")
        );

        String body = contentLine == -1
                ? description
                : join(lines, contentLine + 1, lines.length);

        if (body.trim().isEmpty()) {
            body = content;
        }

        if (documentId.isEmpty()) {
            documentId = file.getName()
                    .replace(".txt", "")
                    .toUpperCase();
        }

        if (title.isEmpty()) {
            title = file.getName();
        }

        if (documentType.isEmpty()) {
            documentType = "Legal Document";
        }

        if (domain.isEmpty()) {
            domain = "Unknown";
        }

        if (subdomain.isEmpty()) {
            subdomain = "Unknown";
        }

        if (jurisdiction.isEmpty()) {
            jurisdiction = "India";
        }

        if (year.isEmpty()) {
            year = "Unknown";
        }

        if (source.isEmpty()) {
            source = "Project Corpus";
        }

        return new LegalDocument(
                documentId,
                title,
                documentType,
                domain,
                subdomain,
                jurisdiction,
                year,
                source,
                content,
                keywords,
                description,
                body
        );
    }

    private static String yearFromExplicitTitleDate(String title) {
        if (title == null || title.trim().isEmpty()) return "";
        // Only infer when the title explicitly contains a judgment-style date
        // introduced by "on". Do not mistake an Act's enactment year for a
        // judgment year, and never guess from arbitrary body text.
        Matcher matcher = Pattern.compile(
                "\\bon\\s+.{1,32}?\\b((?:19|20)\\d{2})\\b",
                Pattern.CASE_INSENSITIVE).matcher(title);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static int findContentLine(String[] lines) {

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("CONTENT:")) {
                return i;
            }
        }

        return -1;
    }

    /* First non-empty occurrence of a field within the header. */
    private static String field(String[] lines, int headerEnd, String name) {

        SimpleList<String> values = allFields(lines, headerEnd, name);

        for (int i = 0; i < values.size(); i++) {
            if (!values.get(i).isEmpty()) {
                return values.get(i);
            }
        }

        return "";
    }

    /*
     * Every occurrence of a field. The value is the rest of the line plus any
     * following lines that are not themselves a known field line.
     */
    private static SimpleList<String> allFields(
            String[] lines,
            int headerEnd,
            String name) {

        SimpleList<String> values = new SimpleList<String>();
        String prefix = name + ":";

        for (int i = 0; i < headerEnd; i++) {

            String trimmed = lines[i].trim();

            if (!trimmed.startsWith(prefix)) {
                continue;
            }

            StringBuilder value = new StringBuilder(
                    trimmed.substring(prefix.length()).trim()
            );

            for (int j = i + 1; j < headerEnd; j++) {

                String following = lines[j].trim();

                if (isKnownFieldLine(following)) {
                    break;
                }

                if (following.isEmpty()) {
                    continue;
                }

                if (value.length() > 0) {
                    value.append(' ');
                }

                value.append(following);
            }

            values.add(value.toString().trim());
        }

        return values;
    }

    private static boolean isKnownFieldLine(String line) {

        for (int i = 0; i < KNOWN_FIELDS.length; i++) {
            if (line.startsWith(KNOWN_FIELDS[i] + ":")) {
                return true;
            }
        }

        return false;
    }

    private static String joinKeywords(
            SimpleList<String> first,
            SimpleList<String> second) {

        StringBuilder merged = new StringBuilder();

        appendKeywordValues(merged, first);
        appendKeywordValues(merged, second);

        return merged.toString();
    }

    private static void appendKeywordValues(
            StringBuilder merged,
            SimpleList<String> values) {

        for (int i = 0; i < values.size(); i++) {

            if (values.get(i).isEmpty()) {
                continue;
            }

            if (merged.length() > 0) {
                merged.append(", ");
            }

            merged.append(values.get(i));
        }
    }

    private static String join(String[] lines, int from, int to) {

        StringBuilder builder = new StringBuilder();

        for (int i = from; i < to; i++) {
            builder.append(lines[i]).append('\n');
        }

        return builder.toString();
    }
}

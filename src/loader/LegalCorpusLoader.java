package loader;

import model.LegalDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LegalCorpusLoader {

    private final String corpusPath;

    public LegalCorpusLoader(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public List<LegalDocument> loadDocuments() throws IOException {

        List<LegalDocument> documents = new ArrayList<>();

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

    private void loadDirectory(
            File directory,
            List<LegalDocument> documents) throws IOException {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                loadDirectory(file, documents);

            } else if (file.getName().endsWith(".txt")) {

                String content =
                        Files.readString(file.toPath());

                LegalDocument document =
                        createDocument(file, content);

                documents.add(document);
            }
        }
    }

    private LegalDocument createDocument(
            File file,
            String content) {

        String documentId =
                getMetadata(content, "DOCUMENT_ID");

        String title =
                getMetadata(content, "TITLE");

        String documentType =
                getMetadata(content, "DOCUMENT_TYPE");

        String jurisdiction =
                getMetadata(content, "JURISDICTION");

        String year =
                getMetadata(content, "YEAR");

        String source =
                getMetadata(content, "SOURCE");

        /*
         * Some documents do not contain SOURCE.
         * Use SOURCE_TYPE when necessary.
         */
        if (source.isEmpty()) {
            source =
                    getMetadata(content, "SOURCE_TYPE");
        }

        /*
         * Fall back to filename if DOCUMENT_ID is missing.
         */
        if (documentId.isEmpty()) {
            documentId =
                    file.getName()
                            .replace(".txt", "")
                            .toUpperCase();
        }

        /*
         * Fall back to filename if TITLE is missing.
         */
        if (title.isEmpty()) {
            title = file.getName();
        }

        /*
         * Reasonable defaults for optional metadata.
         */
        if (documentType.isEmpty()) {
            documentType = "Legal Document";
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
                jurisdiction,
                year,
                source,
                content
        );
    }

    private String getMetadata(
            String content,
            String field) {

        String prefix = field + ":";

        String[] lines =
                content.split("\\R");

        for (String line : lines) {

            String trimmed =
                    line.trim();

            if (trimmed.startsWith(prefix)) {

                return trimmed
                        .substring(prefix.length())
                        .trim();
            }
        }

        return "";
    }
}

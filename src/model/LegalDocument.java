package model;

public class LegalDocument {

    private final String documentId;
    private final String title;
    private final String documentType;
    private final String jurisdiction;
    private final String year;
    private final String source;
    private final String content;

    public LegalDocument(
            String documentId,
            String title,
            String documentType,
            String jurisdiction,
            String year,
            String source,
            String content) {

        this.documentId = documentId;
        this.title = title;
        this.documentType = documentType;
        this.jurisdiction = jurisdiction;
        this.year = year;
        this.source = source;
        this.content = content;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getYear() {
        return year;
    }

    public String getSource() {
        return source;
    }

    public String getContent() {
        return content;
    }
}

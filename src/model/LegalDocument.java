package model;

public class LegalDocument {

    private final String documentId;
    private final String title;
    private final String documentType;
    private final String domain;
    private final String subdomain;
    private final String jurisdiction;
    private final String year;
    private final String source;
    private final String content;
    private final String keywords;
    private final String description;
    private final String body;

    /*
     * Original constructor. Keywords and description are empty and the body
     * is the whole content. Kept so existing callers and tests still compile.
     */
    public LegalDocument(
            String documentId,
            String title,
            String documentType,
            String domain,
            String subdomain,
            String jurisdiction,
            String year,
            String source,
            String content) {

        this(
                documentId,
                title,
                documentType,
                domain,
                subdomain,
                jurisdiction,
                year,
                source,
                content,
                "",
                "",
                content
        );
    }

    public LegalDocument(
            String documentId,
            String title,
            String documentType,
            String domain,
            String subdomain,
            String jurisdiction,
            String year,
            String source,
            String content,
            String keywords,
            String description,
            String body) {

        this.documentId = documentId;
        this.title = title;
        this.documentType = documentType;
        this.domain = domain;
        this.subdomain = subdomain;
        this.jurisdiction = jurisdiction;
        this.year = year;
        this.source = source;
        this.content = content;
        this.keywords = keywords == null ? "" : keywords;
        this.description = description == null ? "" : description;
        this.body = body == null ? "" : body;
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

    public String getDomain() {
        return domain;
    }

    public String getSubdomain() {
        return subdomain;
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

    /* The complete raw file, metadata header included. */
    public String getContent() {
        return content;
    }

    /*
     * Curated or generated keyword list (SEARCH_KEYWORDS or KEYWORDS field).
     * For judgments these keywords are auto-generated and noisy; for Acts,
     * core Acts and legal principles they are curated.
     */
    public String getKeywords() {
        return keywords;
    }

    public String getDescription() {
        return description;
    }

    /*
     * The substantive text only: everything after the CONTENT: marker, or the
     * DESCRIPTION for short curated documents that have no CONTENT: section.
     * The metadata header is excluded so that headers shared by every file
     * cannot create false matches or false similarity.
     */
    public String getBody() {
        return body;
    }

    /* JUDGMENT, ACT, CORE_ACT, CONSTITUTION, PRINCIPLE or OTHER. */
    public String getKind() {

        String type = documentType == null ? "" : documentType.toLowerCase();

        if (type.startsWith("judgment")) {
            return "JUDGMENT";
        }
        if (type.startsWith("core legal act")) {
            return "CORE_ACT";
        }
        if (type.startsWith("act")) {
            return "ACT";
        }
        if (type.startsWith("constitution")) {
            return "CONSTITUTION";
        }
        if (type.startsWith("legal principle")) {
            return "PRINCIPLE";
        }

        return "OTHER";
    }
}

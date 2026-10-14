package search;

import model.LegalDocument;
import structures.SimpleList;

/*
 * Small, conservative link between the issue labels produced by
 * LegalProblemAnalyzer and the DOMAIN / SUBDOMAIN labels stored in the corpus.
 *
 * Only mappings supported by the corpus inventory are listed. Some issues
 * (banking, motor-vehicle, education, tenancy, debt recovery) have no clean
 * dedicated domain mapping. Where a supported mapping exists, it supplies a
 * small bonus and a conservative compatibility signal; incompatible metadata
 * is penalized rather than treated as definitive because corpus labels are
 * incomplete and inconsistent (including across cheque-dishonour judgments).
 */
public final class DomainCrosswalk {

    /* {analyser issue label, corpus DOMAIN ("" = any), corpus SUBDOMAIN ("" = any)} */
    private static final String[][] MAP = {
            {"Property / ownership dispute", "Property and Land Law", ""},
            {"Tenancy-related matter", "Property and Land Law", ""},
            {"Employment-related dispute", "Labour and Employment Law", ""},
            {"Contract-related issue", "Commercial and Corporate Law", "Contract Law"},
            {"Consumer / goods or services dispute", "Consumer Law", ""},
            {"Intellectual property matter", "Intellectual Property Law", ""},
            {"Family / inheritance matter", "Family Law", ""},
            {"Criminal / conspiracy-related issue", "Criminal Law", ""},
            {"Criminal procedure / warrant matter", "Criminal Law", ""},
            {"Property / secured-interest matter", "Property and Land Law", ""},
            {"Cheque / negotiable instrument matter", "Commercial and Corporate Law", ""}
    };

    private DomainCrosswalk() {
    }

    /*
     * Returns the analyser label that makes the document's domain compatible,
     * or null when there is no supported mapping.
     */
    public static String compatibleLabel(
            SimpleList<String> analyserLabels,
            LegalDocument document) {

        if (analyserLabels == null || document == null) {
            return null;
        }

        for (int i = 0; i < analyserLabels.size(); i++) {

            String label = analyserLabels.get(i);

            for (int m = 0; m < MAP.length; m++) {

                if (!MAP[m][0].equals(label)) {
                    continue;
                }

                boolean domainOk = MAP[m][1].isEmpty()
                        || MAP[m][1].equalsIgnoreCase(document.getDomain());

                boolean subdomainOk = MAP[m][2].isEmpty()
                        || MAP[m][2].equalsIgnoreCase(document.getSubdomain());

                if (domainOk && subdomainOk) {
                    return label;
                }
            }
        }

        return null;
    }

    public static boolean hasMapping(SimpleList<String> analyserLabels) {
        if (analyserLabels == null) {
            return false;
        }

        for (int i = 0; i < analyserLabels.size(); i++) {
            for (int m = 0; m < MAP.length; m++) {
                if (MAP[m][0].equals(analyserLabels.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}

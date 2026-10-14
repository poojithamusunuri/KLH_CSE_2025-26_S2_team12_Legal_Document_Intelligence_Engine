package analysis;

import structures.SimpleList;

public class InformationDependencyMap {

    public static class Dependency {

        private final String question;
        private final String requiredInformation;
        private final String reason;

        public Dependency(
                String question,
                String requiredInformation,
                String reason) {

            this.question = question;
            this.requiredInformation = requiredInformation;
            this.reason = reason;
        }

        public String getQuestion() {
            return question;
        }

        public String getRequiredInformation() {
            return requiredInformation;
        }

        public String getReason() {
            return reason;
        }
    }

    private final SimpleList<Dependency> dependencies;

    public InformationDependencyMap() {
        dependencies = new SimpleList<>();
    }

    public void build(
            LegalProblemAnalyzer.AnalysisResult analysis) {

        dependencies.clear();

        if (analysis == null) {
            return;
        }

        SimpleList<String> missing = analysis.getMissingInformation();

        for (int i = 0; i < missing.size(); i++) {
            String information = missing.get(i);
            addDependencyForInformation(information);
        }

        // If an analyzer version supplies no missing-information label for a
        // detected issue, retain the issue/question structure without inventing
        // a legal conclusion.
        if (dependencies.isEmpty()) {
            SimpleList<String> questions = analysis.getQuestions();
            for (int i = 0; i < questions.size(); i++) {
                addDependency(
                        questions.get(i),
                        "Evidence or documents relevant to this question",
                        "The question must be supported by relevant facts or records before further research."
                );
            }
        }
    }

    private void addDependencyForInformation(String information) {
        if (information == null || information.trim().isEmpty()) {
            return;
        }

        if (matches(information, "Property ownership documents", "Ownership history")) {
            addDependency(
                    "Who legally owns or possesses the property?",
                    information,
                    "Ownership evidence establishes the starting point for evaluating the property issue."
            );
            return;
        }

        if (matches(information, "Possession or transfer records", "Sale or transfer document", "Property registration details")) {
            addDependency(
                    "What transfer, title, registration, or possession record supports the claim?",
                    information,
                    "Transfer and registration records help establish how the property interest changed or is recorded."
            );
            return;
        }

        if (matches(information, "Evidence supporting the opposing claim")) {
            addDependency(
                    "Does another party have a competing claim?",
                    information,
                    "Competing evidence must be examined alongside the primary ownership records."
            );
            return;
        }

        if (matches(information, "Employment relationship details", "Employment contract", "Employment agreement or appointment terms")) {
            addDependency(
                    "What employment terms or role is involved?",
                    information,
                    "The employment relationship and governing terms must be established first."
            );
            return;
        }

        if (matches(information, "Salary or payment records", "Payment records or salary slips")) {
            addDependency(
                    "What payment was due and what records show the payment history?",
                    information,
                    "Payment records establish the amounts, dates, and history relevant to the reported issue."
            );
            return;
        }

        if (matches(information, "Dismissal or termination details", "Termination communication", "Termination or employment communication")) {
            addDependency(
                    "What happened when the employment was ended or disputed?",
                    information,
                    "Termination records provide the stated event, timing, and communication surrounding the employment change."
            );
            return;
        }

        if (matches(information, "Applicable company policy or employment records", "Applicable company policy")) {
            addDependency(
                    "What workplace rule or employment record is relevant?",
                    information,
                    "The applicable workplace rule or record helps define the factual research question."
            );
            return;
        }

        if (matches(information, "Warrant, search record, or other authority relied upon")) {
            addDependency(
                    "Was a warrant or other lawful authority issued, and what search or arrest did it authorize?",
                    information,
                    "The order and its scope are needed to distinguish the reported absence of a shown warrant from whether legal authority existed."
            );
            return;
        }

        if (matches(information, "Relevant police, court, or case records")) {
            addDependency(
                    "What do police or court records show about the search, its grounds, and the procedure followed?",
                    information,
                    "Contemporaneous records can help establish what occurred without assuming the search was lawful or unlawful."
            );
            return;
        }

        if (matches(information, "Loan agreement and sanction documents", "Loan agreement", "Loan sanction documents")) {
            addDependency(
                    "What loan amount, repayment obligations, and conditions did the lender approve?",
                    information,
                    "The sanction letter and loan agreement identify the approved facility, parties, and contractual conditions."
            );
            return;
        }

        if (matches(information, "Mortgage, hypothecation, or security documents", "Mortgage or security documents", "Property security documents")) {
            addDependency(
                    "What document records the property offered as security and the nature of the security interest?",
                    information,
                    "Security documents help establish what asset was offered and what rights were recorded."
            );
            return;
        }

        if (matches(information, "Repayment and outstanding-balance records", "Loan repayment records", "Outstanding loan balance")) {
            addDependency(
                    "What do repayment records show about amounts paid, defaults, and the outstanding balance?",
                    information,
                    "Payment history and account statements help verify the repayment position."
            );
            return;
        }

        if (matches(information, "Contract or agreement", "Complete contract or agreement")) {
            addDependency(
                    "What obligations were agreed and when?",
                    information,
                    "The agreement is the primary record for identifying the parties' stated obligations."
            );
            return;
        }

        if (matches(information, "Performance obligation details", "Evidence of contractual obligations")) {
            addDependency(
                    "What performance was expected?",
                    information,
                    "The expected performance must be identified before comparing it with what occurred."
            );
            return;
        }

        if (matches(information, "Evidence of performance, non-performance, or breach", "Evidence of breach or non-performance")) {
            addDependency(
                    "Was there a failure to perform, breach, or refusal to comply?",
                    information,
                    "Evidence is needed to compare the agreed obligation with the reported conduct."
            );
            return;
        }

        if (matches(information, "Identity of parties")) {
            addDependency(
                    "Who are the parties to the agreement?",
                    information,
                    "Party identity is needed to connect the agreement and the reported conduct to the relevant actors."
            );
            return;
        }

        if (matches(information, "Product purchase records", "Purchase invoice or receipt")) {
            addDependency(
                    "What product or service was purchased or provided?",
                    information,
                    "Purchase records establish the transaction and the item or service involved."
            );
            return;
        }

        if (matches(information, "Payment or refund records")) {
            addDependency(
                    "What payment, refund, or remedy was requested or made?",
                    information,
                    "Payment records help establish the transaction and the reported remedy request."
            );
            return;
        }

        if (matches(information, "Evidence of defect, deficiency, or refusal", "Evidence of defect or deficiency")) {
            addDependency(
                    "What defect, deficiency, or refusal occurred?",
                    information,
                    "Evidence is required to establish what went wrong and what response was made."
            );
            return;
        }

        if (matches(information, "Tenancy agreement", "Lease or tenancy agreement")) {
            addDependency(
                    "What lease, tenancy, or payment arrangement existed?",
                    information,
                    "The tenancy agreement establishes the relevant rental and possession terms."
            );
            return;
        }

        if (matches(information, "Deposit or payment proof", "Payment and deposit records")) {
            addDependency(
                    "What deposit or payment was made?",
                    information,
                    "Payment proof establishes the amount, timing, and transaction relevant to the tenancy issue."
            );
            return;
        }

        if (matches(information, "Ownership or registration records", "Ownership or registration record")) {
            addDependency(
                    "Who owns or registered the relevant work or intellectual property?",
                    information,
                    "Ownership or registration records establish the relevant rights-holder information for research."
            );
            return;
        }

        if (matches(information, "Evidence of use, copying, or authorization")) {
            addDependency(
                    "What use, copying, or authorization is alleged?",
                    information,
                    "Evidence of the use or copying event is needed to investigate the reported intellectual-property issue."
            );
            return;
        }

        if (matches(information, "Bank transaction details", "Bank statement or transaction record")) {
            addDependency(
                    "What transaction occurred and when?",
                    information,
                    "Transaction records establish the event that must be investigated."
            );
            return;
        }

        if (matches(information, "Authorization status")) {
            addDependency(
                    "Was the transaction authorized?",
                    information,
                    "Authorization evidence distinguishes the reported transaction from an authorized activity."
            );
            return;
        }

        if (matches(information, "Accident or incident details", "Accident details")) {
            addDependency(
                    "What happened in the reported accident or incident?",
                    information,
                    "The incident sequence must be established before examining supporting records."
            );
            return;
        }

        if (matches(information, "Damage, injury, or witness records", "Medical or damage records")) {
            addDependency(
                    "What damage, injury, or witness evidence exists?",
                    information,
                    "Supporting records help establish the consequences and available evidence of the incident."
            );
            return;
        }

        if (matches(information, "Family relationship details", "Marriage details")) {
            addDependency(
                    "What family relationship is relevant?",
                    information,
                    "The relevant relationship must be established before examining inheritance or matrimonial records."
            );
            return;
        }

        if (matches(information, "Estate, will, or property records", "Estate/property records", "Will or testamentary document", "Estate or property records")) {
            addDependency(
                    "What estate, will, or property records govern the issue?",
                    information,
                    "Estate and property records provide the factual basis for inheritance research."
            );
            return;
        }

        if (matches(information, "Alleged conspiracy details", "Details of the alleged offence")) {
            addDependency(
                    "What conduct is alleged and what facts support the allegation?",
                    information,
                    "The reported conduct must be documented before relevant legal materials can be researched."
            );
            return;
        }

        if (matches(information, "Identity of involved parties", "Names or identities of persons involved")) {
            addDependency(
                    "Who are the persons involved?",
                    information,
                    "Identifying the relevant actors helps organize the factual and documentary research."
            );
            return;
        }

        if (matches(information, "Relevant communications or records", "Available evidence", "Available evidence such as messages, photographs, videos, or witness statements")) {
            addDependency(
                    "What communications, records, or other evidence are available?",
                    information,
                    "Available evidence determines what factual material can be examined next."
            );
            return;
        }

        if (matches(information, "Institutional complaint or incident records", "College or institutional complaint details")) {
            addDependency(
                    "What institutional complaint or incident record exists?",
                    information,
                    "Institutional records provide the documented account of the reported student issue."
            );
            return;
        }

        // Preserve unknown analyzer vocabulary instead of silently dropping it.
        addDependency(
                "What evidence or document supports this research item?",
                information,
                "This item was identified by the analysis layer and should be verified before further research."
        );
    }

    private boolean matches(String value, String... candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (value.equals(candidates[i])) {
                return true;
            }
        }
        return false;
    }

    private void addDependency(
            String question,
            String information,
            String reason) {

        if (!containsInformation(information)) {

            dependencies.add(
                    new Dependency(
                            question,
                            information,
                            reason
                    )
            );
        }
    }

    private boolean containsInformation(
            String information) {

        for (int i = 0;
                i < dependencies.size();
                i++) {

            if (dependencies.get(i)
                    .getRequiredInformation()
                    .equals(information)) {

                return true;
            }
        }

        return false;
    }

    public SimpleList<Dependency> getDependencies() {
        return dependencies;
    }
}

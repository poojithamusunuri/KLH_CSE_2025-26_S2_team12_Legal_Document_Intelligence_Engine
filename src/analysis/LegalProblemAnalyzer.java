package analysis;

import algorithms.Levenshtein;
import search.LegalTermCatalog;
import structures.SimpleList;

public class LegalProblemAnalyzer {

    private final LegalTermCatalog legalTermCatalog;

    public LegalProblemAnalyzer() {
        legalTermCatalog = new LegalTermCatalog();
    }

    public AnalysisResult analyze(String problem) {

        SimpleList<String> issues = new SimpleList<>();
        SimpleList<String> questions = new SimpleList<>();
        SimpleList<String> missingInformation = new SimpleList<>();

        if (problem == null || problem.trim().isEmpty()) {
            return new AnalysisResult(
                    problem,
                    issues,
                    questions,
                    missingInformation
            );
        }

        LegalSituation situation =
                extractSituation(problem);

        for (int i = 0; i < situation.getLegalDomains().size(); i++) {
            String domain = situation.getLegalDomains().get(i);
            if (!issues.contains(domain)) {
                issues.add(domain);
            }
        }

        for (int i = 0; i < situation.getQuestions().size(); i++) {
            String question = situation.getQuestions().get(i);
            if (!questions.contains(question)) {
                questions.add(question);
            }
        }

        for (int i = 0; i < situation.getMissingInformation().size(); i++) {
            String missing = situation.getMissingInformation().get(i);
            if (!missingInformation.contains(missing)) {
                missingInformation.add(missing);
            }
        }

        if (issues.isEmpty()) {
            questions.add("What exactly happened and what information is available?");
            questions.add("What documents, messages, or records support the account?");
            if (!missingInformation.contains("Basic chronology of events")) {
                missingInformation.add("Basic chronology of events");
            }
            if (!missingInformation.contains("Relevant documents or communications")) {
                missingInformation.add("Relevant documents or communications");
            }
        }

        return new AnalysisResult(
                problem,
                issues,
                questions,
                missingInformation,
                situation
        );
    }

    public SimpleList<String> getLegalDomainsFromSituation(String problem) {
        return extractSituation(problem).getLegalDomains();
    }

    public LegalSituation extractSituation(String problem) {

        LegalSituation situation =
                new LegalSituation();

        if (problem == null || problem.trim().isEmpty()) {
            situation.setConfidence("LOW");
            return situation;
        }

        String text = normalizeForMatching(problem);

        if (text.isEmpty()) {
            situation.setConfidence("LOW");
            return situation;
        }

        // Contextual normalization keeps legal-language aliases explainable without
        // broad or dangerous substitutions.
        String normalizedText = applyAliasNormalization(text);

        String[] words = splitWords(normalizedText);

        // Relationship hints are added only when multiple relevant concepts appear
        // together, instead of relying on a single generic word such as "money".
        if ((containsAny(normalizedText, "employer", "boss", "company", "manager")
                && containsAny(normalizedText, "salary", "wages", "payment", "paid", "unpaid"))
                || (containsAny(normalizedText, "employee", "worker", "staff")
                && containsAny(normalizedText, "salary", "wages", "pay"))) {
            addRelationshipIfMissing(situation, "employer/employee payment relationship");
        }

        if (containsAny(normalizedText, "landlord", "owner", "tenant", "renter")
                && containsAny(normalizedText, "rent", "lease", "deposit", "returned", "refund")) {
            addRelationshipIfMissing(situation, "rent/lease/deposit relationship");
        }

        if (containsAny(normalizedText, "buyer", "seller", "consumer", "customer")
                && containsAny(normalizedText, "product", "goods", "service", "refund", "defective")) {
            addRelationshipIfMissing(situation, "consumer transaction relationship");
        }

        if (containsAny(normalizedText, "borrow", "lend", "loan", "owe", "repay")
                && containsAny(normalizedText, "money", "amount", "debt", "cash")) {
            addRelationshipIfMissing(situation, "money transfer or debt repayment relationship");
        }

        if (containsAny(normalizedText, "bank", "lender", "financial institution")
                && containsAny(normalizedText, "loan", "mortgage", "hypothecation", "secured")
                && containsAny(normalizedText, "property", "land", "house", "plot")) {
            addRelationshipIfMissing(
                    situation,
                    "bank/lender and borrower/property security relationship"
            );
        }

        addDetectedTokens(situation.getActors(), words,
                new String[] {
                        "employer", "employee", "company", "worker", "seller",
                        "buyer", "retailer", "landlord", "tenant", "borrower",
                        "lender", "friend", "neighbour", "neighbor", "owner",
                        "driver", "contractor", "customer", "consumer", "spouse",
                        "parent", "child", "husband", "wife", "brother", "sister",
                        "student", "college", "school", "university", "bank",
                        "police", "officer", "magistrate", "court", "investigator"
                }
        );

        addDetectedTokens(situation.getActions(), words,
                new String[] {
                        "borrow", "lend", "give", "receive", "pay", "purchase",
                        "buy", "sell", "sign", "promise", "transfer", "claim",
                        "occupy", "damage", "hit", "crash", "dismiss", "terminate",
                        "refuse", "withhold", "return", "repay", "repair", "replace",
                        "copy", "use", "publish", "breach", "perform", "harass",
                        "trouble", "bully", "issued", "search", "searched",
                        "searching", "raid", "raided", "enter", "entered",
                        "show", "showed", "showing", "produce", "produced",
                        "seize", "seized", "arrest", "arrested"
                }
        );

        addDetectedTokens(situation.getObjects(), words,
                new String[] {
                        "money", "amount", "loan", "debt", "salary", "wages",
                        "payment", "property", "land", "house", "vehicle", "car",
                        "product", "phone", "laptop", "contract", "agreement",
                        "deposit", "photograph", "logo", "insurance claim", "work",
                        "service", "account", "bank", "transaction", "college",
                        "school", "conspiracy", "cheque", "check", "warrant",
                        "premises", "residence", "home", "room"
                }
        );

        addDetectedTokens(situation.getObjects(), splitWords(text),
                new String[] {"wages", "photo"});

        if (hasPropertySeizureEvidence(normalizedText)) {
            addDomainIfMissing(
                    situation,
                    "Property / seizure or enforcement matter"
            );
            addEvidence(
                    situation,
                    "property seizure, confiscation, or enforcement action"
            );
            addObjectiveIfMissing(
                    situation,
                    "Assess the legal basis and process for seizure or recovery of property"
            );
            addQuestionIfMissing(
                    situation,
                    "Who seized or claims authority over the property, and on what basis?"
            );
            addMissingInformationIfMissing(
                    situation,
                    "Seizure, attachment, confiscation, or enforcement records"
            );
        }

        if (hasPropertyEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Property / ownership dispute");
            addEvidence(situation, "property ownership or possession claim");
            addObjectiveIfMissing(situation, "Resolve ownership or possession dispute");
            addQuestionIfMissing(situation, "Who legally owns or possesses the property?");
            addQuestionIfMissing(situation, "Is there evidence of transfer, title, or possession?");
            addMissingInformationIfMissing(situation, "Property ownership documents");
            addMissingInformationIfMissing(situation, "Possession or transfer records");
        }

        if (hasEmploymentEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Employment-related dispute");
            addEvidence(situation, "employment and salary or payment issue");
            addObjectiveIfMissing(situation, "Recover unpaid wages or resolve employment dispute");
            addQuestionIfMissing(situation, "What employment terms or role is involved?");
            addQuestionIfMissing(situation, "Was the payment promised, contractually required, or statutory?");
            addMissingInformationIfMissing(situation, "Employment relationship details");
            addMissingInformationIfMissing(situation, "Employment agreement or appointment terms");
            addMissingInformationIfMissing(situation, "Salary or payment records");

            if (containsAny(normalizedText,
                    "dismissed", "discharged", "terminated", "fired")) {
                addMissingInformationIfMissing(
                        situation,
                        "Dismissal or termination details"
                );
            }
        }

        if (hasContractEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Contract-related issue");
            addEvidence(situation, "agreement or contractual obligation");
            addObjectiveIfMissing(situation, "Enforce or assess obligations under an agreement");
            addQuestionIfMissing(situation, "What obligations were agreed and when?");
            addQuestionIfMissing(situation, "Was there a failure to perform, breach, or refusal to comply?");
            addMissingInformationIfMissing(situation, "Contract or agreement");
            addMissingInformationIfMissing(situation, "Performance obligation details");
            addMissingInformationIfMissing(situation, "Evidence of performance, non-performance, or breach");
            addMissingInformationIfMissing(situation, "Identity of parties");
        }

        if (hasConsumerEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Consumer / goods or services dispute");
            addEvidence(situation, "consumer transaction and product or service issue");
            addObjectiveIfMissing(situation, "Obtain remedy, replacement, refund, or resolution");
            addQuestionIfMissing(situation, "What product or service was purchased or provided?");
            addQuestionIfMissing(situation, "What defect, deficiency, or failure occurred?");
            addMissingInformationIfMissing(situation, "Product purchase records");
            addMissingInformationIfMissing(situation, "Payment or refund records");
            addMissingInformationIfMissing(situation, "Evidence of defect, deficiency, or refusal");
        }

        if (hasTenancyEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Tenancy-related matter");
            addEvidence(situation, "rent, lease, or deposit issue");
            addObjectiveIfMissing(situation, "Resolve tenancy, rent, or deposit dispute");
            addQuestionIfMissing(situation, "What lease, tenancy, or payment arrangement existed?");
            addQuestionIfMissing(situation, "What was the rent, deposit, or possession arrangement?");
            addMissingInformationIfMissing(situation, "Tenancy agreement");
            addMissingInformationIfMissing(situation, "Deposit or payment proof");
        }

        if (hasMoneyEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Money / debt / recovery matter");
            addEvidence(situation, "money transfer or repayment dispute");
            addObjectiveIfMissing(situation, "Recover money or settle an outstanding amount");
            addQuestionIfMissing(situation, "Was the money a loan, gift, purchase payment, or other transfer?");
            addQuestionIfMissing(situation, "Is there proof of transfer and any agreed repayment date or term?");
            addMissingInformationIfMissing(situation, "Proof of transfer or payment record");
            addMissingInformationIfMissing(situation, "Evidence of the repayment agreement or acknowledgment");
        }

        /*
         * SECURED LOAN / MORTGAGE DETECTION
         *
         * A bank loan secured by property is a distinct research
         * situation from a generic money/debt query. This lets the
         * natural-language input "my bank gave me a loan against my
         * property" enter the legal-research pipeline instead of
         * being treated as an unclassified text search.
         */
        if (hasSecuredLoanEvidence(normalizedText)) {
            addDomainIfMissing(
                    situation,
                    "Banking / secured lending matter"
            );
            addDomainIfMissing(
                    situation,
                    "Property / secured-interest matter"
            );

            addEvidence(
                    situation,
                    "loan secured by property or immovable asset"
            );

            addObjectiveIfMissing(
                    situation,
                    "Understand the rights and obligations arising from the secured loan"
            );

            addQuestionIfMissing(
                    situation,
                    "What loan and security arrangement was agreed with the lender?"
            );

            addQuestionIfMissing(
                    situation,
                    "What property was offered as security and what security interest was created?"
            );

            addQuestionIfMissing(
                    situation,
                    "What are the repayment, default, and enforcement terms?"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Loan agreement and sanction documents"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Mortgage, hypothecation, or security documents"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Repayment and outstanding-balance records"
            );
        }

        /*
         * SEARCH / ARREST WARRANT DETECTION
         */
        if (hasWarrantEvidence(normalizedText)) {
            addDomainIfMissing(
                    situation,
                    "Criminal procedure / warrant matter"
            );

            addEvidence(
                    situation,
                    "search or arrest warrant issue"
            );

            addObjectiveIfMissing(
                    situation,
                    "Assess the legal basis, scope, or validity of the warrant-related action"
            );

            addQuestionIfMissing(
                    situation,
                    "What type of warrant or search order was issued or relied upon?"
            );

            addQuestionIfMissing(
                    situation,
                    "Which authority issued it and under what circumstances?"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Warrant or court order"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Relevant police, court, or case records"
            );

            if (containsAny(normalizedText,
                    "without showing", "without a warrant", "without warrant",
                    "no warrant", "not shown", "not produced", "did not show",
                    "did not produce", "failed to show", "failed to produce",
                    "warrantless")) {
                addEvidence(
                        situation,
                        "the query states that a warrant was not shown or produced"
                );
                addRelationshipIfMissing(
                        situation,
                        "reported search or entry without a warrant being shown"
                );
                addQuestionIfMissing(
                        situation,
                        "Was a warrant or other lawful authority in fact issued, and was it shown or served?"
                );
                addMissingInformationIfMissing(
                        situation,
                        "Warrant, search record, or other authority relied upon"
                );
            }
        }

        /*
         * CHEQUE / DISHONOUR DETECTION
         *
         * A cheque-related query should be treated as a specific
         * legal situation when the text mentions a cheque/check
         * together with dishonour, bounce, insufficient funds,
         * banking details, or an underlying payment/debt.
         */
        if (hasChequeEvidence(normalizedText)) {
            addDomainIfMissing(
                    situation,
                    "Cheque / negotiable instrument matter"
            );

            addEvidence(
                    situation,
                    "cheque dishonour or payment failure"
            );

            addObjectiveIfMissing(
                    situation,
                    "Assess the legal consequences of the dishonoured cheque"
            );

            addQuestionIfMissing(
                    situation,
                    "What was the purpose and amount of the cheque?"
            );

            addQuestionIfMissing(
                    situation,
                    "Why was the cheque dishonoured and what records establish the dishonour?"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Cheque and bank return records"
            );

            addMissingInformationIfMissing(
                    situation,
                    "Underlying payment, debt, or transaction details"
            );
        }

        if (hasVehicleEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Motor vehicle / accident-related matter");
            addEvidence(situation, "vehicle accident or collision");
            addObjectiveIfMissing(situation, "Assess liability, damages, or claims after accident");
            addQuestionIfMissing(situation, "How did the accident or collision occur?");
            addQuestionIfMissing(situation, "What damage, injury, or loss was caused?");
            addMissingInformationIfMissing(situation, "Accident or incident details");
            addMissingInformationIfMissing(situation, "Damage, injury, or witness records");
        }

        if (hasIntellectualPropertyEvidence(normalizedText)) {
            addDomainIfMissing(situation, "Intellectual property matter");
            addEvidence(situation, "possible unauthorized use or copying");
            addObjectiveIfMissing(situation, "Address unauthorized use or protect intellectual property");
            addQuestionIfMissing(situation, "What work or branded material is alleged to be copied or used?");
            addQuestionIfMissing(situation, "Who owns the relevant intellectual property or work?");
            addMissingInformationIfMissing(situation, "Ownership or registration records");
            addMissingInformationIfMissing(situation, "Evidence of use, copying, or authorization");
        }

        if (containsAnyExact(normalizedText,
                "father",
                "mother",
                "brother",
                "sister",
                "husband",
                "wife",
                "spouse",
                "child",
                "children",
                "heir",
                "estate",
                "inheritance",
                "share")
                && containsAny(normalizedText,
                "property", "inheritance", "estate", "will", "dispute", "claim")) {
            addDomainIfMissing(situation, "Family / inheritance matter");
            addEvidence(situation, "family or estate dispute");
            addObjectiveIfMissing(situation, "Resolve family or inheritance issue");
            addQuestionIfMissing(situation, "Who are the relevant family members or legal heirs?");
            addQuestionIfMissing(situation, "What asset, property, or estate is in dispute?");
            addMissingInformationIfMissing(situation, "Family relationship details");
            addMissingInformationIfMissing(situation, "Estate, will, or property records");
        }

        if (containsAny(normalizedText, "bank", "account", "transaction", "card")
                && containsAny(normalizedText, "unauthorized", "without permission", "stolen", "fraud", "unknown")) {
            addDomainIfMissing(situation, "Banking / unauthorized transaction matter");
            addEvidence(situation, "possible unauthorized banking transaction");
            addObjectiveIfMissing(situation, "Trace, stop, or recover an unauthorized transaction");
            addQuestionIfMissing(situation, "What transaction was unauthorized and when did it occur?");
            addMissingInformationIfMissing(situation, "Bank transaction details");
            addMissingInformationIfMissing(situation, "Authorization status");
        }

        if ((containsAny(normalizedText, "college", "school", "university", "student")
                && containsAny(normalizedText, "harass", "ragging", "bully", "trouble", "abuse"))
                || isCatalogTermMentioned(normalizedText, "Ragging")) {
            addDomainIfMissing(situation, "Education / student issue");
            addEvidence(situation, "student or educational-institution complaint or catalog term");
            addObjectiveIfMissing(situation, "Address the reported educational or student issue");
            addQuestionIfMissing(situation, "What happened and which institution or person was involved?");
            addMissingInformationIfMissing(situation, "Institutional complaint or incident records");
        }

        if (isCatalogTermMentioned(normalizedText, "Conspiracy")) {
            addDomainIfMissing(situation, "Criminal / conspiracy-related issue");
            addEvidence(situation, "catalog legal term: conspiracy");
            addObjectiveIfMissing(situation, "Identify the alleged coordinated conduct and relevant legal provisions");
            addQuestionIfMissing(situation, "What conduct, persons, or communications are alleged to form the conspiracy?");
            addMissingInformationIfMissing(situation, "Alleged conspiracy details");
            addMissingInformationIfMissing(situation, "Identity of involved parties");
            addMissingInformationIfMissing(situation, "Relevant communications or records");
        }

        if (situation.getLegalDomains().isEmpty()) {
            addQuestionIfMissing(situation, "What exactly happened and what information is available?");
            addQuestionIfMissing(situation, "What documents, messages, or records support the account?");
            addMissingInformationIfMissing(situation, "Basic chronology of events");
            addMissingInformationIfMissing(situation, "Relevant documents or communications");
            situation.setConfidence("LOW");
        } else {
            String confidence =
                    situation.getLegalDomains().size() >= 2 || situation.getEvidenceIndicators().size() >= 3
                            ? "HIGH"
                            : "MEDIUM";
            situation.setConfidence(confidence);
        }

        if (situation.getEvidenceIndicators().isEmpty()) {
            situation.setProblemEvent("No specific problematic event was confidently identified.");
        } else {
            String problemEvent = situation.getEvidenceIndicators().get(0);
            situation.setProblemEvent(problemEvent);
        }

        return situation;
    }

    private void addDomainIfMissing(LegalSituation situation, String domain) {
        if (!situation.getLegalDomains().contains(domain)) {
            situation.getLegalDomains().add(domain);
        }
    }

    private void addEvidence(LegalSituation situation, String evidence) {
        if (!situation.getEvidenceIndicators().contains(evidence)) {
            situation.getEvidenceIndicators().add(evidence);
        }
    }

    private void addObjectiveIfMissing(LegalSituation situation, String objective) {
        if (!situation.getPossibleObjectives().contains(objective)) {
            situation.getPossibleObjectives().add(objective);
        }
    }

    private void addQuestionIfMissing(LegalSituation situation, String question) {
        if (!situation.getQuestions().contains(question)) {
            situation.getQuestions().add(question);
        }
    }

    private void addRelationshipIfMissing(LegalSituation situation, String relationship) {
        if (!situation.getRelationships().contains(relationship)) {
            situation.getRelationships().add(relationship);
        }
    }

    private boolean hasSecuredLoanEvidence(String text) {
        boolean securedInstrument =
                containsAny(text, "loan", "mortgage", "hypothecation", "secured loan", "secured lending");

        boolean property =
                containsAny(text, "property", "land", "house", "plot", "immovable property");

        boolean securityContext =
                containsAny(text, "bank", "lender", "financial institution",
                        "against", "on my", "on the");

        return securedInstrument && property && securityContext;
    }

    private boolean hasWarrantEvidence(String text) {
        return containsAny(
                text,
                "search warrant",
                "arrest warrant",
                "warrant of arrest",
                "warrant to search",
                "search order"
        ) || (containsAny(text, "warrant")
                && containsAny(text, "search", "arrest", "police", "court"));
    }

    private boolean hasPropertySeizureEvidence(String text) {
        return containsAny(text, "property", "land", "house", "vehicle", "goods")
                && containsAny(text, "seize", "seized", "seizure", "confiscate",
                        "confiscated", "confiscation", "attach", "attached",
                        "attachment");
    }

    private boolean hasPropertyEvidence(String text) {
        return containsAny(text, "house", "home", "land", "property", "title", "possession", "occupy")
                && containsAny(text, "ownership", "owner", "title", "belongs", "claim", "occupy", "possession", "dispute", "boundary", "trespass");
    }

    private boolean hasEmploymentEvidence(String text) {
        return (containsAny(text, "employer", "employee", "company", "boss", "manager", "workplace", "job")
                && containsAny(text, "salary", "wages", "paid", "payment", "unpaid", "withheld", "dismissed", "discharged", "fired", "terminated", "termination"))
                || (containsAny(text, "salary", "wages", "unpaid", "withheld", "dismissed", "discharged", "fired", "terminated", "termination")
                && containsAny(text, "work", "job", "employment"))
                || (containsAny(text, "not paid", "not being paid")
                && containsAny(text, "work", "job", "employment"));
    }

    private boolean hasContractEvidence(String text) {
        return containsAny(text, "contract", "agreement", "deal", "terms", "promise", "obligation", "signed", "executed")
                && (containsAny(text, "breach", "refuse", "refused", "refusing", "obligation", "obligations", "non-performance", "perform", "enforce", "default", "honour", "honouring", "honored")
                || (containsAny(text, "signed", "executed") && containsAny(text, "agreement", "contract")));
    }

    private boolean hasConsumerEvidence(String text) {
        return containsAny(text, "consumer", "customer", "buyer", "seller", "retailer", "purchase", "bought")
                && containsAny(text, "product", "goods", "service", "refund", "repair", "replacement", "defective", "faulty")
                || containsAny(text, "product", "goods", "service")
                && containsAny(text, "defective", "faulty", "refund", "refuse", "refused", "remedy");
    }

    private boolean hasTenancyEvidence(String text) {
        return containsAny(text, "tenant", "landlord", "rent", "rental", "lease", "tenancy", "eviction", "security deposit")
                || containsAny(text, "deposit")
                && containsAny(text, "landlord", "tenant", "rent", "rental", "lease", "return", "refuse", "refused");
    }

    private boolean hasIntellectualPropertyEvidence(String text) {
        return containsAny(text, "copyright", "trademark", "patent", "infringement")
                || containsAny(text, "photo", "image", "logo", "brand")
                && containsAny(text, "copied", "copy", "unauthorized", "publish", "use", "commercially");
    }

    private boolean hasMoneyEvidence(String text) {
        return containsAny(text, "money", "cash", "amount", "funds", "debt", "loan", "borrow", "lend", "owe", "owed", "repay", "return", "pay back")
                && (containsAny(text, "borrow", "lend", "loan", "debt", "owe", "owed", "repay", "pay back")
                || containsAny(text, "unauthorized", "without permission", "bank account", "fraud"))
                && containsAny(text, "refuse", "refused", "refusing", "unpaid", "owed", "owe", "dispute", "recover", "unauthorized", "without permission", "stolen", "fraud", "not");
    }

    private boolean hasChequeEvidence(String text) {
        return containsAny(
                text,
                "cheque",
                "check",
                "dishonour",
                "dishonored",
                "dishonoured"
        )
                && (
                    containsAny(
                            text,
                            "bounce",
                            "bounced",
                            "dishonour",
                            "dishonored",
                            "dishonoured",
                            "insufficient funds",
                            "insufficient balance",
                            "account"
                    )
                    || containsAny(
                            text,
                            "bank",
                            "payment",
                            "debt",
                            "loan",
                            "repay",
                            "issued"
                    )
                );
    }

    private boolean hasVehicleEvidence(String text) {
        return containsAny(text, "vehicle", "car", "truck", "bike", "motorcycle", "driver")
                && containsAny(text, "accident", "collision", "hit", "crash", "damage", "injury", "negligence", "driver");
    }

    private void addMissingInformationIfMissing(LegalSituation situation, String info) {
        if (!situation.getMissingInformation().contains(info)) {
            situation.getMissingInformation().add(info);
        }
    }

    private void addDetectedTokens(SimpleList<String> target, String[] words, String[] keywords) {
        for (int i = 0; i < keywords.length; i++) {
            String keyword = normalizeForMatching(keywords[i]);
            if (keyword.isEmpty()) {
                continue;
            }
            for (int j = 0; j < words.length; j++) {
                String word = words[j];
                if (!word.isEmpty() && word.equals(keyword)) {
                    if (!target.contains(keyword)) {
                        target.add(keyword);
                    }
                }
            }
        }
    }

    private boolean containsAnyExact(
            String text,
            String... keywords) {

        String normalizedText = normalizeForMatching(text);

        for (int i = 0; i < keywords.length; i++) {
            String keyword = normalizeForMatching(keywords[i]);
            if (containsExactTokenOrPhrase(normalizedText, keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCatalogTermMentioned(
            String text,
            String catalogTerm) {

        SimpleList<String> catalogTerms = legalTermCatalog.getTerms();

        for (int i = 0; i < catalogTerms.size(); i++) {
            String term = catalogTerms.get(i);
            if (term.equalsIgnoreCase(catalogTerm)) {
                return containsAny(text, term);
            }
        }

        return false;
    }

    /*
     * Checks whether the text contains at least one
     * keyword or phrase from the supplied list.
     */
    /*
     * ============================================================
     * FUZZY KEYWORD DETECTION
     * ============================================================
     *
     * Exact matches are checked first.
     *
     * If an exact match is not found, Levenshtein distance
     * is used to detect small spelling mistakes.
     *
     * Examples:
     *
     * marriage -> marriage
     * marrage  -> marriage
     * marraige -> marriage
     *
     * divorce  -> divorce
     * divorse  -> divorce
     * divroce  -> divorce
     *
     * This works automatically for all categories that use
     * containsAny().
     */
    private boolean containsAny(
            String text,
            String... keywords) {

        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String normalizedText =
                normalizeForMatching(text);

        if (normalizedText.isEmpty()) {
            return false;
        }

        String[] normalizedWords =
                splitWords(normalizedText);

        /*
         * --------------------------------------------------------
         * STEP 1: Exact word and phrase matching with boundaries
         * --------------------------------------------------------
         */
        for (int i = 0; i < keywords.length; i++) {

            String keyword =
                    normalizeForMatching(keywords[i]);

            if (keyword.isEmpty()) {
                continue;
            }

            if (containsExactTokenOrPhrase(
                    normalizedText,
                    keyword
            )) {
                return true;
            }
        }

        /*
         * --------------------------------------------------------
         * STEP 2: Conservative fuzzy matching
         * --------------------------------------------------------
         *
         * Fuzzy matching is now limited to meaningful tokens only,
         * and only when the keyword itself is a meaningful longer
         * legal term. This avoids false positives from short or
         * generic query words.
         */
        for (int i = 0; i < normalizedWords.length; i++) {

            String word = normalizedWords[i];

            if (!isMeaningfulFuzzyCandidate(word)) {
                continue;
            }

            for (int j = 0; j < keywords.length; j++) {

                String keyword =
                        normalizeForMatching(keywords[j]);

                if (keyword.isEmpty()
                        || keyword.indexOf(' ') >= 0
                        || keyword.length() <= 4
                        || keyword.equals(word)) {
                    continue;
                }

                int distance =
                        Levenshtein.distance(word, keyword);

                int allowedDistance =
                        getAllowedDistance(keyword);

                if (distance <= allowedDistance) {
                    return true;
                }
            }
        }

        return false;
    }

    private String normalizeForMatching(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
            applyAliasNormalization(value.toLowerCase());

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9 ]",
                        " "
                );

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        return normalized.trim();
    }

    private String applyAliasNormalization(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value;

        normalized = normalized.replace("can't", "cannot");
        normalized = normalized.replace("won't", "will not");
        normalized = normalized.replace("n't", " not");
        normalized = normalized.replace("'re", " are");
        normalized = normalized.replace("'ve", " have");
        normalized = normalized.replace("'ll", " will");
        normalized = normalized.replace("'d", " would");

        normalized = normalized.replace(" wages ", " salary ");
        normalized = normalized.replace(" wages", " salary");
        normalized = normalized.replace("salarys", "salary");
        normalized = normalized.replace("loaned", "lent");
        normalized = normalized.replace("borrowed", "borrow");
        normalized = normalized.replace("rented", "rent");
        normalized = normalized.replace("dishonor", "dishonour");
        normalized = normalized.replace("dishonored", "dishonour");
        normalized = normalized.replace("dishonoring", "dishonour");
        normalized = normalized.replace("honor", "honour");
        normalized = normalized.replace("honored", "honour");
        normalized = normalized.replace("discharged", "dismissed");
        normalized = normalized.replace("tenant", "tenant");
        normalized = normalized.replace("landlord", "landlord");
        normalized = normalized.replace("automobile", "vehicle");
        normalized = normalized.replace("car", "vehicle");
        normalized = normalized.replace("bike", "vehicle");
        normalized = normalized.replace("motorcycle", "vehicle");
        normalized = normalized.replace("contract", "agreement");
        normalized = normalized.replace("deal", "agreement");
        normalized = normalized.replace("agreements", "agreement");

        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private String[] splitWords(String text) {

        String[] rawWords = text.split(" ");

        int count = 0;

        for (int i = 0; i < rawWords.length; i++) {

            if (!rawWords[i].trim().isEmpty()) {
                count++;
            }
        }

        String[] words = new String[count];
        int index = 0;

        for (int i = 0; i < rawWords.length; i++) {

            String word = rawWords[i].trim();

            if (!word.isEmpty()) {
                words[index] = word;
                index++;
            }
        }

        return words;
    }

    private boolean containsExactTokenOrPhrase(
            String text,
            String keyword) {

        if (keyword == null || keyword.isEmpty()) {
            return false;
        }

        if (keyword.indexOf(' ') >= 0) {
            return containsPhrase(text, keyword);
        }

        String[] tokens = splitWords(text);

        for (int i = 0; i < tokens.length; i++) {

            if (tokens[i].equals(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsPhrase(
            String text,
            String phrase) {

        String[] textTokens = splitWords(text);
        String[] phraseTokens = splitWords(phrase);

        for (int i = 0;
                i <= textTokens.length - phraseTokens.length;
                i++) {

            boolean match = true;

            for (int j = 0; j < phraseTokens.length; j++) {

                if (!textTokens[i + j].equals(phraseTokens[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }

    private boolean isMeaningfulFuzzyCandidate(String word) {

        if (word == null || word.isEmpty()) {
            return false;
        }

        if (word.length() <= 3) {
            return false;
        }

        if (isGenericWord(word)) {
            return false;
        }

        return true;
    }

    private boolean isGenericWord(String word) {

        String[] genericWords = {
                "someone",
                "claiming",
                "claims",
                "says",
                "said",
                "belong",
                "belongs",
                "them",
                "their",
                "they",
                "my",
                "your",
                "that",
                "this",
                "these",
                "those",
                "there",
                "what",
                "who",
                "why",
                "when",
                "where",
                "which",
                "how",
                "does",
                "did",
                "do",
                "is",
                "are",
                "was",
                "were",
                "be",
                "been",
                "being",
                "has",
                "have",
                "had",
                "with",
                "from",
                "into",
                "through",
                "without",
                "within",
                "between",
                "during",
                "after",
                "before",
                "because",
                "while",
                "about",
                "under",
                "over",
                "against",
                "across",
                "around"
        };

        for (int i = 0; i < genericWords.length; i++) {

            if (genericWords[i].equals(word)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ============================================================
     * ALLOWED EDIT DISTANCE
     * ============================================================
     *
     * Short words get no fuzzy tolerance.
     * Longer words can tolerate a small spelling mistake.
     */
    private int getAllowedDistance(String keyword) {

        int length = keyword.length();

        if (length <= 5) {
            return 0;
        }

        if (length <= 7) {
            return 1;
        }

        if (length <= 10) {
            return 2;
        }

        return 3;
    }

    public static class LegalSituation {

        private final SimpleList<String> actors;
        private final SimpleList<String> actions;
        private final SimpleList<String> objects;
        private final SimpleList<String> relationships;
        private final SimpleList<String> legalDomains;
        private final SimpleList<String> possibleObjectives;
        private final SimpleList<String> evidenceIndicators;
        private final SimpleList<String> questions;
        private final SimpleList<String> missingInformation;

        private String problemEvent;
        private String confidence;

        public LegalSituation() {
            this.actors = new SimpleList<String>();
            this.actions = new SimpleList<String>();
            this.objects = new SimpleList<String>();
            this.relationships = new SimpleList<String>();
            this.legalDomains = new SimpleList<String>();
            this.possibleObjectives = new SimpleList<String>();
            this.evidenceIndicators = new SimpleList<String>();
            this.questions = new SimpleList<String>();
            this.missingInformation = new SimpleList<String>();
            this.problemEvent = "No specific problematic event was confidently identified.";
            this.confidence = "LOW";
        }

        public SimpleList<String> getActors() {
            return actors;
        }

        public SimpleList<String> getActions() {
            return actions;
        }

        public SimpleList<String> getObjects() {
            return objects;
        }

        public SimpleList<String> getRelationships() {
            return relationships;
        }

        public SimpleList<String> getLegalDomains() {
            return legalDomains;
        }

        public SimpleList<String> getPossibleObjectives() {
            return possibleObjectives;
        }

        public SimpleList<String> getEvidenceIndicators() {
            return evidenceIndicators;
        }

        public SimpleList<String> getQuestions() {
            return questions;
        }

        public SimpleList<String> getMissingInformation() {
            return missingInformation;
        }

        public String getProblemEvent() {
            return problemEvent;
        }

        public void setProblemEvent(String problemEvent) {
            this.problemEvent = problemEvent;
        }

        public String getConfidence() {
            return confidence;
        }

        public void setConfidence(String confidence) {
            this.confidence = confidence;
        }
    }

    public static class AnalysisResult {

        private final String originalProblem;

        private final SimpleList<String> issues;

        private final SimpleList<String> questions;

        private final SimpleList<String>
                missingInformation;

        private final LegalSituation situation;

        public AnalysisResult(
                String originalProblem,
                SimpleList<String> issues,
                SimpleList<String> questions,
                SimpleList<String> missingInformation) {

            this(
                    originalProblem,
                    issues,
                    questions,
                    missingInformation,
                    new LegalSituation()
            );
        }

        public AnalysisResult(
                String originalProblem,
                SimpleList<String> issues,
                SimpleList<String> questions,
                SimpleList<String> missingInformation,
                LegalSituation situation) {

            this.originalProblem =
                    originalProblem;

            this.issues = issues;

            this.questions = questions;

            this.missingInformation =
                    missingInformation;

            this.situation = situation;
        }

        public String getOriginalProblem() {
            return originalProblem;
        }

        public SimpleList<String> getIssues() {
            return issues;
        }

        public SimpleList<String> getQuestions() {
            return questions;
        }

        public SimpleList<String>
        getMissingInformation() {

            return missingInformation;
        }

        public LegalSituation getSituation() {
            return situation;
        }
    }
}
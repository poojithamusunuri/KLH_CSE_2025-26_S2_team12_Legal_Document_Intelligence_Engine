package search;

/*
 * Shared legal vocabulary: synonym groups used by the ranking model.
 *
 * WHY GROUPS INSTEAD OF WORDS
 *   A person says "my employer has not paid my salary"; a judgment may say
 *   "wages were withheld". Both express the same event, so words with the same
 *   meaning are collected into a GROUP. A document covers a group if it
 *   contains ANY phrase of the group, and a group counts once no matter how
 *   many times it appears. That stops a document from ranking high merely
 *   because it repeats one word many times.
 *
 * ROLES
 *   ACTION  - what happened        (not paid, refused, terminated, copied)
 *   OBJECT  - what it happened to  (salary, deposit, cheque, land)
 *   ACTOR   - who is involved      (employer, landlord, bank, buyer)
 *   CONTEXT - procedural setting   (appeal, limitation)
 *
 *   An ACTION group appearing close to an OBJECT group in a document is the
 *   "event evidence" that separates a document about unpaid salary from a tax
 *   judgment that merely mentions the word salary.
 *
 * HONEST LIMIT
 *   This is a hand-written lexicon, not language understanding. It covers the
 *   situations the analyser detects and can be extended by adding rows here;
 *   no ranking code has to change.
 *
 * All phrases are lower-case a-z / 0-9 with single spaces (see TextNormalizer).
 */
public final class LegalVocabulary {

    public static final int ACTION = 0;
    public static final int OBJECT = 1;
    public static final int ACTOR = 2;
    public static final int CONTEXT = 3;

    /* {label, role, phrase, phrase, ...} */
    private static final String[][] RAW = {

            // ---------------------------------------------------- ACTIONS
            {"non-payment", "A",
                    "not paid", "has not paid", "have not paid", "hasn t paid",
                    "haven t paid", "did not pay", "does not pay", "not pay",
                    "not being paid", "unpaid", "non payment", "nonpayment",
                    "withheld", "withhold", "withholding", "failed to pay",
                    "failure to pay", "refused to pay", "refuses to pay",
                    "refuse to pay", "defaulted", "default in payment",
                    "arrears", "not given", "hasn t given", "hasn t been paid"},

            {"refusal", "A",
                    "refuse", "refused", "refuses", "refusing", "refusal",
                    "declined", "denied", "not honour", "not honouring",
                    "not honoring", "not honored", "not honoured"},

            {"return / refund", "A",
                    "refund", "refunded", "refunding", "repay", "repaid",
                    "repayment", "reimburse", "reimbursement", "give back",
                    "pay back", "paid back", "return the", "return of",
                    "return my", "not return", "not returning", "not returned",
                    "to return", "returned the"},

            {"breach / non-performance", "A",
                    "breach", "breached", "breaches", "non performance",
                    "nonperformance", "failed to perform", "failure to perform",
                    "refused to perform", "refuse to perform", "not perform",
                    "did not perform", "repudiation", "repudiated",
                    "violated the agreement", "violation of the agreement"},

            {"termination / dismissal", "A",
                    "terminate", "terminated", "termination", "dismiss",
                    "dismissed", "dismissal", "discharged from service",
                    "discharged from work", "discharged from employment",
                    "discharged from job", "retrenched", "retrenchment",
                    "removed from service", "sacked", "fired"},

            {"unauthorised act", "A",
                    "unauthorized", "unauthorised", "without authority",
                    "without permission", "without my permission",
                    "without consent", "without authorization",
                    "without authorisation", "without my knowledge",
                    "fraudulent", "forged", "forgery"},

            // Everyday descriptions of taking property map to the statutory
            // theft concept. These are aliases, not a legal conclusion that
            // every reported taking satisfies every element of theft.
            {"theft / dishonest taking", "A",
                    "theft", "steal", "steals", "stealing", "stole", "stolen",
                    "stolen property", "dishonestly takes", "dishonest taking",
                    "taking without consent", "took without permission",
                    "taken without consent", "snatched", "pickpocket", "shoplifting"},

            // Abuse wording is deliberately broad for retrieval only. The
            // engine must not infer a criminal finding from a user's statement.
            {"physical / domestic abuse", "A",
                    "physically abused", "physical abuse", "abused me", "abuse me",
                    "beats", "beat me", "beating", "hit me",
                    "hits me", "assaulted me", "domestic violence", "spousal abuse",
                    "matrimonial cruelty", "cruelty by husband", "husband beats",
                    "husband hit", "wife beats"},

            {"cyber intrusion / account compromise", "A",
                    "hacked", "hacking", "hack into", "hacked into", "unauthorized access",
                    "unauthorised access", "account hacked", "account compromised",
                    "online fraud", "cyber fraud", "identity theft", "stole money online"},

            {"fraud / dishonest inducement", "A",
                    "cheat", "cheated", "cheating", "scam", "scammed", "defrauded",
                    "deceived", "dishonest inducement", "took my money", "took money from me",
                    "deliver any property", "dishonestly induces", "fraudulently induces",
                    "cheating by personation"},

            {"copying / infringement", "A",
                    "copied", "copying", "infringe", "infringed",
                    "infringement", "pirated", "piracy", "plagiarism",
                    "plagiarised", "plagiarized", "reproduced without",
                    "used without permission", "passing off"},

            {"sale / transfer", "A",
                    "sold", "sell", "selling", "sale of", "sale deed",
                    "transferred", "transfer of", "gift deed", "gifted",
                    "conveyed", "conveyance", "alienated", "alienation"},

            {"seizure / confiscation", "A",
                    "seize", "seized", "seizure", "confiscate",
                    "confiscated", "confiscation", "attach", "attached",
                    "attachment"},
            {"search / entry / raid", "A",
                    "search", "searched", "searching", "raid", "raided",
                    "raiding", "enter", "entered", "entry", "searched premises",
                    "search of premises", "search the premises", "searched the house"},

            {"warrant not shown / produced", "A",
                    "without showing me a warrant", "without showing a warrant",
                    "without producing a warrant", "without a warrant", "without warrant",
                    "no warrant", "warrant not shown", "warrant not produced",
                    "did not show the warrant", "did not produce the warrant",
                    "failed to show the warrant", "failed to produce the warrant",
                    "warrant was not shown", "warrant was not produced", "warrantless"},

            {"eviction / dispossession", "A",
                    "evict", "evicted", "eviction", "ejectment", "eject",
                    "vacate", "vacated", "dispossess", "dispossessed",
                    "dispossession", "encroach", "encroachment", "trespass",
                    "unlawful possession", "illegal possession", "occupying",
                    "occupied", "occupation"},

            {"accident / injury", "A",
                    "accident", "collision", "collided", "rash driving",
                    "negligent driving", "rash and negligent", "knocked down",
                    "run over", "hit by", "hit my", "injured", "injuries",
                    "injury"},

            {"defect / deficiency", "A",
                    "defect", "defective", "defects", "faulty",
                    "deficiency in service", "deficient", "substandard",
                    "malfunction", "not working"},

            {"refused delivery", "A",
                    "refused to accept", "refuse to accept", "refused delivery",
                    "refused to take delivery", "declined to accept",
                    "non delivery", "not delivered", "failed to deliver",
                    "accept delivery", "taking delivery"},

            {"cheque dishonour", "A",
                    "bounce", "bounced", "dishonour", "dishonoured", "dishonor",
                    "dishonored", "returned unpaid", "insufficient funds",
                    "insufficiency of funds", "cheque returned"},

            {"delay / limitation", "A",
                    "delay", "delayed", "limitation", "time barred",
                    "barred by limitation", "condone", "condonation",
                    "condoned", "filed late", "out of time", "beyond time",
                    "beyond the period"},

            {"appeal / challenge", "C",
                    "appeal", "appealed", "appellate", "challenge",
                    "challenged", "revision", "writ petition"},

            // ---------------------------------------------------- OBJECTS
            {"salary / wages", "O",
                    "salary", "salaries", "wage", "wages", "remuneration",
                    "emoluments", "pay slip", "payslip", "stipend",
                    "back wages", "backwages"},

            // Competing revenue/tax objects are needed by nearest-object
            // event evidence. They are not query-specific; they exist so a
            // tax judgment cannot borrow a distant salary mention to become
            // a DIRECT salary-nonpayment match.
            {"tax / revenue", "O",
                    "tax", "taxes", "taxable", "taxpayer", "taxpayers",
                    "assessee", "assessees", "assessment", "assessments",
                    "income tax", "tax deduction", "deduction at source",
                    "tax deducted at source", "revenue", "levy", "levied"},

            {"deposit", "O",
                    "deposit", "deposits", "security deposit", "advance rent",
                    "earnest money", "caution money"},

            {"rent / tenancy", "O",
                    "rent", "rental", "lease", "leased", "tenancy", "rented",
                    "tenement", "rent control"},

            // Mortgage is usually a legal instrument/topic noun. It becomes
            // an event only when a separate action such as enforcement,
            // foreclosure, or seizure is detected.
            {"secured lending / mortgage", "C",
                    "loan against", "secured loan", "secured lending",
                    "loan secured by", "loan secured against", "mortgage",
                    "mortgaged", "hypothecation", "pledged property"},

            {"loan / debt", "O",
                    "loan", "loans", "borrowed", "borrow", "lent", "lend",
                    "lending", "debt", "debts", "friendly loan", "hand loan",
                    "money lent", "borrowed money"},

            {"cheque", "O",
                    "cheque", "cheques", "section 138", "negotiable instrument",
                    "negotiable instruments act"},

            {"premises / residence", "C",
                    "house", "home", "premises", "residence", "dwelling",
                    "building", "room", "place"},

            {"goods / product / movable property", "O",
                    "goods", "product", "products", "consignment",
                    "merchandise", "commodity", "commodities",
                    "movable property", "personal belongings", "belongings", "luggage"},

            {"money / funds", "O",
                    "money", "cash", "funds", "amount", "amounts", "savings",
                    "stolen money", "bank balance", "account balance",
                    "deliver any property", "delivery of property", "retained any property"},

            {"noise / neighbourhood nuisance", "C",
                    "loud music", "loud noise", "noise nuisance", "noisy neighbour",
                    "noisy neighbor", "neighbour plays loud music", "neighbor plays loud music",
                    "music at night", "disturbing the neighbourhood", "disturbing the neighborhood"},

            {"contract / agreement", "O",
                    "contract", "contracts", "agreement", "agreements",
                    "contractual", "verbal deal", "oral agreement",
                    "oral contract", "obligation", "obligations"},

            {"land / property", "O",
                    "land", "lands", "property", "properties", "plot", "flat",
                    "immovable property", "agricultural land"},

            {"title / ownership", "O",
                    "title", "ownership", "title deed", "adverse possession",
                    "possession", "ownership rights"},

            {"bank account", "O",
                    "account", "accounts", "bank account", "savings account",
                    "current account", "passbook"},

            {"debit / transaction", "O",
                    "transaction", "transactions", "debit", "debited",
                    "withdrawal", "withdrawn", "deducted", "deduction", "atm",
                    "net banking", "online banking", "upi", "credit card",
                    "debit card"},

            {"photo / creative work", "O",
                    "photo", "photos", "photograph", "photographs", "image",
                    "images", "logo", "logos", "trademark", "copyright",
                    "song", "artwork", "painting", "website", "software",
                    "literary work", "artistic work"},

            {"shares / pledged security", "O",
                    "shares", "share certificate", "securities", "pledge",
                    "pledged", "pledgee", "pledgor", "hypothecation",
                    "mortgage", "mortgaged"},

            {"vehicle", "O",
                    "vehicle", "vehicles", "car", "truck", "lorry", "bus",
                    "motorcycle", "scooter", "motor vehicle", "two wheeler"},

            {"inheritance / estate", "O",
                    "inheritance", "inherit", "inherited", "heir", "heirs",
                    "succession", "successor", "last will", "will deed",
                    "testamentary", "testament", "probate", "legacy",
                    "partition", "ancestral property", "coparcener",
                    "intestate"},

            {"marriage / family matter", "O",
                    "marriage", "marital", "divorce", "matrimonial",
                    "maintenance", "alimony", "custody", "cruelty"},

            // Keep dowry as its own concept; mapping it to generic marriage
            // vocabulary caused unrelated marriage statutes to appear.
            {"dowry-related issue", "O",
                    "dowry", "dowry demand", "dowry harassment", "dowry death"},

            {"search warrant / arrest warrant", "C",
                    "search warrant", "arrest warrant", "warrant of arrest",
                    "warrant to search", "search order"},

            // ------------------------------------------------------ ACTORS
            {"employer / employee", "R",
                    "employer", "employers", "employee", "employees",
                    "employment", "boss", "workman", "workmen", "worker",
                    "workers"},

            {"owner / landlord / tenant", "R",
                    "owner", "owners", "landlord", "landlords", "landlady",
                    "lessor", "lessee", "tenant", "tenants"},

            {"bank / lender", "R",
                    "bank", "banks", "banker", "bankers", "sbi", "lender",
                    "financial institution", "creditor", "creditors"},

            {"seller / buyer", "R",
                    "seller", "sellers", "buyer", "buyers", "purchaser",
                    "vendor", "vendee", "retailer", "shopkeeper",
                    "manufacturer", "dealer"},

            {"family member", "R",
                    "uncle", "aunt", "brother", "sister", "father", "mother",
                    "son", "daughter", "grandfather", "grandmother",
                    "relative", "relatives", "cousin", "nephew", "niece",
                    "spouse", "husband", "wife", "family"},

            {"police / investigating authority", "R",
                    "police", "police officer", "investigating officer",
                    "investigator", "magistrate", "inspector", "law enforcement"},

            {"borrower / debtor", "R",
                    "borrower", "borrowers", "debtor", "debtors", "obligor"}
    };

    private static final String[] LABELS;
    private static final int[] ROLES;
    private static final String[][] PHRASES;

    static {

        int n = RAW.length;

        LABELS = new String[n];
        ROLES = new int[n];
        PHRASES = new String[n][];

        for (int g = 0; g < n; g++) {

            String[] row = RAW[g];

            LABELS[g] = row[0];
            ROLES[g] = roleFromCode(row[1]);

            int phraseCount = row.length - 2;
            String[] expanded = new String[phraseCount * 2];
            int used = 0;

            for (int p = 0; p < phraseCount; p++) {

                String phrase = row[p + 2];

                used = addUnique(expanded, used, phrase);

                // Single words also match their plural / singular form.
                if (phrase.indexOf(' ') == -1 && phrase.length() >= 4) {

                    if (phrase.endsWith("s")) {
                        used = addUnique(
                                expanded, used,
                                phrase.substring(0, phrase.length() - 1));
                    } else {
                        used = addUnique(expanded, used, phrase + "s");
                    }
                }
            }

            PHRASES[g] = new String[used];

            for (int p = 0; p < used; p++) {
                PHRASES[g][p] = expanded[p];
            }
        }
    }

    private LegalVocabulary() {
    }

    public static int size() {
        return LABELS.length;
    }

    public static String label(int group) {
        return LABELS[group];
    }

    public static int role(int group) {
        return ROLES[group];
    }

    public static String[] phrases(int group) {
        return PHRASES[group];
    }

    private static int roleFromCode(String code) {

        if (code.equals("A")) {
            return ACTION;
        }
        if (code.equals("O")) {
            return OBJECT;
        }
        if (code.equals("R")) {
            return ACTOR;
        }
        return CONTEXT;
    }

    private static int addUnique(String[] target, int used, String value) {

        for (int i = 0; i < used; i++) {
            if (target[i].equals(value)) {
                return used;
            }
        }

        target[used] = value;
        return used + 1;
    }
}

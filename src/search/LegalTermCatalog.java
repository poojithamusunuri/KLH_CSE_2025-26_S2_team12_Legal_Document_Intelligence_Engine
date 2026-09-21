package search;

import structures.SimpleList;

public class LegalTermCatalog {

    private final SimpleList<String> terms;

    public LegalTermCatalog() {

        terms = new SimpleList<>();

        // ============================================================
        // CORE LEGAL ACTS
        // ============================================================

        add("Companies Act, 2013");
        add("Indian Contract Act, 1872");
        add("Information Technology Act, 2000");
        add("Consumer Protection Act, 2019");
        add("Copyright Act, 1957");
        add("Environment Protection Act, 1986");

        // ============================================================
        // COMMON LEGAL ACTS FROM THE CORPUS
        // ============================================================

        add("Negotiable Instruments Act, 1881");
        add("Companies Act, 1956");
        add("Income-tax Act, 1961");
        add("Land Acquisition Act, 1894");
        add("Transfer of Property Act, 1882");
        add("Registration Act, 1908");
        add("Limitation Act, 1963");
        add("Indian Evidence Act, 1872");
        add("General Clauses Act, 1897");
        add("Specific Relief Act, 1963");
        add("Motor Vehicles Act, 1988");
        add("Finance Act, 1969");
        add("SEBI Act, 1992");

        // ============================================================
        // BIHAR ACTS
        // ============================================================

        add("Bihar Land Disputes Resolution Act, 2009");
        add("Bihar Land Reforms Act, 1950");
        add("Bihar Tenancy Act, 1885");
        add("Bihar Privileged Persons Homestead Tenancy Act, 1947");
        add("Bihar Bhoodan Yagna Act, 1954");
        add("Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act, 1961");
        add("Bihar Consolidation of Holdings and Prevention of Fragmentation Act, 1956");

        // ============================================================
        // OTHER EXTERNAL ACTS
        // ============================================================

        add("Central Excises and Salt Act, 1944");
        add("Bombay Sales Tax Act, 1959");
        add("Foreign Exchange Management Act, 1999");
        add("Press and Registration of Books Act, 1867");
        add("Recovery of Debts Due to Banks and Financial Institutions Act, 1993");
        add("Interest Act, 1978");
        add("Benami Transactions (Prohibition) Act, 1988");
        add("Central Sales Tax Act, 1956");
        add("Payment of Wages Act, 1936");
        add("Indian Succession Act, 1925");
        add("Defence of India Act, 1939");
        add("Government of India Act, 1935");
        add("Stamp Act, 1899");
        add("Essential Commodities Act, 1955");
        add("Factories Act, 1948");
        add("Employees' State Insurance Act, 1948");
        add("Nawab Salar Jung Bahadur (Administration of Assets) Act, 1950");
        add("Estate Duty Act, 1953");
        add("Electricity (Supply) Act, 1948");
        add("Indian Electricity Act, 1910");
        add("Usurious Loans Act, 1918");
        add("Indian Partnership Act, 1932");
        add("Indian Income-tax Act, 1922");
        add("Major Ports Act");
        add("Rent Act");
        add("Wealth-tax Act");
        add("Prevention of Corruption Act");
        add("Foreign Exchange Regulations Act");
        add("Commissions of Enquiry Act");
        add("Urban Ceiling Regulation Act");
        add("Entertainment Tax Act");
        add("Sale of Goods Act, 1893");

        // ============================================================
        // COMMON LEGAL TERMS
        // ============================================================

        add("Divorce");
        add("Marriage");
        add("Maintenance");
        add("Custody");
        add("Alimony");
        add("Adoption");
        add("Inheritance");
        add("Property");
        add("Land");
        add("Ownership");
        add("Sale");
        add("Transfer");
        add("Contract");
        add("Conspiracy");
        add("Agreement");
        add("Breach of Contract");
        add("Specific Performance");
        add("Compensation");
        add("Damages");
        add("Negligence");
        add("Fraud");
        add("Forgery");
        add("Criminal Procedure");
        add("Civil Procedure");
        add("Bail");
        add("Arrest");
        add("Assault");
        add("Murder");
        add("Attempt to Murder");
        add("Theft");
        add("Robbery");
        add("Cheating");
        add("Fraud");
        add("Forgery");
        add("Cheque");
        add("Bounce");
        add("Bounced Cheque");
        add("Dishonour");
        add("Dishonor");
        add("Kidnapping");
        add("Criminal Intimidation");
        add("Defamation");
        add("Evidence");
        add("Witness");
        add("Appeal");
        add("Petition");
        add("Judgment");
        add("Order");
        add("Jurisdiction");
        add("Limitation");
        add("Injunction");
        add("Constitutional Rights");
        add("Fundamental Rights");
        add("Right to Property");
        add("Consumer Rights");
        add("Employment");
        add("Dismissal");
        add("Salary");
        add("Tax");
        add("Income Tax");
        add("Corruption");
        add("Copyright");
        add("Trademark");
        add("Patent");
        add("Cyber Crime");
        add("Data Protection");
        add("Ragging");
        add("Anti-Ragging");
        add("Ragging in Educational Institution");
    }

    private void add(String term) {
        terms.add(term);
    }

    public SimpleList<String> getTerms() {
        return terms;
    }
}

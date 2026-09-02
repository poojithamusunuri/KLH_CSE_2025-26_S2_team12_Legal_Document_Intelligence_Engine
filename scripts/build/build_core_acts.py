from pathlib import Path

BASE_DIR = Path("data/corpus/core_acts")
BASE_DIR.mkdir(parents=True, exist_ok=True)


CORE_ACTS = [

    {
        "id": "COREACT001",
        "filename": "bharatiya_nyaya_sanhita_2023.txt",
        "title": "Bharatiya Nyaya Sanhita, 2023",
        "year": "2023",
        "domain": "Criminal Law",
        "subdomain": "Substantive Criminal Law",
        "keywords": (
            "criminal law, offences, punishment, homicide, theft, "
            "sexual offences, organized crime, criminal liability"
        ),
        "description": (
            "India's modern framework for substantive criminal law, "
            "covering offences, criminal liability and punishments."
        ),
        "concepts": [
            "General principles of criminal liability",
            "Offences against the human body",
            "Offences against women and children",
            "Offences against property",
            "Offences against the State",
            "Organized crime",
            "Terrorism-related offences",
            "Punishments and sentencing"
        ]
    },

    {
        "id": "COREACT002",
        "filename": "bharatiya_nagarik_suraksha_sanhita_2023.txt",
        "title": "Bharatiya Nagarik Suraksha Sanhita, 2023",
        "year": "2023",
        "domain": "Criminal Law",
        "subdomain": "Criminal Procedure",
        "keywords": (
            "criminal procedure, FIR, arrest, investigation, bail, "
            "trial, police, magistrate, criminal court"
        ),
        "description": (
            "India's modern framework governing criminal procedure, "
            "including investigation, arrest, bail, trial and criminal justice processes."
        ),
        "concepts": [
            "Registration of criminal information",
            "Investigation procedures",
            "Arrest and rights of accused persons",
            "Bail",
            "Jurisdiction of criminal courts",
            "Trials and criminal proceedings",
            "Appeals and revision",
            "Execution of criminal sentences"
        ]
    },

    {
        "id": "COREACT003",
        "filename": "bharatiya_sakshya_adhiniyam_2023.txt",
        "title": "Bharatiya Sakshya Adhiniyam, 2023",
        "year": "2023",
        "domain": "Procedural Law",
        "subdomain": "Law of Evidence",
        "keywords": (
            "evidence, proof, admissibility, witness, electronic records, "
            "documents, burden of proof"
        ),
        "description": (
            "India's modern legal framework governing evidence, "
            "relevance, admissibility, proof and electronic records."
        ),
        "concepts": [
            "Relevancy of facts",
            "Admissions and confessions",
            "Documentary evidence",
            "Electronic and digital records",
            "Witness testimony",
            "Burden of proof",
            "Presumptions",
            "Examination of witnesses"
        ]
    },

    {
        "id": "COREACT004",
        "filename": "information_technology_act_2000.txt",
        "title": "Information Technology Act, 2000",
        "year": "2000",
        "domain": "Technology and Digital Law",
        "subdomain": "Cyber Law",
        "keywords": (
            "cyber law, computer offence, electronic records, hacking, "
            "digital signature, cybercrime, intermediary"
        ),
        "description": (
            "A foundational Indian law governing electronic records, "
            "digital transactions, cyber offences and related matters."
        ),
        "concepts": [
            "Legal recognition of electronic records",
            "Digital and electronic signatures",
            "Cyber offences",
            "Unauthorized access",
            "Computer-related offences",
            "Intermediary liability",
            "Electronic governance",
            "Cyber security"
        ]
    },

    {
        "id": "COREACT005",
        "filename": "digital_personal_data_protection_act_2023.txt",
        "title": "Digital Personal Data Protection Act, 2023",
        "year": "2023",
        "domain": "Technology and Digital Law",
        "subdomain": "Data Protection and Privacy",
        "keywords": (
            "personal data, data protection, privacy, consent, data fiduciary, "
            "data principal, digital personal data"
        ),
        "description": (
            "India's statutory framework governing the processing of "
            "digital personal data and responsibilities relating to data protection."
        ),
        "concepts": [
            "Consent",
            "Lawful processing of personal data",
            "Rights of data principals",
            "Duties of data principals",
            "Responsibilities of data fiduciaries",
            "Children's personal data",
            "Data protection governance",
            "Penalties and enforcement"
        ]
    },

    {
        "id": "COREACT006",
        "filename": "consumer_protection_act_2019.txt",
        "title": "Consumer Protection Act, 2019",
        "year": "2019",
        "domain": "Consumer Law",
        "subdomain": "Consumer Rights and Protection",
        "keywords": (
            "consumer, consumer rights, defective goods, deficiency in service, "
            "unfair trade practice, product liability, consumer commission"
        ),
        "description": (
            "Indian legislation providing consumer rights and mechanisms "
            "for addressing consumer disputes and unfair practices."
        ),
        "concepts": [
            "Consumer rights",
            "Defective goods",
            "Deficiency in services",
            "Unfair trade practices",
            "Product liability",
            "Consumer commissions",
            "Consumer complaints",
            "E-commerce consumer protection"
        ]
    },

    {
        "id": "COREACT007",
        "filename": "indian_contract_act_1872.txt",
        "title": "Indian Contract Act, 1872",
        "year": "1872",
        "domain": "Commercial and Corporate Law",
        "subdomain": "Contract Law",
        "keywords": (
            "contract, agreement, offer, acceptance, consideration, "
            "breach, indemnity, guarantee, agency"
        ),
        "description": (
            "A foundational Indian law governing contracts and general "
            "principles relating to legally enforceable agreements."
        ),
        "concepts": [
            "Offer and acceptance",
            "Consideration",
            "Capacity to contract",
            "Free consent",
            "Void and voidable agreements",
            "Performance of contracts",
            "Breach of contract",
            "Indemnity and guarantee",
            "Agency"
        ]
    },

    {
        "id": "COREACT008",
        "filename": "companies_act_2013.txt",
        "title": "Companies Act, 2013",
        "year": "2013",
        "domain": "Commercial and Corporate Law",
        "subdomain": "Corporate Law",
        "keywords": (
            "company, corporate governance, directors, shareholders, "
            "incorporation, board, audit, company law"
        ),
        "description": (
            "India's principal statutory framework governing incorporation, "
            "management, governance and regulation of companies."
        ),
        "concepts": [
            "Incorporation of companies",
            "Corporate personality",
            "Directors and management",
            "Share capital",
            "Shareholders",
            "Corporate governance",
            "Audit and accounts",
            "Corporate compliance"
        ]
    },

    {
        "id": "COREACT009",
        "filename": "environment_protection_act_1986.txt",
        "title": "Environment (Protection) Act, 1986",
        "year": "1986",
        "domain": "Environmental Law",
        "subdomain": "Environmental Protection",
        "keywords": (
            "environment, pollution, environmental protection, hazardous substances, "
            "environmental standards, government regulation"
        ),
        "description": (
            "A central Indian law providing a framework for environmental "
            "protection and regulation."
        ),
        "concepts": [
            "Protection of the environment",
            "Environmental standards",
            "Prevention of pollution",
            "Hazardous substances",
            "Government regulatory powers",
            "Environmental compliance",
            "Directions and enforcement"
        ]
    },

    {
        "id": "COREACT010",
        "filename": "copyright_act_1957.txt",
        "title": "Copyright Act, 1957",
        "year": "1957",
        "domain": "Intellectual Property Law",
        "subdomain": "Copyright Law",
        "keywords": (
            "copyright, author, literary work, artistic work, infringement, "
            "license, intellectual property"
        ),
        "description": (
            "Indian legislation governing copyright protection, ownership, "
            "rights of authors and infringement."
        ),
        "concepts": [
            "Copyright ownership",
            "Protected works",
            "Rights of authors",
            "Assignment and licensing",
            "Copyright infringement",
            "Exceptions and limitations",
            "Civil remedies",
            "Criminal consequences"
        ]
    }

]


def create_document(act):

    concepts = "\n".join(
        f"- {concept}"
        for concept in act["concepts"]
    )

    content = f"""DOCUMENT_ID: {act["id"]}

TITLE: {act["title"]}

DOCUMENT_TYPE: Core Legal Act Knowledge

JURISDICTION: India

AUTHORITY: Parliament of India

YEAR: {act["year"]}

DOMAIN: {act["domain"]}

SUBDOMAIN: {act["subdomain"]}

SOURCE_TYPE: Curated Legal Knowledge Summary

SEARCH_KEYWORDS: {act["keywords"]}

DESCRIPTION:

{act["description"]}

CORE LEGAL CONCEPTS:

{concepts}

IMPORTANT NOTE:

This document is a structured legal knowledge summary created for
information retrieval and educational purposes. It is not a substitute
for the complete statutory text or professional legal advice.

CONTENT:

This document represents the major legal areas, concepts and topics
covered by the legislation identified above. Retrieval systems may use
this structured information to identify potentially relevant legal
materials for a user's query.

"""

    path = BASE_DIR / act["filename"]

    path.write_text(
        content,
        encoding="utf-8"
    )

    print(
        f"✓ Created: {act['filename']} "
        f"→ {act['domain']}"
    )


print("\n" + "=" * 60)
print("BUILDING CORE INDIAN LEGAL ACTS CORPUS")
print("=" * 60 + "\n")

for act in CORE_ACTS:
    create_document(act)

print("\n" + "=" * 60)
print("CORE ACTS CORPUS COMPLETE")
print("=" * 60)
print(f"Total core Acts created: {len(CORE_ACTS)}")
print(f"Location: {BASE_DIR}")

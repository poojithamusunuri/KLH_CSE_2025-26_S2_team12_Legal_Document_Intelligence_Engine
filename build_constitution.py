from pathlib import Path

BASE_DIR = Path("data/corpus/constitution")
BASE_DIR.mkdir(parents=True, exist_ok=True)

documents = [
    {
        "id": "CONST003",
        "filename": "constitution_article_13.txt",
        "title": "Constitution of India - Article 13: Laws inconsistent with Fundamental Rights",
        "domain": "Constitutional Law",
        "keywords": "fundamental rights, judicial review, unconstitutional laws, State action",
        "content": """Article 13: Laws inconsistent with or in derogation of the fundamental rights.

(1) All laws in force in the territory of India immediately before the commencement of this Constitution, in so far as they are inconsistent with the provisions of this Part, shall, to the extent of such inconsistency, be void.

(2) The State shall not make any law which takes away or abridges the rights conferred by this Part and any law made in contravention of this clause shall, to the extent of the contravention, be void."""
    },
    {
        "id": "CONST004",
        "filename": "constitution_article_19.txt",
        "title": "Constitution of India - Article 19: Protection of Certain Rights Regarding Freedom",
        "domain": "Constitutional Law",
        "keywords": "freedom of speech, expression, assembly, association, movement, profession",
        "content": """Article 19: Protection of certain rights regarding freedom of speech, etc.

All citizens shall have the right—

(a) to freedom of speech and expression;
(b) to assemble peaceably and without arms;
(c) to form associations or unions;
(d) to move freely throughout the territory of India;
(e) to reside and settle in any part of the territory of India; and
(g) to practise any profession, or to carry on any occupation, trade or business.

These freedoms are subject to the constitutional restrictions specified in Article 19."""
    },
    {
        "id": "CONST005",
        "filename": "constitution_article_21.txt",
        "title": "Constitution of India - Article 21: Protection of Life and Personal Liberty",
        "domain": "Constitutional Law",
        "keywords": "life, personal liberty, procedure established by law, fundamental rights",
        "content": """Article 21: Protection of life and personal liberty.

No person shall be deprived of his life or personal liberty except according to procedure established by law."""
    },
    {
        "id": "CONST006",
        "filename": "constitution_article_21a.txt",
        "title": "Constitution of India - Article 21A: Right to Education",
        "domain": "Constitutional Law",
        "keywords": "education, children, fundamental right, free education",
        "content": """Article 21A: Right to education.

The State shall provide free and compulsory education to all children of the age of six to fourteen years in such manner as the State may, by law, determine."""
    },
    {
        "id": "CONST007",
        "filename": "constitution_article_22.txt",
        "title": "Constitution of India - Article 22: Protection Against Arrest and Detention",
        "domain": "Constitutional Law",
        "keywords": "arrest, detention, lawyer, magistrate, preventive detention",
        "content": """Article 22 provides constitutional safeguards against arrest and detention. A person arrested must be informed of the grounds of arrest and must be allowed to consult and be defended by a legal practitioner of choice. The arrested person must generally be produced before the nearest magistrate within twenty-four hours, excluding the time necessary for the journey."""
    },
    {
        "id": "CONST008",
        "filename": "constitution_article_32.txt",
        "title": "Constitution of India - Article 32: Constitutional Remedies",
        "domain": "Constitutional Law",
        "keywords": "Supreme Court, writs, habeas corpus, mandamus, certiorari, fundamental rights",
        "content": """Article 32: Remedies for enforcement of rights conferred by this Part.

The right to move the Supreme Court by appropriate proceedings for the enforcement of the rights conferred by Part III is guaranteed.

The Supreme Court has power to issue directions, orders or writs, including writs in the nature of habeas corpus, mandamus, prohibition, quo warranto and certiorari, for enforcement of fundamental rights."""
    },
    {
        "id": "CONST009",
        "filename": "constitution_article_39a.txt",
        "title": "Constitution of India - Article 39A: Equal Justice and Free Legal Aid",
        "domain": "Constitutional Law",
        "keywords": "equal justice, free legal aid, access to justice, legal assistance",
        "content": """Article 39A directs the State to secure that the operation of the legal system promotes justice on a basis of equal opportunity and to provide free legal aid, by suitable legislation, schemes or other means, so that opportunities for securing justice are not denied because of economic or other disabilities."""
    },
    {
        "id": "CONST010",
        "filename": "constitution_article_136.txt",
        "title": "Constitution of India - Article 136: Special Leave to Appeal",
        "domain": "Constitutional Law",
        "keywords": "Supreme Court, special leave petition, appeal, judicial discretion",
        "content": """Article 136 gives the Supreme Court discretionary power to grant special leave to appeal from judgments, decrees, determinations, sentences or orders made by courts or tribunals in India, subject to constitutional limitations."""
    },
    {
        "id": "CONST011",
        "filename": "constitution_article_141.txt",
        "title": "Constitution of India - Article 141: Law Declared by Supreme Court",
        "domain": "Constitutional Law",
        "keywords": "Supreme Court, precedent, binding law, stare decisis",
        "content": """Article 141: Law declared by Supreme Court to be binding on all courts.

The law declared by the Supreme Court shall be binding on all courts within the territory of India."""
    },
    {
        "id": "CONST012",
        "filename": "constitution_article_226.txt",
        "title": "Constitution of India - Article 226: High Court Writ Jurisdiction",
        "domain": "Constitutional Law",
        "keywords": "High Court, writs, habeas corpus, mandamus, prohibition, quo warranto, certiorari",
        "content": """Article 226 empowers every High Court to issue directions, orders or writs, including writs in the nature of habeas corpus, mandamus, prohibition, quo warranto and certiorari, for enforcement of fundamental rights and for other purposes."""
    }
]

for doc in documents:
    path = BASE_DIR / doc["filename"]

    if path.exists():
        print(f"Skipping existing: {doc['filename']}")
        continue

    text = f"""DOCUMENT_ID: {doc['id']}

TITLE: {doc['title']}

DOCUMENT_TYPE: Constitution

DOMAIN: {doc['domain']}

JURISDICTION: India

YEAR: 1950

AUTHORITY: Constituent Assembly of India

SOURCE: Constitution of India

SOURCE_TYPE: Primary Legal Text

KEYWORDS: {doc['keywords']}

CONTENT:

{doc['content']}
"""

    path.write_text(text, encoding="utf-8")
    print(f"Created: {doc['filename']}")

print("\nConstitution corpus update complete.")
print(f"Total files: {len(list(BASE_DIR.glob('*.txt')))}")

from pathlib import Path

BASE_DIR = Path("data/corpus/legal_principles")

BASE_DIR.mkdir(parents=True, exist_ok=True)

# Remove old generated principle files
for file in BASE_DIR.glob("*.txt"):
    file.unlink()


legal_principles = [

    {
        "title": "Rule of Law",
        "category": "Constitutional Law",
        "description": """
The Rule of Law is the principle that every person and authority,
including the government, is subject to law. Government power must be
exercised according to established legal rules rather than arbitrary will.
""",
        "keywords": "rule of law, equality, legality, government accountability, arbitrary power"
    },

    {
        "title": "Natural Justice",
        "category": "Administrative Law",
        "description": """
Natural Justice refers to fundamental procedural fairness in decision-making.
It generally requires impartial decision-makers and a fair opportunity for
affected persons to present their case.
""",
        "keywords": "natural justice, fairness, hearing, impartiality, administrative law"
    },

    {
        "title": "Audi Alteram Partem",
        "category": "Natural Justice",
        "description": """
Audi Alteram Partem means 'hear the other side'. A person affected by a
decision should ordinarily be given a fair opportunity to know the case
against them and respond before an adverse decision is made.
""",
        "keywords": "audi alteram partem, right to hearing, fair hearing, natural justice"
    },

    {
        "title": "Nemo Judex in Causa Sua",
        "category": "Natural Justice",
        "description": """
Nemo Judex in Causa Sua means that no person should be a judge in their
own cause. The principle requires impartial decision-making and seeks to
prevent bias or a reasonable apprehension of bias.
""",
        "keywords": "nemo judex, bias, impartiality, conflict of interest"
    },

    {
        "title": "Res Judicata",
        "category": "Civil Procedure",
        "description": """
Res Judicata prevents the same dispute between the same parties from being
litigated repeatedly once it has been finally decided by a competent court,
subject to the applicable legal requirements.
""",
        "keywords": "res judicata, final judgment, civil procedure, previous decision"
    },

    {
        "title": "Stare Decisis",
        "category": "Judicial Precedent",
        "description": """
Stare Decisis is the principle of following binding judicial precedent.
It promotes consistency, predictability, and stability in the legal system.
The binding force of precedent depends on the applicable court hierarchy.
""",
        "keywords": "stare decisis, precedent, judicial decisions, binding authority"
    },

    {
        "title": "Judicial Review",
        "category": "Constitutional Law",
        "description": """
Judicial Review is the power of courts to examine the legality and
constitutional validity of legislative and executive actions within the
scope permitted by the Constitution and applicable law.
""",
        "keywords": "judicial review, constitutionality, executive action, legislation"
    },

    {
        "title": "Equality Before Law",
        "category": "Constitutional Law",
        "description": """
Equality Before Law means that persons are subject to the ordinary law of
the land and are entitled to equal legal protection, subject to legitimate
constitutional and legal classifications.
""",
        "keywords": "equality, equal protection, constitution, discrimination"
    },

    {
        "title": "Basic Structure Doctrine",
        "category": "Indian Constitutional Law",
        "description": """
The Basic Structure Doctrine is a principle of Indian constitutional law
under which Parliament's power to amend the Constitution does not extend
to destroying or damaging its basic structure.
""",
        "keywords": "basic structure, constitution amendment, parliament, constitutional law"
    },

    {
        "title": "Doctrine of Proportionality",
        "category": "Constitutional and Administrative Law",
        "description": """
The Doctrine of Proportionality examines whether a restriction or state
action has a legitimate objective and whether the means adopted are
appropriately connected and proportionate to that objective.
""",
        "keywords": "proportionality, reasonable restriction, constitutional rights"
    },

    {
        "title": "Doctrine of Legitimate Expectation",
        "category": "Administrative Law",
        "description": """
Legitimate Expectation may arise when a public authority, through a
consistent practice or representation, creates an expectation that a
particular procedure or benefit will be considered, subject to law and
the limits of the doctrine.
""",
        "keywords": "legitimate expectation, public authority, administrative law"
    },

    {
        "title": "Doctrine of Severability",
        "category": "Constitutional Law",
        "description": """
The Doctrine of Severability allows the valid parts of a law to remain
operative when an unconstitutional part can be separated without defeating
the legislative purpose, depending on the facts and statutory structure.
""",
        "keywords": "severability, unconstitutional law, valid provisions"
    },

    {
        "title": "Doctrine of Eclipse",
        "category": "Indian Constitutional Law",
        "description": """
The Doctrine of Eclipse is associated with Indian constitutional law and
explains how certain pre-Constitution laws inconsistent with fundamental
rights may become inoperative to the extent of inconsistency rather than
necessarily being void for all purposes.
""",
        "keywords": "doctrine of eclipse, fundamental rights, pre constitution law"
    },

    {
        "title": "Burden of Proof",
        "category": "Evidence Law",
        "description": """
The Burden of Proof refers to the legal responsibility of establishing
facts or assertions before a court. The applicable burden depends on the
nature of the proceeding and the relevant law.
""",
        "keywords": "burden of proof, evidence, legal responsibility, facts"
    },

    {
        "title": "Presumption of Innocence",
        "category": "Criminal Law",
        "description": """
The Presumption of Innocence is a fundamental principle of criminal
justice under which an accused person is generally treated as innocent
unless guilt is established according to the applicable legal standard.
""",
        "keywords": "presumption of innocence, criminal law, accused, guilt"
    },

    {
        "title": "Mens Rea",
        "category": "Criminal Law",
        "description": """
Mens Rea refers to the mental element required for many criminal offences,
such as intention, knowledge, recklessness, or another legally specified
state of mind.
""",
        "keywords": "mens rea, criminal intent, knowledge, mental element"
    },

    {
        "title": "Actus Reus",
        "category": "Criminal Law",
        "description": """
Actus Reus refers to the external element of an offence, including an act,
omission, or legally prohibited conduct where the law recognizes such
conduct as part of the offence.
""",
        "keywords": "actus reus, criminal act, conduct, omission"
    },

    {
        "title": "Double Jeopardy",
        "category": "Criminal Law",
        "description": """
The principle against Double Jeopardy protects against being prosecuted
or punished more than once for the same offence in circumstances covered
by constitutional or statutory protections.
""",
        "keywords": "double jeopardy, prosecution, punishment, same offence"
    },

    {
        "title": "Habeas Corpus",
        "category": "Constitutional Remedies",
        "description": """
Habeas Corpus is a legal remedy used to challenge unlawful detention by
requiring the detaining authority to justify the legal basis for a
person's detention before a competent court.
""",
        "keywords": "habeas corpus, unlawful detention, liberty, constitutional remedy"
    },

    {
        "title": "Mandamus",
        "category": "Constitutional Remedies",
        "description": """
Mandamus is a judicial remedy that may direct a public authority or other
appropriate body to perform a legal duty when the requirements for such
relief are satisfied.
""",
        "keywords": "mandamus, public duty, writ, constitutional remedy"
    },

    {
        "title": "Certiorari",
        "category": "Constitutional Remedies",
        "description": """
Certiorari is a judicial remedy through which a superior court may review
and, where legally justified, quash an order or decision of a subordinate
court, tribunal, or authority.
""",
        "keywords": "certiorari, judicial review, tribunal, court order"
    },

    {
        "title": "Prohibition",
        "category": "Constitutional Remedies",
        "description": """
A writ of Prohibition may be issued to prevent a subordinate court,
tribunal, or authority from continuing proceedings beyond its jurisdiction
or contrary to applicable legal limits.
""",
        "keywords": "prohibition, writ, jurisdiction, tribunal"
    },

    {
        "title": "Quo Warranto",
        "category": "Constitutional Remedies",
        "description": """
Quo Warranto is a remedy used to question the legal authority by which a
person holds a public office, where the legal requirements for such
proceedings are met.
""",
        "keywords": "quo warranto, public office, legal authority, writ"
    },

    {
        "title": "Public Interest Litigation",
        "category": "Indian Constitutional Law",
        "description": """
Public Interest Litigation is a form of constitutional litigation in India
that may allow courts to consider matters involving public interest,
including situations where affected persons may face barriers to accessing
justice.
""",
        "keywords": "PIL, public interest litigation, access to justice, constitutional law"
    },

    {
        "title": "Reasonable Classification",
        "category": "Constitutional Law",
        "description": """
The principle of Reasonable Classification recognizes that equality does
not necessarily require identical treatment in all circumstances.
Classification must satisfy constitutional requirements and cannot be
arbitrary.
""",
        "keywords": "reasonable classification, equality, article 14, constitutional law"
    },

    {
        "title": "Principle of Due Process and Fair Procedure",
        "category": "Constitutional Law",
        "description": """
Fair procedure requires that exercises of legal power affecting important
rights or interests follow procedures that are lawful, fair, and consistent
with applicable constitutional and statutory protections.
""",
        "keywords": "fair procedure, due process, liberty, constitutional rights"
    },

    {
        "title": "Principle of Judicial Independence",
        "category": "Constitutional Law",
        "description": """
Judicial Independence means that courts and judges should be able to decide
cases independently, according to law and the Constitution, without improper
external influence or pressure.
""",
        "keywords": "judicial independence, judiciary, courts, constitution"
    },

    {
        "title": "Doctrine of Ultra Vires",
        "category": "Administrative Law",
        "description": """
The Doctrine of Ultra Vires means that an authority must act within the
powers granted to it by law. Actions taken beyond lawful authority may be
subject to judicial challenge.
""",
        "keywords": "ultra vires, administrative authority, legal powers"
    },

    {
        "title": "Principle Against Arbitrary State Action",
        "category": "Constitutional Law",
        "description": """
State action must be exercised according to law and constitutional
principles. Arbitrary, irrational, or unfair exercises of public power
may be subject to judicial scrutiny depending on the circumstances.
""",
        "keywords": "arbitrary action, state action, fairness, constitutional law"
    },

    {
        "title": "Principle of Access to Justice",
        "category": "Legal Rights",
        "description": """
Access to Justice refers to the ability of individuals to seek and obtain
legal remedies through fair and effective legal institutions and procedures.
It is an important element of the effective protection of legal rights.
""",
        "keywords": "access to justice, legal remedy, courts, legal rights"
    }
]


print("Generating Legal Principles corpus...\n")

for index, principle in enumerate(legal_principles, start=1):

    document_id = f"PRINCIPLE{index:03d}"

    filename = f"legal_principle_{index:03d}.txt"

    filepath = BASE_DIR / filename

    content = f"""DOCUMENT_ID: {document_id}

TITLE: {principle["title"]}

DOCUMENT_TYPE: Legal Principle

CATEGORY: {principle["category"]}

JURISDICTION: India / General Legal Principle

SOURCE: Curated Legal Knowledge Base

KEYWORDS:
{principle["keywords"]}

DESCRIPTION:
{principle["description"].strip()}

"""

    filepath.write_text(content, encoding="utf-8")

    print(f"[{index}/{len(legal_principles)}] Saved: {filename}")
    print(f"        {principle['title']}")


print("\n========================================")
print("LEGAL PRINCIPLES CORPUS COMPLETE")
print("========================================")
print(f"Total principles created: {len(legal_principles)}")
print(f"Location: {BASE_DIR}")

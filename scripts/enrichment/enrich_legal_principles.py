from pathlib import Path
import re

BASE_DIR = Path("data/corpus/legal_principles")


def classify(title, content):
    text = (title + " " + content).lower()

    rules = [
        (
            "Constitutional Law",
            "Constitutional Principles",
            ["basic structure", "judicial review", "constitutional"]
        ),
        (
            "Criminal Law",
            "Criminal Liability",
            ["mens rea", "actus reus", "criminal liability", "intention"]
        ),
        (
            "Civil Law",
            "Civil Liability and Remedies",
            ["negligence", "damages", "tort", "liability"]
        ),
        (
            "Administrative Law",
            "Administrative Justice",
            ["natural justice", "ultra vires", "administrative"]
        ),
        (
            "Procedural Law",
            "Judicial Procedure",
            ["res judicata", "precedent", "stare decisis", "burden of proof"]
        ),
        (
            "Human Rights Law",
            "Rights and Liberties",
            ["human rights", "liberty", "equality", "dignity"]
        ),
        (
            "Contract Law",
            "Contractual Principles",
            ["contract", "agreement", "consideration", "offer", "acceptance"]
        )
    ]

    for domain, subdomain, keywords in rules:
        if any(keyword in text for keyword in keywords):
            return domain, subdomain

    return "General Jurisprudence", "General Legal Principles"


def make_keywords(title, domain, subdomain):
    words = re.findall(r"[A-Za-z]{4,}", title.lower())

    stop_words = {
        "principle", "legal", "law", "doctrine",
        "the", "and", "for", "with"
    }

    keywords = []

    for word in words:
        if word not in stop_words and word not in keywords:
            keywords.append(word)

    keywords.extend([domain.lower(), subdomain.lower()])

    return ", ".join(keywords[:10])


count = 0

for filepath in sorted(BASE_DIR.glob("*.txt")):

    text = filepath.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    title_match = re.search(r"TITLE:\s*(.*)", text)

    if title_match:
        title = title_match.group(1).strip()
    else:
        title = filepath.stem.replace("_", " ")

    domain, subdomain = classify(title, text)
    keywords = make_keywords(title, domain, subdomain)

    # Remove previous enrichment metadata if the script is run again
    text = re.sub(
        r"\nDOMAIN:.*?\n\nSUBDOMAIN:.*?\n\nSOURCE_TYPE:.*?\n\nKEYWORDS:.*?\n",
        "\n",
        text,
        flags=re.DOTALL
    )

    metadata = f"""

DOMAIN: {domain}

SUBDOMAIN: {subdomain}

SOURCE_TYPE: Curated Legal Knowledge Summary

KEYWORDS: {keywords}
"""

    if "CONTENT:" in text:
        text = text.replace(
            "CONTENT:",
            metadata + "\nCONTENT:",
            1
        )
    else:
        text += metadata

    filepath.write_text(
        text,
        encoding="utf-8"
    )

    count += 1
    print(f"[{count}] {filepath.name} -> {domain}")

print("\n========================================")
print("LEGAL PRINCIPLES METADATA COMPLETE")
print("========================================")
print(f"Files updated: {count}")

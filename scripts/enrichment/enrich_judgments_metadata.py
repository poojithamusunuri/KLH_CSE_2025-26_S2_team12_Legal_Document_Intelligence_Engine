from pathlib import Path
import re

BASE_DIR = Path("data/corpus/judgments")


def classify_case(case_type, title, content):
    text = (
        case_type + " " + title + " " + content[:5000]
    ).lower()

    rules = [
        (
            "Property and Land Law",
            "Land and Property Disputes",
            ["land&property", "land", "property", "title dispute", "land reforms"]
        ),
        (
            "Tax and Revenue Law",
            "Income Tax and Revenue",
            ["tax", "income-tax", "income tax", "revenue"]
        ),
        (
            "Commercial and Corporate Law",
            "Business and Corporate Disputes",
            ["company", "companies", "commercial", "corporate", "business"]
        ),
        (
            "Labour and Employment Law",
            "Employment and Industrial Relations",
            ["labour", "labor", "employment", "industrial dispute", "workman"]
        ),
        (
            "Criminal Law",
            "Criminal Justice",
            ["criminal", "murder", "offence", "offense", "bail", "conviction"]
        ),
        (
            "Constitutional Law",
            "Constitutional Remedies",
            ["article 226", "article 32", "constitutional validity", "constitution"]
        ),
        (
            "Family Law",
            "Family and Personal Law",
            ["divorce", "marriage", "maintenance", "custody", "matrimonial"]
        )
    ]

    for domain, subdomain, keywords in rules:
        if any(keyword in text for keyword in keywords):
            return domain, subdomain

    return "Civil and Administrative Law", "General Civil Litigation"


def make_keywords(case_type, title, domain, subdomain):
    words = re.findall(
        r"[A-Za-z]{4,}",
        (case_type + " " + title).lower()
    )

    stop_words = {
        "the", "and", "with", "from", "this",
        "that", "versus", "state", "india",
        "another", "others", "ors", "anr"
    }

    keywords = []

    for word in words:
        if word not in stop_words and word not in keywords:
            keywords.append(word)

    keywords.append(case_type.lower())
    keywords.append(domain.lower())

    return ", ".join(keywords[:12])


count = 0

for filepath in sorted(BASE_DIR.glob("*.txt")):

    text = filepath.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    title_match = re.search(
        r"TITLE:\s*(.*)",
        text
    )

    case_type_match = re.search(
        r"CASE_TYPE:\s*(.*)",
        text
    )

    if not title_match:
        print(f"Skipping {filepath.name}: TITLE missing")
        continue

    title = title_match.group(1).strip()

    case_type = (
        case_type_match.group(1).strip()
        if case_type_match
        else "Unknown"
    )

    domain, subdomain = classify_case(
        case_type,
        title,
        text
    )

    keywords = make_keywords(
        case_type,
        title,
        domain,
        subdomain
    )

    # Remove previous enrichment if script is run again
    text = re.sub(
        r"\nDOMAIN:.*?\n\nSUBDOMAIN:.*?\n\nSOURCE_TYPE:.*?\n\nSEARCH_KEYWORDS:.*?\n",
        "\n",
        text,
        flags=re.DOTALL
    )

    metadata = f"""

DOMAIN: {domain}

SUBDOMAIN: {subdomain}

SOURCE_TYPE: Judicial Decision

SEARCH_KEYWORDS: {keywords}
"""

    text = text.replace(
        "CONTENT:",
        metadata + "\nCONTENT:",
        1
    )

    filepath.write_text(
        text,
        encoding="utf-8"
    )

    count += 1

    print(
        f"[{count}] {filepath.name} "
        f"-> {case_type} -> {domain}"
    )

print("\n========================================")
print("JUDGMENT METADATA ENRICHMENT COMPLETE")
print("========================================")
print(f"Files updated: {count}")

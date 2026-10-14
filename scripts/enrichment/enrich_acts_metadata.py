from pathlib import Path
import re

ACTS_DIR = Path("data/corpus/acts")

def classify(title, content):
    text = (title + " " + content).lower()

    rules = [
        (
            "Criminal Law",
            "Criminal Offences and Procedure",
            ["criminal", "penal", "offence", "offense", "prison", "police", "investigation"]
        ),
        (
            "Labour and Employment Law",
            "Employment Regulation",
            ["labour", "labor", "employee", "employment", "wages", "worker", "workmen"]
        ),
        (
            "Tax and Revenue Law",
            "Taxation",
            ["tax", "duty", "revenue", "customs", "excise", "gst"]
        ),
        (
            "Commercial and Corporate Law",
            "Business Regulation",
            ["company", "companies", "business", "commercial", "corporation"]
        ),
        (
            "Environmental Law",
            "Environmental Regulation",
            ["environment", "pollution", "forest", "wildlife", "water conservation"]
        ),
        (
            "Agricultural Law",
            "Agricultural Regulation",
            ["agriculture", "agricultural", "farmer", "livestock", "crop"]
        ),
        (
            "Property and Land Law",
            "Land and Property Regulation",
            ["land", "property", "tenancy", "lease", "estate"]
        ),
        (
            "Administrative Law",
            "Government Regulation",
            ["government", "authority", "regulation", "administration"]
        )
    ]

    for domain, subdomain, keywords in rules:
        if any(keyword in text for keyword in keywords):
            return domain, subdomain

    return "General Legal Regulation", "Legislative Regulation"


def make_keywords(title, domain, subdomain):
    words = re.findall(r"[A-Za-z]{4,}", title.lower())

    stop_words = {
        "this", "that", "with", "from", "into", "under",
        "amendment", "act", "acts", "india", "indian",
        "government", "state"
    }

    keywords = []

    for word in words:
        if word not in stop_words and word not in keywords:
            keywords.append(word)

    keywords.extend([
        domain.lower(),
        subdomain.lower()
    ])

    return ", ".join(keywords[:10])


count = 0

for filepath in sorted(ACTS_DIR.glob("*.txt")):

    text = filepath.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    if "DOMAIN:" in text:
        print(f"Skipping already enriched: {filepath.name}")
        continue

    title_match = re.search(
        r"TITLE:\s*(.*)",
        text
    )

    title = (
        title_match.group(1).strip()
        if title_match
        else filepath.stem
    )

    content = text

    domain, subdomain = classify(title, content)

    keywords = make_keywords(
        title,
        domain,
        subdomain
    )

    metadata = f"""

DOMAIN: {domain}

SUBDOMAIN: {subdomain}

SOURCE_TYPE: Legislative Legal Text

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

    print(
        f"[{count}] {filepath.name} -> {domain}"
    )

print("\n================================")
print("ACT METADATA ENRICHMENT COMPLETE")
print("================================")
print(f"Files updated: {count}")

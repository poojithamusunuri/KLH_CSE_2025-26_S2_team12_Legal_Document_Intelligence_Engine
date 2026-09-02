from pathlib import Path
import re

ACTS_DIR = Path("data/corpus/acts")

def classify(title):
    title_lower = title.lower()

    rules = [
        (
            "Technology and Digital Law",
            "Digital Identity and Data Governance",
            ["aadhaar", "digital", "information technology", "data protection"]
        ),
        (
            "Criminal Law",
            "Criminal Offences and Procedure",
            ["penal", "criminal procedure", "narcotic", "offences", "prevention of corruption"]
        ),
        (
            "Labour and Employment Law",
            "Employment Regulation",
            ["labour", "labor", "wages", "employee", "employment", "workmen", "workers"]
        ),
        (
            "Tax and Revenue Law",
            "Taxation and Public Revenue",
            ["tax", "income tax", "goods and services tax", "customs", "excise", "revenue"]
        ),
        (
            "Commercial and Corporate Law",
            "Business and Corporate Regulation",
            ["companies", "company", "partnership", "competition", "banking", "insurance"]
        ),
        (
            "Environmental Law",
            "Environmental Protection",
            ["environment", "forest", "wildlife", "water pollution", "air pollution"]
        ),
        (
            "Agricultural Law",
            "Agriculture and Rural Regulation",
            ["agriculture", "agricultural", "farmer", "livestock", "seed"]
        ),
        (
            "Property and Land Law",
            "Land and Property Regulation",
            ["land", "property", "real estate", "tenancy", "lease"]
        ),
        (
            "Family Law",
            "Marriage and Family Relations",
            ["marriage", "divorce", "adoption", "maintenance"]
        )
    ]

    for domain, subdomain, keywords in rules:
        if any(keyword in title_lower for keyword in keywords):
            return domain, subdomain

    return "Administrative and Regulatory Law", "Government Regulation"


def make_keywords(title, domain, subdomain):
    words = re.findall(r"[A-Za-z]{4,}", title.lower())

    stop_words = {
        "this", "that", "with", "from", "into", "under",
        "amendment", "act", "acts", "india", "indian",
        "government", "state", "other"
    }

    keywords = []

    for word in words:
        if word not in stop_words and word not in keywords:
            keywords.append(word)

    keywords.append(domain.lower())
    keywords.append(subdomain.lower())

    return ", ".join(keywords[:10])


count = 0

for filepath in sorted(ACTS_DIR.glob("*.txt")):

    text = filepath.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    title_match = re.search(r"TITLE:\s*(.*)", text)

    if not title_match:
        print(f"Skipping {filepath.name}: no title found")
        continue

    title = title_match.group(1).strip()

    domain, subdomain = classify(title)

    keywords = make_keywords(title, domain, subdomain)

    # Remove old enrichment metadata
    text = re.sub(
        r"\nDOMAIN:.*?\n\nSUBDOMAIN:.*?\n\nSOURCE_TYPE:.*?\n\nKEYWORDS:.*?\n",
        "\n",
        text,
        flags=re.DOTALL
    )

    metadata = f"""

DOMAIN: {domain}

SUBDOMAIN: {subdomain}

SOURCE_TYPE: Legislative Legal Text

KEYWORDS: {keywords}
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

    print(f"[{count}] {filepath.name} -> {domain}")

print("\n================================")
print("ACT METADATA CORRECTION COMPLETE")
print("================================")
print(f"Files updated: {count}")

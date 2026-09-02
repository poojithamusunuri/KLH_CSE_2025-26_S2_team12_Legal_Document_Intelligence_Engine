import re
import json
from pathlib import Path
from collections import defaultdict, Counter


INPUT_FILE = Path("results/verified_act_candidates.txt")
OUTPUT_DIR = Path("results")

OUTPUT_JSON = OUTPUT_DIR / "canonical_acts.json"
ALIASES_JSON = OUTPUT_DIR / "act_aliases.json"
REJECTED_FILE = OUTPUT_DIR / "rejected_act_candidates.txt"
INVENTORY_FILE = OUTPUT_DIR / "canonical_act_inventory.txt"


# -------------------------------------------------------------------
# Known canonical Acts appearing in the current corpus
# -------------------------------------------------------------------

CANONICAL_ACTS = {
    "negotiable instruments act": "Negotiable Instruments Act",
    "negotiable instrument act": "Negotiable Instruments Act",
    "income-tax act": "Income-tax Act",
    "indian income-tax act": "Indian Income-tax Act",
    "companies act": "Companies Act",
    "indian companies act": "Indian Companies Act",
    "land acquisition act": "Land Acquisition Act",
    "transfer of property act": "Transfer of Property Act",
    "registration act": "Registration Act",
    "indian registration act": "Indian Registration Act",
    "specific relief act": "Specific Relief Act",
    "general clauses act": "General Clauses Act",
    "andhra pradesh general clauses act": "Andhra Pradesh General Clauses Act",
    "indian contract act": "Indian Contract Act",
    "indian evidence act": "Indian Evidence Act",
    "evidence act": "Evidence Act",
    "limitation act": "Limitation Act",
    "finance act": "Finance Act",
    "central excises and salt act": "Central Excises and Salt Act",
    "industrial disputes act": "Industrial Disputes Act",
    "motor vehicles act": "Motor Vehicles Act",
    "sebi act": "SEBI Act",
    "securities and exchange board of india act": "Securities and Exchange Board of India Act",
    "state financial corporation act": "State Financial Corporation Act",
    "state financial corporations act": "State Financial Corporations Act",
    "recovery of debts due to banks and financial institutions act":
        "Recovery of Debts Due to Banks and Financial Institutions Act",
    "indian succession act": "Indian Succession Act",
    "indian trusts act": "Indian Trusts Act",
    "indian stamp act": "Indian Stamp Act",
    "sale of goods act": "Sale of Goods Act",
    "fatal accidents act": "Fatal Accidents Act",
    "foreign exchange management act": "Foreign Exchange Management Act",
    "commissions of inquiry act": "Commissions of Inquiry Act",
    "urban land (ceiling and regulation) act":
        "Urban Land (Ceiling and Regulation) Act",
    "usurious loans act": "Usurious Loans Act",
    "press and registration of books act":
        "Press and Registration of Books Act",

    # Bihar
    "bihar tenancy act": "Bihar Tenancy Act",
    "bihar land reforms act": "Bihar Land Reforms Act",
    "bihar bhoodan yagna act": "Bihar Bhoodan Yagna Act",
    "bihar privileged persons homestead tenancy act":
        "Bihar Privileged Persons Homestead Tenancy Act",
    "bihar land disputes resolution act":
        "Bihar Land Disputes Resolution Act",
    "bihar land reforms (fixation of ceiling and acquisition of surplus land) act":
        "Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act",
    "bihar consolidation of holdings and prevention of fragmentation act":
        "Bihar Consolidation of Holdings and Prevention of Fragmentation Act",

    # Andhra Pradesh
    "andhra pradesh co-operative societies act":
        "Andhra Pradesh Co-operative Societies Act",
    "andhra pradesh (telangana area) tenancy and agricultural lands act":
        "Andhra Pradesh (Telangana Area) Tenancy and Agricultural Lands Act",
    "andhra pradesh ceiling on agricultural holdings act":
        "Andhra Pradesh Ceiling on Agricultural Holdings Act",
    "andhra pradesh (andhra area) co-operative societies act":
        "Andhra Pradesh (Andhra Area) Co-operative Societies Act",
    "andhra pradesh (andhra area) shops and establishments act":
        "Andhra Pradesh (Andhra Area) Shops and Establishments Act",

    # Other Acts appearing in corpus
    "delhi municipal corporation act": "Delhi Municipal Corporation Act",
    "textile undertakings (taking over of management) act":
        "Textile Undertakings (Taking Over of Management) Act",
    "bombay non-agriculturists loans act":
        "Bombay Non-Agriculturists Loans Act",
    "punjab urban immovable property tax act":
        "Punjab Urban Immovable Property Tax Act",
    "orissa sales tax act": "Orissa Sales Tax Act",
    "central sales tax act": "Central Sales Tax Act",
    "bombay sales tax act": "Bombay Sales Tax Act",
    "hyderabad general sales tax act": "Hyderabad General Sales Tax Act",
    "u.p. sugarcane (regulation of supply and purchase) act":
        "U.P. Sugarcane (Regulation of Supply and Purchase) Act",
    "government of india act": "Government of India Act",
    "administration of evacuee property act":
        "Administration of Evacuee Property Act",
    "scheduled castes and the scheduled tribes (prevention of atrocities) act":
        "Scheduled Castes and the Scheduled Tribes (Prevention of Atrocities) Act",
    "interest act": "Interest Act",
    "kv.at act": "K.V.A.T. Act",
    "k.v.a.t act": "K.V.A.T. Act",
    "registration and other related laws (amendment) act":
        "Registration and Other Related Laws (Amendment) Act",
}


# -------------------------------------------------------------------
# Normalization helpers
# -------------------------------------------------------------------

def normalize_spaces(text):
    return re.sub(r"\s+", " ", text.strip())


def clean_candidate(text):
    text = normalize_spaces(text)

    # Remove numbering such as "i)", "ii)", "a)", etc.
    text = re.sub(r"^(?:[ivxlcdm]+|[a-z])\s*[\)\.]\s*", "", text, flags=re.I)

    # Remove obvious leading citation fragments
    prefixes = [
        "of the ",
        "of ",
        "under the ",
        "under ",
        "provisions of the ",
        "provisions of ",
        "pursuant to the ",
        "pursuant to ",
        "in exercise of power conferred by the ",
        "in accordance with the provisions of the ",
        "read with ",
        "the ",
    ]

    changed = True

    while changed:
        changed = False

        for prefix in prefixes:
            if text.lower().startswith(prefix):
                text = text[len(prefix):].strip()
                changed = True

    return text


def canonicalize_name(text):
    text = clean_candidate(text)
    lower = text.lower()

    # Remove punctuation differences
    lower = lower.replace("–", "-").replace("—", "-")
    lower = re.sub(r"\s+", " ", lower)

    # Remove common citation garbage at beginning
    lower = re.sub(r"^(?:s\.|sec\.|section)\s*\d+[a-z]?\s+", "", lower)

    # Handle abbreviations
    abbreviation_map = {
        "n. i. act": "negotiable instruments act",
        "n.i. act": "negotiable instruments act",
        "i.t. act": "income-tax act",
        "indian i.t. act": "income-tax act",
        "w.t. act": "wealth-tax act",
    }

    for old, new in abbreviation_map.items():
        if lower.startswith(old):
            lower = new + lower[len(old):]
            break

    # Remove leading "indian" for known equivalence
    if lower.startswith("indian income-tax act"):
        lower = "indian income-tax act"

    # Find canonical base
    for key, canonical_base in CANONICAL_ACTS.items():

        if lower.startswith(key):

            remainder = lower[len(key):]

            # Extract year
            year_match = re.search(r"\b(1[5-9]\d{2}|20\d{2})\b", remainder)

            if year_match:
                year = year_match.group(1)
                return f"{canonical_base}, {year}"

            # No year
            return canonical_base

    return None


def looks_like_garbage(text):

    lower = text.lower()

    garbage_patterns = [
        "preamble of the act",
        "objects and reasons of the act",
        "provisions of the act",
        "provision of the act",
        "under the act",
        "under any of the",
        "competent authority under the act",
        "act which relate",
        "act which relates",
        "same, failing which",
        "document affecting",
        "at the present moment",
        "in my opinion",
        "has next submitted",
        "has submitted",
        "petitioners have",
        "authority has",
        "challenge to the constitutional",
        "is under challenge",
        "is clear and unambiguous",
        "complex issues",
        "a dispute relating",
        "a settlee or a raiyat",
        "overriding effect",
        "chief justice has held",
        "he has taken",
        "he has submitted",
        "nor it is the plea",
        "wife of one",
        "yogendra mishra",
        "mr. lalit kishore",
    ]

    return any(pattern in lower for pattern in garbage_patterns)


# -------------------------------------------------------------------
# Parse verified inventory
# -------------------------------------------------------------------

def parse_candidates():
    """
    Parse results/verified_act_candidates.txt.

    The verified inventory is stored as repeated blocks:

        ACT: negotiable instrument act, 1881
        OCCURRENCES: 43
        JUDGMENTS: 30
        FILES: judgment_087.txt, ...

    Return the same structure expected by the rest of this script.
    """

    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Could not find {INPUT_FILE}"
        )

    text = INPUT_FILE.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    candidates = []

    # Each candidate is separated by a long line of "=" characters.
    blocks = re.split(r"\n\s*={20,}\s*\n", text)

    for block in blocks:
        block = block.strip()

        if not block:
            continue

        act_match = re.search(
            r"^ACT:\s*(.+?)\s*$",
            block,
            re.MULTILINE
        )

        occurrences_match = re.search(
            r"^OCCURRENCES:\s*(\d+)\s*$",
            block,
            re.MULTILINE
        )

        judgments_match = re.search(
            r"^JUDGMENTS:\s*(\d+)\s*$",
            block,
            re.MULTILINE
        )

        # Ignore anything that is not a complete candidate block.
        if not act_match:
            continue

        if not occurrences_match or not judgments_match:
            continue

        candidate = act_match.group(1).strip()

        if not candidate:
            continue

        occurrences = int(occurrences_match.group(1))
        judgment_count = int(judgments_match.group(1))

        candidates.append({
            "candidate": candidate,
            "occurrences": occurrences,
            "judgments": judgment_count
        })

    return candidates

# -------------------------------------------------------------------
# Main
# -------------------------------------------------------------------

def main():

    OUTPUT_DIR.mkdir(exist_ok=True)

    candidates = parse_candidates()

    canonical = defaultdict(lambda: {
        "total_occurrences": 0,
        "judgment_count_estimate": 0,
        "aliases": []
    })

    rejected = []

    for item in candidates:

        candidate = item["candidate"]

        if looks_like_garbage(candidate):
            rejected.append(candidate)
            continue

        canonical_name = canonicalize_name(candidate)

        if not canonical_name:
            rejected.append(candidate)
            continue

        data = canonical[canonical_name]

        data["total_occurrences"] += item["occurrences"]

        data["judgment_count_estimate"] = max(
            data["judgment_count_estimate"],
            item["judgments"]
        )

        if candidate not in data["aliases"]:
            data["aliases"].append(candidate)

    # Sort by frequency
    sorted_acts = sorted(
        canonical.items(),
        key=lambda x: (
            -x[1]["judgment_count_estimate"],
            -x[1]["total_occurrences"],
            x[0]
        )
    )

    # Canonical Act JSON
    act_json = {}

    for name, data in sorted_acts:
        act_json[name] = data

    OUTPUT_JSON.write_text(
        json.dumps(
            act_json,
            indent=2,
            ensure_ascii=False
        ),
        encoding="utf-8"
    )

    # Alias map
    aliases = {}

    for name, data in sorted_acts:
        for alias in data["aliases"]:
            aliases[alias] = name

    ALIASES_JSON.write_text(
        json.dumps(
            aliases,
            indent=2,
            ensure_ascii=False
        ),
        encoding="utf-8"
    )

    # Rejected candidates
    REJECTED_FILE.write_text(
        "\n".join(sorted(set(rejected))),
        encoding="utf-8"
    )

    # Human-readable inventory
    lines = []

    lines.append("=" * 90)
    lines.append("CANONICAL ACT INVENTORY")
    lines.append("=" * 90)
    lines.append("")
    lines.append(
        f"Unique canonical Acts: {len(sorted_acts)}"
    )
    lines.append("")
    lines.append(
        f"{'#':>4} {'JUDGMENTS':>12} {'OCCURRENCES':>14}  ACT"
    )
    lines.append("-" * 90)

    for i, (name, data) in enumerate(sorted_acts, 1):

        lines.append(
            f"{i:>4} "
            f"{data['judgment_count_estimate']:>12} "
            f"{data['total_occurrences']:>14}  "
            f"{name}"
        )

    INVENTORY_FILE.write_text(
        "\n".join(lines),
        encoding="utf-8"
    )

    # Console
    print()
    print("=" * 90)
    print("CANONICAL ACT INVENTORY")
    print("=" * 90)
    print()
    print(f"Input candidates:       {len(candidates)}")
    print(f"Canonical Acts:         {len(sorted_acts)}")
    print(f"Rejected candidates:    {len(set(rejected))}")
    print()
    print("TOP 30")
    print("-" * 90)

    for i, (name, data) in enumerate(sorted_acts[:30], 1):

        print(
            f"{i:>3}. "
            f"{data['judgment_count_estimate']:>3} judgments | "
            f"{data['total_occurrences']:>4} occurrences | "
            f"{name}"
        )

    print()
    print("Saved:")
    print(f"  {OUTPUT_JSON}")
    print(f"  {ALIASES_JSON}")
    print(f"  {REJECTED_FILE}")
    print(f"  {INVENTORY_FILE}")
    print()


if __name__ == "__main__":
    main()

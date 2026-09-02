import json
import re
from pathlib import Path
from collections import defaultdict


MASTER_FILE = Path("results/act_master_catalog.json")
OUTPUT_DIR = Path("results")

ALIASES_FILE = OUTPUT_DIR / "resolved_act_aliases.json"
REPORT_FILE = OUTPUT_DIR / "act_alias_resolution_report.txt"


# ------------------------------------------------------------
# Manual legal aliases
# These are only aliases where we are confident they refer
# to the same Act.
# ------------------------------------------------------------

MANUAL_ALIASES = {

    "Negotiable Instruments Act, 1881": [
        "Negotiable Instruments Act, 1881",
        "Negotiable Instrument Act, 1881",
        "Negotiable Instruments Act 1881",
        "Negotiable Instrument Act 1881",
        "N.I. Act, 1881",
        "N. I. Act, 1881",
        "N.I. Act",
        "N. I. Act",
        "NI Act",
    ],

    "Information Technology Act, 2000": [
        "Information Technology Act, 2000",
        "Information Technology Act 2000",
        "I.T. Act, 2000",
        "I.T. Act 2000",
        "IT Act, 2000",
        "IT Act 2000",
    ],

    "Indian Contract Act, 1872": [
        "Indian Contract Act, 1872",
        "Indian Contract Act 1872",
        "Contract Act, 1872",
        "Contract Act 1872",
    ],

    "Companies Act, 2013": [
        "Companies Act, 2013",
        "Companies Act 2013",
    ],

    "Companies Act, 1956": [
        "Companies Act, 1956",
        "Companies Act 1956",
        "Indian Companies Act, 1956",
        "Indian Companies Act 1956",
    ],

    "Income-tax Act, 1961": [
        "Income-tax Act, 1961",
        "Income-tax Act 1961",
        "Income Tax Act, 1961",
        "Income Tax Act 1961",
        "I.T. Act, 1961",
        "I.T. Act 1961",
        "IT Act, 1961",
        "IT Act 1961",
    ],

    "Land Acquisition Act, 1894": [
        "Land Acquisition Act, 1894",
        "Land Acquisition Act 1894",
    ],

    "Transfer of Property Act, 1882": [
        "Transfer of Property Act, 1882",
        "Transfer of Property Act 1882",
        "TP Act, 1882",
        "TP Act 1882",
    ],

    "Registration Act, 1908": [
        "Registration Act, 1908",
        "Registration Act 1908",
        "Indian Registration Act, 1908",
        "Indian Registration Act 1908",
    ],

    "Limitation Act, 1963": [
        "Limitation Act, 1963",
        "Limitation Act 1963",
    ],

    "Indian Evidence Act, 1872": [
        "Indian Evidence Act, 1872",
        "Indian Evidence Act 1872",
        "Evidence Act, 1872",
        "Evidence Act 1872",
    ],

    "General Clauses Act, 1897": [
        "General Clauses Act, 1897",
        "General Clauses Act 1897",
    ],

    "Specific Relief Act, 1963": [
        "Specific Relief Act, 1963",
        "Specific Relief Act 1963",
    ],

    "Motor Vehicles Act, 1988": [
        "Motor Vehicles Act, 1988",
        "Motor Vehicles Act 1988",
    ],

    "Finance Act, 1969": [
        "Finance Act, 1969",
        "Finance Act 1969",
    ],

    "SEBI Act, 1992": [
        "SEBI Act, 1992",
        "SEBI Act 1992",
        "Securities and Exchange Board of India Act, 1992",
        "Securities and Exchange Board of India Act 1992",
    ],

}


def normalize(text):
    text = text.lower().strip()

    # Normalize unicode punctuation
    text = text.replace("–", "-")
    text = text.replace("—", "-")

    # Remove punctuation differences
    text = re.sub(r"[.,;:]+", " ", text)

    # Collapse whitespace
    text = re.sub(r"\s+", " ", text)

    return text.strip()


def extract_year(title):
    match = re.search(r"\b(18|19|20)\d{2}\b", title)

    if match:
        return match.group(0)

    return None


def build_title_index(master):
    index = {}

    for normalized_key, data in master.items():
        title = data["title"]

        index[normalize(title)] = data

    return index


def build_aliases(master):
    title_index = build_title_index(master)

    aliases = defaultdict(list)

    unresolved = []

    # --------------------------------------------------------
    # First: every authoritative title aliases itself
    # --------------------------------------------------------

    for data in master.values():

        act_id = data["act_id"]
        title = data["title"]

        aliases[act_id].append({
            "alias": title,
            "normalized": normalize(title),
            "type": "canonical"
        })

    # --------------------------------------------------------
    # Manual aliases
    # --------------------------------------------------------

    for canonical_title, alias_list in MANUAL_ALIASES.items():

        normalized_canonical = normalize(canonical_title)

        target = title_index.get(normalized_canonical)

        if target is None:

            # Try matching without punctuation
            target = None

            for key, data in title_index.items():

                if normalize(data["title"]) == normalized_canonical:
                    target = data
                    break

        if target is None:

            unresolved.append(canonical_title)
            continue

        act_id = target["act_id"]

        for alias in alias_list:

            aliases[act_id].append({
                "alias": alias,
                "normalized": normalize(alias),
                "type": "manual"
            })

    return aliases, unresolved


def save_outputs(master, aliases, unresolved):

    OUTPUT_DIR.mkdir(exist_ok=True)

    output = {}

    for act_id, items in aliases.items():

        data = next(
            item
            for item in master.values()
            if item["act_id"] == act_id
        )

        unique = {}
        for item in items:
            unique[item["normalized"]] = item

        output[act_id] = {
            "act_id": act_id,
            "canonical_title": data["title"],
            "year": data.get("year"),
            "aliases": list(unique.values())
        }

    with ALIASES_FILE.open(
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            output,
            f,
            indent=2,
            ensure_ascii=False
        )

    with REPORT_FILE.open(
        "w",
        encoding="utf-8"
    ) as f:

        f.write("=" * 90 + "\n")
        f.write("ACT ALIAS RESOLUTION REPORT\n")
        f.write("=" * 90 + "\n\n")

        f.write(
            f"Master Acts: {len(master)}\n"
        )

        f.write(
            f"Acts with aliases: {len(output)}\n"
        )

        f.write(
            f"Unresolved canonical names: {len(unresolved)}\n\n"
        )

        f.write("-" * 90 + "\n")

        for act_id, data in sorted(
            output.items(),
            key=lambda x: x[1]["canonical_title"].lower()
        ):

            f.write(
                f"\n{act_id} | "
                f"{data['canonical_title']}\n"
            )

            for alias in data["aliases"]:

                f.write(
                    f"    [{alias['type']}] "
                    f"{alias['alias']}\n"
                )

        if unresolved:

            f.write("\n")
            f.write("=" * 90 + "\n")
            f.write("UNRESOLVED MANUAL ALIASES\n")
            f.write("=" * 90 + "\n")

            for item in unresolved:
                f.write(f"{item}\n")


def main():

    if not MASTER_FILE.exists():

        raise FileNotFoundError(
            f"Could not find {MASTER_FILE}"
        )

    with MASTER_FILE.open(
        "r",
        encoding="utf-8"
    ) as f:

        master = json.load(f)

    aliases, unresolved = build_aliases(master)

    save_outputs(
        master,
        aliases,
        unresolved
    )

    total_aliases = sum(
        len(items)
        for items in aliases.values()
    )

    print()
    print("=" * 90)
    print("ACT ALIAS RESOLUTION")
    print("=" * 90)
    print()
    print(f"Master Acts:       {len(master)}")
    print(f"Acts with aliases: {len(aliases)}")
    print(f"Total aliases:     {total_aliases}")
    print(f"Unresolved:        {len(unresolved)}")
    print()
    print("Saved:")
    print(f"  {ALIASES_FILE}")
    print(f"  {REPORT_FILE}")
    print()


if __name__ == "__main__":
    main()

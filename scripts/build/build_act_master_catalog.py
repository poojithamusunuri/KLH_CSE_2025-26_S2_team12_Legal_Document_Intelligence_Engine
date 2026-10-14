import json
import re
from pathlib import Path


ACT_DIR = Path("data/corpus/acts")
CORE_ACT_DIR = Path("data/corpus/core_acts")
OUTPUT_DIR = Path("results")

OUTPUT_JSON = OUTPUT_DIR / "act_master_catalog.json"
OUTPUT_TXT = OUTPUT_DIR / "act_master_catalog.txt"


def parse_act_file(path):
    text = path.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    metadata = {}

    for line in text.splitlines():
        line = line.strip()

        if not line:
            continue

        if ":" in line:
            key, value = line.split(":", 1)

            key = key.strip()
            value = value.strip()

            if key in {
                "DOCUMENT_ID",
                "TITLE",
                "DOCUMENT_TYPE",
                "JURISDICTION",
                "AUTHORITY",
                "SOURCE",
                "DESCRIPTION",
                "YEAR",
                "DOMAIN",
                "SUBDOMAIN",
                "SOURCE_TYPE",
            }:
                metadata[key] = value

    return metadata


def normalize_name(name):
    name = name.lower().strip()

    # normalize whitespace
    name = re.sub(r"\s+", " ", name)

    # normalize dash variants
    name = name.replace("–", "-")
    name = name.replace("—", "-")

    return name


def build_catalog():
    OUTPUT_DIR.mkdir(exist_ok=True)

    files = []

    if ACT_DIR.exists():
        files.extend(sorted(ACT_DIR.glob("*.txt")))

    if CORE_ACT_DIR.exists():
        files.extend(sorted(CORE_ACT_DIR.glob("*.txt")))

    catalog = {}

    for path in files:
        metadata = parse_act_file(path)

        title = metadata.get("TITLE")

        if not title:
            print(f"WARNING: No TITLE found in {path}")
            continue

        document_id = metadata.get(
            "DOCUMENT_ID",
            path.stem
        )

        normalized = normalize_name(title)

        # Prevent accidental duplicate titles
        if normalized in catalog:
            print(
                f"WARNING: Duplicate Act title detected: {title}"
            )

        catalog[normalized] = {
            "act_id": document_id,
            "title": title,
            "year": metadata.get("YEAR"),
            "document_type": metadata.get("DOCUMENT_TYPE"),
            "jurisdiction": metadata.get("JURISDICTION"),
            "authority": metadata.get("AUTHORITY"),
            "source": metadata.get("SOURCE"),
            "domain": metadata.get("DOMAIN"),
            "subdomain": metadata.get("SUBDOMAIN"),
            "source_type": metadata.get("SOURCE_TYPE"),
            "file": str(path),
            "normalized_name": normalized,
        }

    return catalog


def save_outputs(catalog):
    with OUTPUT_JSON.open(
        "w",
        encoding="utf-8"
    ) as f:
        json.dump(
            catalog,
            f,
            indent=2,
            ensure_ascii=False
        )

    rows = sorted(
        catalog.values(),
        key=lambda x: x["title"].lower()
    )

    with OUTPUT_TXT.open(
        "w",
        encoding="utf-8"
    ) as f:

        f.write("=" * 90 + "\n")
        f.write("ACT MASTER CATALOGUE\n")
        f.write("=" * 90 + "\n\n")

        f.write(
            f"Unique Act documents: {len(rows)}\n\n"
        )

        f.write(
            f"{'#':>4}  "
            f"{'ACT ID':<14}  "
            f"TITLE\n"
        )

        f.write("-" * 90 + "\n")

        for i, item in enumerate(rows, 1):
            f.write(
                f"{i:>4}  "
                f"{item['act_id']:<14}  "
                f"{item['title']}\n"
            )

    print()
    print("=" * 90)
    print("ACT MASTER CATALOGUE")
    print("=" * 90)
    print()
    print(f"Act documents catalouged: {len(catalog)}")
    print(f"Acts catalogued:     {len(catalog)}")
    print()
    print("Saved:")
    print(f"  {OUTPUT_JSON}")
    print(f"  {OUTPUT_TXT}")
    print()


def main():
    catalog = build_catalog()
    save_outputs(catalog)


if __name__ == "__main__":
    main()

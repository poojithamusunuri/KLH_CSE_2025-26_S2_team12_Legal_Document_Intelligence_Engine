from pathlib import Path
import re

BASE_DIR = Path("data/corpus/constitution")

CONSTITUTION_METADATA = {
    "constitution_preamble.txt": {
        "domain": "Constitutional Law",
        "subdomain": "Constitutional Foundations",
        "keywords": "constitution, preamble, justice, liberty, equality, fraternity, democracy, republic"
    },

    "constitution_article_14.txt": {
        "domain": "Constitutional Law",
        "subdomain": "Fundamental Rights and Equality",
        "keywords": "article 14, equality before law, equal protection, fundamental rights, constitution"
    }
}


def add_metadata(file_path, metadata):

    text = file_path.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    if "DOMAIN:" in text:
        print(f"Skipping {file_path.name} - already enriched")
        return

    metadata_block = f"""

DOMAIN: {metadata['domain']}

SUBDOMAIN: {metadata['subdomain']}

SOURCE_TYPE: Constitutional Legal Text

SEARCH_KEYWORDS: {metadata['keywords']}

"""

    content_index = text.find("CONTENT:")

    if content_index != -1:

        updated_text = (
            text[:content_index]
            + metadata_block
            + "\nCONTENT:\n"
            + text[content_index + len("CONTENT:"):].lstrip()
        )

    else:

        updated_text = text + metadata_block

    file_path.write_text(
        updated_text,
        encoding="utf-8"
    )

    print(
        f"✓ {file_path.name} "
        f"→ {metadata['domain']}"
    )


print("\nENRICHING CONSTITUTION DOCUMENTS\n")

updated = 0

for filename, metadata in CONSTITUTION_METADATA.items():

    file_path = BASE_DIR / filename

    if file_path.exists():

        add_metadata(file_path, metadata)
        updated += 1

    else:

        print(f"⚠ File not found: {filename}")


print("\n" + "=" * 50)
print("CONSTITUTION METADATA ENRICHMENT COMPLETE")
print("=" * 50)
print(f"Files processed: {updated}")

from pathlib import Path
from collections import Counter
import re

BASE_DIR = Path("data/corpus")

folders = [
    "constitution",
    "acts",
    "legal_principles",
    "judgments"
]

print("\n" + "=" * 60)
print("LEGAL DOCUMENT INTELLIGENCE ENGINE")
print("CORPUS QUALITY AUDIT")
print("=" * 60)

total_documents = 0

for folder in folders:

    folder_path = BASE_DIR / folder

    print(f"\n📁 {folder.upper()}")

    if not folder_path.exists():
        print("Folder not found")
        continue

    files = list(folder_path.glob("*.txt"))

    print(f"Documents: {len(files)}")

    total_documents += len(files)

    domains = Counter()
    subdomains = Counter()

    missing_metadata = []

    for file in files:

        try:
            text = file.read_text(
                encoding="utf-8",
                errors="ignore"
            )

            domain_match = re.search(
                r"DOMAIN:\s*(.+)",
                text
            )

            subdomain_match = re.search(
                r"SUBDOMAIN:\s*(.+)",
                text
            )

            if domain_match:
                domains[domain_match.group(1).strip()] += 1
            else:
                missing_metadata.append(
                    f"{file.name} → DOMAIN missing"
                )

            if subdomain_match:
                subdomains[
                    subdomain_match.group(1).strip()
                ] += 1

        except Exception as e:
            print(f"Error reading {file.name}: {e}")

    if domains:

        print("\nDomain distribution:")

        for domain, count in domains.most_common():

            print(f"  {domain}: {count}")

    if subdomains:

        print("\nSubdomain distribution:")

        for subdomain, count in subdomains.most_common():

            print(f"  {subdomain}: {count}")

    if missing_metadata:

        print("\n⚠ Missing metadata:")

        for item in missing_metadata[:10]:

            print(" ", item)

        if len(missing_metadata) > 10:

            print(
                f"  ... and {len(missing_metadata) - 10} more"
            )

    else:

        print("\n✓ Metadata check passed")


print("\n" + "=" * 60)

print(f"TOTAL DOCUMENTS: {total_documents}")

print("=" * 60)

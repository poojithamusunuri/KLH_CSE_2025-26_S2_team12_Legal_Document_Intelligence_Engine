import os
import re
from collections import Counter

JUDGMENTS_DIR = "data/corpus/judgments"
OUTPUT_FILE = "results/named_act_references.txt"

# Looks for a proper Act title followed by a year.
# We deliberately require several words before "Act" so that
# phrases like "the Act, 2013" are not treated as Act names.

pattern = re.compile(
    r"\b("
    r"(?:[A-Z][A-Za-z0-9&'().,\-]*\s+){1,15}"
    r"(?:Act)"
    r"(?:,?\s+(?:of\s+)?(?:18|19|20)\d{2})"
    r")",
    re.IGNORECASE
)

references = Counter()

for filename in sorted(os.listdir(JUDGMENTS_DIR)):
    if not filename.endswith(".txt"):
        continue

    path = os.path.join(JUDGMENTS_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    # Normalize whitespace
    text = re.sub(r"\s+", " ", text)

    found_in_document = set()

    for match in pattern.finditer(text):
        phrase = match.group(1).strip()

        # Clean common surrounding punctuation
        phrase = phrase.strip(" ,.;:()[]\"'")

        # Normalize whitespace
        phrase = re.sub(r"\s+", " ", phrase)

        # Reject obviously incomplete references
        lower = phrase.lower()

        bad_starts = [
            "the act",
            "this act",
            "said act",
            "of the act",
            "under the act",
            "provisions of the act",
            "section of the act",
        ]

        if any(lower.startswith(x) for x in bad_starts):
            continue

        # Must contain "Act"
        if not re.search(r"\bAct\b", phrase, re.IGNORECASE):
            continue

        # Must end with a four-digit year
        if not re.search(r"(18|19|20)\d{2}$", phrase):
            continue

        found_in_document.add(phrase)

    for ref in found_in_document:
        references[ref] += 1


with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("NAMED ACT REFERENCES FOUND IN JUDGMENTS\n")
    f.write("=" * 80 + "\n\n")

    for ref, count in references.most_common():
        f.write(f"{count:4d} | {ref}\n")


print("=" * 80)
print("NAMED ACT REFERENCE EXTRACTION")
print("=" * 80)
print()
print(f"Unique named Act candidates: {len(references)}")
print(f"Saved to: {OUTPUT_FILE}")
print()
print("TOP 100")
print("-" * 80)

for ref, count in references.most_common(100):
    print(f"{count:4d} | {ref}")
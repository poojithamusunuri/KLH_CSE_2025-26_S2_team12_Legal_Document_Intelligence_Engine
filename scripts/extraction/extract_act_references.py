import os
import re
from collections import Counter

JUDGMENTS_DIR = "data/corpus/judgments"
OUTPUT_FILE = "results/act_reference_candidates.txt"

# Patterns intentionally focus on Indian statute references
patterns = [
    r"\b([A-Z][A-Za-z0-9&(),.'\- ]{2,100}?\bAct),?\s+(?:of\s+)?(?:19|20)\d{2}\b",
    r"\b([A-Z][A-Za-z0-9&(),.'\- ]{2,100}?\bAct)\s+(?:19|20)\d{2}\b",
]

# Noise phrases that commonly appear around citations
noise_prefixes = [
    "the ",
    "of the ",
    "under the ",
    "provisions of the ",
    "section of the ",
    "sections of the ",
    "under ",
    "in the ",
]

references = Counter()

for filename in sorted(os.listdir(JUDGMENTS_DIR)):
    if not filename.endswith(".txt"):
        continue

    path = os.path.join(JUDGMENTS_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    # Normalize whitespace
    text = re.sub(r"\s+", " ", text)

    found = set()

    for pattern in patterns:
        for match in re.finditer(pattern, text, flags=re.IGNORECASE):
            phrase = match.group(0).strip()

            # Clean whitespace and punctuation
            phrase = re.sub(r"\s+", " ", phrase)
            phrase = phrase.strip(" ,.;:()")

            # Remove obvious surrounding citation language
            lower = phrase.lower()

            for prefix in noise_prefixes:
                if lower.startswith(prefix):
                    phrase = phrase[len(prefix):].strip()
                    lower = phrase.lower()

            # Reject obviously bad extractions
            if len(phrase) < 8:
                continue

            if "act" not in phrase.lower():
                continue

            # Keep only reasonably statute-like references
            found.add(phrase)

    for ref in found:
        references[ref] += 1


with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("ACT REFERENCE CANDIDATES\n")
    f.write("=" * 70 + "\n\n")

    for ref, count in references.most_common():
        f.write(f"{count:4d} | {ref}\n")

print("=" * 70)
print("LEGAL ACT REFERENCE EXTRACTION")
print("=" * 70)
print()
print(f"Unique candidate references: {len(references)}")
print(f"Saved to: {OUTPUT_FILE}")
print()
print("TOP 50 REFERENCES")
print("-" * 70)

for ref, count in references.most_common(50):
    print(f"{count:4d} | {ref}")
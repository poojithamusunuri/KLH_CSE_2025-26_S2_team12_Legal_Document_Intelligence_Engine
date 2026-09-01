import os
import re
from collections import Counter

JUDGMENTS_DIR = "data/corpus/judgments"

# Common legal Act naming patterns.
PATTERNS = [
    r"\b([A-Z][A-Za-z&().,\- ]{2,100}Act),?\s+(?:of\s+)?(18|19|20)\d{2}",
    r"\b([A-Z][A-Za-z&().,\- ]{2,100}Act)\s+(18|19|20)\d{2}",
]

counter = Counter()

for filename in sorted(os.listdir(JUDGMENTS_DIR)):

    if not filename.endswith(".txt"):
        continue

    path = os.path.join(JUDGMENTS_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    text = re.sub(r"\s+", " ", text)

    for pattern in PATTERNS:

        matches = re.findall(pattern, text)

        for name, year_prefix in matches:

            name = name.strip()

            # Remove obvious fragments
            name = re.sub(
                r"^(of|the|under|provisions of|section|sub-section)\s+",
                "",
                name,
                flags=re.IGNORECASE
            )

            # Ignore very short or obviously broken candidates
            if len(name) < 5:
                continue

            # Avoid candidates ending in random punctuation
            name = name.strip(" ,.;:-")

            full_name = f"{name}, {year_prefix}XX"

            counter[full_name.lower()] += 1


print("=" * 80)
print("ACT REFERENCE INVENTORY")
print("=" * 80)
print()

print(f"Unique normalized candidates: {len(counter)}")
print()

print("TOP 100")
print("-" * 80)

for i, (name, count) in enumerate(counter.most_common(100), 1):
    print(f"{i:3} | {count:4} | {name}")

# Save
os.makedirs("results", exist_ok=True)

with open(
    "results/act_inventory_raw.txt",
    "w",
    encoding="utf-8"
) as f:

    for name, count in counter.most_common():
        f.write(f"{count}\t{name}\n")

print()
print("=" * 80)
print("Saved to:")
print("results/act_inventory_raw.txt")
print("=" * 80)
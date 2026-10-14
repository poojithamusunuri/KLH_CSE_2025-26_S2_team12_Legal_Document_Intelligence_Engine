import os
import re

JUDGMENT_DIR = "data/corpus/judgments"

files = sorted(
    f for f in os.listdir(JUDGMENT_DIR)
    if f.endswith(".txt")
)

print("=" * 100)
print("JUDGMENT HEADER / CASE IDENTITY INSPECTION")
print("=" * 100)

for filename in files:

    path = os.path.join(JUDGMENT_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    lines = [
        line.strip()
        for line in text.splitlines()
        if line.strip()
    ]

    print("\n" + "=" * 100)
    print(filename)
    print("-" * 100)

    # Print first 25 non-empty lines.
    for line in lines[:25]:
        print(line[:250])
import os
import re
from collections import defaultdict

JUDGMENTS_DIR = "data/corpus/judgments"

# More conservative patterns.
patterns = [
    re.compile(
        r"\b([A-Z][A-Za-z&().,\- ]{2,100}?\bAct),?\s+(18[0-9]{2}|19[0-9]{2}|20[0-9]{2})",
        re.IGNORECASE
    ),

    re.compile(
        r"\b([A-Z][A-Za-z&().,\- ]{2,100}?\bAct),?\s+of\s+(18[0-9]{2}|19[0-9]{2}|20[0-9]{2})",
        re.IGNORECASE
    )
]

# Known phrases that are usually NOT Act names.
bad_starts = (
    "preamble",
    "provisions",
    "provision",
    "section",
    "sub-section",
    "subsection",
    "chapter",
    "clause",
    "illustration",
    "objects and reasons",
    "competent authority",
    "under the",
    "of the",
    "the provisions",
    "the objects"
)

references = defaultdict(lambda: {
    "count": 0,
    "judgments": set()
})

for filename in sorted(os.listdir(JUDGMENTS_DIR)):

    if not filename.endswith(".txt"):
        continue

    path = os.path.join(JUDGMENTS_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    text = re.sub(r"\s+", " ", text)

    for pattern in patterns:

        for match in pattern.finditer(text):

            name = match.group(1).strip()
            year = match.group(2)

            name = re.sub(r"\s+", " ", name)
            name = name.strip(" ,.;:-")

            lower = name.lower()

            # Remove obvious broken candidates.
            if lower.startswith(bad_starts):
                continue

            if len(name) < 5:
                continue

            # Remove leading "of the", "the", etc.
            name = re.sub(
                r"^(of|the|under)\s+",
                "",
                name,
                flags=re.IGNORECASE
            ).strip()

            canonical_candidate = f"{name}, {year}"

            key = canonical_candidate.lower()

            references[key]["count"] += 1
            references[key]["judgments"].add(filename)


# Sort by number of occurrences
sorted_refs = sorted(
    references.items(),
    key=lambda x: (
        len(x[1]["judgments"]),
        x[1]["count"]
    ),
    reverse=True
)

print("=" * 90)
print("VERIFIED ACT CANDIDATE INVENTORY")
print("=" * 90)
print()

print(f"Unique candidates: {len(sorted_refs)}")
print()

print(
    f"{'#':>3} "
    f"{'OCC':>5} "
    f"{'JUDG':>5} "
    f"ACT"
)

print("-" * 90)

for i, (act, info) in enumerate(sorted_refs[:100], 1):

    print(
        f"{i:3} "
        f"{info['count']:5} "
        f"{len(info['judgments']):5} "
        f"{act}"
    )


# Save detailed results
os.makedirs("results", exist_ok=True)

with open(
    "results/verified_act_candidates.txt",
    "w",
    encoding="utf-8"
) as f:

    for act, info in sorted_refs:

        f.write("=" * 80 + "\n")
        f.write(f"ACT: {act}\n")
        f.write(f"OCCURRENCES: {info['count']}\n")
        f.write(f"JUDGMENTS: {len(info['judgments'])}\n")
        f.write(
            "FILES: "
            + ", ".join(sorted(info["judgments"]))
            + "\n"
        )
        f.write("\n")

print()
print("=" * 90)
print("Saved:")
print("results/verified_act_candidates.txt")
print("=" * 90)
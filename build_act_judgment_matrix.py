import os
import re
import csv
from collections import defaultdict

JUDGMENT_DIR = "data/corpus/judgments"
OUTPUT_DIR = "results"

os.makedirs(OUTPUT_DIR, exist_ok=True)

# ------------------------------------------------------------
# Common legal Act-name patterns.
# This is deliberately conservative.
# We want recognizable Act names, not arbitrary sentences.
# ------------------------------------------------------------

ACT_PATTERN = re.compile(
    r"""
    (?P<name>
        (?:[A-Z][A-Za-z.&'()/-]+(?:\s+[A-Za-z][A-Za-z.&'()/-]+){0,15})
        \s+
        Act
        (?:,)?\s+
        (?:18|19|20)\d{2}
    )
    """,
    re.VERBOSE | re.IGNORECASE
)


def normalize_act(name):
    """Normalize superficial spelling/format differences."""

    name = name.strip()

    # Remove surrounding punctuation
    name = re.sub(r'^[\s,.;:()\[\]"]+', '', name)
    name = re.sub(r'[\s,.;:()\[\]"]+$', '', name)

    # Normalize whitespace
    name = re.sub(r'\s+', ' ', name)

    # Normalize common abbreviation
    replacements = {
        r'\bI\.T\. Act\b': 'Income-tax Act',
        r'\bI\. T\. Act\b': 'Income-tax Act',
        r'\bIT Act\b': 'Income-tax Act',
        r'\bN\. I\. Act\b': 'Negotiable Instruments Act',
        r'\bN\.I\. Act\b': 'Negotiable Instruments Act',
    }

    for pattern, replacement in replacements.items():
        name = re.sub(pattern, replacement, name, flags=re.IGNORECASE)

    return name


def extract_acts(text):
    """
    Extract recognizable Act-name + year references.
    Returns normalized names.
    """

    matches = ACT_PATTERN.findall(text)

    acts = set()

    for match in matches:
        normalized = normalize_act(match)

        # Basic quality filters
        words = normalized.split()

        if len(words) < 3:
            continue

        if not re.search(r'\bAct\s*,?\s*(18|19|20)\d{2}\b',
                         normalized,
                         re.IGNORECASE):
            continue

        acts.add(normalized)

    return sorted(acts)


# ------------------------------------------------------------
# Process judgments
# ------------------------------------------------------------

act_to_judgments = defaultdict(set)
judgment_to_acts = defaultdict(set)

judgment_files = sorted(
    f for f in os.listdir(JUDGMENT_DIR)
    if f.endswith(".txt")
)

print("=" * 80)
print("ACT ↔ JUDGMENT RELATIONSHIP ANALYSIS")
print("=" * 80)

print(f"\nJudgments found: {len(judgment_files)}\n")

for filename in judgment_files:

    path = os.path.join(JUDGMENT_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    acts = extract_acts(text)

    judgment_id = os.path.splitext(filename)[0]

    for act in acts:
        act_to_judgments[act].add(judgment_id)
        judgment_to_acts[judgment_id].add(act)


# ------------------------------------------------------------
# Save ACT → JUDGMENTS
# ------------------------------------------------------------

act_output = os.path.join(
    OUTPUT_DIR,
    "act_judgment_relationships.csv"
)

with open(
    act_output,
    "w",
    newline="",
    encoding="utf-8"
) as f:

    writer = csv.writer(f)

    writer.writerow([
        "act",
        "judgment_count",
        "judgments"
    ])

    for act, judgments in sorted(
        act_to_judgments.items(),
        key=lambda x: (-len(x[1]), x[0])
    ):

        writer.writerow([
            act,
            len(judgments),
            ";".join(sorted(judgments))
        ])


# ------------------------------------------------------------
# Save JUDGMENT → ACTS
# ------------------------------------------------------------

judgment_output = os.path.join(
    OUTPUT_DIR,
    "judgment_act_relationships.csv"
)

with open(
    judgment_output,
    "w",
    newline="",
    encoding="utf-8"
) as f:

    writer = csv.writer(f)

    writer.writerow([
        "judgment",
        "act_count",
        "acts"
    ])

    for judgment, acts in sorted(
        judgment_to_acts.items()
    ):

        writer.writerow([
            judgment,
            len(acts),
            ";".join(sorted(acts))
        ])


# ------------------------------------------------------------
# Human-readable report
# ------------------------------------------------------------

report_output = os.path.join(
    OUTPUT_DIR,
    "act_judgment_report.txt"
)

with open(
    report_output,
    "w",
    encoding="utf-8"
) as f:

    f.write("=" * 80 + "\n")
    f.write("ACT ↔ JUDGMENT RELATIONSHIP REPORT\n")
    f.write("=" * 80 + "\n\n")

    f.write(
        f"Total judgments analysed: {len(judgment_files)}\n"
    )

    f.write(
        f"Unique Act references detected: "
        f"{len(act_to_judgments)}\n\n"
    )

    f.write("-" * 80 + "\n")
    f.write("ACTS BY NUMBER OF JUDGMENTS\n")
    f.write("-" * 80 + "\n\n")

    for i, (act, judgments) in enumerate(
        sorted(
            act_to_judgments.items(),
            key=lambda x: (-len(x[1]), x[0])
        ),
        start=1
    ):

        f.write(
            f"{i:3}. {len(judgments):3} judgments | "
            f"{act}\n"
        )


print("=" * 80)
print("RESULT")
print("=" * 80)

print(
    f"\nUnique recognizable Acts: "
    f"{len(act_to_judgments)}"
)

print(
    f"\nSaved:"
)

print(
    f"  {act_output}"
)

print(
    f"  {judgment_output}"
)

print(
    f"  {report_output}"
)

print("\nTOP 30 ACTS")
print("-" * 80)

for i, (act, judgments) in enumerate(
    sorted(
        act_to_judgments.items(),
        key=lambda x: (-len(x[1]), x[0])
    )[:30],
    start=1
):

    print(
        f"{i:2}. {len(judgments):3} judgments | {act}"
    )

print("\nDone.")
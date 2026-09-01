import os
import re

JUDGMENTS_DIR = "data/corpus/judgments"

print("=" * 75)
print("JUDGMENT CORPUS ANALYSIS")
print("=" * 75)
print()

results = []

for filename in sorted(os.listdir(JUDGMENTS_DIR)):
    if not filename.endswith(".txt"):
        continue

    path = os.path.join(JUDGMENTS_DIR, filename)

    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    # Normalize whitespace
    clean = re.sub(r"\s+", " ", text)

    # Count legal references
    sections = re.findall(
        r"\bsection\s+\d+[A-Za-z]?(?:\([0-9A-Za-z]+\))*",
        clean,
        re.IGNORECASE
    )

    acts = re.findall(
        r"\b[A-Z][A-Za-z&().,\- ]{2,80}\bAct(?:,?\s+(?:of\s+)?(?:18|19|20)\d{2})",
        clean
    )

    constitutional = re.findall(
        r"\bArticle\s+\d+[A-Za-z]?",
        clean,
        re.IGNORECASE
    )

    case_markers = re.findall(
        r"\b(?:v\.|vs\.|versus|supra|reported in|AIR|SCC)\b",
        clean,
        re.IGNORECASE
    )

    results.append({
        "file": filename,
        "characters": len(clean),
        "sections": len(set(x.lower() for x in sections)),
        "act_references": len(set(x.lower() for x in acts)),
        "constitutional_refs": len(set(x.lower() for x in constitutional)),
        "case_markers": len(case_markers)
    })


# Sort by richness
results.sort(
    key=lambda x: (
        x["act_references"]
        + x["constitutional_refs"]
        + x["case_markers"]
        + x["sections"]
    ),
    reverse=True
)

print(
    f"{'FILE':<18}"
    f"{'CHARS':>10}"
    f"{'SECTIONS':>12}"
    f"{'ACTS':>10}"
    f"{'ARTICLES':>12}"
    f"{'CASE REFS':>12}"
)

print("-" * 75)

for r in results:
    print(
        f"{r['file']:<18}"
        f"{r['characters']:>10}"
        f"{r['sections']:>12}"
        f"{r['act_references']:>10}"
        f"{r['constitutional_refs']:>12}"
        f"{r['case_markers']:>12}"
    )

print()
print("=" * 75)
print("CORPUS TOTALS")
print("=" * 75)

print(f"Judgments: {len(results)}")
print(f"Total characters: {sum(r['characters'] for r in results):,}")
print(f"Total section references: {sum(r['sections'] for r in results):,}")
print(f"Total Act references: {sum(r['act_references'] for r in results):,}")
print(f"Total constitutional references: {sum(r['constitutional_refs'] for r in results):,}")
print(f"Total case-reference markers: {sum(r['case_markers'] for r in results):,}")

print()
print("TOP 20 RICHEST JUDGMENTS")
print("-" * 75)

for i, r in enumerate(results[:20], 1):
    score = (
        r["act_references"]
        + r["constitutional_refs"]
        + r["case_markers"]
        + r["sections"]
    )

    print(
        f"{i:2}. {r['file']:<18} "
        f"score={score:<4} "
        f"Acts={r['act_references']:<3} "
        f"Sections={r['sections']:<3} "
        f"Articles={r['constitutional_refs']:<3} "
        f"Cases={r['case_markers']:<3}"
    )
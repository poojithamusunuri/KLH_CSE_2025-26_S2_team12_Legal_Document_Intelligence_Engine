import csv
import json
from pathlib import Path
from collections import defaultdict


INPUT_FILE = Path("results/judgment_act_links.csv")

OUTPUT_CSV = Path(
    "results/judgment_act_relationships.csv"
)

OUTPUT_JSON = Path(
    "results/judgment_act_relationships.json"
)

OUTPUT_TXT = Path(
    "results/judgment_act_relationships.txt"
)


def main():

    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Could not find {INPUT_FILE}"
        )

    rows = list(
        csv.DictReader(
            INPUT_FILE.open(
                encoding="utf-8"
            )
        )
    )

    # ---------------------------------------------------------
    # Group all raw references by Judgment + Act
    # ---------------------------------------------------------

    grouped = defaultdict(list)

    for row in rows:

        key = (
            row["judgment_id"],
            row["act_id"],
        )

        grouped[key].append(row)

    relationships = []

    for (judgment_id, act_id), refs in grouped.items():

        first = refs[0]

        aliases = set()
        sections = set()
        contexts = []

        for ref in refs:

            alias = ref.get("alias_used", "").strip()

            if alias:
                aliases.add(alias)

            section_text = ref.get(
                "sections",
                ""
            ).strip()

            if section_text:

                for section in section_text.split(";"):

                    section = section.strip()

                    if section:
                        sections.add(section)

            context = ref.get(
                "context",
                ""
            ).strip()

            if context:
                contexts.append(context)

        relationships.append({

            "judgment_id": judgment_id,

            "act_id": act_id,

            "act_title": first["act_title"],

            "act_status": first["act_status"],

            "reference_count": len(refs),

            "aliases_used": sorted(
                aliases
            ),

            "sections": sorted(
                sections,
                key=lambda x: (
                    int("".join(
                        c for c in x
                        if c.isdigit()
                    ) or 0),
                    x
                )
            ),

            "contexts": contexts,

        })

    # ---------------------------------------------------------
    # Sort
    # ---------------------------------------------------------

    relationships.sort(
        key=lambda x: (
            x["judgment_id"],
            x["act_id"]
        )
    )

    # ---------------------------------------------------------
    # CSV
    # ---------------------------------------------------------

    with OUTPUT_CSV.open(
        "w",
        newline="",
        encoding="utf-8"
    ) as f:

        writer = csv.DictWriter(
            f,
            fieldnames=[
                "judgment_id",
                "act_id",
                "act_title",
                "act_status",
                "reference_count",
                "aliases_used",
                "sections",
            ],
        )

        writer.writeheader()

        for relationship in relationships:

            writer.writerow({

                "judgment_id":
                    relationship["judgment_id"],

                "act_id":
                    relationship["act_id"],

                "act_title":
                    relationship["act_title"],

                "act_status":
                    relationship["act_status"],

                "reference_count":
                    relationship["reference_count"],

                "aliases_used":
                    "; ".join(
                        relationship["aliases_used"]
                    ),

                "sections":
                    "; ".join(
                        relationship["sections"]
                    ),

            })

    # ---------------------------------------------------------
    # JSON
    # ---------------------------------------------------------

    OUTPUT_JSON.write_text(
        json.dumps(
            relationships,
            indent=2,
            ensure_ascii=False
        ),
        encoding="utf-8"
    )

    # ---------------------------------------------------------
    # Human-readable report
    # ---------------------------------------------------------

    local_relationships = [
        r for r in relationships
        if r["act_status"] == "local"
    ]

    external_relationships = [
        r for r in relationships
        if r["act_status"] == "external"
    ]

    act_judgments = defaultdict(set)

    for r in relationships:

        act_judgments[
            r["act_id"]
        ].add(
            r["judgment_id"]
        )

    lines = []

    lines.append("=" * 90)
    lines.append(
        "NORMALIZED JUDGMENT ↔ ACT RELATIONSHIPS"
    )
    lines.append("=" * 90)
    lines.append("")

    lines.append(
        f"Raw reference rows: {len(rows)}"
    )

    lines.append(
        f"Unique Judgment ↔ Act relationships: "
        f"{len(relationships)}"
    )

    lines.append(
        f"Local relationships: "
        f"{len(local_relationships)}"
    )

    lines.append(
        f"External relationships: "
        f"{len(external_relationships)}"
    )

    lines.append("")

    lines.append("-" * 90)
    lines.append(
        "TOP ACTS BY UNIQUE JUDGMENT COVERAGE"
    )
    lines.append("-" * 90)

    ranked = sorted(
        act_judgments.items(),
        key=lambda x: len(x[1]),
        reverse=True
    )

    for rank, (act_id, judgments) in enumerate(
        ranked,
        start=1
    ):

        act_title = next(
            r["act_title"]
            for r in relationships
            if r["act_id"] == act_id
        )

        status = next(
            r["act_status"]
            for r in relationships
            if r["act_id"] == act_id
        )

        lines.append(
            f"{rank:3}. "
            f"{len(judgments):3} judgments | "
            f"{status:8} | "
            f"{act_id:10} | "
            f"{act_title}"
        )

    lines.append("")

    lines.append("-" * 90)
    lines.append(
        "RELATIONSHIP DETAILS"
    )
    lines.append("-" * 90)

    for r in relationships:

        lines.append(
            f"{r['judgment_id']} → "
            f"{r['act_id']} | "
            f"{r['act_status']} | "
            f"{r['act_title']}"
        )

        lines.append(
            f"    references: "
            f"{r['reference_count']}"
        )

        lines.append(
            f"    sections: "
            f"{', '.join(r['sections']) or 'none'}"
        )

        lines.append("")

    OUTPUT_TXT.write_text(
        "\n".join(lines),
        encoding="utf-8"
    )

    # ---------------------------------------------------------
    # Console
    # ---------------------------------------------------------

    print("=" * 90)
    print(
        "NORMALIZED JUDGMENT ↔ ACT RELATIONSHIPS"
    )
    print("=" * 90)
    print()

    print(
        f"Raw reference rows: "
        f"{len(rows)}"
    )

    print(
        f"Unique relationships: "
        f"{len(relationships)}"
    )

    print(
        f"Local relationships: "
        f"{len(local_relationships)}"
    )

    print(
        f"External relationships: "
        f"{len(external_relationships)}"
    )

    print()

    print("Saved:")
    print(f"  {OUTPUT_CSV}")
    print(f"  {OUTPUT_JSON}")
    print(f"  {OUTPUT_TXT}")

    print()

    print("-" * 90)
    print("TOP ACTS")
    print("-" * 90)

    for rank, (act_id, judgments) in enumerate(
        ranked[:20],
        start=1
    ):

        act_title = next(
            r["act_title"]
            for r in relationships
            if r["act_id"] == act_id
        )

        print(
            f"{rank:2}. "
            f"{len(judgments):3} judgments | "
            f"{act_title}"
        )


if __name__ == "__main__":
    main()

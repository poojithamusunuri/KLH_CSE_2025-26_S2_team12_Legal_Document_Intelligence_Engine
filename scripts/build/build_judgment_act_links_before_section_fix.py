import csv
import json
import re
from pathlib import Path
from collections import defaultdict


# ===============================================================
# PATHS
# ===============================================================

JUDGMENT_DIR = Path("data/corpus/judgments")

RESULTS_DIR = Path("results")

MASTER_FILE = RESULTS_DIR / "act_master_catalog.json"

EXTERNAL_FILE = RESULTS_DIR / "external_act_registry.json"

EXTERNAL_ALIAS_FILE = RESULTS_DIR / "external_act_aliases.json"

OUTPUT_CSV = RESULTS_DIR / "judgment_act_links.csv"

OUTPUT_JSON = RESULTS_DIR / "judgment_act_links.json"

OUTPUT_TXT = RESULTS_DIR / "judgment_act_links.txt"


# ===============================================================
# TEXT NORMALIZATION
# ===============================================================

def normalize(text):
    """
    Normalize text for matching.

    Keeps punctuation because legal references such as:

        section 17(1-A)
        section 53-A

    contain meaningful punctuation.
    """

    text = text.lower()

    text = text.replace("–", "-")
    text = text.replace("—", "-")

    # Normalize non-breaking spaces
    text = text.replace("\xa0", " ")

    # Collapse whitespace
    text = re.sub(r"\s+", " ", text)

    return text.strip()


# ===============================================================
# LOCAL ACT CATALOGUE
# ===============================================================

def load_local_acts():

    data = json.loads(
        MASTER_FILE.read_text(
            encoding="utf-8"
        )
    )

    acts = {}

    if not isinstance(data, dict):
        return acts

    for _, value in data.items():

        if not isinstance(value, dict):
            continue

        act_id = value.get("act_id")
        title = value.get("title")

        if not act_id or not title:
            continue

        acts[act_id] = {
            "act_id": act_id,
            "title": title,
            "status": "local",
        }

    return acts


# ===============================================================
# EXTERNAL ACTS
# ===============================================================

def load_external_acts():

    data = json.loads(
        EXTERNAL_FILE.read_text(
            encoding="utf-8"
        )
    )

    acts = {}

    for external_id, item in data.items():

        acts[external_id] = {
            "act_id": external_id,
            "title": item["title"],
            "status": "external",
        }

    return acts


# ===============================================================
# EXTERNAL ALIASES
# ===============================================================

def load_external_aliases():

    data = json.loads(
        EXTERNAL_ALIAS_FILE.read_text(
            encoding="utf-8"
        )
    )

    aliases = {}

    for act_id, item in data.items():

        aliases[act_id] = item.get(
            "aliases",
            []
        )

    return aliases


# ===============================================================
# LOCAL ACT ALIASES
# ===============================================================

def local_aliases(act_id, title):
    """
    Generate only safe aliases for a local Act.

    IMPORTANT:

    Do NOT generate a generic year-less alias.

    Example:

        Companies Act, 2013
        Companies Act 2013

    are safe.

    But:

        Companies Act

    is NOT safe because the corpus may contain
    Companies Act, 1956 as an external Act.
    """

    aliases = {
        title,
        title.replace(",", ""),
    }

    return sorted(
        aliases,
        key=len,
        reverse=True
    )


# ===============================================================
# SECTION NORMALIZATION
# ===============================================================

def normalize_section(section):
    """
    Normalize section notation.

    Examples:

        53A       -> 53a
        53-A      -> 53a
        17(1-A)   -> 17(1a)
        11 A      -> 11a
    """

    section = section.strip()

    # Remove whitespace
    section = re.sub(
        r"\s+",
        "",
        section
    )

    # Normalize hyphens inside section identifiers
    section = section.replace(
        "-",
        ""
    )

    return section.lower()


# ===============================================================
# SECTION PATTERN
# ===============================================================

SECTION_PATTERN = (
    r"(\d+[a-z]?"
    r"(?:\s*\(\s*[a-z0-9-]+\s*\))?"
    r"(?:\s*\(\s*[a-z0-9-]+\s*\))?)"
)


# ===============================================================
# SENTENCE BOUNDARIES
# ===============================================================

def sentence_bounds(text, start, end):
    """
    Return the sentence containing the Act mention.

    This is safer than blindly taking a huge +/-300 character
    context because nearby section references may belong to
    another Act.
    """

    left_candidates = [
        text.rfind(".", 0, start),
        text.rfind(";", 0, start),
        text.rfind(":", 0, start),
        text.rfind("?", 0, start),
        text.rfind("!", 0, start),
    ]

    left = max(left_candidates)

    right_candidates = [
        p
        for p in [
            text.find(".", end),
            text.find(";", end),
            text.find(":", end),
            text.find("?", end),
            text.find("!", end),
        ]
        if p != -1
    ]

    if right_candidates:
        right = min(right_candidates)
    else:
        right = len(text)

    return (
        max(0, left + 1),
        min(len(text), right)
    )


# ===============================================================
# ACT VARIANTS
# ===============================================================

def build_act_variants(title):
    """
    Build safe variants of an Act name.

    These variants are used ONLY for determining whether a
    nearby section belongs to the current Act.

    We deliberately avoid overly generic variants where possible.
    """

    variants = {
        normalize(title),
        normalize(
            title.replace(",", "")
        ),
    }

    # Remove final year
    without_year = re.sub(
        r",?\s+\d{4}$",
        "",
        title
    ).strip()

    if without_year:
        variants.add(
            normalize(without_year)
        )

        variants.add(
            normalize(
                without_year.replace(",", "")
            )
        )

    title_lower = title.lower()

    # -----------------------------------------------------------
    # Common legal abbreviations
    # -----------------------------------------------------------

    if "negotiable instruments act" in title_lower:
        variants.update({
            "negotiable instruments act",
            "negotiable instrument act",
            "n. i. act",
            "n.i. act",
            "ni act",
        })

    if "income-tax act" in title_lower:
        variants.update({
            "income-tax act",
            "income tax act",
            "i.t. act",
            "it act",
        })

    if "companies act" in title_lower:
        variants.add(
            "companies act"
        )

    if "transfer of property act" in title_lower:
        variants.update({
            "transfer of property act",
            "tp act",
        })

    if "registration act" in title_lower:
        variants.update({
            "registration act",
            "indian registration act",
        })

    if "specific relief act" in title_lower:
        variants.add(
            "specific relief act"
        )

    if "land acquisition act" in title_lower:
        variants.add(
            "land acquisition act"
        )

    if "general clauses act" in title_lower:
        variants.add(
            "general clauses act"
        )

    if "indian evidence act" in title_lower:
        variants.update({
            "indian evidence act",
            "evidence act",
        })

    if "limitation act" in title_lower:
        variants.update({
            "limitation act",
            "indian limitation act",
        })

    if "motor vehicles act" in title_lower:
        variants.add(
            "motor vehicles act"
        )

    if "finance act" in title_lower:
        variants.add(
            "finance act"
        )

    if "sebi act" in title_lower:
        variants.update({
            "sebi act",
            "securities and exchange board of india act",
        })

    return sorted(
        {
            normalize(v)
            for v in variants
            if v
        },
        key=len,
        reverse=True
    )


# ===============================================================
# EXPLICIT SECTION ↔ ACT ASSOCIATION
# ===============================================================

def extract_explicit_sections(
    text,
    act_start,
    act_end,
    act_title
):
    """
    Extract sections explicitly associated with the Act.

    Supported forms:

        section 53A of the Transfer of Property Act, 1882

        section 17(1-A) of the Registration Act, 1908

        under section 138 of the Negotiable Instruments Act

        u/s 138 of the Negotiable Instruments Act

        section 138, Negotiable Instruments Act

        Transfer of Property Act, 1882, section 53A

    IMPORTANT:

    We do NOT collect every section number within a large
    surrounding context.

    A section is attached to an Act only when the text gives
    reasonably strong evidence that the section belongs to
    that Act.
    """

    sections = []

    def add(section):

        section = normalize_section(section)

        if section and section not in sections:
            sections.append(section)

    act_variants = build_act_variants(
        act_title
    )

    # -----------------------------------------------------------
    # Context around Act mention
    # -----------------------------------------------------------

    context_start = max(
        0,
        act_start - 220
    )

    context_end = min(
        len(text),
        act_end + 220
    )

    context = text[
        context_start:context_end
    ]

    # -----------------------------------------------------------
    # Pattern 1
    #
    # section 53A of the Transfer of Property Act
    #
    # Section occurs BEFORE Act.
    # -----------------------------------------------------------

    pattern_before = re.compile(
        r"\bsections?\s+"
        + SECTION_PATTERN
        + r"\s+of\s+"
        r"(?:the\s+)?",
        re.IGNORECASE
    )

    for match in pattern_before.finditer(
        context
    ):

        absolute_start = (
            context_start +
            match.start()
        )

        # Look at the text immediately following
        # "section X of"
        after = context[
            match.end():
            match.end() + 180
        ]

        after_normalized = normalize(
            after
        )

        matched_act = any(
            variant in after_normalized
            for variant in act_variants
        )

        if matched_act:

            # Must be reasonably close to the
            # current Act mention.
            if abs(
                absolute_start - act_start
            ) <= 220:

                add(
                    match.group(1)
                )

    # -----------------------------------------------------------
    # Pattern 2
    #
    # under section 138 of the Act
    # u/s 138 of the Act
    #
    # Only accept if the current Act is nearby.
    # -----------------------------------------------------------

    pattern_under = re.compile(
        r"\b(?:under|u/s)\s+"
        r"section\s+"
        + SECTION_PATTERN
        + r"\s+of\s+"
        r"(?:the\s+)?"
        r"(?:said\s+)?"
        r"(?:above\s+)?"
        r"(?:act\b)?",
        re.IGNORECASE
    )

    for match in pattern_under.finditer(
        context
    ):

        absolute_start = (
            context_start +
            match.start()
        )

        if abs(
            absolute_start - act_start
        ) <= 220:

            # This pattern is intentionally accepted
            # only when the phrase occurs very close
            # to the Act mention.
            add(
                match.group(1)
            )

    # -----------------------------------------------------------
    # Pattern 3
    #
    # section 138, Negotiable Instruments Act
    # -----------------------------------------------------------

    pattern_comma = re.compile(
        r"\bsections?\s+"
        + SECTION_PATTERN
        + r"\s*,\s*",
        re.IGNORECASE
    )

    for match in pattern_comma.finditer(
        context
    ):

        absolute_start = (
            context_start +
            match.start()
        )

        after = context[
            match.end():
            match.end() + 180
        ]

        after_normalized = normalize(
            after
        )

        matched_act = any(
            variant in after_normalized
            for variant in act_variants
        )

        if matched_act:

            if abs(
                absolute_start - act_start
            ) <= 180:

                add(
                    match.group(1)
                )

    # -----------------------------------------------------------
    # Pattern 4
    #
    # Act ... section X
    #
    # Example:
    #
    # Transfer of Property Act, 1882, section 53A
    #
    # This is weaker, so we require the same sentence.
    # -----------------------------------------------------------

    s_start, s_end = sentence_bounds(
        text,
        act_start,
        act_end
    )

    sentence = text[
        s_start:s_end
    ]

    # Distance within same sentence
    # must remain reasonably small.
    section_regex = re.compile(
        r"\b(?:section|s\.|u/s)\s*"
        + SECTION_PATTERN,
        re.IGNORECASE
    )

    for match in section_regex.finditer(
        sentence
    ):

        absolute_start = (
            s_start +
            match.start()
        )

        if abs(
            absolute_start - act_start
        ) <= 180:

            # If this section is already captured,
            # no need to add it again.
            add(
                match.group(1)
            )

    # -----------------------------------------------------------
    # Pattern 5
    #
    # "the Act, section X"
    #
    # This is also restricted to the same sentence.
    # -----------------------------------------------------------

    if act_end <= s_end:

        after_act = text[
            act_end:
            min(
                s_end,
                act_end + 180
            )
        ]

        after_act_sections = re.finditer(
            r"\b(?:section|s\.|u/s)\s*"
            + SECTION_PATTERN,
            after_act,
            re.IGNORECASE
        )

        for match in after_act_sections:

            add(
                match.group(1)
            )

    return sections


# ===============================================================
# FIND ACT REFERENCES
# ===============================================================

def find_references(
    text,
    act_id,
    title,
    aliases,
    status
):

    references = []

    all_aliases = set(
        aliases
    )

    # Local Acts get safe generated aliases.
    if status == "local":

        all_aliases.update(
            local_aliases(
                act_id,
                title
            )
        )

    # Also ensure the canonical title itself
    # is always searchable.
    all_aliases.add(
        title
    )

    # Longest aliases first.
    all_aliases = sorted(
        all_aliases,
        key=len,
        reverse=True
    )

    normalized_text = normalize(
        text
    )

    # -----------------------------------------------------------
    # Search every alias
    # -----------------------------------------------------------

    for alias in all_aliases:

        normalized_alias = normalize(
            alias
        )

        if len(normalized_alias) < 6:
            continue

        pattern = re.escape(
            normalized_alias
        )

        for match in re.finditer(
            pattern,
            normalized_text,
            flags=re.IGNORECASE
        ):

            start = match.start()
            end = match.end()

            # ---------------------------------------------------
            # Extract Act-associated sections
            # ---------------------------------------------------

            sections = extract_explicit_sections(
                normalized_text,
                start,
                end,
                title
            )

            # ---------------------------------------------------
            # Evidence context
            # ---------------------------------------------------

            context_start = max(
                0,
                start - 180
            )

            context_end = min(
                len(normalized_text),
                end + 180
            )

            context = normalized_text[
                context_start:context_end
            ]

            references.append({
                "alias_used": alias,
                "sections": sections,
                "context": context,
                "start": start,
            })

    # -----------------------------------------------------------
    # Remove duplicates
    # -----------------------------------------------------------

    unique = []

    seen = set()

    for ref in references:

        key = (
            ref["start"],
            normalize(
                ref["alias_used"]
            )
        )

        if key in seen:
            continue

        seen.add(key)

        unique.append(
            ref
        )

    return unique


# ===============================================================
# MAIN
# ===============================================================

def main():

    RESULTS_DIR.mkdir(
        exist_ok=True
    )

    # -----------------------------------------------------------
    # Load catalogues
    # -----------------------------------------------------------

    local_acts = load_local_acts()

    external_acts = load_external_acts()

    external_aliases = load_external_aliases()

    all_acts = {}

    all_acts.update(
        local_acts
    )

    all_acts.update(
        external_acts
    )

    # -----------------------------------------------------------
    # Header
    # -----------------------------------------------------------

    print("=" * 90)
    print("JUDGMENT ↔ ACT LINK EXTRACTION")
    print("=" * 90)
    print()

    print(
        f"Local Acts:    {len(local_acts)}"
    )

    print(
        f"External Acts: {len(external_acts)}"
    )

    print(
        f"Total Acts:    {len(all_acts)}"
    )

    print()

    # -----------------------------------------------------------
    # Judgment files
    # -----------------------------------------------------------

    judgment_files = sorted(
        JUDGMENT_DIR.glob(
            "*.txt"
        )
    )

    print(
        f"Judgments found: {len(judgment_files)}"
    )

    print()

    links = []

    # ===========================================================
    # PROCESS JUDGMENTS
    # ===========================================================

    for judgment_file in judgment_files:

        judgment_id = (
            judgment_file.stem
        )

        text = judgment_file.read_text(
            encoding="utf-8",
            errors="ignore"
        )

        # -------------------------------------------------------
        # Process every Act
        # -------------------------------------------------------

        for act_id, act in all_acts.items():

            if act["status"] == "external":

                aliases = external_aliases.get(
                    act_id,
                    []
                )

            else:

                aliases = []

            references = find_references(
                text=text,
                act_id=act_id,
                title=act["title"],
                aliases=aliases,
                status=act["status"],
            )

            # ---------------------------------------------------
            # Convert references to links
            # ---------------------------------------------------

            for reference in references:

                links.append({

                    "judgment_id":
                        judgment_id,

                    "act_id":
                        act_id,

                    "act_title":
                        act["title"],

                    "act_status":
                        act["status"],

                    "alias_used":
                        reference["alias_used"],

                    "sections":
                        "; ".join(
                            reference["sections"]
                        ),

                    "context":
                        reference["context"],
                })

    # ===========================================================
    # CSV OUTPUT
    # ===========================================================

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
                "alias_used",
                "sections",
                "context",
            ],
        )

        writer.writeheader()

        writer.writerows(
            links
        )

    # ===========================================================
    # JSON OUTPUT
    # ===========================================================

    grouped = defaultdict(
        list
    )

    for link in links:

        grouped[
            link["judgment_id"]
        ].append(
            link
        )

    OUTPUT_JSON.write_text(
        json.dumps(
            grouped,
            indent=2,
            ensure_ascii=False,
        ),
        encoding="utf-8",
    )

    # ===========================================================
    # ACT COVERAGE
    # ===========================================================

    act_counts = defaultdict(
        set
    )

    for link in links:

        act_counts[
            link["act_id"]
        ].add(
            link["judgment_id"]
        )

    # ===========================================================
    # HUMAN-READABLE REPORT
    # ===========================================================

    lines = []

    lines.append(
        "=" * 90
    )

    lines.append(
        "JUDGMENT ↔ ACT RELATIONSHIP REPORT"
    )

    lines.append(
        "=" * 90
    )

    lines.append("")

    lines.append(
        f"Judgments processed: "
        f"{len(judgment_files)}"
    )

    lines.append(
        f"Raw Act references: "
        f"{len(links)}"
    )

    lines.append(
        f"Acts with relationships: "
        f"{len(act_counts)}"
    )

    lines.append("")

    lines.append(
        "-" * 90
    )

    lines.append(
        "TOP ACTS BY JUDGMENT COVERAGE"
    )

    lines.append(
        "-" * 90
    )

    ranked = sorted(
        act_counts.items(),
        key=lambda x: len(x[1]),
        reverse=True
    )

    for rank, (
        act_id,
        judgment_ids
    ) in enumerate(
        ranked[:50],
        start=1
    ):

        act = all_acts[
            act_id
        ]

        lines.append(
            f"{rank:3}. "
            f"{len(judgment_ids):3} judgments | "
            f"{act_id:10} | "
            f"{act['title']}"
        )

    OUTPUT_TXT.write_text(
        "\n".join(lines),
        encoding="utf-8"
    )

    # ===========================================================
    # CONSOLE SUMMARY
    # ===========================================================

    print("=" * 90)
    print("RESULT")
    print("=" * 90)
    print()

    print(
        f"Judgments processed: "
        f"{len(judgment_files)}"
    )

    print(
        f"Total Act relationships: "
        f"{len(links)}"
    )

    print(
        f"Acts linked to judgments: "
        f"{len(act_counts)}"
    )

    print()

    print("Saved:")

    print(
        f"  {OUTPUT_CSV}"
    )

    print(
        f"  {OUTPUT_JSON}"
    )

    print(
        f"  {OUTPUT_TXT}"
    )

    print()

    print(
        "-" * 90
    )

    print(
        "TOP 20 ACTS"
    )

    print(
        "-" * 90
    )

    for rank, (
        act_id,
        judgment_ids
    ) in enumerate(
        ranked[:20],
        start=1
    ):

        act = all_acts[
            act_id
        ]

        print(
            f"{rank:2}. "
            f"{len(judgment_ids):3} judgments | "
            f"{act['title']}"
        )


# ===============================================================
# ENTRY POINT
# ===============================================================

if __name__ == "__main__":
    main()

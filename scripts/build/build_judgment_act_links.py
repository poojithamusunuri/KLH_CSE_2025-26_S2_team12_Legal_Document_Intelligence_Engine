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

        # IMPORTANT:
        # Companies Act has multiple historical versions:
        #
        #   Companies Act, 1956
        #   Companies Act, 2013
        #
        # Therefore the generic "companies act" variant must
        # NEVER be used to identify a specific Act.
        #
        # Only year-specific variants are allowed.

        year_match = re.search(
            r"\\b(18|19|20)\\d{2}\\b",
            title
        )

        if year_match:

            year = year_match.group(0)

            # Remove generic version if present.
            act_variants = {
                v
                for v in act_variants
                if normalize(v) != "companies act"
            }

            # Add only the correct year-specific variants.
            act_variants.update({
                normalize(f"companies act, {year}"),
                normalize(f"companies act {year}"),
            })

    if "income-tax act" in title_lower:
        act_variants.update({
            "income-tax act",
            "income tax act",
        })

    if "negotiable instruments act" in title_lower:
        act_variants.update({
            "negotiable instruments act",
            "negotiable instrument act",
            "ni act",
            "n.i. act",
        })

    if "motor vehicles act" in title_lower:
        act_variants.add(
            "motor vehicles act"
        )

    if "finance act" in title_lower:
        act_variants.add(
            "finance act"
        )

    if "sebi act" in title_lower:
        act_variants.update({
            "sebi act",
            "securities and exchange board of india act",
        })

    act_variants = sorted(
        {
            normalize(v)
            for v in act_variants
            if v
        },
        key=len,
        reverse=True
    )

    # ===========================================================
    # 1. EXPLICIT SECTION + ACT REFERENCES
    #
    # Only capture a section when the judgment explicitly writes:
    #
    #   section 53a of the Transfer of Property Act, 1882
    #   section 17(1-a) of the Registration Act, 1908
    #   section 23 of the Indian Contract Act, 1872
    #
    # This is the strongest and safest form of relationship.
    # ===========================================================

    explicit_pattern = (
        r"\b(?:section|sec\.|s\.)\s*"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
        r"\s+of\s+(?:the\s+)?"
        r"([a-z][a-z0-9 ,.\-()&]*?\bact\b"
        r"(?:\s*,?\s*\d{4})?)"
    )

    for match in re.finditer(
        explicit_pattern,
        normalized_text,
        flags=re.IGNORECASE
    ):

        section = normalize_section(
            match.group(1)
        )

        mentioned_act = normalize(
            match.group(2)
        )

        # -------------------------------------------------------
        # Determine whether the mentioned Act is THIS Act.
        #
        # IMPORTANT:
        #
        # For an EXPLICIT legal construction such as:
        #
        #   section 10 of the specific relief act
        #   section 40 of the transfer of property act
        #   section 17 of the registration act
        #
        # a year-less Act name is acceptable.
        #
        # This is safe because the section itself is explicitly
        # attached to the Act.
        #
        # For STANDALONE Act-name matching later in this function,
        # we still use the safer year-specific aliases.
        # -------------------------------------------------------

        belongs_to_current_act = False

        canonical_title = normalize(title)

        canonical_without_comma = normalize(
            title.replace(",", "")
        )

        # Exact canonical forms.
        exact_variants = {
            canonical_title,
            canonical_without_comma,
        }

        # -------------------------------------------------------
        # Add year-specific supplied aliases.
        # -------------------------------------------------------

        for alias in aliases:

            normalized_alias = normalize(alias)

            if re.search(
                r"\b\d{4}\b",
                normalized_alias
            ):
                exact_variants.add(
                    normalized_alias
                )

        # -------------------------------------------------------
        # Local Act aliases are also year-specific.
        # -------------------------------------------------------

        if status == "local":

            for alias in local_aliases(
                act_id,
                title
            ):

                normalized_alias = normalize(alias)

                if re.search(
                    r"\b\d{4}\b",
                    normalized_alias
                ):
                    exact_variants.add(
                        normalized_alias
                    )

        # -------------------------------------------------------
        # FIRST: exact year-specific matching.
        # -------------------------------------------------------

        mentioned_no_comma = mentioned_act.replace(
            ",",
            ""
        )

        for variant in exact_variants:

            if (
                mentioned_act == variant
                or mentioned_no_comma == variant.replace(",", "")
            ):
                belongs_to_current_act = True
                break

        # -------------------------------------------------------
        # SECOND: explicit section references may safely use the
        # year-less form of THIS Act.
        #
        # Example:
        #
        #   section 10 of the specific relief act
        #
        # should match:
        #
        #   Specific Relief Act, 1963
        #
        # But we DO NOT use this relaxed rule for standalone
        # Act-name matching below.
        # -------------------------------------------------------

        if not belongs_to_current_act:

            mentioned_without_year = re.sub(
                r",?\s+\d{4}$",
                "",
                mentioned_act
            ).strip()

            title_without_year = re.sub(
                r",?\s+\d{4}$",
                "",
                canonical_title
            ).strip()

            if (
                mentioned_without_year
                == title_without_year
            ):
                belongs_to_current_act = True

        # -------------------------------------------------------
        # THIRD: harmless punctuation difference.
        # -------------------------------------------------------

        if not belongs_to_current_act:

            mentioned_no_comma = mentioned_act.replace(
                ",",
                ""
            )

            title_no_comma = canonical_title.replace(
                ",",
                ""
            )

            title_no_year_no_comma = re.sub(
                r"\s+\d{4}$",
                "",
                title_no_comma
            ).strip()

            mentioned_no_year_no_comma = re.sub(
                r"\s+\d{4}$",
                "",
                mentioned_no_comma
            ).strip()

            if (
                mentioned_no_year_no_comma
                == title_no_year_no_comma
            ):
                belongs_to_current_act = True

        if not belongs_to_current_act:
            continue

        # -------------------------------------------------------
        # Store the explicit relationship.
        # -------------------------------------------------------

        start_pos = match.start()
        end_pos = match.end()

        context_start = max(
            0,
            start_pos - 180
        )

        context_end = min(
            len(normalized_text),
            end_pos + 180
        )

        context = normalized_text[
            context_start:context_end
        ]

        references.append({
            "alias_used": mentioned_act,
            "sections": [section],
            "context": context,
            "start": start_pos,
        })

    # ===========================================================
    # 2. STANDALONE ACT REFERENCES
    #
    # Examples:
    #
    #   the Registration Act 1908
    #   the Transfer of Property Act, 1882
    #
    # These prove that the judgment mentions the Act, but we
    # DO NOT infer nearby sections.
    #
    # This prevents unrelated sections from being attached to
    # the Act merely because they occur nearby in reproduced
    # statutory text.
    # ===========================================================

    explicit_positions = {
        ref["start"]
        for ref in references
    }

    for alias in all_aliases:

        pattern = re.escape(alias)

        for match in re.finditer(
            pattern,
            normalized_text,
            flags=re.IGNORECASE
        ):

            start_pos = match.start()
            end_pos = match.end()

            # ---------------------------------------------------
            # Skip occurrences already represented by an
            # explicit "section X of Act" reference.
            # ---------------------------------------------------

            already_explicit = False

            for explicit_start in explicit_positions:

                if abs(
                    start_pos - explicit_start
                ) <= 250:

                    already_explicit = True
                    break

            if already_explicit:
                continue

            # ---------------------------------------------------
            # Standalone Act mention.
            #
            # IMPORTANT:
            # Do NOT call extract_sections() here.
            # Nearby sections may belong to another Act or to
            # reproduced statutory text.
            # ---------------------------------------------------

            context_start = max(
                0,
                start_pos - 180
            )

            context_end = min(
                len(normalized_text),
                end_pos + 180
            )

            context = normalized_text[
                context_start:context_end
            ]

            references.append({
                "alias_used": alias,
                "sections": [],
                "context": context,
                "start": start_pos,
            })

# ===========================================================
    # 3. DEDUPLICATE
    # ===========================================================

    unique = []

    seen = set()

    for ref in sorted(
        references,
        key=lambda r: r["start"]
    ):

        key = (
            ref["start"],
            normalize(
                ref["alias_used"]
            ),
            tuple(
                ref["sections"]
            )
        )

        if key in seen:
            continue

        seen.add(key)
        unique.append(ref)

    return unique

# ===============================================================
# SECTION EXTRACTION
# ===============================================================

def extract_sections(
    text,
    start,
    end,
    act_title=None
):
    """
    Extract section numbers that are strongly associated with
    a particular Act occurrence.

    This function intentionally uses a small local window.

    Examples:

        section 53-A of the Transfer of Property Act
            -> 53a

        section 17(1-a) of the Registration Act
            -> 17(1a)

        section 138 of the Negotiable Instruments Act
            -> 138

    It does NOT collect every section appearing hundreds of
    characters away from the Act name.
    """

    sections = []

    normalized_text = normalize(text)

    def add_section(section):

        if not section:
            return

        section = normalize_section(
            section
        )

        if (
            section
            and section not in sections
        ):
            sections.append(
                section
            )

    # ===========================================================
    # 1. Section immediately BEFORE the Act name
    #
    # Example:
    #
    # section 53-a of the transfer of property act
    #
    # ===========================================================

    before_text = normalized_text[
        max(0, start - 120):start
    ]

    before_patterns = [

        r"\bsection\s+"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
        r"\s+of\s+(?:the\s+)?$",

        r"\bsec\.\s*"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
        r"\s+of\s+(?:the\s+)?$",

        r"\bs\.\s*"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
        r"\s+of\s+(?:the\s+)?$",
    ]

    for pattern in before_patterns:

        match = re.search(
            pattern,
            before_text,
            flags=re.IGNORECASE
        )

        if match:
            add_section(
                match.group(1)
            )

    # ===========================================================
    # 2. Section immediately AFTER the Act name
    #
    # Example:
    #
    # Registration Act, 1908, section 17
    #
    # ===========================================================

    after_text = normalized_text[
        end:
        min(
            len(normalized_text),
            end + 120
        )
    ]

    after_pattern = (
        r"^\s*[,;:\-\)]*\s*"
        r"(?:section|sec\.|s\.)\s*"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
    )

    match = re.search(
        after_pattern,
        after_text,
        flags=re.IGNORECASE
    )

    if match:
        add_section(
            match.group(1)
        )

    # ===========================================================
    # 3. Very local explicit "section X of ACT" relationship
    #
    # This is intentionally limited to a small context.
    # ===========================================================

    if act_title:

        normalized_title = normalize(
            act_title
        )

        context_start = max(
            0,
            start - 120
        )

        context_end = min(
            len(normalized_text),
            end + 120
        )

        context = normalized_text[
            context_start:context_end
        ]

        explicit_pattern = (
            r"\b(?:section|sec\.|s\.)\s*"
            r"(\d+[a-z]?"
            r"(?:\s*-\s*[a-z])?"
            r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
            r")"
            r"\s+of\s+(?:the\s+)?"
            + re.escape(normalized_title)
        )

        for match in re.finditer(
            explicit_pattern,
            context,
            flags=re.IGNORECASE
        ):

            add_section(
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
    """
    Find references to a specific Act.

    Handles:

    1. Explicit section + Act references.
    2. Standalone Act-name references.
    3. Historical Act versions such as:
           Companies Act, 1956
           Companies Act, 2013
    4. Yearless references for historical Acts using
       document-level version evidence.
    """

    references = []
    normalized_text = normalize(text)

    # ===========================================================
    # BUILD ALIASES
    # ===========================================================

    all_aliases = set(aliases or [])

    if status == "local":
        all_aliases.update(
            local_aliases(
                act_id,
                title
            )
        )

    all_aliases.add(title)

    all_aliases = sorted(
        {
            normalize(alias)
            for alias in all_aliases
            if alias and len(normalize(alias)) >= 6
        },
        key=len,
        reverse=True
    )

    # ===========================================================
    # ACT VARIANTS
    # ===========================================================

    canonical_title = normalize(title)

    act_variants = {
        canonical_title,
        normalize(title.replace(",", ""))
    }

    without_year = re.sub(
        r",?\s+\d{4}$",
        "",
        canonical_title
    ).strip()

    if without_year:
        act_variants.add(without_year)
        act_variants.add(
            normalize(without_year.replace(",", ""))
        )

    title_lower = title.lower()

    if "registration act" in title_lower:
        act_variants.update({
            "registration act",
            "indian registration act",
        })

    if "transfer of property act" in title_lower:
        act_variants.update({
            "transfer of property act",
            "tp act",
        })

    if "specific relief act" in title_lower:
        act_variants.add(
            "specific relief act"
        )

    if "land acquisition act" in title_lower:
        act_variants.add(
            "land acquisition act"
        )

    if "general clauses act" in title_lower:
        act_variants.add(
            "general clauses act"
        )

    if "indian evidence act" in title_lower:
        act_variants.update({
            "indian evidence act",
            "evidence act",
        })

    if "limitation act" in title_lower:
        act_variants.update({
            "limitation act",
            "indian limitation act",
        })

    # IMPORTANT:
    #
    # Do NOT add generic "companies act".
    #
    # Companies Act, 1956 and Companies Act, 2013
    # must remain separate.

    if "income-tax act" in title_lower:
        act_variants.update({
            "income-tax act",
            "income tax act",
        })

    if "negotiable instruments act" in title_lower:
        act_variants.update({
            "negotiable instruments act",
            "negotiable instrument act",
            "ni act",
            "n.i. act",
        })

    if "motor vehicles act" in title_lower:
        act_variants.add(
            "motor vehicles act"
        )

    if "finance act" in title_lower:
        act_variants.add(
            "finance act"
        )

    if "sebi act" in title_lower:
        act_variants.update({
            "sebi act",
            "securities and exchange board of india act",
        })

    act_variants = {
        normalize(v)
        for v in act_variants
        if v
    }

    # ===========================================================
    # HISTORICAL VERSION DETECTION
    # ===========================================================

    title_year_match = re.search(
        r"\b(18|19|20)\d{2}\b",
        canonical_title
    )

    title_year = (
        title_year_match.group(0)
        if title_year_match
        else None
    )

    historical_ambiguous = (
        "companies act" in title_lower
    )

    # ===========================================================
    # DOCUMENT-LEVEL HISTORICAL VERSION EVIDENCE
    # ===========================================================
    #
    # For Companies Act:
    #
    # If the document explicitly mentions:
    #
    #     Companies Act, 1956
    #
    # and never mentions:
    #
    #     Companies Act, 2013
    #
    # then yearless references such as:
    #
    #     section 536 of the Companies Act
    #
    # can safely resolve to 1956.
    #
    # ===========================================================

    document_version = None

    if historical_ambiguous:
        explicit_1956 = re.search(
            r"\bcompanies\s+act\s*,?\s*1956\b",
            normalized_text,
            flags=re.IGNORECASE
        )

        explicit_2013 = re.search(
            r"\bcompanies\s+act\s*,?\s*2013\b",
            normalized_text,
            flags=re.IGNORECASE
        )

        if explicit_1956 and not explicit_2013:
            document_version = "1956"

        elif explicit_2013 and not explicit_1956:
            document_version = "2013"

    # ===========================================================
    # EXPLICIT SECTION + ACT REFERENCES
    # ===========================================================

    explicit_pattern = (
        r"\b(?:section|sec\.|s\.)\s*"
        r"(\d+[a-z]?"
        r"(?:\s*-\s*[a-z])?"
        r"(?:\s*\(\s*[a-z0-9-]+\s*\))*"
        r")"
        r"\s+of\s+(?:the\s+)?"
        r"([a-z][a-z0-9.-]*(?:\s+[a-z][a-z0-9.-]*){0,12}"
r"\s+act\b"
r"(?:\s*,?\s*\d{4})?)"
    )

    explicit_positions = []

    for match in re.finditer(
        explicit_pattern,
        normalized_text,
        flags=re.IGNORECASE
    ):

        section = normalize_section(
            match.group(1)
        )

        mentioned_act = normalize(
            match.group(2)
        )

        mentioned_year_match = re.search(
            r"\b(18|19|20)\d{2}\b",
            mentioned_act
        )

        mentioned_year = (
            mentioned_year_match.group(0)
            if mentioned_year_match
            else None
        )

        belongs_to_current_act = False

        # -------------------------------------------------------
        # YEAR-QUALIFIED REFERENCE
        # -------------------------------------------------------

        if title_year and mentioned_year:

            belongs_to_current_act = (
                title_year == mentioned_year
            )

        # -------------------------------------------------------
        # YEARLESS REFERENCE
        # -------------------------------------------------------

        elif not mentioned_year:

            # Historical Acts:
            #
            # Use document-level version evidence.

            if historical_ambiguous:

                # A yearless reference can inherit the document-level
                # Companies Act version ONLY when the referenced Act
                # is actually "companies act".
                #
                # Do NOT inherit unrelated references such as:
                #
                #   section 61 of the co-operative societies act
                #   section 170 of the gujarat act
                #   section 28 of this act
                #
                # Those belong to other Acts.

                mentioned_base = re.sub(
                    r",?\s+\d{4}$",
                    "",
                    mentioned_act
                ).strip()

                if (
                    document_version
                    and title_year
                    and document_version == title_year
                    and mentioned_base == "companies act"
                ):
                    belongs_to_current_act = True

            else:

                for variant in act_variants:

                    if (
                        mentioned_act == variant
                        or variant in mentioned_act
                        or mentioned_act in variant
                    ):
                        belongs_to_current_act = True
                        break

        # -------------------------------------------------------
        # NON-HISTORICAL ACTS
        # -------------------------------------------------------

        elif not title_year:

            for variant in act_variants:

                if (
                    mentioned_act == variant
                    or variant in mentioned_act
                    or mentioned_act in variant
                ):
                    belongs_to_current_act = True
                    break

        if not belongs_to_current_act:
            continue

        start_pos = match.start()
        end_pos = match.end()

        context_start = max(
            0,
            start_pos - 180
        )

        context_end = min(
            len(normalized_text),
            end_pos + 180
        )

        context = normalized_text[
            context_start:context_end
        ]

        references.append({
            "alias_used": mentioned_act,
            "sections": [section],
            "context": context,
            "start": start_pos,
        })

        explicit_positions.append(
            start_pos
        )

    # ===========================================================
    # STANDALONE ACT-NAME REFERENCES
    # ===========================================================

    for alias in all_aliases:

        pattern = re.escape(alias)

        for match in re.finditer(
            pattern,
            normalized_text,
            flags=re.IGNORECASE
        ):

            start_pos = match.start()
            end_pos = match.end()

            matched_text = normalize(
                match.group(0)
            )

            mentioned_year_match = re.search(
                r"\b(18|19|20)\d{2}\b",
                matched_text
            )

            mentioned_year = (
                mentioned_year_match.group(0)
                if mentioned_year_match
                else None
            )

            # ---------------------------------------------------
            # HISTORICAL ACT PROTECTION
            # ---------------------------------------------------

            if historical_ambiguous:

                # A year-qualified occurrence must match.

                if mentioned_year:

                    if (
                        title_year
                        and mentioned_year != title_year
                    ):
                        continue

                # Yearless occurrence can only be accepted when
                # document-level evidence identifies the version.

                else:

                    if (
                        not document_version
                        or not title_year
                        or document_version != title_year
                    ):
                        continue

            elif (
                title_year
                and mentioned_year
                and mentioned_year != title_year
            ):
                continue

            # ---------------------------------------------------
            # Avoid duplicate explicit references.
            # ---------------------------------------------------

            already_explicit = False

            for explicit_start in explicit_positions:

                if abs(start_pos - explicit_start) <= 250:
                    already_explicit = True
                    break

            if already_explicit:
                continue

            # ---------------------------------------------------
            # Immediately associated section BEFORE Act.
            # ---------------------------------------------------

            sections = []

            before_text = normalized_text[
                max(0, start_pos - 100):
                start_pos
            ]

            before_patterns = [
                r"\bsection\s+"
                r"(\d+[a-z]?"
                r"(?:\s*-\s*[a-z])?"
                r"(?:\s*\([a-z0-9-]+\))*"
                r")"
                r"\s+of\s+(?:the\s+)?$",

                r"\bs\.?\s*"
                r"(\d+[a-z]?"
                r"(?:\s*-\s*[a-z])?"
                r"(?:\s*\([a-z0-9-]+\))*"
                r")"
                r"\s+of\s+(?:the\s+)?$",
            ]

            for pattern_before in before_patterns:

                before_match = re.search(
                    pattern_before,
                    before_text,
                    flags=re.IGNORECASE
                )

                if before_match:

                    section = normalize_section(
                        before_match.group(1)
                    )

                    if section not in sections:
                        sections.append(section)

            # ---------------------------------------------------
            # Immediately associated section AFTER Act.
            # ---------------------------------------------------

            after_text = normalized_text[
                end_pos:
                min(
                    len(normalized_text),
                    end_pos + 100
                )
            ]

            after_pattern = (
                r"^\s*[,;:\-\)]*\s*"
                r"(?:section|sec\.|s\.)\s*"
                r"(\d+[a-z]?"
                r"(?:\s*-\s*[a-z])?"
                r"(?:\s*\([a-z0-9-]+\))*"
                r")"
            )

            after_match = re.search(
                after_pattern,
                after_text,
                flags=re.IGNORECASE
            )

            if after_match:

                section = normalize_section(
                    after_match.group(1)
                )

                if section not in sections:
                    sections.append(section)

            context_start = max(
                0,
                start_pos - 180
            )

            context_end = min(
                len(normalized_text),
                end_pos + 180
            )

            context = normalized_text[
                context_start:context_end
            ]

            references.append({
                "alias_used": alias,
                "sections": sections,
                "context": context,
                "start": start_pos,
            })

    # ===========================================================
    # DEDUPLICATION
    # ===========================================================

    unique = []
    seen = set()

    for ref in sorted(
        references,
        key=lambda r: r["start"]
    ):

        key = (
            ref["alias_used"],
            tuple(ref["sections"]),
            ref["start"]
        )

        if key in seen:
            continue

        seen.add(key)
        unique.append(ref)

    # ===========================================================
    # SORT
    # ===========================================================

    unique.sort(
        key=lambda ref: ref["start"]
    )

    return unique



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

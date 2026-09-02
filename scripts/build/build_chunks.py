import json
import re
from pathlib import Path

CORPUS_DIR = Path("data/corpus")
OUTPUT_DIR = Path("data/processed")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

MAX_CHUNK_SIZE = 1800
MIN_CHUNK_SIZE = 150
OVERLAP = 200


def parse_document(file_path):
    text = file_path.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    metadata = {}
    content = text

    if "CONTENT:" in text:
        header, content = text.split("CONTENT:", 1)

        lines = header.splitlines()
        current_key = None
        current_value = []

        for line in lines:
            line = line.strip()

            if not line:
                continue

            if ":" in line:
                key, value = line.split(":", 1)

                if key.isupper():
                    if current_key:
                        metadata[current_key] = " ".join(
                            current_value
                        ).strip()

                    current_key = key.strip()
                    current_value = [value.strip()]

                else:
                    if current_key:
                        current_value.append(line)

            else:
                if current_key:
                    current_value.append(line)

        if current_key:
            metadata[current_key] = " ".join(
                current_value
            ).strip()

    return metadata, content.strip()


def clean_text(text):
    return re.sub(
        r"\s+",
        " ",
        text
    ).strip()


def split_large_text(text):
    """
    Split only large text blocks.
    Try paragraph and sentence boundaries first.
    """

    text = clean_text(text)

    if len(text) <= MAX_CHUNK_SIZE:
        return [text]

    chunks = []
    start = 0

    while start < len(text):

        remaining = len(text) - start

        if remaining <= MAX_CHUNK_SIZE:
            chunk = text[start:].strip()

            if chunk:
                chunks.append(chunk)

            break

        end = start + MAX_CHUNK_SIZE

        # Prefer paragraph boundaries
        split_point = text.rfind("\n", start, end)

        # Then sentence boundaries
        if split_point <= start + MIN_CHUNK_SIZE:
            split_point = max(
                text.rfind(". ", start, end),
                text.rfind("? ", start, end),
                text.rfind("! ", start, end)
            )

        # Then word boundaries
        if split_point <= start + MIN_CHUNK_SIZE:
            split_point = text.rfind(" ", start, end)

        # Final fallback
        if split_point <= start:
            split_point = end

        chunk = text[start:split_point].strip()

        if chunk:
            chunks.append(chunk)

        # Move forward WITHOUT starting in middle of word
        next_start = split_point

        if next_start < len(text) and text[next_start].isspace():
            next_start += 1

        # Add overlap safely
        overlap_start = max(
            start + 1,
            next_start - OVERLAP
        )

        # Move overlap start to nearest word boundary
        space_pos = text.find(" ", overlap_start)

        if space_pos != -1 and space_pos < next_start:
            start = space_pos + 1
        else:
            start = next_start

    return chunks


def split_by_sections(content):
    """
    Split legal documents by SECTION headings.
    """

    content = content.strip()

    pattern = r"(?=SECTION:\s*\d+)"

    parts = re.split(
        pattern,
        content,
        flags=re.IGNORECASE
    )

    parts = [
        clean_text(part)
        for part in parts
        if clean_text(part)
    ]

    # If no meaningful sections found,
    # process as one document
    if len(parts) <= 1:
        return split_large_text(content)

    chunks = []

    for part in parts:

        section_chunks = split_large_text(part)

        chunks.extend(section_chunks)

    return chunks


def merge_tiny_chunks(chunks):
    """
    Merge tiny chunks with the previous chunk
    so we don't have useless fragments.
    """

    if not chunks:
        return []

    merged = []

    for chunk in chunks:

        if (
            merged
            and len(chunk) < MIN_CHUNK_SIZE
            and len(merged[-1]) + len(chunk) + 1 <= MAX_CHUNK_SIZE
        ):
            merged[-1] += " " + chunk

        else:
            merged.append(chunk)

    return merged


def process_file(file_path, collection_name):

    metadata, content = parse_document(file_path)

    document_id = metadata.get(
        "DOCUMENT_ID",
        file_path.stem
    )

    title = metadata.get(
        "TITLE",
        file_path.stem
    )

    chunks = split_by_sections(content)

    chunks = merge_tiny_chunks(chunks)

    processed_chunks = []

    for index, chunk in enumerate(chunks):

        processed_chunks.append(
            {
                "chunk_id": f"{document_id}_chunk_{index + 1}",
                "document_id": document_id,
                "title": title,
                "document_type": metadata.get(
                    "DOCUMENT_TYPE",
                    collection_name
                ),
                "domain": metadata.get(
                    "DOMAIN",
                    "Unknown"
                ),
                "subdomain": metadata.get(
                    "SUBDOMAIN",
                    "Unknown"
                ),
                "jurisdiction": metadata.get(
                    "JURISDICTION",
                    "India"
                ),
                "source_type": metadata.get(
                    "SOURCE_TYPE",
                    "Unknown"
                ),
                "content": chunk
            }
        )

    return processed_chunks


def main():

    all_chunks = []

    collections = [
        "constitution",
        "acts",
        "legal_principles",
        "judgments"
    ]

    print("=" * 60)
    print("LEGAL DOCUMENT INTELLIGENCE ENGINE")
    print("BUILDING STRUCTURED LEGAL CHUNKS")
    print("=" * 60)

    for collection in collections:

        collection_path = CORPUS_DIR / collection

        if not collection_path.exists():
            print(f"\nFolder not found: {collection}")
            continue

        files = list(collection_path.glob("*.txt"))

        collection_chunks = []

        for file_path in files:

            chunks = process_file(
                file_path,
                collection
            )

            collection_chunks.extend(chunks)
            all_chunks.extend(chunks)

        print(f"\n{collection.upper()}")
        print(f"Documents: {len(files)}")
        print(f"Chunks: {len(collection_chunks)}")

    output_file = OUTPUT_DIR / "legal_chunks.json"

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            all_chunks,
            file,
            ensure_ascii=False,
            indent=2
        )

    print("\n" + "=" * 60)
    print("STRUCTURED CHUNKING COMPLETE")
    print("=" * 60)

    print(f"Total chunks: {len(all_chunks)}")
    print(f"Saved to: {output_file}")


if __name__ == "__main__":
    main()

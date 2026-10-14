from datasets import load_dataset
from pathlib import Path
import re

print("Loading Indian legal judgments dataset...")

dataset = load_dataset(
    "opennyaiorg/InJudgements_dataset",
    split="train",
    streaming=True
)

BASE_DIR = Path("data/corpus")

# Create judgments folder
(BASE_DIR / "judgments").mkdir(parents=True, exist_ok=True)


def clean_text(text):
    if not text:
        return ""

    text = re.sub(r"\s+", " ", text)
    return text.strip()


def is_good_document(title, text):
    if not title or not text:
        return False

    # Ensure meaningful legal content
    if len(text) < 1500:
        return False

    return True


count = 0
max_documents = 150

print("\nGenerating legal corpus...\n")

for item in dataset:

    if count >= max_documents:
        break

    title = str(item.get("Titles", "")).strip()
    text = clean_text(str(item.get("Text", "")))

    if not is_good_document(title, text):
        continue

    court_name = str(
        item.get("Court_Name_Normalized")
        or item.get("Court_Name")
        or "Indian Court"
    ).strip()

    case_type = str(
        item.get("Case_Type")
        or "Court Judgment"
    ).strip()

    court_type = str(
        item.get("Court_Type")
        or "Indian Judiciary"
    ).strip()

    document_url = str(
        item.get("Doc_url")
        or ""
    ).strip()

    count += 1

    document_id = f"JUDG{count:03d}"

    filename = f"judgment_{count:03d}.txt"

    filepath = BASE_DIR / "judgments" / filename

    content = f"""DOCUMENT_ID: {document_id}

TITLE: {title}

DOCUMENT_TYPE: Judgment

JURISDICTION: India

AUTHORITY: {court_name}

COURT_TYPE: {court_type}

CASE_TYPE: {case_type}

SOURCE: OpenNyAI InJudgements Dataset

SOURCE_URL: {document_url}

CONTENT:

{text}
"""

    filepath.write_text(content, encoding="utf-8")

    print(f"[{count}/150] Saved: {filename}")
    print(f"        {title[:100]}")

print("\n========================================")
print("CORPUS GENERATION COMPLETE")
print("========================================")
print(f"Total documents created: {count}")
print(f"Location: {BASE_DIR / 'judgments'}")

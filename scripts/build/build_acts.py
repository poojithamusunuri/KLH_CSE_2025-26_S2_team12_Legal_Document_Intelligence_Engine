from datasets import load_dataset
from pathlib import Path
import re

print("Loading Indian laws dataset...")

dataset = load_dataset(
    "manjot007/Indian-Laws",
    split="train",
    streaming=True
)

BASE_DIR = Path("data/corpus/acts")
BASE_DIR.mkdir(parents=True, exist_ok=True)

# Remove old generated Act files
for file in BASE_DIR.glob("*.txt"):
    file.unlink()


def clean_text(text):
    if not text:
        return ""

    text = re.sub(r'\s+', ' ', str(text))
    return text.strip()


def clean_filename(name):
    """Create a safe filename."""
    name = re.sub(r'[^a-zA-Z0-9]+', '_', name.lower())
    return name.strip('_')[:60]


max_acts = 40
max_sections_per_act = 25

acts = {}

print("\nCollecting legal Acts and sections...\n")

for item in dataset:

    title = clean_text(item.get("act_title", ""))
    section = clean_text(item.get("section", ""))
    law_text = clean_text(item.get("law", ""))

    if not title or not law_text:
        continue

    # Create dictionary entry for each Act
    if title not in acts:

        if len(acts) >= max_acts:
            continue

        acts[title] = []

    # Limit sections stored per Act
    if len(acts[title]) >= max_sections_per_act:
        continue

    section_content = f"SECTION: {section}\n\n{law_text}"
    acts[title].append(section_content)

    # Stop once enough Acts have collected enough material
    if len(acts) >= max_acts:
        all_ready = all(
            len(sections) >= 5
            for sections in acts.values()
        )

        if all_ready:
            break


print("\nGenerating Act documents...\n")

count = 0

for title, sections in acts.items():

    if len(sections) < 2:
        continue

    count += 1

    document_id = f"ACT{count:03d}"

    filename = f"act_{count:03d}.txt"
    filepath = BASE_DIR / filename

    combined_text = "\n\n".join(sections)

    content = f"""DOCUMENT_ID: {document_id}

TITLE: {title}

DOCUMENT_TYPE: Act

JURISDICTION: India

AUTHORITY: Parliament of India

SOURCE: Indian-Laws Dataset

DESCRIPTION:
Indian legislation containing {len(sections)} legal sections.

CONTENT:

{combined_text}
"""

    filepath.write_text(content, encoding="utf-8")

    print(f"[{count}] Saved: {filename}")
    print(f"    {title[:100]}")
    print(f"    Sections collected: {len(sections)}")


print("\n========================================")
print("ACTS CORPUS GENERATION COMPLETE")
print("========================================")
print(f"Total Acts created: {count}")
print(f"Location: {BASE_DIR}")

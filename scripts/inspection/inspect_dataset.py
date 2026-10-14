from datasets import load_dataset

print("Loading dataset...")

ds = load_dataset(
    "KanoonGPT/indian-legal-documents",
    split="train",
    streaming=True
)

count = 0

for row in ds:
    print("\n========================================")
    print("DOCUMENT NUMBER:", count + 1)
    print("========================================")

    print("DOC ID:", row.get("doc_id"))
    print("TITLE:", row.get("document_title"))
    print("TYPE:", row.get("document_type"))
    print("JURISDICTION:", row.get("document_jurisdiction"))
    print("AUTHORITY:", row.get("issuing_authority"))
    print("DATE:", row.get("issue_date"))
    print("DESCRIPTION:", row.get("short_description"))

    text = row.get("text", "")
    print("\nTEXT PREVIEW:")
    print(text[:1000])

    count += 1

    if count == 3:
        break

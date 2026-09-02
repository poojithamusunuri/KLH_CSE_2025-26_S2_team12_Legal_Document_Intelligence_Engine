import json
from collections import Counter

with open(
    "data/processed/legal_chunks.json",
    "r",
    encoding="utf-8"
) as f:
    chunks = json.load(f)

lengths = [len(chunk["content"]) for chunk in chunks]

print("=" * 60)
print("CHUNK QUALITY AUDIT")
print("=" * 60)

print(f"\nTotal chunks: {len(chunks)}")

print(f"\nShortest chunk: {min(lengths)} characters")
print(f"Longest chunk: {max(lengths)} characters")
print(f"Average chunk: {sum(lengths) / len(lengths):.2f} characters")

print("\nChunk size distribution:")

ranges = {
    "0-100": 0,
    "101-300": 0,
    "301-600": 0,
    "601-1000": 0,
    "1001-1200": 0,
    "1200+": 0
}

for length in lengths:

    if length <= 100:
        ranges["0-100"] += 1
    elif length <= 300:
        ranges["101-300"] += 1
    elif length <= 600:
        ranges["301-600"] += 1
    elif length <= 1000:
        ranges["601-1000"] += 1
    elif length <= 1200:
        ranges["1001-1200"] += 1
    else:
        ranges["1200+"] += 1

for name, count in ranges.items():
    percentage = (count / len(chunks)) * 100
    print(f"{name}: {count} ({percentage:.1f}%)")


print("\n" + "=" * 60)
print("SAMPLE CHUNKS")
print("=" * 60)

for i in [0, 100, 1000, len(chunks) - 1]:

    if i < len(chunks):

        chunk = chunks[i]

        print(f"\nCHUNK {i}")
        print("-" * 40)
        print("ID:", chunk["chunk_id"])
        print("Document:", chunk["title"])
        print("Length:", len(chunk["content"]))
        print("Content preview:")
        print(chunk["content"][:500])

print("\n" + "=" * 60)
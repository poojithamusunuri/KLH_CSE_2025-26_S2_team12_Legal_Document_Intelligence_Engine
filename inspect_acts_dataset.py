from datasets import load_dataset

print("Loading dataset...")

dataset = load_dataset(
    "manjot007/Indian-Laws",
    split="train",
    streaming=True
)

for i, item in enumerate(dataset):
    print("\n==============================")
    print("RECORD:", i + 1)
    print("==============================")
    print("Available fields:")
    print(item.keys())

    print("\nFull record:")
    for key, value in item.items():
        text = str(value)
        print(f"\n{key}: {text[:1000]}")

    if i >= 2:
        break

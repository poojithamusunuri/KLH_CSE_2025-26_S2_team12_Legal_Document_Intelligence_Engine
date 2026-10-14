# Data Directory

This directory contains the accepted legal-document baseline and a separately tracked set of official statutory excerpts used by the Legal Document Intelligence Engine.

## Directory Structure

```text
data/
├── corpus/                 # Accepted original baseline: 242 text documents
│   ├── acts/
│   ├── constitution/
│   ├── core_acts/
│   ├── judgments/
│   └── legal_principles/
└── statutory_additions/    # 7 selected official India Code statutory excerpts
```

## Baseline and supplemental track

The original `data/corpus/` directory contains 242 documents and is preserved independently. `data/statutory_additions/` contains seven selected excerpts from the Bharatiya Nyaya Sanhita, 2023, the Indian Contract Act, 1872, and the Information Technology Act, 2000. The production CLI loads both directories, for 249 searchable documents total.

The statutory additions are excerpts rather than full Acts. Their source URLs, exact provision scope, and version cautions are documented in `data/statutory_additions/README.md` and in each document's metadata header.

## Data integrity and legal scope

No statutory text was invented to fill source gaps. The original baseline is independent from the expansion track. This project does not continuously sync amendments and does not establish that any statute applies to a particular fact pattern. Verify current official text and applicability before legal reliance.

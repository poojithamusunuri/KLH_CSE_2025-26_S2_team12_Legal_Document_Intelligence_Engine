# Data Directory

This directory contains the legal-document corpus and generated data used by the Legal Document Intelligence Engine.

## Directory Structure

```text
data/
├── corpus/
│   ├── acts/
│   ├── constitution/
│   ├── core_acts/
│   └── judgments/
│
└── processed/
    └── legal_chunks.json
```

## Corpus

The corpus contains the legal documents used by the search engine, organized into acts, constitution, core acts, and judgments.

The current local corpus contains 242 documents.

## Processed Data

data/processed/legal_chunks.json contains processed legal-document chunks generated during the data preparation workflow.

## Data Availability

The corpus and generated processed data are excluded from version control. The repository contains the source code and data-processing scripts required to work with the corpus.

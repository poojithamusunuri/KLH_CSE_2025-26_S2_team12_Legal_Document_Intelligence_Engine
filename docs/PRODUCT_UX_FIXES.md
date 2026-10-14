# Product UX and evidence clarity update

This update keeps the Java console application and its DSA/search implementations, while making the ordinary user experience less implementation-heavy.

## Search results

- Compact mode uses plain-language search types and omits internal ranking scores and document IDs.
- Fuzzy legal-term searches display the corrected term and a short matching excerpt when available.
- A document that merely mentions the term is labeled `TERM MENTIONED IN TEXT`, not `DIRECT`.
- Detailed result cards explain that a text mention does not establish that the judgment decides the issue.
- Matched concepts and terms are deduplicated before display.
- `house`, `premises`, and `building` no longer automatically imply the `land / property` concept.

## Citation network

- Corpus-wide citation rankings display readable judgment titles and Act names where metadata is available.
- `flow current` explains that relationships come from corpus metadata and may not relate to the search topic.
- A current-result relationship is hidden when the linked Act title or recorded alias cannot be found in the displayed judgment text.

## Scope

These are interface and evidence-labeling improvements, not a claim that the corpus or legal relevance model is complete. The engine remains a research/discovery prototype; a keyword excerpt or citation edge is not a legal conclusion.

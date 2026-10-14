# User-facing search interface update

- Removed standalone algorithm demonstrations from the user-facing command; algorithms remain implemented and testable in source.
- Added `compare` as an optional document-text similarity tool instead of automatically showing comparisons during every search.
- Removed repeated matched terms in displayed results (case-insensitive deduplication).
- Removed algorithm names and raw match offsets from document cards.
- Replaced the repetitive relevance-classification legend with a simple result heading and clearer notice when no close situation match was found.
- Simplified the search-strategy section to a search overview.
- Internal algorithm implementations, source data, and research features remain in the project.

This is a console application, not an Amazon-style web storefront. The changes prioritize a more product-like command-line experience without claiming that lexical relevance guarantees legal correctness.

# Console UX Guide

## Default mode

The engine starts in **compact mode**. It shows a short list of search results with a plain-language search type, readable match labels and, for corrected legal-term searches, the resolved term plus a short excerpt that explains why a result appeared. Internal ranking numbers, algorithm names, and document IDs are kept out of the compact result card.

A text mention is not the same as a judgment deciding an issue. When the result is a keyword/content reference, the interface says so explicitly.

## Commands

| Command | Purpose |
|---|---|
| `help` | Show search examples and supported commands |
| `compact` | Return to concise results |
| `detailed` | Show query interpretation, analysis, and expanded result records |
| `details` | Alias for `detailed`; explicitly enables detailed mode |
| `flow` | Explore the corpus-wide judgment–Act citation network |
| `flow current` | Show corpus-recorded citation links involving visible results, with a warning that a link may not relate to the query |
| `compare` | Compare textual overlap among current results; available only when enough results exist |
| `optimize` | Prioritize documents from an active legal-situation search |
| `optimize N` | Prioritize N documents directly (1–20) |
| `schedule` | Plan research tasks within an available time budget |
| `schedule H` | Schedule tasks within H hours directly (1–24) |
| `session` | Review the active query and research context |
| `<query>` | Search the legal corpus |
| `exit` | Close the application |

## Citation-flow views

### `flow` — corpus overview

Shows corpus-level citation-network statistics and the most connected judgments and Acts. Where the corpus metadata can be matched to a loaded document, the interface displays its title instead of only an internal ID. Network counts describe the loaded dataset; they do not prove that a statute applies to a user's facts.

### `flow current` — links for visible results

Shows relationships recorded in the citation metadata for the current displayed results. The engine warns that such links may not relate to the search topic. For a displayed judgment, a relationship is hidden when neither the Act title nor a recorded alias can be found in the loaded judgment text. A displayed link is still a research-navigation clue, not legal advice or proof of applicability.

## Search evidence

- **Direct/title match:** a stronger candidate signal, not a guarantee of legal correctness.
- **Term mentioned in text:** the searched term occurs in the document, but the document may discuss it only in passing or while quoting another case.
- **Related/background:** the document shares concepts or legal vocabulary but is not confirmed to answer the situation.
- **Weak/background:** limited overlap; use cautiously.

For fuzzy spelling, the engine shows the resolved term and an excerpt from the document where that term appears when available. This helps the user judge whether the result is useful instead of relying on a label alone.

## Detailed mode

Detailed mode adds the legal-situation analysis and expanded document information for project demonstrations and deeper review. `compare` is a separate, optional command because raw text similarity is not the same as legal relevance. Optimization and scheduling are also explicit commands so that the normal search output stays focused.

The source code and tests retain the DSA implementations for algorithm demonstrations and correctness evaluation. The ordinary search interface does not need to print standalone algorithm demos or internal scoring diagnostics for every user.

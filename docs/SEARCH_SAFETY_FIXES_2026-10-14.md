# Search Safety Fixes — 14 October 2026

## Scope

This patch addresses the reproduced search-quality defects reported for the General Retrieval V4 project. It does not claim complete legal reasoning, complete statutory coverage, or multilingual support.

## Changes made

1. **Conservative spelling correction.** Fuzzy correction now applies only to a single token and uses bounded edit distance. Structural/LCS fallback matching was removed because it could rewrite valid words such as `rape`, `dowry`, and `stole` into unrelated catalog terms. Small single-token typos such as `murde` can still resolve to `Murder`.
2. **Everyday theft and cyber wording.** The vocabulary now links forms such as `stole`, `stolen`, `stolen property`, `phone`, `bag`, `movable property`, `hacked`, `identity theft`, and `cheated` to the corresponding retrieval concepts. These are search aliases, not legal findings.
3. **More specific abuse phrases.** Broad one-word matches such as `abuse`/`abused` were removed from the physical/domestic-abuse group. Multi-word descriptions are not allowed to retrieve a document solely because a generic synonym occurs incidentally; the user phrase must have meaningful phrase evidence for this single-concept abuse path.
4. **Evidence-aware ranking and abstention.** Related-tier results now require stronger distinct-concept or event evidence for multi-concept queries involving theft. For concrete object queries such as a stolen bag or phone, non-statutory judgments must mention that object instead of borrowing broad `goods`/`property` overlap. Weak-only candidates are not shown as results. When no adequate result remains, the CLI reports that the loaded corpus did not match the query and does not imply that no law exists.
5. **Consistent term labels.** Incidental mentions for exact and fuzzy term searches use the same `TERM MENTIONED IN TEXT` label unless the title itself matches.
6. **Working-directory resilience.** Corpus paths are resolved from the current directory and compiled class location. Starting the application from another directory works when the compiled `out` directory remains inside the project; missing data produces a readable error without a Java stack trace.
7. **Regression tests.** Added `SearchSafetyRegressionTest` to verify spelling safeguards, single-token typo handling, theft/cyber statute retrieval and suppression of weak-only noise results.

## Reproduced-query outcomes

| Query | Expected safe behavior after patch |
|---|---|
| `bus stolen bag` | BNS theft excerpt ranks first; any judgment result must mention a bag, and all are related research leads rather than direct answers to the specific facts |
| `My husband beats me` | No result if the corpus cannot support the stated event; unrelated family/tenancy/tax results are not presented |
| `Someone physically abused me` | No result unless the corpus contains meaningful phrase evidence; incidental generic uses of abuse terminology are not enough |
| `Someone hacked my bank account and stole money online` | IT Act sections 66C/66D and the section 43 excerpt are retrievable as related statutory leads; no claim that the user’s facts are legally established |
| `rape` | Not rewritten to `Appeal`; no-match if no suitable corpus item exists |
| `dowry` | Not rewritten to `Forgery`; no-match if no suitable corpus item exists |
| `stole` / `someone stole my phone` | BNS section 303 theft excerpt is retrievable as related statutory background |
| `He cheated me and took my money` | BNS section 318 cheating excerpt is retrievable as related statutory background |
| `My neighbour plays loud music at night` | Weak-only unrelated judgments are suppressed; clear no-match message |
| `murder` / `murde` | Incidental passage uses a consistent term-mention label; `murde` can still be corrected to `Murder` |

## Verification

- Original baseline remains 242 documents in `data/corpus/`.
- Seven provenance-tracked statutory excerpts remain separate under `data/statutory_additions/`.
- Production corpus count: 249 documents (242 baseline + 7 excerpts).
- Full test script completed with exit code 0.
- Benchmark: 20 cases, 15 target cases, 0 failures; target hit@3 13/15, recall@10 14/15, MRR 0.848.
- Scenario evaluation remains 19 pass, 11 partial, 0 fail. The partial scenarios are not represented as resolved by this patch.
- Manual wrong-working-directory smoke test passed: the application loaded 249 documents and exited without a stack trace.

## Limits

The vocabulary and ranking remain rule-based. Statutory additions are selected excerpts rather than complete Acts. The project does not provide Hindi or other non-English search support and does not replace legal advice or source verification.

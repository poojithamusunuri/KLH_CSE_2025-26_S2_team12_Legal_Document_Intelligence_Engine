# Relevance Benchmark

## Purpose and limits

This is a 20-case, corpus-backed regression sample. It is not statistically representative and does not measure overall legal accuracy, legal correctness, or completeness of the accepted 242-document corpus. Queries and expected document IDs are manually selected annotations. They were reviewed against the cited document content and domain metadata, but are not independently adjudicated by legal experts.

The benchmark exercises situation-ranking mode directly. It does not replace tests of the production exact/fuzzy query routes, which are covered separately by `tests.ProductionQueryRouteTest`.

## Case types and gold labels

- `DIRECT`: the expected target document directly addresses the described event or issue under the current conservative tier definition.
- `RELATED`: the target provides relevant legal background or an adjacent issue, without establishing the described factual event.
- `NO_DIRECT`: the query must not produce any `DIRECT_MATCH`; related candidates may still be returned. The test reports the candidate count and top-ranked IDs so this cannot be mistaken for a successful no-result search.
- `NO_MATCH`: strict empty retrieval. The test passes only when both candidate count is zero and the result list is empty.

For target-bearing cases, the benchmark checks the configured document ID's actual tier and rank across the full ranked corpus. Its printed top-five list is diagnostic and is not itself a relevance judgment for every candidate.

## Metric definitions

Direct-tier precision and recall are calculated only over cases with an independently reviewed expected document ID. Each such query/target pair is treated as one observation:

- A true positive is a gold `DIRECT` target returned as `DIRECT_MATCH`.
- A false negative is a gold `DIRECT` target returned at another tier or not found.
- A false positive is a non-`DIRECT` gold target returned as `DIRECT_MATCH`.
- A true negative is a non-`DIRECT` gold target returned at another tier or not found.

Precision is $TP/(TP+FP)$. Recall is $TP/(TP+FN)$. `NO_DIRECT` and strict `NO_MATCH` cases are excluded from these target-pair confusion counts.

MRR is the mean reciprocal rank of the configured target document over the target-bearing cases: each found target contributes $1/rank$, and a missing target contributes zero; the sum is divided by the number of target-bearing cases. Rank is measured over all ranked corpus candidates, not only the console's displayed top ten.

## Current run

The last focused run reported:

- Cases: 20
- Target-bearing cases: 15
- `NO_DIRECT` cases: 4
- Strict `NO_MATCH` cases: 1
- Target documents retrieved somewhere in the full ranked corpus: 15
- Failures: 0
- Direct TP / FP / FN / TN: 1 / 0 / 0 / 14
- Direct precision / recall: 1.000 / 1.000
- Target hit@3: 13 / 15
- Target recall@10: 14 / 15
- MRR over the 15 target-bearing cases: 0.848

These precision and recall values have only one positive DIRECT target and fourteen non-DIRECT target observations. They are a regression signal for these annotations, not a statistically useful estimate of general performance. The top-k target-hit rates and MRR are likewise specific to these selected target/query pairs.

## Reviewed document scope

Repeated targets include curated summaries and judgments. In particular, `COREACT006` is a consumer-law summary, `COREACT007` is a contract-law summary, and `COREACT010` is a copyright-law summary; those support related legal-background retrieval, not direct factual incident claims. `JUDG051` concerns employment/labor-court procedure and termination rather than the described salary nonpayment. `JUDG076` concerns an electricity-consumer deposit, not a residential landlord security deposit. `JUDG087` is a section 138 cheque case; for a standalone phrase query it is treated as related. `JUDG017` directly concerns a bus accident and dependency compensation. `ACT026` concerns Ajmer tenancy and land records and is related background, not an inheritance judgment.

No direct corpus judgment was established by this sample for unauthorized bank-account use or residential landlord security-deposit return; those are `NO_DIRECT` checks, not claims that the entire corpus contains no such authority. The generic query `pay` is the only strict `NO_MATCH` case and currently yields no candidates.

## Reproducibility

Run `bash scripts/test.sh`. The benchmark prints per-case target tier, rank, candidate count, and the top five IDs/tiers. A passing test does not establish relevance for every unannotated result; top candidate relevance still requires review, and corpus domain metadata is known to be uneven.


## Corpus scope update

The benchmark's historical expected-document annotations refer to the accepted 242-document baseline. The production CLI now loads that baseline plus seven separately identified statutory excerpts (249 total). Benchmark numbers must not be described as a legal-accuracy guarantee; the new track is covered by `CorpusTrackIntegrationTest`, and broader relevance labels still require reviewed expected-document assertions.

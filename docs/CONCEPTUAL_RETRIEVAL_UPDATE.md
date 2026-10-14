# Conceptual Retrieval Update

Date: 14 October 2026

## Purpose

The system previously relied primarily on the query's recognized vocabulary groups. This update adds a conservative, offline situation-interpreter before the existing retrieval ranker. It looks for combinations of action, object, and context cues, then adds a canonical legal retrieval cue while preserving the original query.

## Supported situation frames

The initial frame set covers possible property taking, fraud/dishonest inducement, unauthorised account access, physical/domestic abuse, unpaid wages, unreturned tenancy deposits, harassment/stalking, neighbourhood noise nuisance, consumer goods/service defects, contract non-performance, identity/data misuse, and property entry/possession.

The interpreter requires multiple independent situation slots before adding a concept. A vague prompt such as `What is the law?` is not assigned an invented issue. It does not decide whether a crime occurred or whether a statute applies.

## Integration

The interpreted query enters the existing shared relevance pipeline. The original corpus remains 242 documents, with seven separately tracked statutory excerpts; production search loads both tracks (249 documents). Existing exact/fuzzy/general search routing, evidence-aware ranking, citations, explanations, negative-condition handling, and research tools remain in place.

## Validation added

`tests.SituationInterpreterTest` checks generalized paraphrases for theft, cheating, account intrusion, physical abuse, unpaid wages, tenancy deposits, nuisance, consumer defects, identity misuse, and contract/property concepts. It also checks that vague queries are not assigned a concept and that novel theft, cheating, and account-intrusion paraphrases retrieve the relevant statutory excerpt where the corpus supports it.

`tests.SearchSafetyRegressionTest` continues to cover unsafe spelling rewrites, theft retrieval, bag-specific result constraints, cyber-law retrieval, and suppression of weak-only neighbourhood-noise results.

## Limitations

This is a deterministic concept/frame layer, not a neural embedding model or a general-purpose language model. It is offline and requires no API key, but it will miss paraphrases outside its current cues. Future work could add a locally packaged pretrained sentence-embedding model and a reviewed, broader paraphrase benchmark; no model weights are claimed to be included in this release. Retrieval results are research leads, not legal advice, and must be checked against the primary source and facts.

# Build and Test Report

## Clean build and corpus integrity

- Recursive Java source compilation: PASS.
- Recursive test-source compilation: PASS.
- `bash scripts/test.sh`: PASS, exit code 0.
- Corpus files retained: 242 text documents.
- Citation relationship data retained: 425 data rows in `results/judgment_act_relationships.csv`.
- Backup files in the delivered project: 0.
- The test script rebuilds `out/` before executing the test suite; the distributable ZIP excludes compiled output.

## Automated validation

- `SituationInterpreterTest`: 0 failures; paraphrase-to-statute integration checks.
- `QueryAnalysisIntegrationTest`: 7 passed, 0 failed.
- `ProductionQueryRouteTest`: 6 checks passed, 0 failed.
- `QueryFallbackTest`: 3 checks passed, 0 failed.
- `Phase3LegalAnalysisTest`: 24 passed, 0 failed.
- `Phase4ResearchPipelineTest`: 6 passed, 0 failed.
- `AlgorithmCorrectnessTest`: 20 passed, 0 failed.
- `Phase2SearchRoutingTest`: 0 failures.
- `RelevanceAdversarialTest`: 0 failures; paired salary/tax synthetic regression passed.
- `RelevanceBenchmarkTest`: 20 cases, 0 failures; target-hit@3 13/15, target-recall@10 14/15, MRR 0.848.
- `OptimizerValidationTest`: 0 failures.
- `RelevanceFocusedRegressionTest`: 0 failures.
- `CLIRegressionTest`: 0 failures.
- `ConceptGeneralizationRegressionTest`: 0 failures; positive/negative incident-frame checks, item-evidence filtering, stolen-car retrieval, and no-match handling.
- `SearchSafetyRegressionTest`: 0 failures; prevents real words being rewritten to unrelated catalog terms, checks typo tolerance, theft/cyber statutory retrieval, and weak-only abstention.
- `ScenarioEvaluation`: 19 classification passes, 11 partial, 0 failures, 9 uncertainty-handled.
- The complete script now contains 18 test classes and completed with exit code 0 in the current development build.

The benchmark's direct-tier precision/recall are 1.000/1.000 on this run, but only one gold DIRECT target is present; those figures are not a meaningful estimate of general retrieval accuracy. The benchmark is a small manually reviewed regression set. `ScenarioEvaluation` is classification-only and does not score retrieval; retrieval is exercised separately by the relevance benchmark and CLI integration tests, while broader legal relevance still requires independently reviewed expected-document assertions.

## Retrieval and event-analysis changes

- Added vocabulary roles for police/search/entry/premises and warrant-negation phrases.
- Warrant analysis now extracts police actors and search actions, records the user's statement that a warrant was not shown/produced, and creates specific missing-information and dependency questions.
- Added conservative domain mappings for criminal-procedure/warrant, property-secured-interest, and cheque/negotiable-instrument queries.
- Added a penalty for incompatible domains when a supported mapping exists.
- Added required-context checks for secured-lending/mortgage, cheque-dishonour, warrant, and warrant-not-shown groups. Mortgage/land and cheque/dishonour evidence must co-occur within the event window to be retained as meaningful candidates.
- A search of a house without a warrant being shown returns criminal-procedure background first and explicitly reports that a direct answer was not established. It does not claim that a background case proves the described search was unlawful.
- Optimizer coverage is no longer credited to weak lexical matches. Integrated related-only results are not counted as verified question coverage; the research pack is still a shortlist for human review.
- Document similarity now compares substantive document bodies rather than full files including metadata headers. Its output explicitly says similarity is not legal relevance.

## Query routing and CLI UX

- Removed any requirement to describe a personal legal scenario before searching a term.
- `help`, `flow`, `flow current`, `optimize`, `optimize N`, `schedule`, `schedule H`, `session`, `compact`, `detailed`, `details`, `algorithms`, and `exit` are supported.
- `details` is an explicit alias for `detailed`; it does not toggle modes. Only `compact` switches back.
- Invalid `optimize`/`schedule` arguments receive usage guidance rather than silently becoming legal searches.
- `flow current` is dispatched before ordinary query retrieval and is covered by a command regression test.
- General natural-language ranking reports Aho–Corasick-assisted scoring; query routing and fallback pattern-matching choices are displayed separately.
- Judgment year is inferred only when an explicit judgment-style date appears in its title. Act enactment years are not mistaken for judgment years, and missing metadata is not invented.
- Miller–Rabin and randomized top-K are exposed through a clearly labelled `algorithms` demonstration. Corpus scanning is sequential; parallel processing is not claimed.

## Corpus-ground-truth caution

The handover mentioned JUDG135 as a genuine salary non-payment judgment. In the supplied corpus, JUDG135 is *Smt. Dayawati vs Smt. Bhagwati Devi*, a property/possession dispute containing historical salary references. It is not used as a direct salary ground-truth document. The salary/tax regression instead checks a controlled synthetic positive/negative pair and requires real corpus JUDG108 to remain non-DIRECT.

## Known limitations

- The situation interpreter is a conservative rule-and-lexicon layer, not a pretrained embedding model. The adversarial regression cases are developer-authored and are not an independent external evaluation.

- Retrieval remains lexical and rule-based; it is not legal semantic understanding and does not decide legal rights.
- The corpus has uneven domain coverage and inconsistent metadata. For some situations, only related/background material is available.
- Direct-tier calibration has a small positive benchmark sample. Conventional precision@3 is not reported because most non-target top-ranked documents are not individually adjudicated; target-hit@3, target-recall@10 and MRR are reported instead.
- Citation links are navigation aids, not proof that a statute applies to a fact pattern.
- The `algorithms` command is a demonstration for Miller–Rabin; the other algorithms are tested or used where documented, but no unrelated algorithm is forced into legal retrieval merely to claim integration.


## Corpus track separation (current audit build)

- Original accepted baseline: 242 text documents in `data/corpus/` (kept separate and unchanged).
- Official statutory expansion: 7 selected excerpts in `data/statutory_additions/`.
- Combined production load: 249 documents.
- The additions are sourced from official India Code PDF URLs recorded in each text file and in `data/statutory_additions/README.md`; they are not complete Acts.
- Fresh verification status must be taken from the release audit log shipped with the final package, not inferred from earlier benchmark reports.

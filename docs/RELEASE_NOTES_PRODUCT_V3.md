# Product UI / Retrieval Quality Update — V3

## Changes

- Natural-language property-seizure searches that explicitly allege missing prior notice no longer classify generic seizure/property vocabulary overlap as a direct match.
- If the loaded corpus does not contain a result supporting that key notice condition near the action and property evidence, generic documents are suppressed for this specific fact pattern rather than presented as plausible case matches.
- Empty-result messaging now says that no document matched the full query and explicitly avoids implying that no applicable law or remedy exists.
- `flow current` does not show case-specific citation links when the visible results are only keyword mentions or related/background documents. Use `flow` for the corpus-wide citation network.
- Compact search output remains product-oriented; algorithm routing is tested through the router API rather than requiring internal implementation names to appear in the user interface.
- Updated the production route regression test to match the product-facing output while separately validating KMP and Rabin–Karp route selection.

## Validation

- `bash scripts/build.sh` — passed
- `tests.RelevanceFocusedRegressionTest` — passed, including the missing-prior-notice false-positive regression
- `tests.CLIRegressionTest` — passed
- `tests.ProductionQueryRouteTest` — passed
- `tests.RelevanceBenchmarkTest` — 20 cases, 0 reported failures
- `tests.ScenarioEvaluation` — 19 classification passes, 11 partial classifications, 0 failures; uncertainty handling 9/9
- `tests.AlgorithmCorrectnessTest` — 20/20 passed
- `bash scripts/test.sh` — completed successfully

## Known limitation

The system is a corpus-bounded, lexical/proximity retrieval prototype. It can identify related text and recorded citation links, but it does not establish the legal merits of a situation. A no-result response means no qualifying match was found in the loaded corpus/search configuration, not that no law or remedy exists.

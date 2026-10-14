# General Retrieval V4 — Release Notes

## Purpose

This release upgrades the existing Java legal research prototype toward a general-purpose, evidence-aware retrieval pipeline. It is not a fully semantic or unlimited universal legal AI system; retrieval is scoped to the loaded Indian legal corpus.

## Architecture changes

- Added `search.QueryConstraintAnalyzer`, a domain-independent component that extracts explicit negative conditions such as `without ...`, `no ...`, `not ...`, `failed to ...`, and checks for nearby textual support around action/object evidence.
- Replaced the prior property-seizure/notice-only production guard with the shared condition analyzer. The same mechanism is exercised by notice, warrant, salary, and deposit tests.
- Negative-condition evidence is required for a `DIRECT` classification and contributes to ranking. Unsupported candidates may remain as `RELATED`/`WEAK` background leads, but they are not represented as confirmed answers.
- Aho–Corasick-assisted concept scanning and the existing rarity-weighted coverage, action/object proximity, title, phrase, and domain signals remain in place.
- For natural-language factual situations, generic Acts without a matching title are ranked behind judgments that describe factual events. Acts remain searchable and can rank highly for instrument/title queries.
- Tightened the no-event path so title/term overlap alone cannot establish a direct situation match.
- Added a conservative no-match guard for multi-token queries containing no recognized legal-vocabulary concept: a single accidental lexical hit no longer returns unrelated weak background leads. This is covered by a CLI regression test; as with any lexical threshold, it can trade some recall for fewer false positives.
- Added generic condition regression tests and documented the architecture and limitations.

## Validation

Run `bash scripts/test.sh` from the project root. The release test suite includes query routing, analysis, algorithms, relevance adversarial tests, the 20-case relevance benchmark, optimizer validation, focused regressions, CLI regressions, and scenario evaluation.

## Known limitations

- The system still relies on lexical/concept groups and proximity heuristics. It can miss paraphrases and may return background documents whose text shares a topic without resolving the described facts.
- A `RELATED` result is a research lead, not a confirmed answer. Explicit negative-condition checks reduce false direct matches but do not prove legal applicability.
- No result means the loaded corpus did not produce a candidate meeting the configured checks; it does not establish that no applicable law exists.
- The original baseline contains 242 documents under `data/corpus/`. A separate track contains seven official India Code statutory excerpts (three BNS provisions, two Indian Contract Act provisions, and two Information Technology Act excerpts), for 249 documents loaded by the CLI. These excerpts are not complete Acts and do not provide continuous amendment tracking. The system cannot be assumed to cover all Indian laws, current amendments, jurisdictions, or factual situations.
- Citation relationships are navigation metadata. Similarity is textual overlap. Research schedules are estimates.


## Statutory source expansion

The original 242-document corpus remains unchanged. `data/statutory_additions/` holds seven selected statutory excerpts with official India Code URLs and explicit excerpt-scope notes. `tests.CorpusTrackIntegrationTest` verifies baseline count, supplemental count, unique IDs, provenance, searchability, and no-match behavior.

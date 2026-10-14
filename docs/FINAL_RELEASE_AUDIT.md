# Final Release Audit — 14 October 2026

## Release scope

This audit covers the agreed release tasks: a separately tracked expansion of authentic statutory text, combined search/DSA/citation/explanation/no-match validation, and clean extraction/build/test/package verification.

## Corpus integrity

- Original baseline: **242 `.txt` documents** in `data/corpus/`.
- Baseline preservation: all 242 original text files were SHA-256 compared against the uploaded V4 ZIP; every file matched byte-for-byte, with no missing, extra, or changed baseline files.
- Separate statutory track: **7 selected statutory excerpts** in `data/statutory_additions/`.
- Combined production load: **249 documents**.
- Statutory instruments represented: Bharatiya Nyaya Sanhita, 2023 (sections 303, 316(1), 318(1)); Indian Contract Act, 1872 (sections 10 and 73); Information Technology Act, 2000 (selected section 43 clauses and sections 66C–66D).
- Provenance: every excerpt contains an official India Code source URL and an explicit description of its excerpt scope. The linked source PDFs are not bundled because direct retrieval from the official host failed in the runtime; selected text was transcribed from searchable official India Code source content. No missing statutory text was fabricated.
- Scope warning: these are selected excerpts, **not full Acts**, and the repository is not a continuously updated statutory service. Verify amendments and applicability against the official source before legal reliance.

## Build and tests

- Java source compilation: passed.
- Test source compilation: passed.
- `tests.CorpusTrackIntegrationTest`: passed; verifies 242 baseline, 7 additions, unique IDs, official provenance, searchable statutory text, and no-match behavior.
- `tests.CLIRegressionTest`: passed; combined production workflow exercises fuzzy/legal-term search, compact/detailed modes, DSA-related commands, citations, explanations/background warnings, statutory search, and an intentionally nonsensical query that must return no match.
- Full `bash scripts/test.sh`: all 18 test classes completed with no automated test failures in the audit run.
- Clean extraction verification: the final candidate ZIP was extracted into a new directory; `bash scripts/build.sh` succeeded, the corpus-track integration test passed, and a CLI smoke test confirmed 249 loaded documents, searchable IT Act text, citation-flow command handling, and honest no-match output.
- Full test suite from that clean extraction exited with code `0`; the run includes all 18 test classes listed in `scripts/test.sh`.
- Relevance benchmark: 20 cases, 0 failures; target hit@3 = 13/15, target recall@10 = 14/15, MRR = 0.848. These small-set metrics are regression signals, not general legal-accuracy guarantees.
- Scenario classification evaluation: 19 pass, 11 partial, 0 fail; uncertainty handled 9/9. This is a classification-only evaluation. The 11 partial labels remain documented and must not be misrepresented as complete scenario success. Retrieval is separately covered by benchmark and CLI tests.
- Search-safety regression fixes are documented in `docs/SEARCH_SAFETY_FIXES_2026-10-14.md`. They cover conservative single-token spelling correction, everyday theft/cyber aliases, abuse-query specificity, weak-only abstention, consistent incidental-term labels, and working-directory-safe corpus discovery.
- The latest concept-safety update is documented in `docs/CONCEPT_INTERPRETATION_REGRESSION_FIXES.md`. It removes object-only theft triggers, gates theft judgments on the named item, exposes tentative situation recognition to users, suppresses unsupported personal-incident result lists, and adds adversarial positive/negative regression tests.

## Known limitations

1. The statutory expansion is intentionally excerpt-based; it is not a full statutory library.
2. Retrieval remains lexical/concept-and-proximity based with a conservative hand-written situation interpreter. It is not a pretrained embedding model or human-level semantic understanding; paraphrases can be missed, and related results may be background only. The adversarial tests are developer-authored, not an independent external evaluation.
3. The scenario evaluation still has 11 partial classification labels. No retrieval correctness is claimed by that classification-only suite.
4. Citation relationships are navigation metadata and do not prove a statute applies to a fact pattern.
5. The project is an academic legal-research aid, not legal advice.

## Reproducibility

From the extracted project root on a Java-compatible environment:

```bash
bash scripts/build.sh
bash scripts/test.sh
java -cp out Main
```

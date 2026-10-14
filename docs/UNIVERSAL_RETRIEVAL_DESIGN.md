# General-Purpose Retrieval Design

## Goal

The retrieval pipeline is designed to generalize across different legal topics instead of embedding a separate production rule for each sample query. The application remains corpus-scoped: results can only be found in the documents currently loaded by the project.

## Pipeline

1. **Query interpretation** classifies legal terms, spelling variants, and natural-language situations.
2. **Concept expansion** maps known synonyms and query terms into shared concept groups.
3. **Candidate scanning** uses the existing Aho-Corasick-assisted multi-pattern scan and the project's existing string-matching algorithms.
4. **Relevance scoring** combines rarity-weighted concept coverage, action/object proximity, title and phrase evidence, and supported domain context.
5. **Constraint validation** extracts explicit negative conditions from query language (for example, `without ...`, `no ...`, `did not ...`, or `failed to ...`). A candidate is not surfaced as a case-specific match unless the negated target has local textual support near the query's action and object evidence. This logic is domain-independent; it does not contain a property-seizure-only rule.
6. **Evidence-first output** explains that a word mention is not proof that a judgment decides the issue. If the loaded corpus cannot support a key condition, the engine may return no candidates rather than overstate relevance.
7. **Research tools** remain separate from primary retrieval: citation links are navigation metadata, similarity is textual overlap, and scheduling is a planning aid.

## Negative-condition analyzer

`src/search/QueryConstraintAnalyzer.java` extracts explicit negative clauses and checks for a matching cue and target term near the action and object evidence. This is a conservative lexical guardrail, not a full semantic model. It is intended to prevent common false positives such as treating a judgment that says a notice was issued as evidence for a query alleging that no notice was given.

## Important limitations

- This is not an unlimited or fully semantic universal legal AI system. It is a general-purpose retrieval prototype for the loaded corpus.
- Concept expansion and condition checks remain lexical and can miss paraphrases that use different vocabulary.
- A no-result outcome means the loaded corpus did not produce a candidate meeting the configured evidence checks; it does not mean that no applicable law or remedy exists.
- The corpus's size, source quality, jurisdiction, currency, and completeness constrain what the application can retrieve.
- The system does not replace legal advice or determine the legal outcome of a dispute.

## Regression coverage

`tests/QueryConstraintAnalyzerTest.java` checks the same generic condition mechanism against notice, warrant, salary, and deposit examples, including positive-condition and unrelated-negation counterexamples. The full suite should be run with `bash scripts/test.sh` before release.

# Intelligent Research Pipeline — Implementation Notes

## What happens for a natural-language query

1. The query processor classifies a legal term, likely typo, or situation.
2. The legal problem analyzer identifies possible domains, factual objects, and missing information using explainable rules.
3. The dependency map turns known missing-information categories into specific verification questions where a rule exists.
4. Search retrieves and ranks corpus candidates. A result marked RELATED or CONTENT_REFERENCE is not treated as a direct match to the user's incident.
5. The research optimizer evaluates the current research questions against the actual candidate-document text. It uses meaningful terms, legal vocabulary, title signals, and local co-occurrence within a 320-character window; it then uses greedy maximum coverage under the document budget.
6. The schedule is generated from the current query's missing-information tasks. Task durations are planning estimates, not predictions of actual legal-work time. Dependencies are respected by the scheduler.
7. Similarity uses suffix-array/LCP shared-text coverage and reports the longest shared sequence separately. A long sequence alone does not imply high whole-document similarity.
8. Citation flow remains a corpus-level graph analysis. The `flow current` command relates the current result set to that corpus-level analysis without inventing citation edges.

## Output length and clarity changes

- Removed the repeated research-plan block after the dependency map; the dependency map already contains the task, question, and reason.
- Removed the repeated search-count summary from the search-results header.
- Limited detailed document records to the top five results (top three in compact mode) and stated when additional results are omitted.
- Limited document-similarity comparisons to the top three alternatives.
- Changed optimization wording to “potential question matches” and “potentially covered”. These are textual relevance signals, not verified answers or legal conclusions.
- Corrected the vocabulary label `friend / borrower` to `borrower / debtor` and removed unrelated friend/acquaintance terms from that group.
- Distinguished direct/title matches from content references when passing relevance tiers into optimization; a document mentioning a legal term is not automatically treated as directly relevant to the reported situation.
- Added specific dependency questions and different effort estimates for loan terms, property-security documents, and repayment history.

## Important limitations

This remains an explainable, rule-based Java research prototype. It does not use a language model to understand legal meaning, verify legal propositions, or determine whether a document is authoritative/current. Textual co-occurrence is still a heuristic. Review cited passages and primary legal sources before relying on any result. The optimizer can select fewer documents than the budget when no additional candidate contributes qualifying signals.

## Rebuild and verify

```bash
bash scripts/build.sh
bash scripts/test.sh
java -cp out Main
```

The test script runs the existing query, legal-analysis, algorithm, relevance, optimizer, and scenario regression tests.

# Relevance and Pipeline Validation

## Current relevance model

The scorer remains a general lexical/feature-based model. It does not claim semantic legal understanding.

For event-style queries, direct evidence is based on:

1. query concept coverage;
2. action/object event evidence;
3. domain compatibility where a supported analyzer domain is available; and
4. the existing direct-match thresholds.

The action/object check no longer treats one character distance as the whole decision. For every action occurrence, it finds the nearest occurrence of the query's target object. A known competing tax/revenue object can override the target when it is closer. `EVENT_WINDOW_CHARS = 180` is retained only as an outer sanity bound.

## Salary/tax regression

The regression suite now checks both sides together using a controlled pair of synthetic documents:

- genuine employer non-payment of salary → DIRECT;
- employer non-payment of tax with salary mentioned separately → not DIRECT.

The real corpus case `JUDG108` is separately required to remain non-DIRECT for the salary query.

### Important corpus correction

The earlier handover claimed that `JUDG135` was a genuine salary non-payment judgment. Inspection of the supplied corpus shows that `JUDG135` is **Smt. Dayawati vs Smt. Bhagwati Devi**, a property/possession dispute that merely contains historical salary references. Its domain is property/land law and its body contains unrelated uses of “not paid” and “salary”. It is therefore not used as a DIRECT ground truth in the final regression. Treating it as a direct salary case would make the benchmark itself misleading.

## Always-search routing

The production flow no longer blocks a query because the legal-problem analyzer has low confidence.

- exact legal term → search;
- fuzzy legal term → search;
- general text → ranked search;
- unknown/short term → still reaches corpus search;
- zero candidates → honest corpus no-result message.

This means new legal vocabulary can be searched without adding a new hard-coded clarification branch first.

## Short-term UX

Exact/fuzzy legal terms and short low-confidence text queries use a compact search view. The full issue decomposition, optimizer, scheduler, and similarity pipeline remains available for fuller situation-style queries, where those components provide useful context.

## Validation limits

The relevance labels are explainable lexical estimates, not legal conclusions. The benchmark is intentionally small, and `ScenarioEvaluation` is classification-only and does not score retrieval; retrieval is exercised separately by the relevance benchmark and CLI integration tests, while broader legal relevance still requires independently reviewed expected-document assertions. The corpus is uneven across legal domains. The system is an academic legal-research aid, not legal advice.

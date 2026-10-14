# Syllabus Coverage and Project Scope

## Scope rule
The accepted 242-document legal corpus is the project's domain dataset. Algorithms are included because they support a user-visible legal-research task, a supporting pipeline stage, or a required DSA demonstration. The project does **not** claim to implement every topic from every syllabus module.

## Implemented and relevant

| Concept / algorithm | Status | Why it belongs in this project |
|---|---|---|
| Custom lists / core data structures | Implemented | Supports the project's DSA implementations and data flow. |
| KMP | Implemented | Exact legal-term/pattern search. |
| Rabin–Karp | Implemented | Fuzzy/legal-term retrieval route and hash-based pattern matching. |
| Z-algorithm | Implemented | General text-pattern search route. |
| Aho–Corasick | Implemented | Matches multiple legal vocabulary terms in one pass. |
| Levenshtein / Damerau–Levenshtein | Implemented | Typo tolerance and near-match comparison. |
| Suffix array + LCP/Kasai | Implemented | Compares matching documents and estimates textual similarity. |
| Graph traversal / citation network | Implemented, domain-specific | Inspects judgment-to-Act relationships. |
| Maximum bipartite matching | Implemented; primary network metric | Measures the largest one-to-one pairing of linked judgments and Acts. |
| Greedy research optimization | Implemented heuristic | Selects a research pack under a budget/priority constraint. |
| Greedy task scheduling | Implemented heuristic | Orders research tasks subject to dependencies/priorities. |
| Randomized selection | Implemented | Supporting algorithmic selection operation; not a legal conclusion. |

## Simplified / heuristic components

- Query interpretation and issue detection use rules, keywords, and normalization; they are not a trained language model or full semantic parser.
- Relevance tiers are lexical/feature-based heuristics. They are not legal authority assessments and can produce false positives and false negatives.
- Research optimization and scheduling are heuristics; no global-optimality or approximation guarantee is claimed.
- Citation relationships depend on the supplied relationship dataset. A graph edge records a detected/curated relationship, not a legal holding or a measure of precedential strength.

## Removed from the user-facing workflow

- The standalone Miller–Rabin `prime` console demo was removed because primality testing does not support the legal-research workflow. Its old source/test may remain in the codebase as an algorithm correctness exercise, but it is not presented as a project feature.
- The raw max-flow number is not presented as a meaningful project result. Under the current network construction it collapses to total citation mentions. Maximum bipartite matching is presented as the structure-dependent network result instead.

## Not implemented / not claimed

- Full semantic retrieval, neural embeddings, or an LLM-based legal reasoner.
- Dinic, min-cut analysis, min-cost max-flow, or a calibrated capacity model for legal influence.
- Formal NP-hardness reductions, approximation-ratio proofs, or guaranteed-optimal research plans.
- Parallel algorithms, reservoir sampling, or a complete implementation of every syllabus topic.
- A claim that the system gives legal advice or that corpus absence disproves a claim.

## Dataset
The bundled legal corpus is accepted for this project and contains 242 documents. Coverage is uneven across legal topics; result quality is bounded by corpus coverage and the heuristic retrieval model.

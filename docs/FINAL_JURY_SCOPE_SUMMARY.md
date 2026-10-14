# Jury Presentation Scope Summary

**Project:** Legal Document Intelligence Engine — Java console-based legal-research prototype.

**Dataset:** Accepted original baseline, 242 documents, plus a separately tracked expansion of 7 official statutory excerpts (249 searchable documents total). The statutory additions are excerpts, not full Acts; provenance and limitations are documented in `data/statutory_additions/README.md`.

**Core flow:** plain-English legal problem → rule-based issue/question analysis → legal-document retrieval and relevance tiers → document comparison and citation-network inspection → prioritized research plan / budgeted research pack / task schedule.

**Algorithms tied to the workflow:** KMP, Rabin–Karp, Z-algorithm, Aho–Corasick, edit distance, suffix array/LCP, custom data structures, citation graph analysis and maximum bipartite matching, greedy research optimization, and greedy scheduling.

**Be precise in the viva:**
- The query analyzer and relevance scorer are rule/feature-based heuristics, not semantic AI.
- Unknown terms are not blocked by the analyzer: the production route always attempts corpus search, and short legal-term queries use a compact results view.
- Relevance labels indicate estimated textual/task fit, not whether a document is legally authoritative.
- Event-style direct matching uses nearest-object evidence with a tax/revenue conflict check; it is not a semantic proof of legal relevance.
- Maximum bipartite matching is the primary citation-network result: it counts the largest one-to-one pairing of judgments and Acts that have a recorded relationship.
- Raw citation mentions are a descriptive count. The previous max-flow value was not informative under its capacity construction and is not presented as a substantive finding.
- Optimization/scheduling are heuristics; no global optimality guarantee is claimed.
- The project implements selected relevant concepts, not every topic in the syllabus.
- Corpus coverage is uneven; the tool is a research aid, not legal advice.

**Validation:** run `bash scripts/test.sh`. The relevance benchmark is a small regression set, not a full-corpus precision/recall study. See `docs/RELEVANCE_BENCHMARK.md`, `docs/SYLLABUS_COVERAGE_AND_SCOPE.md`, and `docs/BUILD_AND_TEST_REPORT.md`.

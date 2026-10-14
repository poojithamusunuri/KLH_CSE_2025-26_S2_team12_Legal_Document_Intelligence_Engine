# Legal Document Intelligence Engine

## Project Information

**Course:** Data Structures and Algorithms – 3  
**Team:** 12
**Supervisor:** Dr. S. Vinay Kumar, Associate Professor, Department of Computer Science and Engineering  


---

## Team Members

| Name | ID |
|---|---|
| Musunuri Poojitha | 2520030541 |
| Meenakshi Madhavaram | 2520030315 |
| Sandhya Rani | 2520030473 |
---


# Legal Document Intelligence Engine

**Java 17+ · Offline-first legal research prototype**

A Java console-based legal research assistant that helps users structure a legal problem, identify missing facts and documents, search a local legal corpus, compare documents, inspect citation relationships, and organize a research plan.

> **Important scope boundary:** This is an academic research-support tool, not a lawyer or a legal-advice system. It does not decide who is legally right. Results depend on the coverage and quality of the included corpus.


# Abstract

The Legal Document Intelligence Engine is an advanced algorithm-based
system designed to analyse, search, compare, and process legal text
documents efficiently.

The project uses a legal document corpus and applies advanced algorithms
from multiple areas of computer science, including string algorithms,
dynamic programming, graph algorithms, network flow, approximation,
randomised algorithms, and parallel algorithm concepts.

The system is designed as an integrated legal text analytics platform
rather than a collection of separate algorithm implementations. Users
can interact with the system and observe how different advanced
algorithms solve real-world legal document processing problems.

The planned system supports exact pattern searching, fuzzy matching,
multi-pattern search, document similarity analysis, legal citation
analysis, scheduling demonstrations, randomised algorithms, and
algorithm performance comparisons.

The project follows the principle of implementing core algorithms
manually to demonstrate a clear understanding of advanced algorithm
design and analysis.

---

# Objectives

1. Build an integrated legal document analysis system.
2. Implement efficient exact pattern matching algorithms.
3. Support fuzzy searching using edit-distance techniques.
4. Analyse similarities between legal documents.
5. Demonstrate graph and network-flow applications.
6. Demonstrate scheduling and approximation algorithms.
7. Implement selected randomised algorithms.
8. Compare algorithm performance and complexity.
9. Apply advanced algorithms to a meaningful real-world domain.
10. Provide a modular and reproducible project that can be cloned and
   executed by other users.

---

# Advanced Algorithm Coverage

The project is designed to cover concepts from multiple modules of the
Advanced Algorithms syllabus.

| Area | Algorithms / Concepts | Application in Project |
|---|---|---|
| String Algorithms | KMP | Exact legal term search |
| String Algorithms | Rabin-Karp | Rolling-hash based pattern search |
| String Algorithms | Z Algorithm | Efficient pattern matching |
| String Algorithms | Aho-Corasick | Multi-pattern legal term search |
| Suffix Structures | Suffix Array and LCP | Document similarity analysis |
| Dynamic Programming | Levenshtein Distance | Fuzzy legal search |
| Dynamic Programming | Sequence Alignment | Clause and document comparison |
| Network Flow | Max Flow / Min Cut | Legal citation and relationship analysis |
| Approximation | Scheduling algorithms | Resource and case scheduling demonstrations |
| Randomised Algorithms | Miller-Rabin | Probabilistic primality testing |
| Randomised Algorithms | Reservoir Sampling | Sampling from large document collections |
| Parallel Concepts | Parallel search/reduction | Large corpus processing demonstrations |

---


## Requirements

- JDK 17 or newer (`java -version` and `javac -version`)
- macOS/Linux, or Windows PowerShell / Git Bash

## Build and run

Run commands from the project root—the directory containing `src/`, `tests/`, and `data/`.

### macOS, Linux, or Git Bash

```bash
bash scripts/build.sh
java -cp out Main
```

Or use the convenience launcher:

```bash
bash scripts/run.sh
```

### Windows PowerShell

```powershell
Remove-Item out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path out | Out-Null
$src = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }
javac -d out $src
$tests = Get-ChildItem -Recurse -Filter *.java -Path tests | ForEach-Object { $_.FullName }
javac -cp out -d out $tests
java -cp out Main
```

Recursive source discovery is intentional because the source is organized into packages and nested directories. Do not use `javac -d out src/*.java`.

## Professional console interface

The application starts in **compact mode** for a focused first-use experience. Switch to `detailed` when you want query interpretation, legal-problem analysis, pipeline diagnostics, and additional result explanations.

Examples of supported workflows:

- A legal term such as `search warrant` is handled as a legal-term search.
- A typo such as `raging` may be resolved to `Ragging` when the catalog matcher identifies it as the closest legal term.
- A natural-language situation such as `my bank gave me a loan against my property` can be analyzed into actors, objects, relationships, legal domains, missing information, research dependencies, and a research plan before retrieval.
- Related documents are labelled as `RELATED` or `WEAK / BACKGROUND` rather than presented as confirmed legal answers.
- `optimize` asks how many documents to prioritize and returns up to that number from the current ranked result set. Its question-coverage indicators are heuristic signals, not proof that a document answers a question.
- `schedule` asks for the available time budget and creates a dependency-aware plan within that limit. Task durations are planning estimates.
- Compact mode shows at most three results. Detailed mode shows more analysis and up to five result records. Optimization and scheduling run only when requested.
- A zero-result search explains that the current corpus returned no qualifying document; it does not claim that no applicable law exists.

At the `Legal Engine >` prompt:

```text
help           Show the system guide and examples
details        Alias for detailed; enable full research diagnostics
detailed       Enable the full research-oriented interface
compact        Enable concise search-result output
flow           Explain the corpus-wide citation network
flow current   Show citation links involving documents in the latest results
optimize       Ask for a document budget, then prioritize that many documents
optimize N     Prioritize N documents directly (1–20)
schedule       Ask for available hours, then schedule research tasks
schedule H     Schedule within H hours directly (1–24)
session        Show the current research-session state
algorithms     Demonstrate standalone Miller–Rabin and randomized top-K selection
exit           Close the application
<query>        Search the legal corpus
```

The console organizes detailed analysis into:

1. Query Understanding
2. Legal Problem Analysis (when a situation is detected)
3. Search Strategy
4. Corpus Search
5. Search Results
6. Document Similarity (detailed mode, when situation-level context is available)
7. Result Interpretation

Optimization and scheduling are explicit commands so the user can choose the document budget and time limit.

The system supports legal-term research and natural-language legal situations. It distinguishes **corpus evidence**, **ranking signals**, and **legal conclusions**.

## Run all tests

On macOS/Linux or Git Bash:

```bash
bash scripts/test.sh
```

The test script performs a clean compile of source and test files and runs the following test classes (the exact list should match the current `scripts/test.sh` in the checked-out version):

- `tests.CorpusTrackIntegrationTest`
- `tests.SearchSafetyRegressionTest`
- `tests.SituationInterpreterTest`
- `tests.ConceptGeneralizationRegressionTest`
- `tests.TrapQueryRegressionTest`
- `tests.QueryAnalysisIntegrationTest`
- `tests.ProductionQueryRouteTest`
- `tests.QueryFallbackTest`
- `tests.Phase3LegalAnalysisTest`
- `tests.Phase4ResearchPipelineTest`
- `tests.AlgorithmCorrectnessTest`
- `tests.Phase2SearchRoutingTest`
- `tests.RelevanceAdversarialTest`
- `tests.ScenarioEvaluation`
- `tests.RelevanceBenchmarkTest`
- `tests.OptimizerValidationTest`
- `tests.RelevanceFocusedRegressionTest`
- `tests.CLIRegressionTest`

A successful test run is evidence that the included checks passed; it is not proof of comprehensive legal accuracy or coverage.

## Try these queries

At the console prompt, try the following demonstration set.

### Legal-term search

- `breach of contract`
- `search warrant`
- `arrest warrant`
- `cheque dishonour`
- `mortgage`
- `ragging`

### Fuzzy legal-term search

- `raging` — may resolve to `Ragging` when the catalog matcher identifies it as the closest legal term.

### Natural-language legal situations

- `My employer has not paid my salary for three months.`
- `The owner refuses to return my rental deposit.`
- `My bank gave me a loan against my property.`
- `Police searched my house without showing me a warrant.`

### Regression and safety examples

These examples are intended to check that the system distinguishes similar-looking but different situations:

- `my phone fell and the screen broke`
- `I lost my bag in the train`
- `my car was stolen`
- `my land was seized by the government`
- `I accessed my account online`
- `someone hacked my bank account and stole money online`
- `section 318`

Expected outcomes depend on the current corpus and test fixtures. A query describing a loss or accident should not automatically be classified as theft, and ordinary account access should not automatically be classified as hacking.

### Research tools

- `flow` — corpus-wide citation-network analysis
- `flow current` — filters corpus citation metadata to links involving documents visible in the latest search report; a citation does not prove legal applicability
- `optimize` / `optimize [N]` — asks for or accepts a document budget, then prioritizes that many current candidates
- `schedule` / `schedule [H]` — asks for or accepts an hour budget and schedules the active session's tasks within it
- `session` — displays the active research questions and retrieved-document count

Search results distinguish direct, related, and weak matches. If the corpus has no direct match, the engine should not imply that a loosely related document is authoritative.

## Research pipeline

1. Analyze a plain-language problem into issues, actors, actions, objects, and questions.
2. Identify missing information and documents.
3. Map dependencies between research tasks.
4. Search and rank the local corpus.
5. Explain why each result is relevant.
6. Compare documents and inspect citation relationships.
7. Build a prioritized research plan and a budget-constrained research pack.
8. Schedule tasks while respecting dependencies.

## Situation understanding and retrieval

The search pipeline preserves the user's original wording and looks for a conservative combination of action, object, and context cues. When more than one independent part of the described situation supports a concept, it may add a canonical retrieval cue and pass the interpreted query into the evidence-aware ranker. This is intended to connect some paraphrases with statutory language while retaining the original wording for context.

The same workflow continues through document ranking, citation display, explanations, research planning, and no-match handling. Weak or merely incidental matches should not be presented as answers.

The situation interpreter is local and explainable. It is **not a pretrained neural embedding model**, does not call an online AI service, and cannot guarantee that every paraphrase will be understood. Its coverage remains limited by the implemented concept rules, the evaluation set, and the corpus.

## Latest validation and implementation notes

- [Concept interpretation regression fixes](docs/CONCEPT_INTERPRETATION_REGRESSION_FIXES.md)
- [Search safety fixes (14 October 2026)](docs/SEARCH_SAFETY_FIXES_2026-10-14.md)
- [Intelligent pipeline notes](docs/INTELLIGENT_PIPELINE_NOTES.md)
- [Build and test report](docs/BUILD_AND_TEST_REPORT.md)
- [Relevance and pipeline validation](docs/RELEVANCE_AND_PIPELINE_VALIDATION.md)
- [Relevance benchmark notes](docs/RELEVANCE_BENCHMARK.md)
- [Syllabus coverage and scope](docs/SYLLABUS_COVERAGE_AND_SCOPE.md)
- [Final jury scope summary](docs/FINAL_JURY_SCOPE_SUMMARY.md)
- [General Retrieval V4 release notes](docs/GENERAL_RETRIEVAL_V4_RELEASE_NOTES.md)
- [Conceptual retrieval update](docs/CONCEPTUAL_RETRIEVAL_UPDATE.md)
- **[Known limitations](docs/KNOWN_LIMITATIONS.md)** — corpus coverage, statutory excerpts, retrieval limits, benchmark scope, and language support.

Consult the reports in the checked-out project for the actual results from that version. Do not infer that a test passed merely because it is listed here.

## Corpus and limitations

The accepted original baseline contains 242 documents under `data/corpus/`. A separate `data/statutory_additions/` track contains seven selected statutory excerpts transcribed from official India Code PDFs. The original baseline is not overwritten. The production CLI is intended to load both tracks (249 searchable documents total) and report the baseline and excerpt counts separately. The additions are excerpts, not complete Acts, and each should include official source provenance and a scope note.

Coverage is uneven: some topics have useful supporting documents while others may have little or no directly relevant coverage, including banking fraud, motor-vehicle accidents, student/education complaints, and other domains.

The scenario evaluator reports `PASS`, `PARTIAL`, `FAIL`, and `UNCERTAIN`; these are evaluation categories, not legal conclusions. A lack of corpus evidence is not evidence that a real-world claim lacks merit. Verify primary sources and consult a qualified legal professional for advice.

For the fuller limitations statement, see [docs/KNOWN_LIMITATIONS.md](docs/KNOWN_LIMITATIONS.md).

## Project layout

```text
.
├── src/                    Java implementation
├── tests/                  Integration, phase, algorithm, and scenario tests
├── data/
│   ├── corpus/             Original legal-document baseline
│   └── statutory_additions/ Selected statutory excerpts
├── results/                Citation and corpus-analysis data
├── scripts/                Build, run, and test helpers
├── docs/                   Architecture, validation, and limitation notes
├── reports/                Academic deliverables (if present)
└── README.md
```

## General Retrieval V4

The retrieval pipeline includes conservative situation-to-concept interpretation, negative-condition analysis, and evidence-aware ranking. See [General Retrieval V4 release notes](docs/GENERAL_RETRIEVAL_V4_RELEASE_NOTES.md) for architecture changes, validation instructions, and known limitations.

The system is corpus-scoped and does not claim comprehensive semantic understanding of every Indian legal query. The offline situation-interpreter layer is explainable but limited; its outputs should be treated as retrieval assistance, not legal determinations.




# Supervisor

**Dr. S. Vinay Kumar**  
Associate Professor  
Department of Computer Science and Engineering

---

# License

This project is developed for academic and educational purposes as part
of the Data Structures and Algorithms – 3 course.

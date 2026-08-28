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

# Current Project Status

## Project Transformation Phase

The project is currently being transformed into the Legal Document
Intelligence Engine.

The architecture is being organised so that different advanced
algorithm modules can work together as part of one integrated system.

### Current implemented algorithm components

The existing codebase contains implementations related to:

- Knuth-Morris-Pratt (KMP)
- Rabin-Karp
- Levenshtein Distance
- Trie-based searching
- Search indexing
- Ranking-related functionality

These components will be reviewed, reorganised, and adapted to the
Legal Document Intelligence Engine architecture.

Future modules will be added progressively according to the course
requirements and project development plan.

---

# Proposed System Features

The Legal Document Intelligence Engine is planned to provide the
following capabilities:

```text
Legal Document Corpus
        |
        v
   Corpus Loader
        |
        v
   Legal Documents
        |
        +-----------------------------+
        |                             |
        v                             v
   Exact Search                  Fuzzy Search
   KMP                           Levenshtein
   Rabin-Karp
   Z Algorithm
        |                             |
        +-------------+---------------+
                      |
                      v
              Document Analysis
                      |
        +-------------+-------------+
        |                           |
        v                           v
 Document Similarity          Citation Analysis
 Suffix Structures            Graph / Network Flow
        |                           |
        +-------------+-------------+
                      |
                      v
              Result Presentation
```

---

# Planned Project Structure

```text
Legal_Document_Intelligence_Engine/
│
├── data/
│   └── corpus/
│       ├── acts/
│       ├── cases/
│       └── legal_documents/
│
├── docs/
│   └── project_documentation/
│
├── reports/
│   ├── abstract.pdf
│   └── presentation.pptx
│
├── results/
│   └── algorithm_results/
│
├── src/
│   ├── LegalDocument.java
│   ├── CorpusLoader.java
│   │
│   ├── stringalgorithms/
│   │   ├── KMP.java
│   │   ├── RabinKarp.java
│   │   ├── ZAlgorithm.java
│   │   └── AhoCorasick.java
│   │
│   ├── dynamicprogramming/
│   │   └── Levenshtein.java
│   │
│   ├── graph/
│   │   └── CitationGraph.java
│   │
│   ├── flow/
│   │   └── MaxFlow.java
│   │
│   ├── randomized/
│   │   ├── MillerRabin.java
│   │   └── ReservoirSampling.java
│   │
│   └── Main.java
│
├── README.md
└── .gitignore
```

---

# Getting Started

This section explains how an external user can clone and run the
project.

## Prerequisites

The following software is required:

### 1. Java Development Kit

Install Java JDK 17 or later.

After installation, verify Java:

```bash
java -version
```

Also verify the Java compiler:

```bash
javac -version
```

Both commands should display the installed Java version.

### 2. Git

Install Git to clone the repository.

Verify the installation:

```bash
git --version
```

---

# Clone the Repository

Clone the project using:

```bash
git clone https://github.com/poojithamusunuri/KLH_CSE_2025-26_S2_team4_Legal_Document_Intelligence_Engine.git
```

Move into the project directory:

```bash
cd KLH_CSE_2025-26_S2_team4_Legal_Document_Intelligence_Engine
```

---

# Compile the Project

Create the output directory if required:

```bash
mkdir -p out
```

Compile the Java source files:

```bash
javac -d out src/*.java
```

---

# Run the Project

Run the main program using:

```bash
java -cp out Main
```

---

# Typical Workflow

```text
Clone Repository
       |
       v
Install / Verify Java
       |
       v
Open Terminal in Project Folder
       |
       v
Compile Java Files
       |
       v
Run Main Program
       |
       v
Choose Required Algorithm
       |
       v
Enter Query / Input
       |
       v
View Results
```

---

# Algorithm Implementation Philosophy

The purpose of this project is not only to use existing library
functions.

Where appropriate, core algorithms are implemented manually so that
their internal logic, complexity, and behaviour can be analysed.

The project focuses on:

- Algorithm design
- Correctness
- Time complexity
- Space complexity
- Performance comparison
- Real-world application

---

# Testing

The project will include test cases for:

- Exact pattern matches
- Multiple matching documents
- No-match queries
- Fuzzy search queries
- Algorithm consistency
- Edge cases
- Performance comparisons

Testing documents and results will be maintained in the appropriate
`docs/` and `results/` directories.

---

# Future Development

Planned improvements include:

- Legal document corpus expansion
- Z Algorithm implementation
- Aho-Corasick multi-pattern matching
- Suffix Array and LCP implementation
- Advanced document similarity analysis
- Citation graph analysis
- Max-flow based demonstrations
- Approximation algorithms
- Miller-Rabin primality testing
- Reservoir sampling
- Performance benchmarking
- Integrated interactive menu
- Improved documentation and visualisation

---

# Supervisor

**Dr. S. Vinay Kumar**  
Associate Professor  
Department of Computer Science and Engineering

---

# License

This project is developed for academic and educational purposes as part
of the Data Structures and Algorithms – 3 course.

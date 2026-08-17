# Product Search and Recommendation System for Shopping

## Project Information

**Course:** Data Structures and Algorithms – 3  
**Team:** 4  
**Supervisor:** Dr. S. Vinay Kumar, Associate Professor, Department of Computer Science and Engineering  
**Current Phase:** Pattern/String Matching Implementation – Review 2

---

## Team Members

| Name | ID |
|---|---|
| Talluri. HimaBindu Sree | 2520030484 |
| Musunuri. Poojitha | 2520030541 |

---

## Abstract

The Product Search and Recommendation System for Shopping is a
data-structures-and-algorithms-based project designed to provide
efficient product searching and recommendation capabilities over a
structured product corpus.

The system processes a collection of product text documents and applies
multiple algorithms and data structures to support product search,
pattern matching, typo correction, autocomplete, and product ranking.

For exact string matching, the system implements the
Knuth-Morris-Pratt (KMP) and Rabin-Karp algorithms. KMP uses the
Longest Proper Prefix which is also Suffix (LPS) array to avoid
unnecessary comparisons, while Rabin-Karp uses a rolling hash to
identify candidate matches efficiently.

The project also incorporates Levenshtein distance for typo correction,
Trie-based structures for autocomplete, and ranking mechanisms for
ordering relevant products.

---

## Objectives

1. Implement efficient pattern and string matching algorithms.
2. Search product information stored in the project's product corpus.
3. Compare KMP and Rabin-Karp for the same search queries.
4. Support typo correction using Levenshtein distance.
5. Provide autocomplete functionality using Trie-based structures.
6. Rank relevant products using appropriate data structures.
7. Demonstrate practical applications of data structures and algorithms
   in a product-search scenario.

---

## Algorithms and Data Structures

The project includes:

- Knuth-Morris-Pratt (KMP) pattern matching
- Rabin-Karp pattern matching
- Levenshtein edit distance
- Trie-based autocomplete
- Hash-based lookup
- Product ranking
- Product corpus loading and search

---

## Current Phase Status

### Review 2 – Pattern/String Matching

**Status: Implemented and tested**

The current implementation includes:

- KMP string matching
- Rabin-Karp string matching
- Search over the project's 20-product TXT corpus
- Comparison of KMP and Rabin-Karp
- Matching-document consistency verification
- Execution-time benchmarking
- Operation-count comparison

Both algorithms are integrated into the project's `SearchEngine`.

---

## Project Flow

```text
20 Product TXT Files
        |
        v
  CorpusLoader
        |
        v
 ProductDocument[]
        |
        v
   User Query
        |
        +-------------------+
        |                   |
        v                   v
       KMP             Rabin-Karp
        |                   |
        +---------+---------+
                  |
                  v
        Matching Products
                  |
                  v
       Algorithm Comparison

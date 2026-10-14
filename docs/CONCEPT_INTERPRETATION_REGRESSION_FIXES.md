# Concept interpretation and retrieval safety update

## What changed

- Removed `bag`, `phone`, and related personal-item names from the generic movable-property vocabulary group. Naming an object no longer acts as evidence that theft occurred.
- Theft interpretation now requires an explicit theft term, or a taking event with context that supports an unauthorized taking. `lost`, `disappeared`, `went missing`, a device fault, and a virus are not theft cues by themselves.
- Added a specific item-evidence gate for judgment results on theft queries. Generic judgments that do not mention the item in the query are withheld; a relevant statutory excerpt can still be returned as a legal lead.
- Restricted account-intrusion interpretation to explicit compromise or unauthorized-access wording. Ordinary account use and paying an account fee are not automatically classified as hacking or fraud.
- Tightened abuse, wage non-payment, rental-deposit, identity-misuse, nuisance, and property-seizure frames so a shared noun is not enough to infer the situation.
- The compact user-facing output now calls the concept interpreter and labels recognized situations as tentative. For example, an abuse description can show a possible situation while honestly saying that the loaded corpus did not match it.
- Unclassified personal incident queries with no direct evidence return a clear no-match rather than filling the screen with coincidental lexical overlaps. Ordinary legal-term searches continue to use the regular retrieval path.
- Existing DSA algorithms, research commands, citations, and corpus tracks remain integrated.

## Regression coverage

`tests.ConceptGeneralizationRegressionTest` adds positive paraphrases and adversarial negatives for theft, abuse, account intrusion, wages, rental deposits, identity misuse, nuisance, and land/property seizure. It checks that an explicit stolen-car query retrieves the BNS theft excerpt and that judgments omitting the named item are not displayed for that incident.

`tests.SituationInterpreterTest`, `tests.SearchSafetyRegressionTest`, and `tests.CLIRegressionTest` continue to cover corpus integration, spelling correction, legal retrieval, no-match handling, and the user-facing tentative explanation.

## Important limitation

This remains an offline, explainable rule-and-lexicon interpretation layer combined with the existing ranked retrieval system. It is **not** a pretrained neural embedding model and it is not human-level language understanding. The expanded regression suite is developer-authored adversarial testing, not an independent external evaluation. Generalization beyond the covered frames is not guaranteed. Hindi and other non-English query support is not claimed.

The 20-case retrieval benchmark remains a small internal benchmark. Its scores should not be presented as proof of broad legal accuracy. Scenario classification still includes partial cases, and every returned statute or judgment must be checked for actual factual and legal support.

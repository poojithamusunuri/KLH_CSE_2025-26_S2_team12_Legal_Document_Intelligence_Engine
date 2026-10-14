# Official statutory additions (separate track)

This directory contains **seven selected statutory-text documents** transcribed from official India Code PDFs. They are excerpts, not complete Acts. The original `data/corpus/` directory is unchanged and remains the 242-document baseline.

## Contents

| Document ID | Instrument / provision | Official source |
|---|---|---|
| `STATUTE_BNS_2023_S303` | Bharatiya Nyaya Sanhita, 2023, section 303 excerpt | https://www.indiacode.nic.in/bitstream/123456789/20062/1/a202345.pdf |
| `STATUTE_BNS_2023_S316_1` | Bharatiya Nyaya Sanhita, 2023, section 316(1) | https://www.indiacode.nic.in/bitstream/123456789/20062/1/a202345.pdf |
| `STATUTE_BNS_2023_S318` | Bharatiya Nyaya Sanhita, 2023, section 318(1) and explanation | https://www.indiacode.nic.in/bitstream/123456789/20062/1/a202345.pdf |
| `STATUTE_ICA_1872_S10` | Indian Contract Act, 1872, section 10 | https://www.indiacode.nic.in/bitstream/123456789/2187/2/A187209.pdf |
| `STATUTE_ICA_1872_S73` | Indian Contract Act, 1872, section 73 and explanation | https://www.indiacode.nic.in/bitstream/123456789/2187/2/A187209.pdf |
| `STATUTE_IT_ACT_2000_S43_EXCERPT` | Information Technology Act, 2000, selected section 43 clauses | https://www.indiacode.nic.in/bitstream/123456789/13116/1/it_act_2000_updated.pdf |
| `STATUTE_IT_ACT_2000_S66C_66D` | Information Technology Act, 2000, sections 66C and 66D | https://www.indiacode.nic.in/bitstream/123456789/1999/1/A2000-21%20%281%29.pdf |

## Provenance and scope

- Source authority: India Code, the Government of India's official Central Acts repository.
- BNS PDF source labels the text as on 6 October 2025. The IT Act excerpts use official India Code PDF versions linked per file; the sections are selected excerpts, not a complete consolidated Act. The repository does not claim this as a live consolidated-law feed.
- Contract Act text is from the official India Code PDF linked above. Verify amendments and current applicability before relying on it.
- Some documents explicitly omit illustrations, penalty subsections, or other parts of the provision. The `DESCRIPTION` header in each file states the excerpt scope.
- No text was invented to fill gaps. No downloaded PDF is included because the runtime could not fetch the official host directly; the cited official PDFs were accessible as searchable web-rendered source text for the specific transcribed passages.
- Search results from these excerpts are statutory research leads, not legal advice or a determination that a provision applies to any particular facts.

## Track separation

`data/corpus/` is the accepted original baseline (242 `.txt` files). `data/statutory_additions/` is the separately auditable expansion track (7 `.txt` statutory excerpts plus this README). `Main` loads both tracks for end-to-end search while reporting each count separately.

import json
from pathlib import Path


OUTPUT_DIR = Path("results")

OUTPUT_JSON = OUTPUT_DIR / "external_act_registry.json"
OUTPUT_TXT = OUTPUT_DIR / "external_act_registry.txt"


# -------------------------------------------------------------------
# Acts referenced by judgments but not currently represented by an
# authoritative Act document in the local corpus.
#
# IMPORTANT:
# These are NOT mapped to similar/newer Acts.
# Each remains a distinct legal entity.
# -------------------------------------------------------------------

EXTERNAL_ACTS = [
    {
        "external_id": "EXTACT001",
        "title": "Negotiable Instruments Act, 1881",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT002",
        "title": "Companies Act, 1956",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT003",
        "title": "Income-tax Act, 1961",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT004",
        "title": "Land Acquisition Act, 1894",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT005",
        "title": "Transfer of Property Act, 1882",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT006",
        "title": "Registration Act, 1908",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT007",
        "title": "Limitation Act, 1963",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT008",
        "title": "Indian Evidence Act, 1872",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT009",
        "title": "General Clauses Act, 1897",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT010",
        "title": "Specific Relief Act, 1963",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT011",
        "title": "Motor Vehicles Act, 1988",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT012",
        "title": "Finance Act, 1969",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT013",
        "title": "SEBI Act, 1992",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT021",
        "title": "Bihar Land Disputes Resolution Act, 2009",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT022",
        "title": "Bihar Land Reforms Act, 1950",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT023",
        "title": "Bihar Tenancy Act, 1885",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT024",
        "title": "Bihar Privileged Persons Homestead Tenancy Act, 1947",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT025",
        "title": "Bihar Bhoodan Yagna Act, 1954",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT026",
        "title": "Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act, 1961",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT027",
        "title": "Bihar Consolidation of Holdings and Prevention of Fragmentation Act, 1956",
        "status": "referenced_not_in_corpus",
    },    {
        "external_id": "EXTACT028",
        "title": "Central Excises and Salt Act, 1944",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT029",
        "title": "Bombay Sales Tax Act, 1959",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT030",
        "title": "Foreign Exchange Management Act, 1999",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT031",
        "title": "Press and Registration of Books Act, 1867",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT032",
        "title": "Recovery of Debts Due to Banks and Financial Institutions Act, 1993",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT033",
        "title": "Interest Act, 1978",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT034",
        "title": "Benami Transactions (Prohibition) Act, 1988",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT035",
        "title": "Central Sales Tax Act, 1956",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT036",
        "title": "Payment of Wages Act, 1936",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT037",
        "title": "Indian Succession Act, 1925",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT038",
        "title": "Defence of India Act, 1939",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT039",
        "title": "Government of India Act, 1935",
        "status": "referenced_not_in_corpus",
    },
    {
        "external_id": "EXTACT040",
        "title": "Stamp Act, 1899",
        "status": "referenced_not_in_corpus",
    },    {
        "external_id": "EXTACT041",
        "title": "Essential Commodities Act, 1955",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT042",
        "title": "Factories Act, 1948",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT043",
        "title": "Employees' State Insurance Act, 1948",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT044",
        "title": "Nawab Salar Jung Bahadur (Administration of Assets) Act, 1950",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT045",
        "title": "Estate Duty Act, 1953",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT046",
        "title": "Electricity (Supply) Act, 1948",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT047",
        "title": "Indian Electricity Act, 1910",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT048",
        "title": "Usurious Loans Act, 1918",
        "status": "referenced_not_in_corpus",
    },

    {
        "external_id": "EXTACT049",
        "title": "Indian Partnership Act, 1932",
        "status": "referenced_not_in_corpus",
    },

  {
    "external_id": "EXTACT050",
    "title": "Indian Income-tax Act, 1922",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT051",
    "title": "Major Ports Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT052",
    "title": "Rent Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT053",
    "title": "Wealth-tax Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT054",
    "title": "Prevention of Corruption Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT055",
    "title": "Foreign Exchange Regulations Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT056",
    "title": "Commissions of Enquiry Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT057",
    "title": "Urban Ceiling Regulation Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT058",
    "title": "Entertainment Tax Act",
    "status": "referenced_not_in_corpus",
},

{
    "external_id": "EXTACT059",
    "title": "Sale of Goods Act, 1893",
    "status": "referenced_not_in_corpus",
},
]


def main():

    OUTPUT_DIR.mkdir(exist_ok=True)

    # JSON catalogue
    catalogue = {}

    for act in EXTERNAL_ACTS:
        catalogue[act["external_id"]] = act

    OUTPUT_JSON.write_text(
        json.dumps(catalogue, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

    # Human-readable catalogue
    lines = []

    lines.append("=" * 90)
    lines.append("EXTERNAL ACT REGISTRY")
    lines.append("=" * 90)
    lines.append("")
    lines.append(
        "Acts referenced by judgments but not currently present "
        "as authoritative Act documents in the local corpus."
    )
    lines.append("")
    lines.append(f"External Acts: {len(EXTERNAL_ACTS)}")
    lines.append("")
    lines.append("-" * 90)

    for act in EXTERNAL_ACTS:
        lines.append(
            f'{act["external_id"]:10} | '
            f'{act["title"]} | '
            f'{act["status"]}'
        )

    lines.append("")
    lines.append("=" * 90)

    OUTPUT_TXT.write_text(
        "\n".join(lines),
        encoding="utf-8",
    )

    print("=" * 90)
    print("EXTERNAL ACT REGISTRY")
    print("=" * 90)
    print()
    print(f"External Acts: {len(EXTERNAL_ACTS)}")
    print()
    print("Saved:")
    print(f"  {OUTPUT_JSON}")
    print(f"  {OUTPUT_TXT}")
    print()

    for act in EXTERNAL_ACTS:
        print(
            f'{act["external_id"]:10} | {act["title"]}'
        )


if __name__ == "__main__":
    main()

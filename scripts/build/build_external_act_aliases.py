import json
from pathlib import Path


OUTPUT_DIR = Path("results")

REGISTRY_FILE = OUTPUT_DIR / "external_act_registry.json"
OUTPUT_FILE = OUTPUT_DIR / "external_act_aliases.json"


EXTERNAL_ALIASES = {
    "EXTACT001": [
    "Negotiable Instruments Act, 1881",
    "Negotiable Instrument Act, 1881",
    "Negotiable Instruments Act 1881",
    "Negotiable Instrument Act 1881",
    "Negotiable Instruments Act",
    "Negotiable Instrument Act",
    "N. I. Act, 1881",
    "N. I. Act 1881",
    "N.I. Act",
    "N.I Act",
    "NI Act",
    "NI Act, 1881",
    "NI Act 1881",
],

    "EXTACT002": [
        "Companies Act, 1956",
        "Companies Act 1956",
        "Indian Companies Act, 1956",
        "Indian Companies Act 1956",
    ],

    "EXTACT003": [
        "Income-tax Act, 1961",
        "Income-tax Act 1961",
        "Income Tax Act, 1961",
        "Income Tax Act 1961",
        "I.T. Act, 1961",
        "I.T. Act 1961",
        "IT Act, 1961",
        "IT Act 1961",
    ],

    "EXTACT004": [
        "Land Acquisition Act, 1894",
        "Land Acquisition Act 1894",
    ],

    "EXTACT005": [
        "Transfer of Property Act, 1882",
        "Transfer of Property Act 1882",
    ],

    "EXTACT006": [
        "Registration Act, 1908",
        "Registration Act 1908",
        "Indian Registration Act, 1908",
        "Indian Registration Act 1908",
    ],

    "EXTACT007": [
        "Limitation Act, 1963",
        "Limitation Act 1963",
        "Indian Limitation Act, 1963",
        "Indian Limitation Act 1963",
    ],

    "EXTACT008": [
        "Indian Evidence Act, 1872",
        "Indian Evidence Act 1872",
        "Evidence Act, 1872",
        "Evidence Act 1872",
    ],

    "EXTACT009": [
        "General Clauses Act, 1897",
        "General Clauses Act 1897",
    ],

    "EXTACT010": [
        "Specific Relief Act, 1963",
        "Specific Relief Act 1963",
    ],

    "EXTACT011": [
        "Motor Vehicles Act, 1988",
        "Motor Vehicles Act 1988",
    ],

    "EXTACT012": [
        "Finance Act, 1969",
        "Finance Act 1969",
    ],

    "EXTACT013": [
        "SEBI Act, 1992",
        "SEBI Act 1992",
        "Securities and Exchange Board of India Act, 1992",
        "Securities and Exchange Board of India Act 1992",
    ],

    "EXTACT021": [
        "Bihar Land Disputes Resolution Act, 2009",
        "Bihar Land Disputes Resolution Act 2009",
        "Bihar Land Disputes Resolution Act",
    ],

    "EXTACT022": [
        "Bihar Land Reforms Act, 1950",
        "Bihar Land Reforms Act 1950",
        "Bihar Land Reforms Act",
    ],

    "EXTACT023": [
        "Bihar Tenancy Act, 1885",
        "Bihar Tenancy Act 1885",
        "Bihar Tenancy Act",
    ],

    "EXTACT024": [
        "Bihar Privileged Persons Homestead Tenancy Act, 1947",
        "Bihar Privileged Persons Homestead Tenancy Act 1947",
        "Bihar Privileged Persons Homestead Tenancy Act",
    ],

    "EXTACT025": [
        "Bihar Bhoodan Yagna Act, 1954",
        "Bihar Bhoodan Yagna Act 1954",
        "Bihar Bhoodan Yagna Act",
    ],

    "EXTACT026": [
        "Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act, 1961",
        "Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act 1961",
        "Bihar Land Reforms (Fixation of Ceiling and Acquisition of Surplus Land) Act",
    ],

    "EXTACT027": [
        "Bihar Consolidation of Holdings and Prevention of Fragmentation Act, 1956",
        "Bihar Consolidation of Holdings and Prevention of Fragmentation Act 1956",
        "Bihar Consolidation of Holdings and Prevention of Fragmentation Act",
    ],    "EXTACT028": [
        "Central Excises and Salt Act, 1944",
        "Central Excises and Salt Act 1944",
    ],

    "EXTACT029": [
        "Bombay Sales Tax Act, 1959",
        "Bombay Sales Tax Act 1959",
    ],

    "EXTACT030": [
        "Foreign Exchange Management Act, 1999",
        "Foreign Exchange Management Act 1999",
        "FEMA",
    ],

    "EXTACT031": [
        "Press and Registration of Books Act, 1867",
        "Press and Registration of Books Act 1867",
    ],

    "EXTACT032": [
        "Recovery of Debts Due to Banks and Financial Institutions Act, 1993",
        "Recovery of Debts Due To Banks and Financial Institutions Act, 1993",
        "Recovery of Debts Due to Banks and Financial Institutions Act 1993",
    ],

    "EXTACT033": [
        "Interest Act, 1978",
        "Interest Act 1978",
    ],

    "EXTACT034": [
        "Benami Transactions (Prohibition) Act, 1988",
        "Benami Transactions (Prohibition) Act 1988",
        "Benami Transaction (Prohibition) Act, 1988",
    ],

    "EXTACT035": [
        "Central Sales Tax Act, 1956",
        "Central Sales Tax Act 1956",
    ],

    "EXTACT036": [
        "Payment of Wages Act, 1936",
        "Payment of Wages Act 1936",
    ],

    "EXTACT037": [
        "Indian Succession Act, 1925",
        "Indian Succession Act 1925",
    ],

    "EXTACT038": [
        "Defence of India Act, 1939",
        "Defence of India Act 1939",
    ],

    "EXTACT039": [
        "Government of India Act, 1935",
        "Government of India Act 1935",
    ],

    "EXTACT040": [
        "Stamp Act, 1899",
        "Stamp Act 1899",
    ],    "EXTACT041": [
        "Essential Commodities Act, 1955",
        "Essential Commodities Act 1955",
    ],

    "EXTACT042": [
        "Factories Act, 1948",
        "Factories Act 1948",
    ],

    "EXTACT043": [
        "Employees' State Insurance Act, 1948",
        "Employees' State Insurance Act 1948",
        "Employees State Insurance Act, 1948",
        "Employees State Insurance Act 1948",
    ],

    "EXTACT044": [
        "Nawab Salar Jung Bahadur (Administration of Assets) Act, 1950",
        "Nawab Salar Jung Bahadur (Administration of Assets) Act 1950",
        "Administration of Assets Act, 1950",
    ],

    "EXTACT045": [
        "Estate Duty Act, 1953",
        "Estate Duty Act 1953",
    ],

    "EXTACT046": [
        "Electricity (Supply) Act, 1948",
        "Electricity (Supply) Act 1948",
        "Electricity Supply Act, 1948",
        "Electricity Supply Act 1948",
    ],

    "EXTACT047": [
        "Indian Electricity Act, 1910",
        "Indian Electricity Act 1910",
    ],

    "EXTACT048": [
        "Usurious Loans Act, 1918",
        "Usurious Loans Act 1918",
    ],

    "EXTACT049": [
        "Indian Partnership Act, 1932",
        "Indian Partnership Act 1932",
        "Partnership Act, 1932",
        "Partnership Act 1932",
    ],

    "EXTACT050": [
        "Indian Income-tax Act, 1922",
        "Indian Income-tax Act 1922",
        "Indian Income Tax Act, 1922",
        "Indian Income Tax Act 1922",
    ],
    "EXTACT053": [
    "Wealth-tax Act, 1957",
    "Wealth-tax Act 1957",
    "Wealth Tax Act, 1957",
    "Wealth Tax Act 1957",
],

"EXTACT054": [
    "Prevention of Corruption Act, 1947",
    "Prevention of Corruption Act 1947",
    "Prevention of Corruption Act",
],

"EXTACT055": [
    "Foreign Exchange Regulations Act, 1947",
    "Foreign Exchange Regulations Act 1947",
    "Foreign Exchange Regulation Act, 1947",
    "Foreign Exchange Regulation Act 1947",
],

"EXTACT056": [
    "Commissions of Enquiry Act, 1952",
    "Commissions of Enquiry Act 1952",
    "Commissions of Inquiry Act, 1952",
    "Commissions of Inquiry Act 1952",
],

"EXTACT057": [
    "Urban Ceiling Regulation Act, 1976",
    "Urban Ceiling Regulation Act 1976",
    "Urban Land (Ceiling and Regulation) Act, 1976",
    "Urban Land (Ceiling and Regulation) Act 1976",
],

"EXTACT058": [
    "Entertainment Tax Act",
    "Entertainment Tax Act, 1939",
    "Entertainment Tax Act 1939",
],

"EXTACT059": [
    "Sale of Goods Act, 1893",
    "Sale of Goods Act 1893",
    "Indian Sale of Goods Act, 1893",
    "Indian Sale of Goods Act 1893",
],
}


def main():

    if not REGISTRY_FILE.exists():
        raise FileNotFoundError(
            f"Missing {REGISTRY_FILE}"
        )

    registry = json.loads(
        REGISTRY_FILE.read_text(encoding="utf-8")
    )

    output = {}

    for external_id, aliases in EXTERNAL_ALIASES.items():

        if external_id not in registry:
            print(
                f"WARNING: {external_id} not found in registry"
            )
            continue

        output[external_id] = {
            "title": registry[external_id]["title"],
            "aliases": sorted(set(aliases)),
        }

    OUTPUT_FILE.write_text(
        json.dumps(
            output,
            indent=2,
            ensure_ascii=False,
        ),
        encoding="utf-8",
    )

    print("=" * 90)
    print("EXTERNAL ACT ALIAS CATALOGUE")
    print("=" * 90)
    print()
    print(f"External Acts: {len(output)}")

    total_aliases = sum(
        len(item["aliases"])
        for item in output.values()
    )

    print(f"Total aliases: {total_aliases}")
    print()
    print("Saved:")
    print(f"  {OUTPUT_FILE}")
    print()

    for external_id, item in output.items():

        print(
            f'{external_id} | '
            f'{item["title"]} | '
            f'{len(item["aliases"])} aliases'
        )


if __name__ == "__main__":
    main()

package search;

import structures.SimpleList;

/**
 * Offline situation-to-concept bridge for everyday legal descriptions.
 *
 * This is deliberately conservative and explainable: it does not label a
 * person's story as a crime or determine legal liability. It recognizes a
 * small set of action/object/context frames and adds a canonical retrieval
 * cue so the existing corpus ranker can find legally worded passages even
 * when the user describes the event differently.
 *
 * It is not a neural embedding model. It is a local concept layer designed to
 * work with the existing search/ranking and evidence checks without network
 * access or an API key.
 */
public final class SituationInterpreter {

    private static final class Frame {
        final String name;
        final String canonicalCue;
        final String[] actionCues;
        final String[] objectCues;
        final String[] contextCues;
        final int minimumSlots;

        Frame(String name, String canonicalCue, String[] actionCues,
              String[] objectCues, String[] contextCues, int minimumSlots) {
            this.name = name;
            this.canonicalCue = canonicalCue;
            this.actionCues = actionCues;
            this.objectCues = objectCues;
            this.contextCues = contextCues;
            this.minimumSlots = minimumSlots;
        }
    }

    private static final Frame[] FRAMES = {
        new Frame("possible theft / taking property", "theft movable property dishonest taking consent",
            new String[]{"took", "taken", "taking", "picked up", "carried away", "carried it away", "walked off with", "pocketed", "snatched", "stole", "stolen", "steal", "pickpocketed"},
            new String[]{"phone", "mobile", "bag", "handbag", "wallet", "purse", "laptop", "bicycle", "bike", "vehicle", "jewellery", "jewelry", "belongings", "my things", "my item"},
            new String[]{"without asking", "without permission", "without my consent", "not mine", "not theirs", "secretly", "from my pocket", "from my bag"}, 2),
        new Frame("possible fraud / dishonest inducement", "cheating",
            new String[]{"tricked", "deceived", "duped", "lied", "false promise", "promised and did not", "pretended", "fooled", "cheated", "scammed", "defrauded"},
            new String[]{"money", "payment", "bank", "investment", "transfer", "loan", "refund", "property", "purchase", "account"},
            new String[]{"took my money", "kept my money", "made me pay", "induced me to pay", "sent money", "online", "fake identity"}, 2),
        new Frame("possible unauthorised account access", "unauthorized access",
            new String[]{"hacked", "broke into", "took over", "got into", "entered without permission", "used my password", "accessed without permission", "accessed without my knowledge"},
            new String[]{"bank account", "account", "phone", "computer", "email", "password", "device", "online banking", "computer system", "data"},
            new String[]{"without permission", "without my knowledge", "stole money", "changed my password", "online", "unauthorized", "unauthorised"}, 2),
        new Frame("possible domestic or physical abuse", "physical abuse",
            new String[]{"hit", "hits", "hitting", "physically abused", "abused", "abuse", "slapped", "slaps", "punched", "kicked", "beats", "beat me", "beating", "physically hurt", "assaulted", "attacked"},
            new String[]{"my body", "my child", "my mother", "my wife", "my husband", "spouse", "partner", "family"},
            new String[]{"husband", "wife", "partner", "domestic", "at home", "repeatedly", "threatened me"}, 2),
        new Frame("possible unpaid wages", "non-payment", 
            new String[]{"not paid", "never paid", "withheld", "refused to pay", "owes me", "still waiting for", "did not receive", "hasn't paid", "hasnt paid"},
            new String[]{"salary", "wages", "pay", "overtime", "salary dues", "monthly pay", "work payment"},
            new String[]{"employer", "boss", "company", "worked", "employee", "job"}, 2),
        new Frame("possible unreturned tenancy deposit", "return / refund",
            new String[]{"kept", "refused to return", "won't return", "wont return", "not returned", "withheld", "deducted"},
            new String[]{"deposit", "security deposit", "advance", "rental deposit"},
            new String[]{"landlord", "tenant", "rented", "rental", "house", "flat", "vacated"}, 2),
        new Frame("possible harassment or stalking", "harassment",
            new String[]{"keeps calling", "repeated calls", "keeps messaging", "following me", "follows me", "watching me", "threatening messages", "won't leave me alone", "wont leave me alone"},
            new String[]{"me", "my home", "my workplace", "my phone", "messages", "calls"},
            new String[]{"repeatedly", "every day", "at night", "after i said stop", "threats"}, 2),
        new Frame("possible neighbourhood noise nuisance", "nuisance",
            new String[]{"plays loud music", "loud music", "making loud noise", "noise every night", "keeps making noise", "disturbs my sleep", "very loud", "blaring music", "blare music", "blares music"},
            new String[]{"neighbour", "neighbor", "next door", "apartment", "building", "music", "noise"},
            new String[]{"at night", "every night", "cannot sleep", "can't sleep", "cant sleep", "repeatedly"}, 2),
        new Frame("possible consumer goods or service dispute", "defect / deficiency",
            new String[]{"does not work", "doesn't work", "did not work", "not working", "defective", "faulty", "poor service", "failed to deliver", "never delivered"},
            new String[]{"product", "phone", "laptop", "item", "service", "order", "purchase", "repair"},
            new String[]{"seller", "shop", "company", "consumer", "refund", "replacement", "warranty"}, 2),
        new Frame("possible contract non-performance", "breach / non-performance",
            new String[]{"broke the agreement", "did not honour", "did not honor", "failed to deliver", "did not do what promised", "backed out", "refused to perform"},
            new String[]{"contract", "agreement", "promise", "deal", "service", "payment", "delivery"},
            new String[]{"signed", "written", "contractor", "seller", "buyer", "terms"}, 2),
        new Frame("possible misuse of personal identity or data", "identity theft",
            new String[]{"used my identity", "impersonated me", "pretended to be me", "used my details", "used my personal information", "created an account in my name"},
            new String[]{"identity", "aadhaar", "personal information", "account", "documents", "phone number", "bank"},
            new String[]{"without permission", "without my knowledge", "online", "fake account", "fraud"}, 2),
        new Frame("possible government land acquisition or seizure", "land acquisition",
            new String[]{"land seized by the government", "my land was seized by the government", "government seized my land", "government acquired my land", "my land was acquired by the government", "land acquired by the government", "government took my land"},
            new String[]{"land", "plot", "property", "farm"},
            new String[]{"government", "acquired", "seized"}, 2),
        new Frame("possible property seizure or possession issue", "property seizure possession",
            new String[]{"my property was seized", "my land was seized", "my property was taken by authorities", "my land was taken by the government", "authorities seized my property"},
            new String[]{"property", "land", "plot", "house"},
            new String[]{"without notice", "without any information", "authorities", "government", "seized"}, 2),
        new Frame("possible property entry or possession issue", "trespass",
            new String[]{"entered my house", "came into my house", "entered my land", "occupied my land", "took possession", "blocked my access", "entered without permission"},
            new String[]{"house", "home", "land", "property", "room", "premises", "plot"},
            new String[]{"without permission", "without consent", "while i was away", "owner", "tenant"}, 2)
    };

    private SituationInterpreter() { }

    /** Adds a conservative canonical concept cue while preserving the user's words. */
    public static String expandForRetrieval(String query) {
        if (query == null || query.trim().isEmpty()) return query == null ? "" : query;
        String padded = " " + normalize(query) + " ";
        StringBuilder expanded = new StringBuilder(query.trim());
        SimpleList<String> added = new SimpleList<String>();
        for (int i = 0; i < FRAMES.length; i++) {
            Frame frame = FRAMES[i];
            if (matchesFrame(frame, padded) && !added.contains(frame.canonicalCue)) {
                expanded.append(" ").append(frame.canonicalCue);
                added.add(frame.canonicalCue);
            }
        }
        return expanded.toString();
    }

    /** Plain-language concepts recognized for user-facing explanations. */
    public static SimpleList<String> concepts(String query) {
        SimpleList<String> result = new SimpleList<String>();
        if (query == null || query.trim().isEmpty()) return result;
        String padded = " " + normalize(query) + " ";
        for (int i = 0; i < FRAMES.length; i++) {
            Frame frame = FRAMES[i];
            if (matchesFrame(frame, padded) && !result.contains(frame.name)) result.add(frame.name);
        }
        return result;
    }

    /**
     * Conservative event-frame checks. Common nouns alone never imply an event:
     * an object such as a phone, account, key or bag is not itself evidence of theft,
     * hacking, fraud or a deposit dispute.
     */
    private static boolean matchesFrame(Frame f, String p) {
        String name = f.name;
        if (name.startsWith("possible theft")) {
            boolean object = has(p, "phone", "mobile", "mobile phone", "bag", "handbag", "wallet", "purse", "laptop", "bicycle", "bike", "car", "vehicle", "jewellery", "jewelry", "belongings", "luggage", "watch", "cash", "property", "item", "scooter", "motorcycle", "chain", "necklace", "ring", "gold");
            boolean explicitTheft = has(p, "steal", "steals", "stealing", "stole", "stolen", "snatched", "pickpocketed", "shoplifted", "thief", "thieves");
            boolean taking = has(p, "took", "taken", "taking", "picked up", "carried away", "carried it away", "walked off with", "pocketed");
            boolean permission = has(p, "without permission", "without my permission", "without permission", "without my consent", "without consent", "without asking", "secretly", "not mine");
            boolean cyber = has(p, "hacked", "hacking", "online", "password", "bank account", "account");
            return !cyber && (explicitTheft || (object && taking && (permission || has(p, "walked away", "another person", "someone else", "carried away", "carried it away", "walked off with"))));
        }
        if (name.startsWith("possible fraud")) {
            boolean dishonest = has(p, "cheated", "cheat", "cheating", "tricked", "deceived", "duped", "scammed", "defrauded", "fraud", "fake identity", "false promise");
            boolean target = has(p, "money", "payment", "bank", "investment", "transfer", "loan", "refund", "property", "purchase", "account", "cheque", "check");
            return dishonest && target;
        }
        if (name.startsWith("possible unauthorised account access")) {
            boolean intrusion = has(p, "hacked", "hacking", "broke into", "took over", "got into", "logged in without permission", "accessed without permission", "accessed without my permission", "accessed without my knowledge", "used my password", "account compromised", "unauthorized access", "unauthorised access");
            boolean target = has(p, "account", "phone", "computer", "email", "password", "device", "online banking", "computer system", "data");
            return intrusion && target;
        }
        if (name.startsWith("possible domestic or physical abuse")) {
            boolean physical = has(p, "hit me", "hits me", "hitting me", "beat me", "beats me", "beating me", "slapped me", "punched me", "kicked me", "physically abused me", "physically hurt me", "assaulted me", "attacked me", "my wife hits", "my husband hits", "my wife beat", "my husband beat", "my spouse hit", "domestic violence", "physical abuse");
            return physical;
        }
        if (name.startsWith("possible unpaid wages")) {
            boolean nonpayment = has(p, "not paid", "never paid", "withheld", "refused to pay", "owes me", "did not receive", "hasn't paid", "hasnt paid", "unpaid", "salary not paid", "wages not paid");
            boolean wage = has(p, "salary", "wages", "pay", "overtime", "salary dues", "monthly pay", "work payment");
            boolean work = has(p, "employer", "boss", "company", "worked", "employee", "job", "work");
            return nonpayment && wage && work;
        }
        if (name.startsWith("possible unreturned tenancy deposit")) {
            boolean withholding = has(p, "kept", "refused to return", "won't return", "wont return", "not returned", "withheld", "deducted");
            boolean deposit = has(p, "deposit", "security deposit", "advance", "rental deposit");
            boolean tenancy = has(p, "landlord", "tenant", "rented", "rental", "vacated", "rent");
            return withholding && deposit && tenancy;
        }
        if (name.startsWith("possible harassment")) {
            boolean conduct = has(p, "keeps calling", "repeated calls", "keeps messaging", "following me", "follows me", "watching me", "threatening messages", "won't leave me alone", "wont leave me alone", "stalking");
            boolean target = has(p, "me", "my home", "my workplace", "my phone", "messages", "calls");
            boolean repeated = has(p, "repeatedly", "every day", "at night", "after i said stop", "threats", "keeps");
            return conduct && target && repeated;
        }
        if (name.startsWith("possible neighbourhood noise")) {
            boolean noise = has(p, "plays loud music", "loud music", "making loud noise", "noise every night", "keeps making noise", "disturbs my sleep", "very loud", "blaring music", "blare music", "blares music");
            boolean neighbour = has(p, "neighbour", "neighbor", "next door", "apartment", "building");
            boolean context = has(p, "at night", "every night", "cannot sleep", "can't sleep", "cant sleep", "repeatedly");
            return noise && neighbour && context;
        }
        if (name.startsWith("possible consumer goods")) {
            boolean defect = has(p, "does not work", "doesn't work", "did not work", "not working", "defective", "faulty", "poor service", "failed to deliver", "never delivered", "broken");
            boolean product = has(p, "product", "phone", "laptop", "item", "service", "order", "purchase", "device");
            boolean seller = has(p, "seller", "shop", "company", "consumer", "refund", "replacement", "warranty");
            return defect && product && seller;
        }
        if (name.startsWith("possible contract")) {
            boolean breach = has(p, "broke the agreement", "did not honour", "did not honor", "failed to deliver", "did not do what promised", "backed out", "refused to perform", "breached the contract");
            boolean agreement = has(p, "contract", "agreement", "promise", "deal", "service", "payment", "delivery");
            boolean context = has(p, "signed", "written", "contractor", "seller", "buyer", "terms");
            return breach && agreement && context;
        }
        if (name.startsWith("possible misuse of personal identity")) {
            boolean misuse = has(p, "used my identity", "impersonated me", "pretended to be me", "used my details", "used my personal information", "created an account in my name");
            boolean target = has(p, "identity", "aadhaar", "personal information", "account", "documents", "phone number", "bank");
            boolean unauthorized = has(p, "without permission", "without my knowledge", "without asking", "fake account", "fraud");
            return misuse && target && unauthorized;
        }
        if (name.startsWith("possible government land acquisition")) {
            return has(p, "land seized by the government", "my land was seized by the government", "government seized my land", "government acquired my land", "my land was acquired by the government", "land acquired by the government", "government took my land");
        }
        if (name.startsWith("possible property seizure")) {
            return has(p, "my property was seized", "my land was seized", "my property was taken by authorities", "my land was taken by the government", "authorities seized my property", "property was seized", "property seized", "seized my property", "seized my house", "seized my land");
        }
        if (name.startsWith("possible property entry")) {
            boolean entry = has(p, "entered my house", "came into my house", "entered my land", "occupied my land", "took possession", "blocked my access", "entered without permission");
            boolean target = has(p, "house", "home", "land", "property", "room", "premises", "plot");
            boolean unauthorized = has(p, "without permission", "without consent", "while i was away", "occupied", "took possession");
            return entry && target && unauthorized;
        }
        return false;
    }

    /** For theft scenarios, a judgment must contain evidence of the named item; a generic theft mention is not enough. */
    public static boolean judgmentSupportsTheftObject(String query, String title, String body) {
        SimpleList<String> recognized = concepts(query);
        boolean theft = false;
        for (int i = 0; i < recognized.size(); i++) {
            if (recognized.get(i).startsWith("possible theft")) theft = true;
        }
        if (!theft) return true;
        String p = " " + normalize(query) + " ";
        String document = " " + normalize((title == null ? "" : title) + " " + (body == null ? "" : body)) + " ";
        String[] items = {"phone", "mobile", "handbag", "bag", "wallet", "purse", "laptop", "bicycle", "bike", "car", "vehicle", "jewellery", "jewelry", "watch", "cash", "luggage", "scooter", "motorcycle", "chain", "necklace", "ring", "gold"};
        boolean hasSpecificItem = false;
        for (String item : items) {
            if (p.contains(" " + item + " ")) {
                hasSpecificItem = true;
                if (document.contains(" " + item + " ") || (item.equals("bike") && document.contains(" bicycle "))
                        || (item.equals("bicycle") && document.contains(" bike "))
                        || (item.equals("mobile") && document.contains(" phone "))
                        || (item.equals("phone") && document.contains(" mobile "))) return true;
            }
        }
        if (hasSpecificItem) return false;
        return has(document, "theft", "stolen", "stole", "stealing", "dishonest taking", "movable property");
    }

    /** A seizure by authorities is not a taking by a person, so theft provisions are hidden for it. */
    public static boolean theftStatuteContradictsSituation(String query, String title) {
        if (title == null || !title.toLowerCase().contains("theft")) return false;
        SimpleList<String> c = concepts(query);
        boolean seizure = false;
        boolean theft = false;
        for (int i = 0; i < c.size(); i++) {
            String name = c.get(i);
            if (name.startsWith("possible property seizure") || name.startsWith("possible government land")) seizure = true;
            if (name.startsWith("possible theft")) theft = true;
        }
        return seizure && !theft;
    }

    /** True for a personal incident sentence that has no recognized legal-event frame. */
    public static boolean isUnclassifiedPersonalIncident(String query) {
        if (query == null || query.trim().isEmpty() || !concepts(query).isEmpty()) return false;
        String p = " " + normalize(query) + " ";
        boolean personal = has(p, "my", "i", "i was", "i have", "someone", "he", "she", "my wife", "my husband", "my neighbour", "my neighbor", "my landlord", "my bank", "the bank", "the landlord", "the cheque", "the government");
        boolean eventVerb = has(p, "fell", "broke", "broken", "lost", "missing", "went missing", "accessed", "paid", "plays", "played", "refused", "kept", "seized", "happened", "crashed", "stopped working", "has a virus", "has virus", "did not pay", "didn't pay", "threatened", "threatens", "forgot", "forgotten", "insulted");
        return personal && eventVerb;
    }

    private static boolean has(String padded, String... cues) {
        for (String cue : cues) {
            String n = normalize(cue);
            if (!n.isEmpty() && padded.contains(" " + n + " ")) return true;
        }
        return false;
    }

    private static boolean containsAny(String padded, String[] cues) {
        for (int i = 0; i < cues.length; i++) {
            String cue = normalize(cues[i]);
            if (!cue.isEmpty() && padded.contains(" " + cue + " ")) return true;
        }
        return false;
    }

    private static int cueCount(String padded, String[] cues) {
        int count = 0;
        for (int i = 0; i < cues.length; i++) {
            String cue = normalize(cues[i]);
            if (!cue.isEmpty() && padded.contains(" " + cue + " ")) count++;
        }
        return count;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase()
                .replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }
}

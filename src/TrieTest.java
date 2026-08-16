import java.util.ArrayList;

public class TrieTest {

    public static void main(String[] args) {

        Trie trie = new Trie();

        trie.insert("samsung");
        trie.insert("samsonite");
        trie.insert("sony");
        trie.insert("gaming");
        trie.insert("google");

        System.out.println(
            "Prefix 'sam'   -> "
            + trie.getWordsWithPrefix("sam")
        );

        System.out.println(
            "Prefix 'so'    -> "
            + trie.getWordsWithPrefix("so")
        );

        System.out.println(
            "Prefix 'ga'    -> "
            + trie.getWordsWithPrefix("ga")
        );

        System.out.println(
            "Prefix 'goo'   -> "
            + trie.getWordsWithPrefix("goo")
        );

        System.out.println(
            "Prefix 'xyz'   -> "
            + trie.getWordsWithPrefix("xyz")
        );

        System.out.println();

        System.out.println(
            "search('sony')     -> "
            + trie.search("sony")
        );

        System.out.println(
            "search('son')      -> "
            + trie.search("son")
        );

        System.out.println(
            "startsWith('son')  -> "
            + trie.startsWith("son")
        );
    }
}
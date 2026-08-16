public class LevenshteinTest {

    public static void main(String[] args) {

        System.out.println(
            "samsng vs samsung = "
            + Levenshtein.distance("samsng", "samsung")
        );

        System.out.println(
            "galaxi vs galaxy = "
            + Levenshtein.distance("galaxi", "galaxy")
        );

        System.out.println(
            "iphone vs iphone = "
            + Levenshtein.distance("iphone", "iphone")
        );

        System.out.println(
            "abc vs xyz = "
            + Levenshtein.distance("abc", "xyz")
        );

        System.out.println(
            "wireles vs wireless = "
            + Levenshtein.distance("wireles", "wireless")
        );
    }
}
package algorithms;

public class MillerRabin {

    private static final long[] WITNESSES = {
            2,
            325,
            9375,
            28178,
            450775,
            9780504,
            179526140
    };

    public static boolean isPrime(long n) {

        if (n < 2) {
            return false;
        }

        if (n == 2 || n == 3) {
            return true;
        }

        if ((n & 1L) == 0) {
            return false;
        }

        if (n < 9) {
            return true;
        }

        long d = n - 1;
        int s = 0;

        while ((d & 1L) == 0) {
            d >>= 1;
            s++;
        }

        for (int i = 0; i < WITNESSES.length; i++) {

            long witness = WITNESSES[i];

            if (witness >= n) {
                break;
            }

            if (!isProbablePrime(n, d, s, witness)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isProbablePrime(
            long n,
            long d,
            int s,
            long witness) {

        long x = modPow(witness, d, n);

        if (x == 1 || x == n - 1) {
            return true;
        }

        for (int r = 1; r < s; r++) {
            x = modMultiply(x, x, n);

            if (x == n - 1) {
                return true;
            }
        }

        return false;
    }

    private static long modPow(
            long base,
            long exponent,
            long mod) {

        long result = 1;
        long currentBase = positiveMod(base, mod);
        long currentExponent = exponent;

        while (currentExponent > 0) {

            if ((currentExponent & 1L) != 0) {
                result = modMultiply(result, currentBase, mod);
            }

            currentBase = modMultiply(currentBase, currentBase, mod);
            currentExponent >>= 1;
        }

        return result;
    }

    private static long modMultiply(
            long a,
            long b,
            long mod) {

        long reducedA = positiveMod(a, mod);
        long reducedB = positiveMod(b, mod);

        long result = 0;

        while (reducedB > 0) {

            if ((reducedB & 1L) != 0) {
                result = addMod(result, reducedA, mod);
            }

            reducedA = doubleMod(reducedA, mod);
            reducedB >>= 1;
        }

        return result;
    }

    private static long addMod(
            long left,
            long right,
            long mod) {

        if (left <= mod - right) {
            return left + right;
        }

        return left - (mod - right);
    }

    private static long doubleMod(
            long value,
            long mod) {

        if (value <= (mod >> 1)) {
            return value + value;
        }

        return value - (mod - value);
    }

    private static long positiveMod(
            long value,
            long mod) {

        long result = value % mod;

        if (result < 0) {
            result += mod;
        }

        return result;
    }
}

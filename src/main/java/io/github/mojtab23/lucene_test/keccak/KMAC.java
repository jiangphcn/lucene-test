package io.github.mojtab23.lucene_test.keccak;

import uk.org.bobulous.java.crypto.keccak.FIPS202;
import uk.org.bobulous.java.crypto.keccak.KeccakSponge;

import java.nio.ByteBuffer;

public class KMAC {

    /**
     * @param k is a key bit string of any length, including zero.
     * @param x is the main input bit string. It may be of any length, including zero.
     * @param l is an integer representing the requested output length in bits.
     * @param s is an optional customization bit string of any length, including zero. If no customization is desired, S is set to the empty string.
     */
    public static void kmac256(final byte[] k, final byte[] x, final int l, final byte[] s) {
        if (l < 0) {
            throw new IllegalArgumentException("L (length), should be greater than or equal 0.");
        }

//        final byte[] newX = bytepad(encodeString(K), 168) || X || rightEncode(L)

    }

    /**
     * @param x is the main input bit string. It may be of any length, including zero.
     * @param l is an integer representing the requested output length in bits.
     *          When the requested output length is zero, i.e., L=0, cSHAKE, KMAC, TupleHash, and ParallelHash return the
     *          empty string as the output.
     * @param n is a function-name bit string, used by NIST to define functions based on cSHAKE.
     *          When no function other than cSHAKE is desired, N is set to the empty string.
     * @param s S is a customization bit string. The user selects this string to define a variant of the
     *          function. When no customization is desired, S is set to the empty string.
     * @return
     */
    public static byte[] cSHAKE128(byte[] x, int l, byte[] n, byte[] s) {
        if (l < 0) {
            throw new IllegalArgumentException("l (length), should be greater than or equal 0.");
        }
        if (s == null) {
            s = new byte[0];
        }
        if (n == null) {
            n = new byte[0];
        }
        if (x == null) {
            x = new byte[0];
        }
        if (l == 0) {
            return new byte[0];
        }
        if (n.length == 0 && s.length == 0) {
            return FIPS202.ExtendableOutputFunction.SHAKE128.withOutputLength(l).apply(x);
        } else {
            KeccakSponge spongeFunction = new KeccakSponge(1344, 256, "", 4096);

        }
        // TODO: 6/2/2018
        return null;
    }


    public static byte[] cSHAKE256() {
//      todo
        return null;
    }


    private static byte[] bytePad(byte[] x, int w) {
        if (w <= 0) {
            throw new IllegalArgumentException("w should be greater than 0.");
        }
        byte[] z = concat(leftEncode(w), x);

        final int mod = z.length % w;
        if (mod != 0) {
            final int pad = w - mod;
            final byte[] temp = new byte[z.length + pad];
            System.arraycopy(z, 0, temp, 0, z.length);
            z = temp;
        }
        return z;
    }

    private static byte[] encodeString(byte[] s) {
        return concat(leftEncode(s.length), s);
    }


    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static byte[] leftEncode(long x) {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        final byte[] array = buffer.array();

        byte n = Long.BYTES;

        for (byte b : array) {
            if (b != 0 || n == 1) {
                break;
            } else {
                n--;
            }
        }

        final byte[] o = new byte[n + 1];

        o[0] = n;

        boolean skipZero = true;
        int i = 1;

        for (byte b : array) {
            if (b != 0 || !skipZero) {
                skipZero = false;
                o[i] = b;
                i++;
            }
        }
        return o;
    }

    private static byte[] rightEncode(long x) {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        final byte[] array = buffer.array();

        byte n = Long.BYTES;

        for (byte b : array) {
            if (b != 0 || n == 1) {
                break;
            } else {
                n--;
            }
        }

        final byte[] o = new byte[n + 1];

        o[n] = n;

        boolean skipZero = true;
        int i = 0;

        for (byte b : array) {
            if (b != 0 || !skipZero) {
                skipZero = false;
                o[i] = b;
                i++;
            }
        }
        return o;
    }

}

package cn.xlibs.lib4j.core.utility;

import java.security.SecureRandom;

/**
 * Random Utils
 *
 * @author Fury
 * @since 2020-04-17
 * <p>
 * All rights Reserved.
 */
public final class RandomUtils {
    private RandomUtils(){}
    private static final char[] ALPHABET;
    private static final int ALPHABET_SIZE;
    /** SecureRandom is preferred to Random */
    private static final SecureRandom rand;
    static {
        // SecureRandom.getInstanceStrong() will cause hang under Linux
        rand = new SecureRandom();
        ALPHABET = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "abcdefghijklmnopqrstuvwxyz0123456789").toCharArray();
        ALPHABET_SIZE = ALPHABET.length;
    }

    /**
     * random string combine with A-Z,a-z,0-9 in <code>length</code> size
     * @param length random string size
     * @return random string
     */
    public static String randAZaz09String(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            sb.append(ALPHABET[rand.nextInt(ALPHABET_SIZE)]);
        }

        return sb.toString();
    }
}

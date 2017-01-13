/*
 * This code belongs to BRjDevs
 * https://github.com/BRjDevs
 */

package br.com.brjdevs.steven.bran.core.utils;

public class MathHelper {
    public static int roundToPowerOf2(int i) {
        int value = i - 1;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        return value + 1;
    }

    public static int getNextPowerOfTwo(int value) {
        return value << 1;
    }

    public static int previousPowerOfTwo(int value) {
        return value >> 1;
    }

    public static long roundToPowerOf2(long l) {
        long value = l - 1;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        return value + 1;
    }

    public static long getNextPowerOfTwo(long value) {
        return value << 1;
    }

    public static long previousPowerOfTwo(long value) {
        return value >> 1;
    }
}

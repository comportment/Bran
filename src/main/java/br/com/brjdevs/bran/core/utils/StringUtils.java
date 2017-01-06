package br.com.brjdevs.bran.core.utils;

import java.util.Arrays;

public class StringUtils {
    public static String[] splitArgs(String args, int expectedArgs) {
        String[] raw = args.split("\\s+", expectedArgs);
        if (expectedArgs < 1) return raw;
        return normalizeArray(raw, expectedArgs);
    }
    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
    public static String[] split(String args, int expectedArgs, String regex) {
        String[] raw = args.split(regex, expectedArgs);
        if (expectedArgs < 1) return raw;
        return normalizeArray(raw, expectedArgs);
    }
    public static String[] normalizeArray(String[] raw, int expectedSize) {
        String[] normalized = new String[expectedSize];

        Arrays.fill(normalized, "");
        for (int i = 0; i < normalized.length; i++) {
            if (i < raw.length && raw[i] != null && !raw[i].isEmpty()) {
                normalized[i] = raw[i];
            }
        }
        return normalized;
    }
    public static String[] splitSimple(String args) {
        return args.split(" ");
    }

    public static int countMatches(CharSequence seq, char c) {
        if(seq == null || seq.length() == 0) {
            return 0;
        } else {
            int count = 0;

            for(int i = 0; i < seq.length(); ++i) {
                if(c == seq.charAt(i)) {
                    ++count;
                }
            }

            return count;
        }
    }
    public static String getContentBetween(String fullSeq, String seq1, String seq2) {
        try {
            return fullSeq.substring(fullSeq.indexOf(seq1) + 1, fullSeq.indexOf(seq2, fullSeq.indexOf(seq2) + 1));
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }
    public static String getContentBetween(String fullSeq, String s) {
        return getContentBetween(fullSeq, s, s);
    }

    public static String neat(String string) {
        String firstChar = String.valueOf(string.charAt(0)).toUpperCase();
        string = string.substring(1).toLowerCase();
        return firstChar + string;
    }
}

package br.com.brjdevs.steven.bran.core.utils;

import java.util.Arrays;
import java.util.Random;

public class StringUtils {
	
	private static final String ACTIVE_BLOCK = "\u2588";
	private static final String EMPTY_BLOCK = "\u200b";
	
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
	    return args.split("\\s+");
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
	
	public static String capitalize(String string) {
		String firstChar = String.valueOf(string.charAt(0)).toUpperCase();
        string = string.substring(1).toLowerCase();
        return firstChar + string;
    }
	
	public static boolean containsEqualsIgnoreCase(String toCheck, String s) {
		return Utils.containsEqualsIgnoreCase(Arrays.asList(toCheck.split("")), s);
	}
	
	public static String getProgressBar(long percent, long total) {
		return getProgressBar(percent, 100, total);
	}
	
	public static String getProgressBar(long percent, long duration, long total) {
		int activeBlocks = (int) ((float) percent / duration * total);
		StringBuilder builder = new StringBuilder().append(EMPTY_BLOCK);
		for (int i = 0; i < 10; i++) builder.append(activeBlocks >= i ? ACTIVE_BLOCK : ' ');
		return builder.append(EMPTY_BLOCK).toString();
	}
	
	public static String randomName(int randomLength) {
		char[] characters = new char[]
				{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
						'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
		
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < randomLength; i++) {
			builder.append(characters[rand.nextInt(characters.length)]);
		}
		return builder.toString();
	}
}

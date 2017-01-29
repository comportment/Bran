package br.com.brjdevs.steven.bran.core.utils;

import java.util.Random;

public class MathUtils {
	private static final Random random = new Random();
	public static int random(int max) {
		return random.nextInt(max);
	}
	public static boolean isInteger(String str) {
		try {
			int i = Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static String toOctalInteger(int i) {
		if (i > 9) return String.valueOf(i);
		return "0" + i;
	}
	public static boolean isInRange(int check, int min, int max) {
		if (min > max) throw new IndexOutOfBoundsException("min > max (" + min + ">" + max + ")");
		return check > min && check < max;
	}
	public static int parseIntOrDefault(String str, int d) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return d;
		}
	}
	
	public static int random(int min, int max) {
		return random.nextInt(max - min) + min;
	}
}

package br.com.brjdevs.bran.core.utils;

public class ArrayUtils {
	
	public static boolean contains(Object[] array, Object item) {
		for (Object o : array) {
			if (o == item) return true;
		}
		return false;
	}
}

package br.com.brjdevs.steven.bran.core.utils;

public class ArrayUtils {
	
	public static <T> boolean contains(T[] array, T item) {
		for (T o : array) {
			if (o != null && o.equals(item)) return true;
		}
		return false;
	}
}

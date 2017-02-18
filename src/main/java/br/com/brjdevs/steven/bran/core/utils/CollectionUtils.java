package br.com.brjdevs.steven.bran.core.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class CollectionUtils {
	
	public static <T> int getMatches(List<T> list, T obj) {
		int t = 0;
		for (T e : list) {
			if (e.equals(obj)) t++;
		}
		return t;
	}
	
	public static <T> int getMatches(Queue<T> queue, T obj) {
		int t = 0;
		for (T e : queue) {
			if (e.equals(obj)) t++;
		}
		return t;
	}
	
	public static <E> E random(List<E> list) {
		return list.get(MathUtils.random(list.size()));
	}
	
	public static <T, K> Entry<T, K> getEntryByIndex(Map<T, K> map, int index) {
		return (Entry<T, K>) map.entrySet().toArray()[index];
	}
}

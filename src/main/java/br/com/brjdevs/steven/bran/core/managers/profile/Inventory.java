package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.itemManager.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
	
	private Map<String, Integer> items;
	private int maxSize;
	
	public Inventory() {
		this.items = new HashMap<>();
		this.maxSize = 10;
	}
	
	public boolean put(Item item) {
		if (!items.containsKey(item.getId()))
			items.put(item.getId(), 0);
		if (getAmountOf(item) >= item.getMaxStack()) return false;
		items.put(item.getId(), getAmountOf(item) + 1);
		return true;
	}
	
	public void remove(Item item) {
		if (getAmountOf(item) > 1)
			items.put(item.getId(), getAmountOf(item) - 1);
		else
			items.remove(item.getId());
	}
	
	public int getAmountOf(Item item) {
		return items.getOrDefault(item.getId(), 0);
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public Map<String, Integer> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	public int size(boolean unique) {
		return !unique ? items.values().stream().mapToInt(Integer::intValue).sum() : items.size();
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
}

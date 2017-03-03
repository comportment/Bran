package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.currency.Item;
import br.com.brjdevs.steven.bran.core.currency.Items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory {
	
	private Map<Integer, AtomicInteger> items;
	
	public Inventory() {
		this.items = new HashMap<>();
	}
	
	public boolean put(Item item) {
		int id = Items.idOf(item);
		if (getAmountOf(item) + 1 < 0)
			return false;
		items.computeIfAbsent(id, i -> new AtomicInteger(0)).incrementAndGet();
		return true;
	}
	
	public boolean remove(Item item) {
		int id = Items.idOf(item);
		if (!items.containsKey(id))
			return false;
		if (getAmountOf(item) > 0)
			items.get(id).decrementAndGet();
		else
			items.remove(id);
		return true;
	}
	
	public int getAmountOf(Item item) {
		return items.getOrDefault(Items.idOf(item), new AtomicInteger(0)).get();
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public Map<Integer, AtomicInteger> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	public int size(boolean unique) {
		return !unique ? items.values().stream().mapToInt(AtomicInteger::get).sum() : items.size();
	}
}

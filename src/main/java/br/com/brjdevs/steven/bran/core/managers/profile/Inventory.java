package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.currency.Item;
import br.com.brjdevs.steven.bran.core.currency.ItemMeta;
import br.com.brjdevs.steven.bran.core.currency.Items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
	
	private Map<Integer, ItemMeta> items;
	
	public Inventory() {
		this.items = new HashMap<>();
	}
	
	public boolean put(Item item) {
		if (getAmountOf(item) + 1 < 0)
			return false;
		items.computeIfAbsent(Items.idOf(item), i -> ItemMeta.of(item)).add();
		return true;
	}
	
	public boolean remove(Item item) {
		int id = Items.idOf(item);
		if (!items.containsKey(id))
			return false;
		if (getAmountOf(item) > 0)
			items.get(id).add();
		else
			items.remove(id);
		return true;
	}
	
	public long getAmountOf(Item item) {
		return items.getOrDefault(ItemMeta.of(item), ItemMeta.of(item)).getAmount();
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public Map<Integer, ItemMeta> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	public long size(boolean unique) {
		return !unique ? items.values().stream().mapToLong(ItemMeta::getAmount).sum() : items.size();
	}
}

package br.net.brjdevs.steven.bran.core.managers.profile;

import br.net.brjdevs.steven.bran.core.currency.Item;
import br.net.brjdevs.steven.bran.core.currency.ItemMeta;
import br.net.brjdevs.steven.bran.core.currency.Items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
	
	private Map<Integer, ItemMeta> items;
	
	public Inventory() {
		this.items = new HashMap<>();
	}
	
	public boolean put(Item item) {
		if (getAmountOf(item) > 100)
			return false;
		items.computeIfAbsent(Items.idOf(item), i -> ItemMeta.of(item)).join();
		return true;
	}
	
	public boolean put(Item item, ItemMeta itemMeta) {
		if (getAmountOf(item) > 100)
			return false;
		int id = Items.idOf(item);
		if (items.containsKey(id))
			items.get(id).join(itemMeta.getAmount());
		else
			items.put(id, itemMeta);
		return true;
	}
	
	public boolean remove(Item item) {
		int id = Items.idOf(item);
		if (!items.containsKey(id))
			return false;
		if (getAmountOf(item) > 1)
			items.get(id).remove();
		else
			items.remove(id);
		return true;
	}
	
	public long getAmountOf(Item item) {
		if (!items.containsKey(Items.idOf(item)))
			return 0;
		return items.get(Items.idOf(item)).getAmount();
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

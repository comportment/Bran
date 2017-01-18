package br.com.brjdevs.steven.bran.core.itemManager;

import org.reflections.Reflections;

import java.util.*;

public class ItemContainer {
	
	private static final Map<String, Item> container = new HashMap<>();
	
	public static void loadItems() {
		Reflections reflections = new Reflections("br.com.brjdevs.steven.bran.core.itemManager.items");
		Set<Class<? extends Item>> items = reflections.getSubTypesOf(Item.class);
		items.forEach(clazz -> {
			try {
				Item item = clazz.newInstance();
				container.put(item.getId(), item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public static Item getItemById(String id) {
		return container.get(id);
	}
	
	public static Map<String, Item> map() {
		return Collections.unmodifiableMap(container);
	}
	
	public Collection<Item> getItems() {
		return container.values();
	}
}

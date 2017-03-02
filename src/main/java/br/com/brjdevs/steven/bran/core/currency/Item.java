package br.com.brjdevs.steven.bran.core.currency;

import java.util.Random;

public class Item {
	
	private final long value;
	private String emoji, name, description;
	private long price;
	private boolean staticValue;
	
	public Item(String emoji, String name, String desc, long value) {
		this.emoji = emoji;
		this.name = name;
		this.description = desc;
		this.value = value;
		this.price = value;
		this.staticValue = false;
	}
	
	public void changePrices(Random r) {
		if (staticValue) return;
		long min = (long) (value * 0.9), max = (long) (value * 1.1), dif = max - min;
		price = min + r.nextInt((int) dif);
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getEmoji() {
		return emoji;
	}
	
	public String getName() {
		return name;
	}
	
	public long getValue() {
		return price;
	}
}

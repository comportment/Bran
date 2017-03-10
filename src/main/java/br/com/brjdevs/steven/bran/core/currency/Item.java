package br.com.brjdevs.steven.bran.core.currency;

import java.util.Random;

public class Item {
	
	private final long value;
	private String emoji, name, description;
	private long price;
	private boolean sellable, buyable;
	private int startDurability;
	
	public Item(String emoji, String name, String desc, int startDurability, long value, boolean sellable, boolean buyable) {
		this.emoji = emoji;
		this.name = name;
		this.description = desc;
		this.startDurability = startDurability;
		this.value = value;
		this.price = value;
		this.sellable = sellable;
		this.buyable = buyable;
	}
	
	public void changePrices(Random r) {
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
	
	public int getStartDurability() {
		return startDurability;
	}
	
	public boolean isBuyable() {
		return buyable;
	}
	
	public boolean isSellable() {
		return sellable;
	}
}

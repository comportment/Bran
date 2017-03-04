package br.com.brjdevs.steven.bran.core.currency;

public class ItemMeta {
	
	private int itemId;
	private long duration;
	private long damage;
	private long amount;
	
	public ItemMeta(Item item, long duration) {
		this.itemId = Items.idOf(item);
		this.duration = duration;
		this.damage = 0;
	}
	
	public ItemMeta(ItemMeta meta) {
		this.itemId = meta.itemId;
		this.duration = meta.duration;
		this.damage = meta.damage;
	}
	
	public static ItemMeta of(Item item) {
		return new ItemMeta(item, item.getStartDurability());
	}
	
	public Item getItem() {
		return Items.fromId(itemId);
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public long getMaxDurability() {
		return duration;
	}
	
	public long getDamage() {
		return damage;
	}
	
	public long getRemainingDurability() {
		return duration - damage;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public void add() {
		this.amount++;
	}
	
	public boolean doDamage(long damage) {
		if (damage > getRemainingDurability())
			return false;
		this.damage += damage;
		return true;
	}
}

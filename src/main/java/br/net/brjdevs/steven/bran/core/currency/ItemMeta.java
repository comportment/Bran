package br.net.brjdevs.steven.bran.core.currency;

public class ItemMeta {
	
	private int itemId;
	private long duration;
	private long damage;
	private long amount;
	
	private ItemMeta(Item item, long duration) {
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
	
	public ItemMeta join() {
		return join(1);
	}
	
	public ItemMeta join(long amount) {
		this.amount += amount;
		if (this.amount > 100)
			this.amount = 100;
		return this;
	}
	
	public void remove() {
		this.amount--;
	}
	
	public boolean doDamage(long damage) {
		if (damage > getRemainingDurability()) {
			if (amount <= 0)
				throw new NullPointerException("No " + getItem().getName() + " left!");
			amount--;
			this.damage = 0;
			return false;
		}
		this.damage += damage;
		return true;
	}
	
	public String toString() {
		Item item = getItem();
		return item.getEmoji() + " " + item.getName() + " x" + getAmount();
	}
}

package br.com.brjdevs.steven.bran.core.currency;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemStack {
	
	private final int amount;
	private final Item item;
	
	public ItemStack(Item item, int amount) {
		this.item = item;
		this.amount = amount;
	}
	
	public static List<ItemStack> stackfy(List<Item> items) {
		return stackfy(items, item -> 1);
	}
	
	private static List<ItemStack> stackfy(List<Item> items, ToIntFunction<Item> amountFunction) {
		return items.stream().map(item -> new ItemStack(item, amountFunction.applyAsInt(item))).collect(Collectors.toList());
	}
	
	public static Map<Item, ItemStack> mapped(List<ItemStack> list) {
		return list.stream().collect(Collectors.toMap(ItemStack::getItem, UnaryOperator.identity(), ItemStack::join));
	}
	
	public static Map<Item, ItemStack> mapped(ItemStack... stacks) {
		return mapped(Arrays.asList(stacks));
	}
	
	public static List<ItemStack> reduce(List<ItemStack> list) {
		return new ArrayList<>(mapped(list).values());
	}
	
	public static List<ItemStack> reduce(ItemStack... stacks) {
		return reduce(Arrays.asList(stacks));
	}
	
	public static String toString(List<ItemStack> list) {
		if (list.isEmpty()) return "There's only dust.";
		return list.stream().map(Object::toString).sorted(randomOrder()).collect(Collectors.joining(", "));
	}
	
	public static String toString(ItemStack... stacks) {
		return toString(Arrays.asList(stacks));
	}
	
	public static Comparator<String> randomOrder() {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		int x = r.nextInt(), y = r.nextInt();
		boolean b = r.nextBoolean();
		return Comparator.comparingInt((String s) -> s.hashCode() ^ x)
				.thenComparingInt(s -> s.length() ^ y)
				.thenComparing(b ? Comparator.naturalOrder() : Comparator.reverseOrder());
	}
	
	@Override
	public String toString() {
		return getItem().getEmoji() + " x " + getAmount();
	}
	
	public int getAmount() {
		return amount;
	}
	
	public Item getItem() {
		return item;
	}
	
	public ItemStack join(ItemStack stack) {
		if (!stack.getItem().equals(this.getItem())) throw new UnsupportedOperationException("Not the same Items");
		return new ItemStack(this.getItem(), this.getAmount() + stack.getAmount());
	}
	
}

package br.net.brjdevs.steven.bran.core.currency;

import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DroppedMoney {
	
	private static final Map<String, AtomicInteger> DROPPED_MONEY = new HashMap<>();
	private static Random r = new Random(System.currentTimeMillis());
	private final AtomicInteger money;
	
	private DroppedMoney(AtomicInteger money) {
		this.money = money;
	}
	
	public static DroppedMoney of(String id) {
		return new DroppedMoney(DROPPED_MONEY.computeIfAbsent(id, k -> new AtomicInteger(0)));
	}
	
	public static DroppedMoney of(TextChannel channel) {
		return of(channel.getId());
	}
	
	public int collect() {
		return money.getAndSet(0);
	}
	
	public void drop(int money) {
		this.money.addAndGet(money);
	}
	
	public boolean dropWithChance(int money, int weight) {
		boolean doDrop = r.nextInt(weight) == 0;
		if (doDrop) drop(money);
		return doDrop;
	}
	
}

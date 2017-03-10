package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.TaskManager;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Items {
	
	public static final Item PICKAXE, MUSICAL_NOTE, PING_PONG, GAME_DIE, STONE, COOPER, SILVER, IRON, GOLD, DIAMOND, EMERALD;
	
	public static final Item[] ALL = {
			PICKAXE = new Item("\u26cf", "Pickaxe", "Mine... mine.... minecraft?", 100, 90, true, true),
			MUSICAL_NOTE = new Item("\uD83C\uDFB5", "Musical Note", "I assume you like music, huh?", 1, 30, true, false),
			PING_PONG = new Item("\uD83C\uDFD3", "Ping Pong Racket", "I won by a few milliseconds.", 1, 20, true, false),
			GAME_DIE = new Item("\uD83C\uDFB2", "Game Die", "It looks like you like games.", 1, 60, true, false),
			STONE = new Item("", "Stone", "", 1, 2, true, false),
			COOPER = new Item("", "Copper", "", 1, 10, true, false),
			SILVER = new Item("", "Silver", "", 1, 15, true, false),
			IRON = new Item("", "Iron", "", 1, 25, true, false),
			GOLD = new Item("", "Gold", "", 1, 50, true, false),
			DIAMOND = new Item("\uD83D\uDD39", "Diamond", "", 1, 100, true, false),
			EMERALD = new Item("", "Emerald", "", 1, 150, true, false)
	};
	
	static {
		TaskManager.startAsyncTask("Market Thread", (service) -> Stream.of(ALL).forEach(item -> item.changePrices(MathUtils.random)), 3600);
	}
	
	public static Optional<Item> fromEmoji(String emoji) {
		return Stream.of(ALL).filter(item -> item.getEmoji().equals(emoji)).findFirst();
	}
	
	public static Item fromId(int id) {
		return ALL[id];
	}
	
	public static int idOf(Item item) {
		return Arrays.asList(ALL).indexOf(item);
	}
}

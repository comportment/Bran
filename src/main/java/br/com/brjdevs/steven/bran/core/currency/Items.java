package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.TaskManager;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Items {
	
	public static final Item PICKAXE, MUSICAL_NOTE, PING_PONG, BAN_HAMMER, KICK_BOOT, GAME_DIE;
	
	public static final Item[] ALL = {
			PICKAXE = new Item("\u26cf", "Pickaxe", "Mine... mine.... minecraft?", 100, 90, true, true),
			MUSICAL_NOTE = new Item("\uD83C\uDFB5", "Musical Note", "I assume you like music, huh?", 1, 30, true, false),
			PING_PONG = new Item("\uD83C\uDFD3", "Ping Pong Racket", "I won by a few milliseconds.", 1, 20, true, false),
			BAN_HAMMER = new Item("\uD83D\uDD28", "Ban Hammer", "Best item ever.", 1, 25, true, false),
			KICK_BOOT = new Item("\uD83D\uDC62", "Kick Boot", "Not so cool as Ban Hammer but pretty great.", 1, 25, true, false),
			GAME_DIE = new Item("\uD83C\uDFB2", "Game Die", "It looks like you like games.", 1, 60, true, false)
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

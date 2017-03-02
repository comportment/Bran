package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.TaskManager;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

import java.util.stream.Stream;

public class Items {
	
	public static final Item PICKAXE, MUSICAL_NOTE, PING_PONG, BAN_HAMMER, KICK_BOOT, GAME_DIE;
	
	public static final Item[] ALL = {
			PICKAXE = new Item("\u26cf", "Pickaxe", "Mine... mine.... minecraft?", 30),
			MUSICAL_NOTE = new Item("\uD83C\uDFB5", "Musical Note", "I assume you like music, huh?", 50),
			PING_PONG = new Item("\uD83C\uDFD3", "Ping Pong Racket", "I won by a few milliseconds.", 20),
			BAN_HAMMER = new Item("\uD83D\uDD28", "Ban Hammer", "Best item ever.", 25),
			KICK_BOOT = new Item("\uD83D\uDC62", "Kick Boot", "Not so cool as Ban Hammer.", 25),
			GAME_DIE = new Item("\uD83C\uDFB2", "Game Die", "It looks like you like games.", 60)
	};
	
	static {
		TaskManager.startAsyncTask("Market Thread", (service) -> Stream.of(ALL).forEach(item -> item.changePrices(MathUtils.random)), 3600);
	}
}

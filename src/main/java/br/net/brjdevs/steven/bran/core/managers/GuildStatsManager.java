package br.net.brjdevs.steven.bran.core.managers;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildStatsManager {
	
	public static final Map<LoggedEvent, AtomicInteger>
			TOTAL_EVENTS = new HashMap<>(),
			DAY_EVENTS = new HashMap<>(),
			HOUR_EVENTS = new HashMap<>(),
			MINUTE_EVENTS = new HashMap<>();
	private static final char ACTIVE_BLOCK = '\u2588';
	private static final char EMPTY_BLOCK = '\u200b';
	private static final ExpirationManager EXPIRATION = new ExpirationManager();
	private static final int MINUTE = 60000, HOUR = 3600000, DAY = 86400000;
	
	public static String bar(int percent, int total) {
		int activeBlocks = (int) ((float) percent / 100f * total);
		StringBuilder builder = new StringBuilder().append('`').append(EMPTY_BLOCK);
		for (int i = 0; i < total; i++) builder.append(activeBlocks > i ? ACTIVE_BLOCK : ' ');
		return builder.append(EMPTY_BLOCK).append('`').toString();
	}
	
	public static EmbedBuilder fillEmbed(Map<LoggedEvent, AtomicInteger> events, EmbedBuilder builder) {
		int total = events.values().stream().mapToInt(AtomicInteger::get).sum();
		
		if (total == 0) {
			builder.addField("Nothing Here.", "Just dust.", false);
			return builder;
		}
		
		events.entrySet().stream()
				.filter(entry -> entry.getValue().get() > 0)
				.sorted(Comparator.comparingInt(entry -> total - entry.getValue().get()))
				.limit(12)
				.forEachOrdered(entry -> {
					int percent = entry.getValue().get() * 100 / total;
					builder.addField(entry.getKey().toString(), String.format("%s %d%% (%d)", bar(percent, 15), percent, entry.getValue().get()), true);
				});
		
		return builder.setFooter("Guilds: " + Bran.getInstance().getGuilds().size(), null);
	}
	
	public static void log(LoggedEvent loggedEvent) {
		long millis = System.currentTimeMillis();
		TOTAL_EVENTS.computeIfAbsent(loggedEvent, k -> new AtomicInteger(0)).incrementAndGet();
		DAY_EVENTS.computeIfAbsent(loggedEvent, k -> new AtomicInteger(0)).incrementAndGet();
		HOUR_EVENTS.computeIfAbsent(loggedEvent, k -> new AtomicInteger(0)).incrementAndGet();
		MINUTE_EVENTS.computeIfAbsent(loggedEvent, k -> new AtomicInteger(0)).incrementAndGet();
		EXPIRATION.letExpire(millis + MINUTE, () -> MINUTE_EVENTS.get(loggedEvent).decrementAndGet());
		EXPIRATION.letExpire(millis + HOUR, () -> HOUR_EVENTS.get(loggedEvent).decrementAndGet());
		EXPIRATION.letExpire(millis + DAY, () -> DAY_EVENTS.get(loggedEvent).decrementAndGet());
	}
	
	public static String resume(Map<LoggedEvent, AtomicInteger> commands) {
		int total = commands.values().stream().mapToInt(AtomicInteger::get).sum();
		
		return (total == 0) ? ("No Events Logged.") : ("Count: " + total + "\n" + commands.entrySet().stream()
				.filter(entry -> entry.getValue().get() > 0)
				.sorted(Comparator.comparingInt(entry -> total - entry.getValue().get()))
				.limit(5)
				.map(entry -> {
					int percent = entry.getValue().get() * 100 / total;
					return String.format("%s %d%% **%s** (%d)", bar(percent, 15), percent, entry.getKey().toString(), entry.getValue().get());
				})
				.collect(Collectors.joining("\n")));
	}
	
	public enum LoggedEvent {
		JOIN, LEAVE;
		
		@Override
		public String toString() {
			return StringUtils.capitalize(name());
		}
	}
	
}

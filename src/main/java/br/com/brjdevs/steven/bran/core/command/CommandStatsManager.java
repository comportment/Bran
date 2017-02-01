package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Expirator;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CommandStatsManager {
	
	public static final Map<ICommand, AtomicInteger>
			TOTAL_CMDS = new HashMap<>(),
			DAY_CMDS = new HashMap<>(),
			HOUR_CMDS = new HashMap<>(),
			MINUTE_CMDS = new HashMap<>();
	
	private static final Expirator EXPIRATOR = new Expirator();
	private static final int MINUTE = 60000, HOUR = 3600000, DAY = 86400000;
	
	public static void log(ICommand cmd) {
		long millis = System.currentTimeMillis();
		TOTAL_CMDS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
		DAY_CMDS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
		HOUR_CMDS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
		MINUTE_CMDS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
		EXPIRATOR.letExpire(millis + MINUTE, () -> MINUTE_CMDS.get(cmd).decrementAndGet());
		EXPIRATOR.letExpire(millis + HOUR, () -> HOUR_CMDS.get(cmd).decrementAndGet());
		EXPIRATOR.letExpire(millis + DAY, () -> DAY_CMDS.get(cmd).decrementAndGet());
	}
	
	public static EmbedBuilder fillEmbed(Map<ICommand, AtomicInteger> commands, EmbedBuilder builder) {
		int total = commands.values().stream().mapToInt(AtomicInteger::get).sum();
		
		if (total == 0) {
			builder.addField("Nothing Here.", "Just dust.", false);
			return builder;
		}
		
		commands.entrySet().stream()
				.filter(entry -> entry.getValue().get() > 0)
				.sorted(Comparator.comparingInt(entry -> total - entry.getValue().get()))
				.limit(12)
				.forEachOrdered(entry -> {
					int percent = entry.getValue().get() * 100 / total;
					builder.addField(entry.getKey().getName(), String.format("[`%s`] %d%%", StringUtils.getProgressBar(percent, 15), percent), true);
				});
		
		return builder;
	}
	
	public static String resume(Map<ICommand, AtomicInteger> commands) {
		int total = commands.values().stream().mapToInt(AtomicInteger::get).sum();
		
		return (total == 0) ? ("No Commands issued.") : ("Count: " + total + "\n" + commands.entrySet().stream()
				.filter(entry -> entry.getValue().get() > 0)
				.sorted(Comparator.comparingInt(entry -> total - entry.getValue().get()))
				.limit(5)
				.map(entry -> {
					int percent = entry.getValue().get() * 100 / total;
					return String.format("[`%s`] %d%% **%s**", StringUtils.getProgressBar(percent, 15), percent, entry.getKey().getName());
				})
				.collect(Collectors.joining("\n")));
	}
}

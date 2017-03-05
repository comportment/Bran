package br.com.brjdevs.steven.bran.core.utils;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.listeners.EventListener;
import br.com.brjdevs.steven.bran.core.managers.GuildStatsManager;
import br.com.brjdevs.steven.bran.core.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

import static br.com.brjdevs.steven.bran.core.command.CommandStatsManager.*;

public class Session extends EventListener<GuildMessageReceivedEvent> {
	
	private final Runtime instance = Runtime.getRuntime();
	public double cpuUsage;
	public long cmds;
	public long msgsReceived;
	public long msgsSent;
	private int availableProcessors = Runtime.getRuntime().availableProcessors();
	private double lastProcessCpuTime = 0;
	private long lastSystemTime = 0;
	
	public Session() {
		super(GuildMessageReceivedEvent.class);
		this.cpuUsage = 0;
		this.cmds = 0;
		this.msgsReceived = 0;
		this.msgsSent = 0;
	}
	
	private static double calculateProcessCpuTime(OperatingSystemMXBean os) {
		return ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuTime();
	}
	
	public void readMessage(boolean isSelf) {
		if (isSelf) msgsSent++; else msgsReceived++;
	}
	
	public String getUptime(){
		final long
				duration = ManagementFactory.getRuntimeMXBean().getUptime(),
				years = duration / 31104000000L,
				months = duration / 2592000000L % 12,
				days = duration / 86400000L % 30,
				hours = duration / 3600000L % 24,
				minutes = duration / 60000L % 60,
				seconds = duration / 1000L % 60;
		String uptime = (years == 0 ? "" : years + " Years, ") + (months == 0 ? "" : months + " Months, ")
				+ (days == 0 ? "" : days + " Days, ") + (hours == 0 ? "" : hours + " Hours, ")
				+ (minutes == 0 ? "" : minutes + " Minutes, ") + (seconds == 0 ? "" : seconds + " Seconds, ");
		
		uptime = StringUtils.replaceLast(uptime, ", ", "");
		return StringUtils.replaceLast(uptime, ",", " and");
	}
	
	public double calculateCpuUsage(OperatingSystemMXBean os) {
		long systemTime = System.nanoTime();
		double processCpuTime = calculateProcessCpuTime(os);
		
		double cpuUsage = (processCpuTime - lastProcessCpuTime) / ((double) (systemTime - lastSystemTime));
		
		lastSystemTime = systemTime;
		lastProcessCpuTime = processCpuTime;
		
		return cpuUsage / availableProcessors;
	}
	
	public MessageEmbed toEmbedAbout(JDA jda) {
		List<Guild> guilds = Bran.getInstance().getGuilds();
		List<TextChannel> channels = Bran.getInstance().getTextChannels();
		List<VoiceChannel> voiceChannels = Bran.getInstance().getVoiceChannels();
		List<User> users = Bran.getInstance().getUsers();
		long audioConnections = guilds.stream().filter(g -> g.getAudioManager().isConnected()).count();
		long queueSize = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> !musicManager.getTrackScheduler().getQueue().isEmpty()).map(musicManager -> musicManager.getTrackScheduler().getQueue().size()).mapToInt(Integer::intValue).sum();
		long nowPlaying = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getPlayer().getPlayingTrack() != null && !musicManager.getTrackScheduler().isPaused()).count();
		long paused = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getTrackScheduler().isPaused()).count();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setAuthor("About me", null, jda.getSelfUser().getEffectiveAvatarUrl());
		embedBuilder.setThumbnail(jda.getSelfUser().getAvatarUrl());
		embedBuilder.addField("\uD83C\uDFD8 Guilds", String.valueOf(guilds.size()), true);
		embedBuilder.addField("\uD83D\uDC65 Users", String.valueOf(users.size()), true);
		embedBuilder.addField("\uD83D\uDCDD Text Channels", String.valueOf(channels.size()), true);
		embedBuilder.addField("\uD83D\uDDE3 Voice Channels", String.valueOf(voiceChannels.size()), true);
		embedBuilder.addField("\uD83D\uDCE4 Sent Messages", String.valueOf(msgsSent), true);
		embedBuilder.addField("\uD83D\uDCE5 Received Messages", String.valueOf(msgsReceived), true);
		embedBuilder.addField("\uD83D\uDCBB Executed Commands", String.valueOf(cmds), true);
		embedBuilder.addField("\uD83D\uDD39 Shards (C/T)", Bran.getInstance().getOnlineShards().length + "/" + Bran.getInstance().getShards().length, true);
		embedBuilder.addField("<:jda:230988580904763393> JDA Version", JDAInfo.VERSION, true);
		embedBuilder.addField("\uD83D\uDCF0 API Responses", String.valueOf(Bran.getInstance().getResponseTotal()), true);
		embedBuilder.addField("\uD83C\uDFB8 Music Stats", "\u00AD", false);
		embedBuilder.addField("\uD83C\uDF10 Connections", String.valueOf(audioConnections), true);
		embedBuilder.addField("\uD83C\uDFB6 Queue size", String.valueOf(queueSize), true);
		embedBuilder.addField("\uD83D\uDD0A Now playing", String.valueOf(nowPlaying), true);
		embedBuilder.addField("\u23f8 Paused", String.valueOf(paused), true);
		return embedBuilder.setColor(Color.DARK_GRAY).build();
	}
	
	public MessageEmbed toEmbedTechnical(JDA jda) {
		String ram = ((instance.totalMemory() - instance.freeMemory()) >> 20) + " MB/" + (instance.maxMemory() >> 20) + " MB";
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setAuthor("Technical Information", null, jda.getSelfUser().getEffectiveAvatarUrl());
		embedBuilder.setThumbnail(jda.getSelfUser().getAvatarUrl());
		embedBuilder.addField("Uptime", getUptime(), false);
		embedBuilder.addField("CPU Usage", String.valueOf(cpuUsage) + "%", true);
		embedBuilder.addField("Threads", String.valueOf(Thread.activeCount()), true);
		embedBuilder.addField("RAM (USAGE/MAX)", ram, true);
		return embedBuilder.setColor(Color.DARK_GRAY).build();
	}
	
	public MessageEmbed toEmbedCmds(JDA jda) {
		return new EmbedBuilder().setTitle("Command Stats", null).setColor(Color.DARK_GRAY)
				.addField("Now", GuildStatsManager.resume(GuildStatsManager.MINUTE_EVENTS), false)
				.addField("Hourly", GuildStatsManager.resume(GuildStatsManager.HOUR_EVENTS), false)
				.addField("Daily", GuildStatsManager.resume(GuildStatsManager.DAY_EVENTS), false)
				.addField("Total", GuildStatsManager.resume(GuildStatsManager.TOTAL_EVENTS), false)
				.setFooter("Guilds: " + Bran.getInstance().getGuilds().size(), null).build();
	}
	
	public MessageEmbed toEmbedGuilds(JDA jda) {
		return new EmbedBuilder().setTitle("Guild Stats", null).setColor(Color.DARK_GRAY)
				.addField("Now", resume(MINUTE_CMDS), false)
				.addField("Hourly", resume(HOUR_CMDS), false)
				.addField("Daily", resume(DAY_CMDS), false)
				.addField("Total", resume(TOTAL_CMDS), false).build();
	}
	
	public String toString(JDA jda) {
		List<Guild> guilds = Bran.getInstance().getGuilds();
		List<TextChannel> channels = Bran.getInstance().getTextChannels();
		List<VoiceChannel> voiceChannels = Bran.getInstance().getVoiceChannels();
		List<User> users = Bran.getInstance().getUsers();
		long audioConnections = guilds.stream().filter(g -> g.getAudioManager().isConnected()).count();
		long queueSize = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> !musicManager.getTrackScheduler().getQueue().isEmpty()).map(musicManager -> musicManager.getTrackScheduler().getQueue().size()).mapToInt(Integer::intValue).sum();
		String ram = ((instance.totalMemory() - instance.freeMemory()) >> 20) + " MB/" + (instance.maxMemory() >> 20) + " MB";
		long nowPlaying = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getPlayer().getPlayingTrack() != null && !musicManager.getTrackScheduler().isPaused()).count();
		long paused = Bran.getInstance().getMusicManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getTrackScheduler().isPaused()).count();
		StringBuilder sb = new StringBuilder();
		sb.append("```prolog\n");
		sb.append("--Bot Stats--\n");
		sb.append("Name: ").append(jda.getSelfUser().getName()).append(" (ID: ").append(jda.getSelfUser().getId()).append(")\n");
		sb.append("Uptime: ").append(getUptime()).append('\n');
		sb.append("Threads: ").append(Thread.activeCount()).append('\n');
		sb.append("JDA Version: ").append(JDAInfo.VERSION).append('\n');
		sb.append("RAM (USAGE/MAX): ").append(ram).append('\n');
		sb.append("CPU Usage: ").append(cpuUsage).append("%\n");
		sb.append("Shards (ONLINE/TOTAL): ").append(Bran.getInstance().getOnlineShards().length).append("/").append(Bran.getInstance().getTotalShards()).append("\n");
		sb.append("API Responses: ").append(Bran.getInstance().getResponseTotal()).append("\n\n");
		sb.append("--General--\n");
		sb.append("Guilds: ").append(guilds.size()).append("\n");
		sb.append("Users: ").append(users.size()).append("\n");
		sb.append("Text Channels: ").append(channels.size()).append("\n");
		sb.append("Voice Channels: ").append(voiceChannels.size()).append("\n");
		sb.append("Sent Messages: ").append(msgsSent).append("\n");
		sb.append("Received Messages: ").append(msgsReceived).append("\n");
		sb.append("Executed Commands: ").append(cmds).append("\n");
		sb.append("Running Polls: ").append(Poll.getRunningPolls().size()).append("\n\n");
		sb.append("--Music--\n");
		sb.append("Connections: ").append(audioConnections).append("\n");
		sb.append("Queue Size: ").append(queueSize).append("\n");
		sb.append("Now Playing: ").append(nowPlaying).append("\n");
		sb.append("Paused: ").append(paused);
		sb.append("```");
		return sb.toString();
	}
	
	public void event(GuildMessageReceivedEvent event) {
		Bran.getInstance().getSession().readMessage(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()));
	}
}

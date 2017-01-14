package br.com.brjdevs.steven.bran.core.utils;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

public class Session {
	
	private final Runtime instance = Runtime.getRuntime();
	private final int mb = 1024 * 1024;
	public double cpuUsage;
	public long cmds;
	public long msgsReceived;
	public long msgsSent;
	public Session() {
		this.cpuUsage = 0;
		this.cmds = 0;
		this.msgsReceived = 0;
		this.msgsSent = 0;
	}

	public void readMessage(boolean isSelf) {
		if (isSelf) msgsSent++; else msgsReceived++;
	}

	public MessageEmbed toEmbed (CommandEvent event) {
		List<Guild> guilds = Bot.getInstance().getGuilds();
		List<TextChannel> channels = Bot.getInstance().getTextChannels();
		List<VoiceChannel> voiceChannels = Bot.getInstance().getVoiceChannels();
		List<User> users = Bot.getInstance().getUsers();
		long audioConnections = guilds.stream().filter(g -> g.getAudioManager().isConnected()).count();
		long queueSize = AudioUtils.getManager().getMusicManagers().entrySet().stream().filter(entry -> !entry.getValue().getTrackScheduler().getQueue().isEmpty()).map(entry -> entry.getValue().getTrackScheduler().getQueue().size()).collect(Collectors.toList()).size();
		String ram = ((instance.totalMemory() - instance.freeMemory()) / mb) + " MB/" + (instance.totalMemory() / mb) + " MB";
		long nowPlaying = AudioUtils.getManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getPlayer().getPlayingTrack() != null).count();
		JDA jda = event.getJDA();
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setAuthor("Bot Stats (" + Util.getUser(jda.getSelfUser()) + ")", null, jda.getSelfUser().getAvatarUrl());
		embedBuilder.addField("Uptime", getUptime(), false);
		embedBuilder.addField("Threads", String.valueOf(Thread.activeCount()), true);
		embedBuilder.addField("RAM (USAGE/MAX)", String.valueOf(ram), true);
		embedBuilder.addField("CPU Usage", String.valueOf(cpuUsage) + "%", true);
		embedBuilder.addField("JDA Version", JDAInfo.VERSION, true);
		embedBuilder.addField("API Responses", jda.getResponseTotal() + "\n\n**General**", true);
		embedBuilder.addField("Shards (ONLINE/TOTAL)", Bot.getInstance().getShards().size() + "/" + Bot.getInstance().getOnlineShards(), true);
		//General Stats
		embedBuilder.addField("Guilds", String.valueOf(guilds.size()), true);
		embedBuilder.addField("Users", String.valueOf(users.size()), true);
		embedBuilder.addField("Text Channels", String.valueOf(channels.size()), true);
		embedBuilder.addField("Voice Channels", String.valueOf(voiceChannels.size()), true);
		embedBuilder.addField("Sent Messages", String.valueOf(msgsSent) + "\n\n**Music**", true);
		embedBuilder.addField("Messages Received", String.valueOf(msgsReceived), true);
		//embedBuilder.addField("Running Polls", String.valueOf(Poll.getRunningPolls().size()), true);
		//Music Stats
		embedBuilder.addField("Connections", String.valueOf(audioConnections), true);
		embedBuilder.addField("Queue Size", String.valueOf(queueSize), true);
		embedBuilder.addField("Now Playing", String.valueOf(nowPlaying), true);
		
		Color color = event.getGuild().getSelfMember().getColor();
		embedBuilder.setColor(color == null ? Color.decode("#F1AC1A") : color);
		
		return embedBuilder.build();
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
	
	public String toString(JDA jda) {
		List<Guild> guilds = Bot.getInstance().getGuilds();
		List<TextChannel> channels = Bot.getInstance().getTextChannels();
		List<VoiceChannel> voiceChannels = Bot.getInstance().getVoiceChannels();
		List<User> users = Bot.getInstance().getUsers();
		long audioConnections = guilds.stream().filter(g -> g.getAudioManager().isConnected()).count();
		long queueSize = AudioUtils.getManager().getMusicManagers().values().stream().filter(musicManager -> !musicManager.getTrackScheduler().getQueue().isEmpty()).map(musicManager -> musicManager.getTrackScheduler().getQueue().size()).collect(Collectors.toList()).size();
		String ram = ((instance.totalMemory() - instance.freeMemory()) / mb) + " MB/" + (instance.totalMemory() / mb) + " MB";
		long nowPlaying = AudioUtils.getManager().getMusicManagers().values().stream().filter(musicManager -> musicManager.getPlayer().getPlayingTrack() != null).count();
		String out = "";
		out += "```prolog\n";
		out += "--Bot Stats--\n";
		out += "Name: " + jda.getSelfUser().getName() + " (ID: " + jda.getSelfUser().getId() + ")\n";
		out += "Uptime: " + getUptime() +'\n';
		out += "Threads: " + Thread.activeCount() + '\n';
		out += "JDA Version: " + JDAInfo.VERSION + '\n';
		out += "RAM (USAGE/MAX): " + ram + '\n';
		out += "CPU Usage: " + cpuUsage + "%\n";
		out += "API Responses: " + jda.getResponseTotal() + "\n\n";
		out += "--General--\n";
		out += "Guilds: " + guilds.size() + "\n";
		out += "Users: " + users.size() + "\n";
		out += "Text Channels: " + channels.size() + "\n";
		out += "Voice Channels: " + voiceChannels.size() + "\n";
		out += "Sent Messages: " + msgsSent + "\n";
		out += "Received Messages: " + msgsReceived  + "\n";
		out += "Executed Commands: " + cmds + "\n";
		out += "Running Polls: " + Poll.getRunningPolls().size() +"\n\n";
		out += "--Music--\n";
		out += "Connections: " + audioConnections + "\n";
		out += "Queue Size: " + queueSize + "\n";
		out += "Now Playing: " + nowPlaying;
		out += "```";
		return out;
	}
}

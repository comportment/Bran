package br.com.brjdevs.steven.bran.core.utils;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.poll.Poll;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.lang.management.ManagementFactory;
import java.util.List;

public class Session implements EventListener {
	
	private final Runtime instance = Runtime.getRuntime();
	public double cpuUsage;
	public long cmds;
	public long msgsReceived;
	public long msgsSent;
	public BotContainer container;
	
	public Session(BotContainer container) {
		this.cpuUsage = 0;
		this.cmds = 0;
		this.msgsReceived = 0;
		this.msgsSent = 0;
		this.container = container;
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
	
	public String toString(JDA jda) {
		List<Guild> guilds = container.getGuilds();
		List<TextChannel> channels = container.getTextChannels();
		List<VoiceChannel> voiceChannels = container.getVoiceChannels();
		List<User> users = container.getUsers();
		long audioConnections = guilds.stream().filter(g -> g.getAudioManager().isConnected()).count();
		long queueSize = container.playerManager.getMusicManagers().values().stream().filter(musicManager -> !musicManager.getTrackScheduler().getQueue().isEmpty()).map(musicManager -> musicManager.getTrackScheduler().getQueue().size()).mapToInt(Integer::intValue).sum();
		String ram = ((instance.totalMemory() - instance.freeMemory()) >> 20) + " MB/" + (instance.maxMemory() >> 20) + " MB";
		long nowPlaying = container.playerManager.getMusicManagers().values().stream().filter(musicManager -> musicManager.getPlayer().getPlayingTrack() != null && !musicManager.getTrackScheduler().isPaused()).count();
		long paused = container.playerManager.getMusicManagers().values().stream().filter(musicManager -> musicManager.getTrackScheduler().isPaused()).count();
		String check = "✅";
		if (audioConnections > nowPlaying + paused) {
			for (Guild guild : jda.getGuilds())
				if (guild.getAudioManager().isConnected() && !container.taskManager.getChannelLeaveTimer().has(guild.getId()) && AudioUtils.isAlone(guild.getAudioManager().getConnectedChannel()))
					check = "❌";
		}
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
		out += "Connections: " + audioConnections + " '" + check + "'\n";
		out += "Queue Size: " + queueSize + "\n";
		out += "Now Playing: " + nowPlaying + "\n";
		out += "Paused: " + paused;
		out += "```";
		return out;
	}
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof MessageReceivedEvent) {
			MessageReceivedEvent event = ((MessageReceivedEvent) e);
			container.getSession().readMessage(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()));
		}
	}
}

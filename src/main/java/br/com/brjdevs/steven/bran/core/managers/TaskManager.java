package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.TrackScheduler;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.Util;
import br.com.brjdevs.steven.bran.refactor.BotContainer;
import br.com.brjdevs.steven.bran.refactor.DiscordLog.Level;
import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static br.com.brjdevs.steven.bran.core.audio.utils.VoiceChannelListener.musicTimeout;

public class TaskManager {
	
	public BotContainer container;
	
	public TaskManager(BotContainer container) {
		this.container = container;
		startAsyncTasks();
	}
	
	public void startAsyncTask(Runnable run, int seconds) {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                run, 0, seconds, TimeUnit.SECONDS);
    }
	
	private void startAsyncTasks() {
	    final OperatingSystemMXBean os =
			    ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
	    startAsyncTask(
			    () -> container.getSession().cpuUsage = (Math.floor(os.getProcessCpuLoad() * 10000) / 100), 5);
		
		startAsyncTask(() -> {
		    try {
			    JsonObject copy = new JsonObject();
			    musicTimeout.entrySet().forEach(entry -> copy.add(entry.getKey(), entry.getValue()));
			    copy.entrySet().forEach(entry -> {
				    JDA jda = container.getShards()[entry.getValue().getAsJsonObject().get("shard").getAsInt()].getJDA();
				    if (jda == null) {
					    musicTimeout.remove(entry.getKey());
					    return;
				    }
				    Guild guild = jda.getGuildById(entry.getKey());
				    if (guild == null) {
					    musicTimeout.remove(entry.getKey());
					    return;
				    }
				    JsonObject info = entry.getValue().getAsJsonObject();
				    info.addProperty("timeout", info.get("timeout").getAsInt() - 1);
				    musicTimeout.remove(guild.getId());
				    if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect() || guild.getSelfMember().getVoiceState().isMuted())
					    return;
				    VoiceChannel channel = guild.getVoiceChannelById(info.get("channelId").getAsString());
				    if (channel == null || !AudioUtils.isAlone(channel)) return;
				    if (info.get("timeout").getAsInt() == 0) {
					    MusicManager player = container.musicPlayerManager.get(guild);
					    TrackScheduler scheduler = player.getTrackScheduler();
					    TrackContext track = scheduler.getCurrentTrack();
					    if (track == null) track = scheduler.getPreviousTrack();
					    scheduler.getQueue().clear();
					    scheduler.play(player.getTrackScheduler().provideNextTrack(true), false);
					    if (track.getContext(jda) != null && track.getContext(jda).canTalk())
						    track.getContext(jda).sendMessage("Nobody joined in 2 minutes, so I cleaned the queue and stopped the player.").queue();
					    player.getTrackScheduler().setPaused(false);
					    if (guild.getAudioManager().isConnected())
						    guild.getAudioManager().closeAudioConnection();
					    return;
				    }
				    musicTimeout.add(guild.getId(), info);
			    });
		    } catch (Exception e) {
			    e.printStackTrace();
			    String url = Hastebin.post(Util.getStackTrace(e));
			    container.getDiscordLog().logToDiscord("Uncaught exception in Thread " + Thread.currentThread().getName(), "An unexpected `" + e.getClass().getSimpleName() + "` occurred.\nMessage: " + e.getMessage() + "\nStackTrace: " + url, Level.FATAL);
		    }
		}, 1);
	}
}

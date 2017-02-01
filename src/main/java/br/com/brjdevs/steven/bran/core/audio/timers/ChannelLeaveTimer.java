package br.com.brjdevs.steven.bran.core.audio.timers;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.TrackScheduler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelLeaveTimer {
	
	public final BotContainer container;
	private final Map<String, ImmutablePair<Long, String>> TIMING_OUT;
	private boolean timingOutUpdated = false;
	
	public ChannelLeaveTimer(BotContainer container) {
		this(new ConcurrentHashMap<>(), container);
	}
	
	public ChannelLeaveTimer(Map<String, ImmutablePair<Long, String>> timingOut, BotContainer container) {
		this.TIMING_OUT = Collections.synchronizedMap(timingOut);
		this.container = container;
		
		Thread thread = new Thread(this::threadcode, "ChannelLeaveTimeout");
		thread.setDaemon(true);
		thread.start();
	}
	
	public ImmutablePair<Long, String> get(String key) {
		return TIMING_OUT.get(key);
	}
	
	public void addMusicPlayer(String id, Long milis, String voiceChannel) {
		TIMING_OUT.put(id, new ImmutablePair<>(milis, voiceChannel));
		timingOutUpdated = true;
		synchronized (this) {
			notify();
		}
	}
	
	public boolean has(String key) {
		return TIMING_OUT.containsKey(key);
	}
	
	public boolean removeMusicPlayer(String id) {
		if (TIMING_OUT.containsKey(id)) {
			TIMING_OUT.remove(id);
			timingOutUpdated = true;
			synchronized (this) {
				notify();
			}
			return true;
		}
		return false;
	}
	
	private void threadcode() {
		//noinspection InfiniteLoopStatement
		while (true) {
			if (TIMING_OUT.isEmpty()) {
				try {
					synchronized (this) {
						wait();
						timingOutUpdated = false;
					}
				} catch (InterruptedException ignored) {
				}
			}
			
			//noinspection OptionalGetWithoutIsPresent
			Entry<String, ImmutablePair<Long, String>> closestEntry = TIMING_OUT.entrySet().stream().sorted(Comparator.comparingLong(entry -> entry.getValue().left)).findFirst().get();
			
			try {
				long timeout = closestEntry.getValue().left - System.currentTimeMillis();
				if (timeout > 0) {
					synchronized (this) {
						wait(timeout);
					}
				}
			} catch (InterruptedException ignored) {
			}
			
			if (!timingOutUpdated) {
				String id = closestEntry.getKey();
				TIMING_OUT.remove(id);
				JDA jda = container.getShards()[container.calcShardId(Long.parseLong(id))].getJDA();
				Guild guild = jda.getGuildById(id);
				if (guild == null) return;
				MusicManager musicManager = container.playerManager.get(guild);
				TrackScheduler scheduler = musicManager.getTrackScheduler();
				TrackContext track = scheduler.getCurrentTrack();
				if (track == null) track = scheduler.getPreviousTrack();
				scheduler.getQueue().clear();
				scheduler.play(musicManager.getTrackScheduler().provideNextTrack(true), false);
				if (track.getContext(jda) != null && track.getContext(jda).canTalk())
					track.getContext(jda).sendMessage("Nobody joined in 2 minutes, so I cleaned the queue and stopped the player.").queue();
				musicManager.getTrackScheduler().setPaused(false);
				if (guild.getAudioManager().isConnected())
					guild.getAudioManager().closeAudioConnection();
			} else timingOutUpdated = false; //and the loop will restart and resolve it
		}
	}
}

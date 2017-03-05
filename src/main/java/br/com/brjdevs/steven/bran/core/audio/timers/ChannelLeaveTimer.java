package br.com.brjdevs.steven.bran.core.audio.timers;

import br.com.brjdevs.steven.bran.core.audio.GuildMusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.TrackScheduler;
import br.com.brjdevs.steven.bran.core.client.Bran;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelLeaveTimer {
	
	private final Map<String, Pair<Long, String>> TIMING_OUT;
	private boolean timingOutUpdated = false;
	
	public ChannelLeaveTimer() {
		this(new ConcurrentHashMap<>());
	}
	
	public ChannelLeaveTimer(Map<String, Pair<Long, String>> timingOut) {
		this.TIMING_OUT = Collections.synchronizedMap(timingOut);
		
		Thread thread = new Thread(this::threadcode, "ChannelLeaveTimeout");
		thread.setDaemon(true);
		thread.start();
	}
	
	public Pair<Long, String> get(String key) {
		return TIMING_OUT.get(key);
	}
	
	public void addMusicPlayer(String id, Long milis, String voiceChannel) {
		TIMING_OUT.put(id, Pair.of(milis, voiceChannel));
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
			Entry<String, Pair<Long, String>> closestEntry = TIMING_OUT.entrySet().stream().sorted(Comparator.comparingLong(entry -> entry.getValue().getLeft())).findFirst().get();
			
			try {
				long timeout = closestEntry.getValue().getLeft() - System.currentTimeMillis();
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
				JDA jda = Bran.getInstance().getShards()[Bran.getInstance().calcShardId(Long.parseLong(id))].getJDA();
				Guild guild = jda.getGuildById(id);
				if (guild != null) {
					GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(guild);
					TrackScheduler scheduler = musicManager.getTrackScheduler();
					if (scheduler.getAudioPlayer().isPaused()) {
						TrackContext track = scheduler.getCurrentTrack() == null ? scheduler.getPreviousTrack()
								: scheduler.getCurrentTrack();
						if (track != null && track.getContext() != null && track.getContext().canTalk())
							track.getContext().sendMessage("Nobody joined the channel, stopping the player...").queue();
						scheduler.stop();
						musicManager.getTrackScheduler().setPaused(false);
					}
				}
			} else timingOutUpdated = false; //and the loop will restart and resolve it
		}
	}
}

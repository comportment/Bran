package br.com.brjdevs.steven.bran.core.audio.timers;

import br.com.brjdevs.steven.bran.core.client.Client;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class MusicRegisterTimeout {
	
	public final Client client;
	private final Map<String, Long> TIMING_OUT;
	private boolean timingOutUpdated = false;
	
	public MusicRegisterTimeout(Client client) {
		this(new ConcurrentHashMap<>(), client);
	}
	
	public MusicRegisterTimeout(Map<String, Long> timingOut, Client client) {
		this.TIMING_OUT = Collections.synchronizedMap(timingOut);
		this.client = client;
		
		Thread thread = new Thread(this::threadcode, "MusicRegisterTimeout");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void addMusicPlayer(String id, Long milis) {
		TIMING_OUT.put(id, milis);
		timingOutUpdated = true;
		synchronized (this) {
			notify();
		}
	}
	
	public void removeMusicPlayer(String id) {
		if (TIMING_OUT.containsKey(id)) {
			TIMING_OUT.remove(id);
			timingOutUpdated = true;
			synchronized (this) {
				notify();
			}
		}
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
			Entry<String, Long> closestEntry = TIMING_OUT.entrySet().stream().sorted(Comparator.comparingLong(Entry::getValue)).findFirst().get();
			
			try {
				long timeout = closestEntry.getValue() - System.currentTimeMillis();
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
				client.getMusicManager().unregister(Long.parseLong(id));
			} else timingOutUpdated = false; //and the loop will restart and resolve it
		}
	}
}

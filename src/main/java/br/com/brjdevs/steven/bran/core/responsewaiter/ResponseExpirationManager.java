package br.com.brjdevs.steven.bran.core.responsewaiter;

import br.com.brjdevs.steven.bran.core.responsewaiter.events.ResponseTimeoutEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseExpirationManager {
	
	private final Map<ResponseWaiter, Long> TIMING_OUT;
	private boolean timingOutUpdated = false;
	
	public ResponseExpirationManager() {
		this(new ConcurrentHashMap<>());
	}
	
	public ResponseExpirationManager(Map<ResponseWaiter, Long> timingOut) {
		this.TIMING_OUT = Collections.synchronizedMap(timingOut);
		
		Thread thread = new Thread(this::threadcode, "MusicRegisterTimeout");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void addResponseWaiter(ResponseWaiter responseWaiter, Long milis) {
		TIMING_OUT.put(responseWaiter, milis);
		timingOutUpdated = true;
		synchronized (this) {
			notify();
		}
	}
	
	public void removeResponseWaiter(ResponseWaiter responseWaiter) {
		if (TIMING_OUT.containsKey(responseWaiter)) {
			TIMING_OUT.remove(responseWaiter);
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
			Entry<ResponseWaiter, Long> closestEntry = TIMING_OUT.entrySet().stream().sorted(Comparator.comparingLong(Entry::getValue)).findFirst().get();
			
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
				ResponseWaiter responseWaiter = closestEntry.getKey();
				TIMING_OUT.remove(responseWaiter);
				ResponseWaiter.responseWaiters.remove(responseWaiter.getUserId());
				closestEntry.getKey().getResponseListener().onEvent(new ResponseTimeoutEvent(responseWaiter));
			} else timingOutUpdated = false; //and the loop will restart and resolve it
		}
	}
	
}

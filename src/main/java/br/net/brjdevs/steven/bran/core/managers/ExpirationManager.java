package br.net.brjdevs.steven.bran.core.managers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpirationManager {
	
	private final Map<Long, List<Runnable>> EXPIRATIONS;
	private boolean updated = false;
	
	public ExpirationManager() {
		EXPIRATIONS = new ConcurrentHashMap<>();
		
		Thread thread = new Thread(this::threadcode, "expiration Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void letExpire(long millis, Runnable onExpire) {
		Objects.requireNonNull(onExpire);
		EXPIRATIONS.computeIfAbsent(millis, k -> new ArrayList<>()).add(onExpire);
		updated = true;
		synchronized (this) {
			notify();
		}
	}
	
	private void threadcode() {
		//noinspection InfiniteLoopStatement
		while (true) {
			if (EXPIRATIONS.isEmpty()) {
				try {
					synchronized (this) {
						wait();
						updated = false;
					}
				} catch (InterruptedException ignored) {
				}
			}
			
			//noinspection OptionalGetWithoutIsPresent
			Map.Entry<Long, List<Runnable>> firstEntry = EXPIRATIONS.entrySet().stream().sorted(Comparator.comparingLong(Map.Entry::getKey)).findFirst().get();
			
			long timeout = firstEntry.getKey() - System.currentTimeMillis();
			if (timeout > 0) {
				synchronized (this) {
					try {
						wait(timeout);
					} catch (InterruptedException ignored) {
					}
				}
			}
			
			if (!updated) {
				EXPIRATIONS.remove(firstEntry.getKey());
				List<Runnable> runnables = firstEntry.getValue();
				runnables.remove(null);
				runnables.forEach(r -> ThreadPoolHelper.getDefaultThreadPool().startThread("Expiration Executable", r));
			} else updated = false; //and the loop will restart and resolve it
		}
	}
}

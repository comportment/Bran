package br.net.brjdevs.steven.bran.core.managers;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CustomExpirationManager<T> {
	
	private final Map<T, Long> EXPIRATIONS;
	private boolean updated = false;
	private Consumer<T> consumer;
	
	public CustomExpirationManager(Consumer<T> consumer) {
		this.consumer = consumer;
		EXPIRATIONS = new ConcurrentHashMap<>();
		
		Thread thread = new Thread(this::threadCode, "CustomExpirationManager Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public boolean isExpiring(T key) {
		return EXPIRATIONS.containsKey(key);
	}
	
	public void letExpire(T key, long milis) {
		EXPIRATIONS.put(key, milis);
		updated = true;
		synchronized (this) {
			notify();
		}
	}
	
	public boolean remove(T key) {
		if (EXPIRATIONS.containsKey(key)) {
			EXPIRATIONS.remove(key);
			updated = true;
			synchronized (this) {
				notify();
			}
			return true;
		}
		return false;
	}
	
	private void threadCode() {
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
			Map.Entry<T, Long> firstEntry = EXPIRATIONS.entrySet().stream().sorted(Comparator.comparingLong(Map.Entry::getValue)).findFirst().get();
			
			long timeout = firstEntry.getValue() - System.currentTimeMillis();
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
				consumer.accept(firstEntry.getKey());
			} else updated = false; //and the loop will restart and resolve it
		}
	}
}

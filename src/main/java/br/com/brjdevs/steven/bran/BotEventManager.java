package br.com.brjdevs.steven.bran;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotEventManager implements IEventManager {
	
	private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	public ExecutorService executor;
	private Bot shard;
	
	public BotEventManager(Bot shard) {
		this.shard = shard;
		this.executor = Executors.newCachedThreadPool();
		register(new Reflections("br.com.brjdevs.steven.bran")
				.getSubTypesOf(EventListener.class).stream()
				.map(clazz -> {
					try {
						return clazz.getConstructor(BotContainer.class).newInstance(shard.container);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).filter(Objects::nonNull).toArray(EventListener[]::new));
	}
	
	private void register(EventListener... o) {
		this.listeners.addAll(Arrays.asList(o));
	}
	
	@Override
	public void register(Object o) {
		if (!(o instanceof EventListener)) {
			throw new IllegalArgumentException("Listener must implement EventListener");
		}
		this.listeners.add((EventListener) o);
	}
	
	@Override
	public void unregister(Object o) {
		if (o instanceof EventListener) {
			listeners.remove(o);
		}
	}
	
	@Override
	public void handle(Event event) {
		if (executor.isShutdown()) {
			shard.container.getDiscordLog().logToDiscord(new Exception("Event Manager Executor for Shard " + shard.getId() + " has already been shutdown!"), event.getClass().getSimpleName());
			return;
		}
		final List<Object> listeners = getRegisteredListeners();
		executor.submit(() -> listeners.forEach(listener -> {
			try {
				shard.container.setLastEvent(shard.getId(), System.currentTimeMillis());
				((EventListener) listener).onEvent(event);
			} catch (Exception e) {
				shard.container.getDiscordLog().logToDiscord(e, "Unexpected error at event `" + event.getClass().getSimpleName() + "`");
				e.printStackTrace();
			}
		}));
	}
	
	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(listeners);
	}
}

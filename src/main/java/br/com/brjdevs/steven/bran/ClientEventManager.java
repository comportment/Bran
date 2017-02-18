package br.com.brjdevs.steven.bran;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

public class ClientEventManager implements IEventManager {
	
	private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	public ExecutorService executor;
	private ClientShard shard;
	
	public ClientEventManager(ClientShard shard) {
		this.shard = shard;
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Event Manager [" + shard.getId() + "]-%d").build());
		register(new Reflections("br.com.brjdevs.steven.bran")
				.getSubTypesOf(EventListener.class).stream()
				.map(clazz -> {
					try {
						return clazz.getConstructor(Client.class).newInstance(shard.client);
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
			shard.client.getDiscordLog().logToDiscord(new Exception("Event Manager Executor for Shard " + shard.getId() + " has already been shutdown!"), event.getClass().getSimpleName());
			return;
		}
		final List<Object> listeners = getRegisteredListeners();
		executor.submit(() -> listeners.forEach(listener -> {
			try {
				shard.client.setLastEvent(shard.getId(), System.currentTimeMillis());
				((EventListener) listener).onEvent(event);
			} catch (Exception e) {
				shard.client.getDiscordLog().logToDiscord(e, "Unexpected error at event `" + event.getClass().getSimpleName() + "`");
				e.printStackTrace();
			}
		}));
	}
	
	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(listeners);
	}
}

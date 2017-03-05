package br.com.brjdevs.steven.bran.core.client;

import br.com.brjdevs.steven.bran.core.listeners.EventListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BranEventManager implements IEventManager {
	
	private final CopyOnWriteArrayList<net.dv8tion.jda.core.hooks.EventListener> listeners = new CopyOnWriteArrayList<>();
	public ExecutorService executor;
	private BranShard shard;
	
	public BranEventManager(BranShard shard) {
		this.shard = shard;
		this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Event Manager [" + shard.getId() + "]-%d").build());
		register(new Reflections("br.com.brjdevs.steven.bran")
				.getSubTypesOf(EventListener.class).stream()
				.map(clazz -> {
					try {
						return clazz.getConstructor().newInstance(shard.getBran());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).filter(Objects::nonNull).toArray(net.dv8tion.jda.core.hooks.EventListener[]::new));
	}
	
	private void register(net.dv8tion.jda.core.hooks.EventListener... o) {
		this.listeners.addAll(Arrays.asList(o));
	}
	
	@Override
	public void register(Object o) {
		if (!(o instanceof net.dv8tion.jda.core.hooks.EventListener)) {
			throw new IllegalArgumentException("Listener must implement EventListener");
		}
		this.listeners.add((net.dv8tion.jda.core.hooks.EventListener) o);
	}
	
	@Override
	public void unregister(Object o) {
		if (o instanceof net.dv8tion.jda.core.hooks.EventListener) {
			listeners.remove(o);
		}
	}
	
	@Override
	public void handle(Event event) {
		if (executor.isShutdown()) {
			shard.getBran().getDiscordLog().logToDiscord(new Exception("Event Manager Executor for Shard " + shard.getId() + " has already been shutdown!"), event.getClass().getSimpleName());
			return;
		}
		final List<Object> listeners = getRegisteredListeners();
		executor.submit(() -> listeners.forEach(listener -> {
			try {
				((net.dv8tion.jda.core.hooks.EventListener) listener).onEvent(event);
				shard.getBran().setLastEvent(shard.getId(), System.currentTimeMillis());
			} catch (Exception e) {
				shard.getBran().getDiscordLog().logToDiscord(e, "Unexpected error at event `" + event.getClass().getSimpleName() + "`");
				e.printStackTrace();
			}
		}));
	}
	
	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(listeners);
	}
}

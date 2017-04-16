package br.net.brjdevs.steven.bran.core.client;

import br.net.brjdevs.steven.bran.core.listeners.EventListener;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EventManager implements IEventManager {

	private static final Logger LOGGER = LoggerFactory.getLogger("Event Manager");
	private static final List<Class<? extends EventListener>> listenerClasses = new ArrayList<>(new Reflections("br.net.brjdevs.steven.bran").getSubTypesOf(EventListener.class));

	private final CopyOnWriteArrayList<EventListener> listeners;
	public ExecutorService executor;
    private Shard shard;
    
    public EventManager(Shard shard) {
		this.shard = shard;
		this.executor = Executors.newCachedThreadPool((r) -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		});
        this.listeners = new CopyOnWriteArrayList<>(listenerClasses.stream().map(clazz -> {
        	try {
        		return clazz.newInstance();
        	} catch (Exception e) {
        		LOGGER.error("Failed to instantiate Listener!", e);
        		return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList()));
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
	@SuppressWarnings("unchecked")
	public void handle(Event event) {
		executor.submit(() ->
				listeners.forEach(listener -> {
			try {
				if (listener.getEvent().isInstance(event)) {
					listener.onEvent(event);
				}
                Bran.getInstance().setLastEvent(shard.getId(), System.currentTimeMillis());
            } catch (Exception e) {
                LOGGER.error("Failed to process event", e);
			}
		})
		);
	}
	
	@Override
	public List<Object> getRegisteredListeners() {
        return new ArrayList<>(listeners);
    }
}

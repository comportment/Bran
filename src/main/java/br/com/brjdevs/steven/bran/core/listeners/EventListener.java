package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Client;
import net.dv8tion.jda.core.events.Event;

public abstract class EventListener<T extends Event> implements net.dv8tion.jda.core.hooks.EventListener {
	
	protected final Class<T> tClass;
	protected final Client client;
	
	protected EventListener(Class<T> tClass, Client client) {
		this.tClass = tClass;
		this.client = client;
	}
	
	public abstract void event(T event);
	
	@Override
	@SuppressWarnings("unchecked")
	public void onEvent(Event event) {
		if (tClass.isInstance(event)) event((T) event);
	}
}

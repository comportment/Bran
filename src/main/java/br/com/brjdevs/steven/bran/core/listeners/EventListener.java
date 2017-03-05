package br.com.brjdevs.steven.bran.core.listeners;

import net.dv8tion.jda.core.events.Event;

public abstract class EventListener<T extends Event> implements net.dv8tion.jda.core.hooks.EventListener {
	
	protected final Class<T> tClass;
	
	protected EventListener(Class<T> tClass) {
		this.tClass = tClass;
	}
	
	public abstract void event(T event);
	
	@Override
	@SuppressWarnings("unchecked")
	public void onEvent(Event event) {
		if (tClass.isInstance(event)) event((T) event);
	}
}

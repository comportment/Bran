package br.net.brjdevs.steven.bran.core.listeners;

import net.dv8tion.jda.core.events.Event;

public abstract class EventListener<T extends Event> {
	
	protected final Class<T> tClass;

	public Class<T> getEvent() {
		return tClass;
	}
	protected EventListener(Class<T> tClass) {
		this.tClass = tClass;
	}

	public abstract void onEvent(T event);
}

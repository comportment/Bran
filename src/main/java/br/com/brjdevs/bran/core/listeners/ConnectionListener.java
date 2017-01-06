package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.utils.DiscordLog;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class ConnectionListener implements EventListener {
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof ReconnectedEvent) {
			DiscordLog.log((ReconnectedEvent) event);
		} else if (event instanceof DisconnectEvent) {
			DiscordLog.log((DisconnectEvent) event);
		}
	}
}

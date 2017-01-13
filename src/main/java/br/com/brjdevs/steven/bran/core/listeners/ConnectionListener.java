package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.utils.DiscordLog;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ConnectionListener implements EventListener {
	
	private static SimpleLog LOG;
	
	static {
		LOG = SimpleLog.getLog("Connection Listener");
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof ReconnectedEvent) {
			DiscordLog.log((ReconnectedEvent) event);
		} else if (event instanceof DisconnectEvent) {
			DiscordLog.log((DisconnectEvent) event);
		} else if (event instanceof ShutdownEvent) {
			LOG.info("ShutdownEvent on Shard " + Bot.getInstance().getShardId(event.getJDA()));
		}
	}
}
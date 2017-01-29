package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.refactor.BotContainer;
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
	
	public BotContainer container;
	
	public ConnectionListener(BotContainer container) {
		this.container = container;
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof ReconnectedEvent) {
			LOG.info("ReconnectedEvent on Shard " + container.getShardId(event.getJDA()));
		} else if (event instanceof DisconnectEvent) {
			LOG.info("DisconnectEvent on Shard " + container.getShardId(event.getJDA()));
		} else if (event instanceof ShutdownEvent) {
			LOG.info("ShutdownEvent on Shard " + container.getShardId(event.getJDA()));
		}
	}
}
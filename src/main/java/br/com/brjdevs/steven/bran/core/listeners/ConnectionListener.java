package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import net.dv8tion.jda.core.events.*;
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
			container.getDiscordLog().logToDiscord("Connection Listener", "ReconnectedEvent on Shard " + container.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof DisconnectEvent) {
			LOG.info("DisconnectEvent on Shard " + container.getShardId(event.getJDA()));
			container.getDiscordLog().logToDiscord("Connection Listener", "DisconnectedEvent on Shard " + container.getShardId(event.getJDA()), Level.WARN);
		} else if (event instanceof ShutdownEvent) {
			LOG.info("ShutdownEvent on Shard " + container.getShardId(event.getJDA()));
			container.getDiscordLog().logToDiscord("Connection Listener", "ShutdownEvent on Shard " + container.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof ResumedEvent) {
			LOG.info("ResumedEvent on Shard " + container.getShardId(event.getJDA()));
			container.getDiscordLog().logToDiscord("Connection Listener", "ResumedEvent on Shard " + container.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof StatusChangeEvent) {
			LOG.info("Status Changed from `" + ((StatusChangeEvent) event).getOldStatus() + "` to `" + ((StatusChangeEvent) event).getStatus() + "` on Shard " + container.getShardId(event.getJDA()));
			container.getDiscordLog().logToDiscord("Connection Listener", "Status Changed from `" + ((StatusChangeEvent) event).getOldStatus() + "` to `" + ((StatusChangeEvent) event).getStatus() + "` on Shard " + container.getShardId(event.getJDA()), Level.INFO);
		}
	}
}
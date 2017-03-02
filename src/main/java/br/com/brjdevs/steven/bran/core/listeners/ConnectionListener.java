package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.client.DiscordLog.Level;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ConnectionListener extends EventListener<Event> {
	
	private static SimpleLog LOG;
	
	static {
		LOG = SimpleLog.getLog("Connection Listener");
	}
	
	
	public ConnectionListener(Client client) {
		super(Event.class, client);
	}
	
	public void event(Event event) {
		if (event instanceof ReconnectedEvent) {
			LOG.info("ReconnectedEvent on Shard " + client.getShardId(event.getJDA()));
			client.getDiscordLog().logToDiscord("Connection Listener", "ReconnectedEvent on Shard " + client.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof DisconnectEvent) {
			LOG.info("DisconnectEvent on Shard " + client.getShardId(event.getJDA()));
			client.getDiscordLog().logToDiscord("Connection Listener", "DisconnectedEvent on Shard " + client.getShardId(event.getJDA()), Level.WARN);
		} else if (event instanceof ShutdownEvent) {
			LOG.info("ShutdownEvent on Shard " + client.getShardId(event.getJDA()));
			client.getDiscordLog().logToDiscord("Connection Listener", "ShutdownEvent on Shard " + client.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof ResumedEvent) {
			LOG.info("ResumedEvent on Shard " + client.getShardId(event.getJDA()));
			client.getDiscordLog().logToDiscord("Connection Listener", "ResumedEvent on Shard " + client.getShardId(event.getJDA()), Level.INFO);
		} else if (event instanceof StatusChangeEvent) {
			LOG.info("Status Changed from `" + ((StatusChangeEvent) event).getOldStatus() + "` to `" + ((StatusChangeEvent) event).getStatus() + "` on Shard " + client.getShardId(event.getJDA()));
			client.getDiscordLog().logToDiscord("Connection Listener", "Status Changed from `" + ((StatusChangeEvent) event).getOldStatus() + "` to `" + ((StatusChangeEvent) event).getStatus() + "` on Shard " + client.getShardId(event.getJDA()), Level.INFO);
		}
	}
}
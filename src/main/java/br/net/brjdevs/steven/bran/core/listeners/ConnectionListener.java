package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import net.dv8tion.jda.core.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListener extends EventListener<StatusChangeEvent> {
	
	private static Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger("Connection Listener");
	}
	
	public ConnectionListener() {
		super(StatusChangeEvent.class);
	}
	
	public void onEvent(StatusChangeEvent event) {
		LOGGER.info("Status changed from '" + event.getOldStatus() + "' to " + event.getStatus() + "' on shard " + Bran.getInstance().getShardId(event.getJDA()));

	}
}
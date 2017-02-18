package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ReadyListener implements EventListener {
	
	private static final SimpleLog LOG = SimpleLog.getLog("Ready Listener");
	
	public Client client;
	
	public ReadyListener(Client client) {
		this.client = client;
	}
	
	@Override
	public void onEvent(Event event) {
		if (!(event instanceof ReadyEvent)) return;
		if (event.getJDA().getShardInfo() != null)
			LOG.info("Got Ready Event on Shard " + event.getJDA().getShardInfo().getShardId());
		else
			LOG.info("Got Ready Event.");
		event.getJDA().removeEventListener(this);
	}
}

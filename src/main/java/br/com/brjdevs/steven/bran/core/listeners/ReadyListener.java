package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ReadyListener extends OptimizedListener<ReadyEvent> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("Ready Listener");
	
	public Client client;
	
	public ReadyListener(Client client) {
		super(ReadyEvent.class);
		this.client = client;
	}
	
	@Override
	public void event(ReadyEvent event) {
		if (event.getJDA().getShardInfo() != null)
			LOG.info("Got Ready Event on Shard " + event.getJDA().getShardInfo().getShardId());
		else
			LOG.info("Got Ready Event.");
		event.getJDA().removeEventListener(this);
	}
}

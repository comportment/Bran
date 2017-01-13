package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Bot;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class ReadyListener implements EventListener {
	
	@Override
	public void onEvent(Event event) {
		if (!(event instanceof ReadyEvent)) return;
		if (event.getJDA().getShardInfo() != null)
			Bot.LOG.info("Got Ready Event on Shard " + event.getJDA().getShardInfo().getShardId());
		else
			Bot.LOG.info("Got Ready Event.");
		event.getJDA().removeEventListener(this);
	}
}

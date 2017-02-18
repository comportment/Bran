package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuildListener implements EventListener {
	
	public Client client;
	
	public GuildListener(Client client) {
		this.client = client;
	}
	
	@Override
	public void onEvent(Event event) {
		
		if (event instanceof GenericGuildEvent) {
			if (event instanceof GuildJoinEvent) {
				client.getDiscordLog().logToDiscord((GuildJoinEvent) event);
			} else if (event instanceof GuildLeaveEvent) {
				client.getDiscordLog().logToDiscord((GuildLeaveEvent) event);
			}
		}
	}
}

package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.BotContainer;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuildListener implements EventListener {
	
	public BotContainer container;
	
	public GuildListener(BotContainer container) {
		this.container = container;
	}
	
	@Override
	public void onEvent(Event event) {
		
		if (event instanceof GenericGuildEvent) {
			if (event instanceof GuildJoinEvent) {
				container.getDiscordLog().logToDiscord((GuildJoinEvent) event);
			} else if (event instanceof GuildLeaveEvent) {
				container.getDiscordLog().logToDiscord((GuildLeaveEvent) event);
			}
		}
	}
}

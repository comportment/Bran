package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Client;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

public class GuildListener extends EventListener<GenericGuildEvent> {
	
	public GuildListener(Client client) {
		super(GenericGuildEvent.class, client);
	}
	
	@Override
	public void event(GenericGuildEvent event) {
		if (event instanceof GuildJoinEvent) {
			client.getDiscordLog().logToDiscord((GuildJoinEvent) event);
		} else if (event instanceof GuildLeaveEvent) {
			client.getDiscordBotData().getDataHolderManager().get().guilds.remove(Long.parseLong(event.getGuild().getId()));
			client.getDiscordLog().logToDiscord((GuildLeaveEvent) event);
		}
	}
}

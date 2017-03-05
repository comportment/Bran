package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.managers.GuildStatsManager;
import br.com.brjdevs.steven.bran.core.managers.GuildStatsManager.LoggedEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

public class GuildListener extends EventListener<GenericGuildEvent> {
	
	public GuildListener() {
		super(GenericGuildEvent.class);
	}
	
	@Override
	public void event(GenericGuildEvent event) {
		if (event instanceof GuildJoinEvent) {
			Bran.getInstance().getDiscordLog().logToDiscord((GuildJoinEvent) event);
			GuildStatsManager.log(LoggedEvent.JOIN);
		} else if (event instanceof GuildLeaveEvent) {
			Bran.getInstance().getDiscordLog().logToDiscord((GuildLeaveEvent) event);
			GuildStatsManager.log(LoggedEvent.LEAVE);
		}
	}
}

package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.managers.GuildStatsManager;
import br.net.brjdevs.steven.bran.core.managers.GuildStatsManager.LoggedEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildListener extends EventListener<GenericGuildEvent> {

	private static Logger LOGGER = LoggerFactory.getLogger("Guild Log");

	public GuildListener() {
		super(GenericGuildEvent.class);
	}
	
	@Override
	public void onEvent(GenericGuildEvent event) {
		if (event instanceof GuildJoinEvent) {
			LOGGER.info("Joined guild '" + event.getGuild().getName() + "' (Members: " + event.getGuild().getMembers().size() + ")");
            GuildStatsManager.log(LoggedEvent.JOIN, event.getGuild());
        } else if (event instanceof GuildLeaveEvent) {
			LOGGER.info("Left guild '" + event.getGuild().getName() + "' (Members: " + event.getGuild().getMembers().size() + ")");
            GuildStatsManager.log(LoggedEvent.LEAVE, event.getGuild());
        }
	}
}

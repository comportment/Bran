package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuildListener implements EventListener {
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof GuildLeaveEvent) {
			GuildLeaveEvent event = (GuildLeaveEvent) e;
			AudioUtils.getManager().unregister(Long.parseLong(event.getGuild().getId()));
		}
	}
}

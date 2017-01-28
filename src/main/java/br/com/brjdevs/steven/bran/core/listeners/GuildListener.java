package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.utils.RequirementsUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuildListener implements EventListener {
	
	@Override
	public void onEvent(Event event) {
		
		if (event instanceof GenericGuildEvent) {
			Guild guild = ((GenericGuildEvent) event).getGuild();
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setDescription("**Name:** " + guild.getName() + "\n**ID:** " + guild.getId() + "\n**Region:** " + guild.getRegion().toString() + "\n**Members:** " + guild.getMembers().size() + "  (" + RequirementsUtils.getBotsPercentage(guild) + "% bots)\n**Owner:** " + Util.getUser(guild.getOwner().getUser()) + " (ID: " + guild.getOwner().getUser().getId() + ")");
			if (event instanceof GuildJoinEvent) {
				embedBuilder.setTitle("\uD83C\uDFE0 Joined Guild");
			} else if (event instanceof GuildLeaveEvent) {
				embedBuilder.setTitle("\uD83C\uDFDA Left Guild");
			} else return;
			Bot.LOGCHANNEL.sendMessage(embedBuilder.build()).queue();
		}
	}
}

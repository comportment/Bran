package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.data.guild.configs.Announces;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class AnnouncesListener implements EventListener {
	public void onEvent(Event e) {
		if (!(e instanceof GenericGuildMemberEvent)) return;
		GenericGuildMemberEvent event = (GenericGuildMemberEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		if (discordGuild.getAnnounces().getChannel(event.getJDA()) == null) return;
		Announces announces = discordGuild.getAnnounces();
		Member member = event.getMember();
		if (event instanceof GuildMemberJoinEvent) {
			if (!Util.isEmpty(announces.getJoinAnnounce()))
				announces.getChannel(event.getJDA()).sendMessage(Announces.parse(announces.getJoinAnnounce(), member)).queue();
			if (!Util.isEmpty(announces.getJoinDMAnnounce()))
				event.getMember().getUser().openPrivateChannel().queue(chan -> chan.sendMessage(Announces.parse(announces.getJoinDMAnnounce(), member)).queue());
		} else if (event instanceof GuildMemberLeaveEvent) {
			if (!Util.isEmpty(announces.getLeaveAnnounce()))
				announces.getChannel(event.getJDA()).sendMessage(Announces.parse(announces.getLeaveAnnounce(), member)).queue();
		}
	}
}

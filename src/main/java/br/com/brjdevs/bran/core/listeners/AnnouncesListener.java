package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.Announces;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import static br.com.brjdevs.bran.core.utils.Util.isEmpty;
import static br.com.brjdevs.bran.core.data.guild.configs.Announces.parse;

public class AnnouncesListener implements EventListener {
	public void onEvent(Event e) {
		if (!(e instanceof GenericGuildMemberEvent)) return;
		GenericGuildMemberEvent event = (GenericGuildMemberEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		if (discordGuild.getAnnounces().getChannel(event.getJDA()) == null) return;
		Announces announces = discordGuild.getAnnounces();
		Member member = event.getMember();
		if (event instanceof GuildMemberJoinEvent) {
			if (!isEmpty(announces.getJoinAnnounce()))
				announces.getChannel(event.getJDA()).sendMessage(parse(announces.getJoinAnnounce(), member)).queue();
			if (!isEmpty(announces.getJoinDMAnnounce()))
				event.getMember().getUser().openPrivateChannel().queue(chan -> chan.sendMessage(parse(announces.getJoinDMAnnounce(), member)).queue());
		} else if (event instanceof GuildMemberLeaveEvent) {
			if (!isEmpty(announces.getLeaveAnnounce()))
				announces.getChannel(event.getJDA()).sendMessage(parse(announces.getLeaveAnnounce(), member)).queue();
		}
	}
}

package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuessListener implements EventListener {
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		Profile profile = discordGuild.getMember(event.getAuthor()).getProfile();
		HangManGame session = HangManGame.getSession(profile);
		if (session == null) return;
		String msg = event.getMessage().getRawContent();
		TextChannel channel = event.getChannel();
		if ("giveup".equals(msg)) {
			if (session.getCreator() != profile)
				session.getListener().onLeaveGame(profile);
			else if (session.getCreator() == profile && !session.getInvitedUsers().isEmpty())
				channel.sendMessage("You can't give up this session because there are other players playing with you, you can just let them finish if you don't want to continue or use `" + Bot.getInstance().getDefaultPrefixes()[0] + "hm pass [MENTION]` to set a new owner to the session.").queue();
			else
				session.getListener().onLoose(true);
			return;
		}
		if (session.getChannel() != event.getChannel()) return;
		if ("info".equals(msg)) {
			channel.sendMessage(session.createEmbed().setDescription("Information on the current Game.").build()).queue();
			return;
		}
		if (!msg.matches("^([A-Za-z]{1})$")) return;
		session.guess(msg);
	}
}

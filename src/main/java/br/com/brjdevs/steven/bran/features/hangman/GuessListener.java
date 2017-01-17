package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.features.hangman.events.LeaveGameEvent;
import br.com.brjdevs.steven.bran.features.hangman.events.LooseEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuessListener implements EventListener {
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		Profile profile = discordGuild.getMember(event.getAuthor()).getProfile();
		HangManGame game = HangManGame.getSession(profile);
		if (game == null) return;
		String msg = event.getMessage().getRawContent();
		TextChannel channel = event.getChannel();
		if ("giveup".equals(msg)) {
			if (game.getCreator() != profile)
				game.getListener().onEvent(new LeaveGameEvent(game, event.getJDA(), profile));
			else if (game.getCreator() == profile && !game.getInvitedUsers().isEmpty())
				channel.sendMessage("You can't give up this session because there are other players playing with you, you can just let them finish if you don't want to continue or use `" + Bot.getDefaultPrefixes()[0] + "hm setCreator [MENTION]` to set a new owner to the session.").queue();
			else
				game.getListener().onEvent(new LooseEvent(game, event.getJDA(), true));
			return;
		}
		if (game.getChannel() != event.getChannel()) return;
		if ("info".equals(msg)) {
			channel.sendMessage(game.createEmbed().setDescription("Information on the current Game.").build()).queue();
			return;
		}
		if (!msg.matches("^([A-Za-z]{1})$")) return;
		game.guess(msg, profile);
		if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().deleteMessage().queue();
		try {
			game.getChannel().deleteMessageById(game.getLastMessage())
					.queue(success -> game.setLastMessage(null),
							throwable -> game.setLastMessage(null));
		} catch (ErrorResponseException ignored) {
			game.setLastMessage(null);
		}
	}
}

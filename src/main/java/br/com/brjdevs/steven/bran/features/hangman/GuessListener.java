package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.features.hangman.events.LeaveGameEvent;
import br.com.brjdevs.steven.bran.features.hangman.events.LooseEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class GuessListener implements EventListener {
	
	public Client client;
	
	public GuessListener(Client client) {
		this.client = client;
	}
	
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		if (event.getAuthor().isFake() || event.getAuthor().isBot()) return;
		Profile profile = client.getProfile(event.getAuthor());
		HangManGame game = HangManGame.getSession(profile);
		if (game == null) return;
		String msg = event.getMessage().getRawContent();
		TextChannel channel = event.getChannel();
		if ("giveup".equals(msg)) {
			if (game.getCreator() != profile)
				game.getListener().onEvent(new LeaveGameEvent(game, event.getJDA(), profile));
			else if (game.getCreator() == profile && !game.getInvitedUsers().isEmpty())
				channel.sendMessage("You can't give up this session because there are other players playing with you, you can just let them finish if you don't want to continue or use `" + client.config.getDefaultPrefixes().get(0) + "hm setCreator [MENTION]` to set a new owner to the session.").queue();
			else
				game.getListener().onEvent(new LooseEvent(game, event.getJDA(), true));
			return;
		}
		if (game.getChannel(client) != event.getChannel()) return;
		if ("info".equals(msg)) {
			channel.sendMessage(game.createEmbed(client).setDescription("Information on the current Game.").build()).queue();
			return;
		}
		if (!msg.matches("^([A-Za-z]{1})$")) return;
		game.guess(msg, profile, client);
		if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().delete().queue();
	}
}

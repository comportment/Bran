package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.listeners.EventListener;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GuessListener extends EventListener<MessageReceivedEvent> {
	
	public GuessListener(Client client) {
		super(MessageReceivedEvent.class, client);
	}
	
	@Override
	public void event(MessageReceivedEvent event) {
		HangManGame game = HangManGame.getGame(event.getAuthor());
		String guess = event.getMessage().getRawContent();
		if (guess.isEmpty()) return;
		if (guess.charAt(0) != '=')
			return;
		if (guess.equals("=giveup")) {
			if (game.isMuliplayer()) {
				event.getChannel().sendMessage("You cannot giveup to a Multiplayer session.").queue();
			} else {
				game.giveup();
			}
			return;
		} else if (guess.equals("=leave")) {
			if (!game.isMuliplayer()) {
				event.getChannel().sendMessage("You cannot leave a Singleplayer session.").queue();
			} else {
				game.leave(event.getAuthor());
			}
			return;
		}
		if (guess.length() != 2)
			return;
		game.guess(guess.charAt(1), client.getDiscordBotData().getDataHolderManager().get().getUser(event.getAuthor()));
	}
}

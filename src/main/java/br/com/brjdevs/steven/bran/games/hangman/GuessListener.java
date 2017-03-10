package br.com.brjdevs.steven.bran.games.hangman;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.listeners.EventListener;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GuessListener extends EventListener<MessageReceivedEvent> {
	
	public GuessListener() {
		super(MessageReceivedEvent.class);
	}
	
	@Override
	public void event(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isFake())
			return;
		HangManGame game = HangManGame.getGame(event.getAuthor());
		if (game == null) return;
		String guess = event.getMessage().getRawContent();
		if (guess.isEmpty()) return;
		if (guess.charAt(0) != '^')
			return;
		if (guess.matches("\\^(give ?up|end|stop)")) {
			if (game.isMuliplayer()) {
				event.getChannel().sendMessage("You cannot giveup to a Multiplayer session.").queue();
			} else {
				game.giveup();
			}
			return;
		} else if (guess.equals("^leave")) {
			if (!game.isMuliplayer()) {
				event.getChannel().sendMessage("You cannot leave a Singleplayer session.").queue();
			} else {
				game.leave(event.getAuthor());
			}
			return;
		}
		if (guess.length() != 2)
			return;
		game.guess(guess.toLowerCase().charAt(1), Bran.getInstance().getDataManager().getUserDataManager().get().getUser(event.getAuthor()));
	}
}

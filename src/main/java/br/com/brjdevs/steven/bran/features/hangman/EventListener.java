package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.features.hangman.events.*;

public class EventListener implements IEventListener {
	
	public Client client;
	
	public EventListener(Client client) {
		this.client = client;
	}
	
	@Override
	public void onEvent(HangManEvent hangManEvent) {
		try {
			hangManEvent.getGame().getLastMessage().get().delete().queue();
		} catch (Exception ignored) {
		}
		if (hangManEvent instanceof GuessEvent) {
			GuessEvent event = (GuessEvent) hangManEvent;
			if (event.isGuessRight()) {
				event.getGame().getChannel(client).sendMessage(event.getGame().createEmbed(client).setDescription("Oh yeah, the character '" + event.getGuess() + "' is in the word! Keep the good job, " + OtherUtils.getUser(event.getProfile().getUser(event.getJDA()))).build()).queue(event.getGame()::setLastMessage);
				event.getProfile().addExperience(1);
				event.getProfile().getBankAccount().addCoins(5, "HangMan Game", event.getProfile().getUser(event.getJDA()).getId(), "You got this coins for guessing a letter.");
			} else {
				event.getGame().getChannel(client).sendMessage(event.getGame().createEmbed(client).setDescription("Too bad, the character '" + event.getGuess() + "' is not in the word! Better luck next time, " + OtherUtils.getUser(event.getProfile().getUser(event.getJDA()))).build()).queue(event.getGame()::setLastMessage);
			}
		} else if (hangManEvent instanceof AlreadyGuessedEvent) {
			AlreadyGuessedEvent event = (AlreadyGuessedEvent) hangManEvent;
			String s = !event.isInWord() ? "I've already told you, %s this character is not in the word!" : "You've already guessed this character, %s!";
			event.getGame().getChannel(client).sendMessage(event.getGame().createEmbed(client).setDescription(String.format(s, OtherUtils.getUser(event.getProfile().getUser(event.getJDA())))).build()).queue(event.getGame()::setLastMessage);
		} else if (hangManEvent instanceof LooseEvent) {
			LooseEvent event = (LooseEvent) hangManEvent;
			event.getGame().getChannel(client).sendMessage(!event.isGiveup() ? event.getGame().createEmbed(client).setDescription("\uD83D\uDE15 Well, I think you did great but it wasn't this time \uD83D\uDE0A. The word was '" + event.getGame().getWord() + "'.").build() : event.getGame().createEmbed(client).setDescription("Aww man... I know you could've done it! The word was '" + event.getGame().getWord() + "'").build()).queue(event.getGame()::setLastMessage);
			event.getGame().end();
			event.getGame().getProfiles().forEach(p -> {
				p.getHMStats().addDefeat();
				p.addExperience(-3);
			});
		} else if (hangManEvent instanceof WinEvent) {
			WinEvent event = (WinEvent) hangManEvent;
			event.getGame().getChannel(client).sendMessage(event.getGame().createEmbed(client).setDescription("\uD83C\uDF89 You did it! You win!! The word is '" + event.getGame().getWord() + "'! \uD83C\uDF89").build()).queue(event.getGame()::setLastMessage);
			event.getGame().end();
			event.getGame().getProfiles().forEach(p -> {
				p.getHMStats().addVictory();
				p.addExperience(4);
				p.getBankAccount().addCoins(5, "HangMan Game", p.getUser(event.getJDA()).getId(), "You got this coins for winning a HangMan game.");
			});
		} else if (hangManEvent instanceof LeaveGameEvent) {
			LeaveGameEvent event = (LeaveGameEvent) hangManEvent;
			event.getGame().remove(event.getProfile());
			event.getProfile().unregisterListener(event.getGame().profileListener);
			event.getProfile().getHMStats().addDefeat();
			event.getGame().getChannel(client).sendMessage(event.getGame().createEmbed(client).setDescription("Ooh boy, why did you leave? You were doing well!").build()).queue(event.getGame()::setLastMessage);
		}
	}
}

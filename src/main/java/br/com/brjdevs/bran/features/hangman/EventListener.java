package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile.Action;
import br.com.brjdevs.bran.core.utils.Util;
import br.com.brjdevs.bran.features.hangman.events.*;
import net.dv8tion.jda.core.EmbedBuilder;

public class EventListener implements IEventListener {
	
	@Override
	public void onEvent(HangManEvent hangManEvent) {
		if (hangManEvent instanceof GuessEvent) {
			GuessEvent event = (GuessEvent) hangManEvent;
			if (event.isGuessRight()) {
				event.getGame().getChannel().sendMessage(event.getGame().createEmbed().setDescription("Oh yeah, the character '" + event.getGuess() + "' is in the word! Keep the good job, " + Util.getUser(event.getProfile().getUser(event.getJDA()))).build()).queue(event.getGame()::setLastMessage);
				event.getProfile().addCoins(2);
				Action action = event.getProfile().addExperience(1);
				if (action == Action.LEVEL_UP)
					event.getGame().getChannel().sendMessage(new EmbedBuilder(event.getProfile().createEmbed(event.getJDA())).setDescription("**You leveled UP!**").build()).queue();
			} else {
				event.getGame().getChannel().sendMessage(event.getGame().createEmbed().setDescription("Too bad, the character '" + event.getGuess() + "' is not in the word! Better luck next time, " + Util.getUser(event.getProfile().getUser(event.getJDA()))).build()).queue(event.getGame()::setLastMessage);
				event.getProfile().addCoins(-2);
				Action action = event.getProfile().addExperience(-1);
				if (action == Action.LEVEL_DOWN)
					event.getGame().getChannel().sendMessage(new EmbedBuilder(event.getProfile().createEmbed(event.getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
			}
		} else if (hangManEvent instanceof AlreadyGuessedEvent) {
			AlreadyGuessedEvent event = (AlreadyGuessedEvent) hangManEvent;
			String s = !event.isInWord() ? "I've already told you, %s this character is not in the word!" : "You've already guessed this character, %s!";
			event.getGame().getChannel().sendMessage(event.getGame().createEmbed().setDescription(String.format(s, Util.getUser(event.getProfile().getUser(event.getJDA())))).build()).queue(event.getGame()::setLastMessage);
		} else if (hangManEvent instanceof LooseEvent) {
			LooseEvent event = (LooseEvent) hangManEvent;
			event.getGame().getChannel().sendMessage(!event.isGiveup() ? event.getGame().createEmbed().setDescription("Well, I think you did great but it wasn't this time. The word was '" + event.getGame().getWord().asString() + "'.").build() : event.getGame().createEmbed().setDescription("Aww man... I know you could've done it! The word was '" + event.getGame().getWord().asString() + "'").build()).queue(event.getGame()::setLastMessage);
			event.getGame().end();
			event.getGame().getProfiles().forEach(p -> {
				p.addCoins(-2);
				p.getHMStats().addDefeat();
				Action action = p.addExperience(-3);
				if (action == Action.LEVEL_DOWN)
					event.getGame().getChannel().sendMessage(new EmbedBuilder(p.createEmbed(event.getGame().getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
			});
		} else if (hangManEvent instanceof WinEvent) {
			WinEvent event = (WinEvent) hangManEvent;
			event.getGame().getChannel().sendMessage(event.getGame().createEmbed().setDescription("\uD83C\uDF89 You did it! You win!! The word is '" + event.getGame().getWord().asString() + "'! \uD83C\uDF89").build()).queue();
			event.getGame().end();
			event.getGame().getProfiles().forEach(p -> {
				p.addCoins(2);
				p.getHMStats().addVictory();
				Action action = p.addExperience(4);
				if (action == Action.LEVEL_UP)
					event.getGame().getChannel().sendMessage(new EmbedBuilder(p.createEmbed(event.getJDA())).setDescription("**You leveled UP!**").build()).queue();
			});
		} else if (hangManEvent instanceof LeaveGameEvent) {
			LeaveGameEvent event = (LeaveGameEvent) hangManEvent;
			event.getGame().remove(event.getProfile());
			event.getProfile().getHMStats().addDefeat();
			event.getGame().getChannel().sendMessage(event.getGame().createEmbed().setDescription("Ooh boy, why did you leave? You were doing well!").build()).queue();
		}
	}
}

package br.com.brjdevs.bran.features.hangman.events;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.features.hangman.HangManGame;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;

public class AlreadyGuessedEvent extends HangManEvent {
	
	@Getter
	protected Profile profile;
	@Getter
	protected boolean isInWord;
	@Getter
	protected String guess;
	
	public AlreadyGuessedEvent(HangManGame game, JDA jda, Profile profile, boolean isInWord, String guess) {
		super(game, jda);
		this.profile = profile;
		this.isInWord = isInWord;
		this.guess = guess;
	}
}

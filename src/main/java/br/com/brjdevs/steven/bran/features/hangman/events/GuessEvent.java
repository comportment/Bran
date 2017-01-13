package br.com.brjdevs.steven.bran.features.hangman.events;

import br.com.brjdevs.steven.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.steven.bran.features.hangman.HangManGame;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;

public class GuessEvent extends HangManEvent {
	
	@Getter
	protected Profile profile;
	@Getter
	protected boolean isGuessRight;
	@Getter
	protected String guess;
	
	public GuessEvent(HangManGame game, JDA jda, Profile profile, boolean isGuessRight, String guess) {
		super(game, jda);
		this.profile = profile;
		this.isGuessRight = isGuessRight;
		this.guess = guess;
	}
}

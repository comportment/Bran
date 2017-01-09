package br.com.brjdevs.bran.features.hangman.events;

import br.com.brjdevs.bran.features.hangman.HangManGame;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;

public class LooseEvent extends HangManEvent {
	
	@Getter
	protected boolean isGiveup;
	
	public LooseEvent(HangManGame game, JDA jda, boolean isGiveup) {
		super(game, jda);
		this.isGiveup = isGiveup;
	}
}

package br.com.brjdevs.steven.bran.features.hangman.events;

import br.com.brjdevs.steven.bran.features.hangman.HangManGame;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;

public abstract class HangManEvent {
	
	@Getter
	protected HangManGame game;
	protected JDA jda;
	
	public HangManEvent(HangManGame game, JDA jda) {
		this.game = game;
		this.jda = jda;
	}
	
	public JDA getJDA() {
		return jda;
	}
}

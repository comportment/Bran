package br.com.brjdevs.steven.bran.features.hangman.events;

import br.com.brjdevs.steven.bran.features.hangman.HangManGame;
import net.dv8tion.jda.core.JDA;

public class WinEvent extends HangManEvent {
	
	public WinEvent(HangManGame game, JDA jda) {
		super(game, jda);
	}
}

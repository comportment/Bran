package br.com.brjdevs.bran.features.hangman.listener;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;

public interface HangManEvents {
	
	void onAlreadyGuessed(boolean miss);
	
	void onGuess(String s);
	
	void onInvalidGuess(String s);
	
	void onLeaveGame(Profile profile);
	
	void onLoose(boolean giveUp);
	
	void onWin();
}

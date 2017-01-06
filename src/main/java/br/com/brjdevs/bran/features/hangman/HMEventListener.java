package br.com.brjdevs.bran.features.hangman;

public interface HMEventListener {
	
	void onAlreadyGuessed(boolean miss);
	
	void onGuess();
	
	void onInvalidGuess(String s);
	
	void onLoose();
	
	void onWin();
}

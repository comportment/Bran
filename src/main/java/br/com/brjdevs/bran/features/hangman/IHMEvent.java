package br.com.brjdevs.bran.features.hangman;

public interface IHMEvent {
	
	void onAlreadyGuessed(boolean miss);
	
	void onGuess();
	
	void onInvalidGuess(String s);
	
	void onLoose();
	
	void onWin();
}

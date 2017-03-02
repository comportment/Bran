package br.com.brjdevs.steven.bran.core.itemManager.items.hangman;

import br.com.brjdevs.steven.bran.core.currency.Profile.Rank;
import br.com.brjdevs.steven.bran.core.itemManager.Item;

public class Guesser implements Item {
	
	@Override
	public String getName() {
		return "Guesser";
	}
	
	@Override
	public String getId() {
		return "HangMan_Guesser";
	}
	
	@Override
	public long getPrice() {
		return 200;
	}
	
	@Override
	public Rank getMinimumRank() {
		return Rank.BEGINNER;
	}
	
	@Override
	public int getMaxStack() {
		return 5;
	}
}

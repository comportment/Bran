package br.com.brjdevs.steven.bran.core.itemManager.items.hangman;

import br.com.brjdevs.steven.bran.core.data.Profile.Rank;
import br.com.brjdevs.steven.bran.core.itemManager.Item;

public class Tip implements Item {
	
	@Override
	public String getName() {
		return "Tip";
	}
	
	@Override
	public String getId() {
		return "HangMan_Tip";
	}
	
	@Override
	public long getPrice() {
		return 100;
	}
	
	@Override
	public Rank getMinimumRank() {
		return Rank.ROOKIE;
	}
	
	@Override
	public int getMaxStack() {
		return Item.INFINITE_STACK;
	}
}

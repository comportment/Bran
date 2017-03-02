package br.com.brjdevs.steven.bran.core.itemManager;

import br.com.brjdevs.steven.bran.core.currency.Profile.Rank;

public interface Item {
	
	int INFINITE_STACK = Integer.MAX_VALUE;
	
	String getName();
	
	String getId();
	
	long getPrice();
	
	Rank getMinimumRank();
	
	int getMaxStack();
}

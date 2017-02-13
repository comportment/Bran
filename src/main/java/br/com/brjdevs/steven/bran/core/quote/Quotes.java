package br.com.brjdevs.steven.bran.core.quote;

import java.util.Random;

public enum Quotes {
	
	FAIL("Something went wrong :confused:", "I blame the cows.", "I wonder: *Why?* :thinking:..."),
	SUCCESS("This was easy, I want a real challenge! :laughing: ", "1, 2, 3 and *dramatic pause*... Done!", "Fast, huh? :muscle: ", "I hope you like it :smile:", "Oh yea boi! :ok_hand:");
	
	private static final Random random = new Random();
	public String[] quotes;
	
	Quotes(String... quotes) {
		this.quotes = quotes;
	}
	
	public static String getQuote(Quotes quote) {
		return quote.quotes[random.nextInt(quote.quotes.length)] + " ";
	}
}

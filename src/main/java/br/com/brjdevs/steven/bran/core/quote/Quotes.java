package br.com.brjdevs.steven.bran.core.quote;

import java.util.Random;

public enum Quotes {
    
    FAIL("Oops :face_palm:", ":unamused: Don't blame me.", "I wonder: *Why?* :thinking:...", "This is embarrassing :sweat_smile:"),
    SUCCESS("This was easy, I want a real challenge! :laughing:", "1, 2, 3 and *dramatic pause*... Done!", "Fast, huh? :muscle: ", "I hope you like it :relaxed:", "Oh yea boi! :ok_hand:", "Sweet!", ":top:", "Your wish is my command :wink:", ":four_leaf_clover: Today's your lucky day!");
    
    private static final Random random = new Random();
	public String[] quotes;
	
	Quotes(String... quotes) {
		this.quotes = quotes;
	}
	
	public static String getQuote(Quotes quote) {
		return quote.quotes[random.nextInt(quote.quotes.length)] + " ";
	}
}

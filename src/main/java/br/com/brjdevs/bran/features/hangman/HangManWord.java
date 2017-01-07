package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;

import java.util.HashSet;
import java.util.Set;

public class HangManWord {
	private String word;
	private Set<String> tips;
	
	public HangManWord(String word) {
		this.word = word;
		this.tips = new HashSet<>();
	}
	
	public static HangManWord getHMWord(String word) {
		return Bot.getInstance().getData().getHangManWords().stream().filter(w -> w.asString().equals(word)).findFirst().orElse(null);
	}
	
	public String asString() {
		return word;
	}

	public boolean addTip(String tip) {
		if (tips.contains(tip)) return false;
		tips.add(tip);
		return true;
	}
	
	public Set<String> getTips() {
		return tips;
	}
}

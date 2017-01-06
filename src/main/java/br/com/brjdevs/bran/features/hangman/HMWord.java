package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;

import java.util.HashSet;
import java.util.Set;

public class HMWord {
	private String word;
	private Set<String> tips;
	
	public HMWord(String word) {
		this.word = word;
		this.tips = new HashSet<>();
	}
	public String getWord() {
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
	
	public static HMWord getHMWord(String word) {
		return Bot.getInstance().getData().getHMWords().stream().filter(w -> w.getWord().equals(word)).findFirst().orElse(null);
	}
}

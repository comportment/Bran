package br.com.brjdevs.steven.bran.core.data.guild.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class WordFilterSettings {
	
	@Getter
	@Setter
	private boolean enabled;
	private List<String> words;
	
	public WordFilterSettings(boolean enabled) {
		this.enabled = enabled;
		this.words = new ArrayList<>();
	}
	
	public List<String> asList() {
		return words;
	}
}

package br.com.brjdevs.steven.bran.core.poll;

import java.util.LinkedList;
import java.util.List;

public class Option {
	private int optionIndex;
	private String option;
	private List<String> votes;
	
	public Option(int optionIndex, String option) {
		this.optionIndex = optionIndex;
		this.option = option;
		this.votes = new LinkedList<>();
	}
	public int getIndex() {
		return optionIndex;
	}
	public String getContent() {
		return option;
	}
	public List<String> getVotes() {
		return votes;
	}
}

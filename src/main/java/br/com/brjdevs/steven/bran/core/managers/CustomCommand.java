package br.com.brjdevs.steven.bran.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CustomCommand {
	
	private static final Random rand = new Random();
	private List<String> answers;
	private String ownerId;
	
	public CustomCommand(List<String> answers, String creatorId) {
		this.answers = answers;
		this.ownerId = creatorId;
	}
	
	public CustomCommand(Collection<String> answers, User creator) {
		this.answers = new ArrayList<>(answers);
		this.ownerId = creator.getId();
	}
	
	public CustomCommand(String firstAnswer, User creator) {
		this.answers = new ArrayList<>();
		this.answers.add(firstAnswer);
		this.ownerId = creator.getId();
	}
	
	public User getCreator(JDA jda) {
		return jda.getUserById(ownerId);
	}
	
	public String getCreatorId() {
		return ownerId;
	}
	
	public String getAnswer(int i) {
		return answers.get(i);
	}
	
	public String getAnswer() {
		return getAnswer(rand.nextInt(answers.size()));
	}
	
	public List<String> getAnswers() {
		return answers;
	}
}

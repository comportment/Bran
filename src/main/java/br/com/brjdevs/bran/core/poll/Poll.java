package br.com.brjdevs.bran.core.poll;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.stream.Collectors;

public class Poll {
	private static final List<Poll> polls = new ArrayList<>();
	private final String pollName;
	private final String creatorId;
	private final LinkedList<Option> options;
	private final String channelId;
	public Poll(String pollName, Member creator, LinkedList<Option> options, TextChannel channel) {
		this.pollName = pollName;
		this.creatorId = creator.getUser().getId();
		this.options = options;
		this.channelId = channel.getId();
		polls.add(this);
	}
	public String getPollName() {
		return pollName;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public LinkedList<Option> getOptions() {
		return options;
	}
	public String getChannelId() {
		return channelId;
	}
	public Option getOption(int optionIndex) {
		return options.stream().filter(option -> option.getIndex() == optionIndex).findFirst().orElse(null);
	}
	/**
	 * Adds or remove a vote to the {@code Poll}
	 * @return false if vote is removed; true if the vote is added
	 * @throws NullPointerException if the index is invalid.
	 */
	public boolean vote(String userId, int optionIndex) {
		Option option = getOption(optionIndex);
		if (option.getVotes().contains(userId)) {
			option.getVotes().remove(userId);
			return false;
		}
		return option.getVotes().add(userId);
	}
	public List<Option> getLeadership() {
		return options.stream().filter(option -> option.getVotes().size() == Collections.max(options.stream().map(o -> o.getVotes().size()).collect(Collectors.toList())) && option.getVotes().size() != 0).collect(Collectors.toList());
	}
	public void remove() {
		polls.remove(this);
	}
	public static List<Poll> getRunningPolls() {
		return polls;
	}
	public static Poll getPoll(TextChannel channel) {
		return polls.stream().filter(poll -> poll.getChannelId().equals(channel.getId())).findFirst().orElse(null);
	}
}

package br.com.brjdevs.steven.bran.core.poll;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.Client;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Poll {
	private final String pollName;
	private final String creatorId;
	private final LinkedList<Option> options;
	private final String channelId;
	private final String guildId;
	private int shardId;
	
	public Poll(String pollName, Member creator, LinkedList<Option> options, TextChannel channel) {
		this.pollName = pollName;
		this.creatorId = creator.getUser().getId();
		this.options = options;
		this.channelId = channel.getId();
		this.guildId = channel.getGuild().getId();
		setShardId(Bran.getInstance().getShardId(channel.getJDA()));
		getRunningPolls().add(this);
	}
	
	public static List<Poll> getRunningPolls() {
        return Bran.getInstance().getDataManager().getPolls().get().polls;
    }
	
	public static Poll getPoll(TextChannel channel) {
		return getRunningPolls().stream().filter(poll -> poll.getChannelId().equals(channel.getId())).findFirst().orElse(null);
	}
	
	public int getShardId() {
		return shardId;
	}
	
	public void setShardId(int shardId) {
		this.shardId = shardId;
	}
    
    public Client getShard(Bran bran) {
        return bran.getShards()[shardId];
	}
	
	public JDA getJDA(Bran bran) {
		return getShard(bran).getJDA();
	}
	
	public String getGuildId() {
		return guildId;
	}
	
	public Guild getGuild(Bran bran) {
		return getJDA(bran).getGuildById(getGuildId());
	}

	public String getPollName() {
		return pollName.isEmpty() ? "\u00AD" : pollName;
	}

	public String getCreatorId() {
		return creatorId;
	}
	
	public User getCreator(Bran bran) {
		return getJDA(bran).getUserById(getCreatorId());
	}

	public LinkedList<Option> getOptions() {
		return options;
	}
	
	public String getChannelId() {
		return channelId;
	}
	
	public TextChannel getChannel(Bran bran) {
		return getJDA(bran).getTextChannelById(getChannelId());
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
		Option option1 = getOption(userId);
		if (option1 != null) {
			option1.getVotes().remove(userId);
		}
		option.getVotes().add(userId);
		return true;
	}
	
	public List<Option> getLeadership() {
		return options.stream().filter(option -> option.getVotes().size() == Collections.max(options.stream().map(o -> o.getVotes().size()).collect(Collectors.toList())) && option.getVotes().size() != 0).collect(Collectors.toList());
	}
	
	public void remove() {
		getRunningPolls().remove(this);
	}
	
	public Option getOption(String userId) {
		return getOptions().stream().filter(op -> op.getVotes().contains(userId)).findFirst().orElse(null);
	}
}

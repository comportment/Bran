package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.utils.Util;
import br.com.brjdevs.bran.features.hangman.listener.HangManEventListener;
import br.com.brjdevs.bran.features.hangman.listener.HangManEvents;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.bran.core.utils.StringUtils.containsEqualsIgnoreCase;
import static br.com.brjdevs.bran.core.utils.Util.containsEqualsIgnoreCase;

public class HangManGame {
	
	private static final List<HangManGame> sessions;
	
	static {
		sessions = new ArrayList<>();
	}

	private final LinkedHashMap<String, Boolean> guesses;
	private final List<Profile> invitedUsers;
	private final HangManWord word;
	private final String channel;
	private final List<String> mistakes;
	@Getter
	private final HangManEvents listener;
	private long lastGuess;
	private Profile creator;
	@Getter
	private String lastMessage;
	private int shard;
	
	public HangManGame(Profile profile, HangManWord word, TextChannel channel) {
		this.listener = new HangManEventListener(this);
		this.guesses = new LinkedHashMap<>();
		this.creator = profile;
		this.mistakes = new ArrayList<>();
		this.word = word;
		this.invitedUsers = new ArrayList<>();
		this.lastGuess = System.currentTimeMillis();
		this.channel = channel.getId();
		this.shard = Bot.getInstance().getShardId(channel.getJDA());
		Arrays.stream(word.asString().split(""))
				.forEach(split ->
						guesses.put(guesses.containsKey(split) ? split + Util.randomName(3) : split, false));
		if (word.asString().contains(" ")) guess(" ");
		sessions.add(this);
	}
	
	public static HangManGame getSession(Profile profile) {
		return sessions.stream().filter(session -> session.getCreator().equals(profile) || session.getInvitedUsers().contains(profile)).findAny().orElse(null);
	}

	public TextChannel getChannel() {
		return getJDA().getTextChannelById(channel);
	}
	
	public JDA getJDA() {
		return Bot.getInstance().getShard(shard);
	}

	public void remove(Profile profile) {
		invitedUsers.remove(profile);
	}

	public void invite(Profile profile) {
		this.invitedUsers.add(profile);
	}

	public List<Profile> getInvitedUsers() {
		return invitedUsers;
	}

	public int getMaxErrors() {
		return 5 + (getInvitedUsers().size() * 3);
	}

	public void guess(String c) {
		this.lastGuess = System.currentTimeMillis();
		if (isGuessed(c)) {
			getListener().onAlreadyGuessed(getMistakes().contains(c));
			return;
		}
		if (isInvalid(c)) {
			mistakes.add(c);
			if (mistakes.size() > getMaxErrors()) {
				getListener().onLoose(false);
				return;
			}
			getListener().onInvalidGuess(c);
			return;
		}
		guesses.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().charAt(0) == String.valueOf(c).toLowerCase().charAt(0)).forEach(entry -> guesses.replace(entry.getKey(), true));
		if (getGuessedLetters().equals(getWord().asString())) {
			getListener().onWin();
			return;
		}
		getListener().onGuess(c);
	}

	public void pass(Profile profile) {
		getInvitedUsers().remove(profile);
		getInvitedUsers().add(creator);
		this.creator = profile;
	}

	public String getGuessedLetters() {
		return String.join("", guesses.entrySet().stream().map(entry -> (entry.getValue() ? entry.getKey().charAt(0) : "\\_") + "").collect(Collectors.toList()));
	}
	
	public HangManWord getWord() {
		return word;
	}

	public List<String> getGuesses() {
		return guesses.entrySet().stream().map(entry -> entry.getValue() ? entry.getKey() : "_").collect(Collectors.toList());
	}

	public List<String> getMistakes() {
		return mistakes;
	}
	
	public boolean isGuessed(String c) {
		return containsEqualsIgnoreCase(getGuesses(), c) || containsEqualsIgnoreCase(getMistakes(), c);
	}
	
	public boolean isInvalid(String c) {
		return !containsEqualsIgnoreCase(getWord().asString(), c);
	}
	
	public Profile getCreator() {
		return creator;
	}
	
	public List<Profile> getProfiles() {
		List<Profile> profiles = new ArrayList<>();
		profiles.add(getCreator());
		profiles.addAll(getInvitedUsers());
		return profiles;
	}
	
	public Field getCurrentGuessesField(boolean inline) {
		return new Field("_ _", "**These are your current guesses:** " + getGuessedLetters() + "\n**These are your current mistakes:** " + String.join(", ", getMistakes().stream().map(String::valueOf).collect(Collectors.toList())) + "         *Total Mistakes: " + getMistakes().size() + "/" + getMaxErrors() + "*", inline);
	}
	
	public Field getInvitedUsersField(boolean inline) {
		return new Field("Invited Users", getInvitedUsers().isEmpty() ? "There are no invited users in this session, use `" + Bot.getInstance().getDefaultPrefixes()[0] + "hm invite [mention]` to invite someone to play with you!" : "There are " + getInvitedUsers().size() + " users playing in this session.\n" + (String.join(", ", getInvitedUsers().stream().map(profile -> profile.getUser(getJDA()).getName()).collect(Collectors.toList()))), inline);
	}
	
	public void setLastMessage(Message message) {
		if (message == null) {
			this.lastMessage = null;
			return;
		}
		this.lastMessage = message.getId();
	}
	
	public EmbedBuilder createEmbed() {
		if (lastMessage != null) {
			try {
				getChannel().deleteMessageById(lastMessage).queue(
						success -> setLastMessage(null),
						fail -> setLastMessage(null));
			} catch (ErrorResponseException ignored) {
			}
		}
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Hang Man");
		builder.setFooter("Session created by " + getCreator().getUser(getJDA()).getName(), Util.getAvatarUrl(getCreator().getUser(getJDA())));
		builder.setColor(getCreator().getEffectiveColor());
		builder.addField(getCurrentGuessesField(false));
		builder.addField(getInvitedUsersField(false));
		return builder;
	}
	
	public void end() {
		sessions.remove(this);
	}
}
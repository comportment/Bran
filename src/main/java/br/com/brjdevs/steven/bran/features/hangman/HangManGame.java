package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.data.bot.settings.HangManWord;
import br.com.brjdevs.steven.bran.core.data.guild.settings.Profile;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import br.com.brjdevs.steven.bran.features.hangman.events.*;
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
	private final IEventListener listener;
	private long lastGuess;
	private Profile creator;
	@Getter
	private String lastMessage;
	private int shard;
	
	public HangManGame(Profile profile, HangManWord word, TextChannel channel) {
		this.listener = new EventListener();
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
		if (word.asString().contains(" ")) {
			guesses.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().charAt(0) == ' ').forEach(entry -> guesses.replace(entry.getKey(), true));
		}
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
	
	public void guess(String string, Profile profile) {
		this.lastGuess = System.currentTimeMillis();
		if (isGuessed(string)) {
			getListener().onEvent(new AlreadyGuessedEvent(this, getJDA(), profile, StringUtils.containsEqualsIgnoreCase(getWord().asString(), string), string));
			return;
		}
		if (isInvalid(string)) {
			mistakes.add(string);
			if (mistakes.size() > getMaxErrors()) {
				getListener().onEvent(new LooseEvent(this, getJDA(), false));
				return;
			}
			getListener().onEvent(new GuessEvent(this, getJDA(), profile, false, string));
			return;
		}
		guesses.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().charAt(0) == String.valueOf(string).toLowerCase().charAt(0)).forEach(entry -> guesses.replace(entry.getKey(), true));
		if (getGuessedLetters().equals(getWord().asString())) {
			getListener().onEvent(new WinEvent(this, getJDA()));
			return;
		}
		getListener().onEvent(new GuessEvent(this, getJDA(), profile, true, string));
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
		return Util.containsEqualsIgnoreCase(getGuesses(), c) || Util.containsEqualsIgnoreCase(getMistakes(), c);
	}
	
	public boolean isInvalid(String c) {
		return !StringUtils.containsEqualsIgnoreCase(getWord().asString(), c);
	}
	
	public Profile getCreator() {
		return creator;
	}
	
	public void setCreator(Profile profile) {
		getInvitedUsers().remove(profile);
		getInvitedUsers().add(creator);
		this.creator = profile;
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
				setLastMessage(null);
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
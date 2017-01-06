package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HMSession {
	private static final String HM_URL = "https://i.imgur.com/GdjzHgp.jpg";
	private static final List<HMSession> sessions;
	
	static {
		sessions = new ArrayList<>();
	}

	private final LinkedHashMap<String, Boolean> guesses;

	private final List<Profile> invitedUsers;

	private final HMWord word;
	
	private final String channel;

	private final List<String> mistakes;
	
	@Getter
	private final IHMEvent listener;

	private long lastGuess;

	private Profile creator;
	
	@Getter
	private String lastMessage;
	
	private int shard;
	public HMSession(Profile profile, HMWord word, TextChannel channel) {
		this.listener = new HMEventListener(this);
		this.guesses = new LinkedHashMap<>();
		this.creator = profile;
		this.mistakes = new ArrayList<>();
		this.word = word;
		this.invitedUsers = new ArrayList<>();
		this.lastGuess = System.currentTimeMillis();
		this.channel = channel.getId();
		this.shard = Bot.getInstance().getShardId(channel.getJDA());
		Arrays.stream(word.getWord().split(""))
				.forEach(split ->
						guesses.put(guesses.containsKey(split) ? split + Util.randomName(3) : split, false));
		if (word.getWord().contains(" ")) guess(" ");
		sessions.add(this);
	}
	
	public static HMSession getSession(Profile profile) {
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
				getListener().onLoose();
				return;
			}
			getListener().onInvalidGuess(c);
			return;
		}
		guesses.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().charAt(0) == String.valueOf(c).toLowerCase().charAt(0)).forEach(entry -> guesses.replace(entry.getKey(), true));
		if (getGuessedLetters().equals(getWord().getWord())) {
			getListener().onWin();
			return;
		}
		getListener().onGuess();
	}

	public void pass(Profile profile) {
		getInvitedUsers().remove(profile);
		getInvitedUsers().add(creator);
		this.creator = profile;
	}

	public String getGuessedLetters() {
		return String.join("", guesses.entrySet().stream().map(entry -> (entry.getValue() ? entry.getKey().charAt(0) : "\\_") + "").collect(Collectors.toList()));
	}

	public HMWord getWord() {
		return word;
	}

	public List<String> getGuesses() {
		return guesses.entrySet().stream().map(entry -> entry.getValue() ? entry.getKey() : "_").collect(Collectors.toList());
	}

	public List<String> getMistakes() {
		return mistakes;
	}
	
	public boolean isGuessed(String c) {
		return getGuesses().contains(c) || getMistakes().contains(c);
	}
	
	public boolean isInvalid(String c) {
		return !getWord().getWord().contains(c);
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
		return new Field("Current Guesses", "**Guesses:** " + getGuessedLetters() + "\n**Mistakes:** " + String.join(", ", getMistakes().stream().map(ch -> ch + "").collect(Collectors.toList())) + "      *Errors: " + getMistakes().size() + "/" + getMaxErrors() + "*", inline);
	}
	
	public Field getInvitedUsersField(boolean inline) {
		return new Field("Invited Users", getInvitedUsers().isEmpty() ? "This is not a MultiPlayer session, use `" + Bot.getInstance().getDefaultPrefixes()[0] + "hm invite [MENTION]` to invite someone to play with you!" : "There are " + getInvitedUsers().size() + " users playing in this session.\n" + (String.join(", ", getInvitedUsers().stream().map(profile -> profile.getUser(getJDA()).getName()).collect(Collectors.toList()))), inline);
	}
	
	public void setLastMessage(Message message) {
		this.lastMessage = message.getId();
	}
	
	public MessageEmbed createEmbed(EmbedInfo embedInfo) {
		if (lastMessage != null) {
			try {
				getChannel().deleteMessageById(lastMessage).queue();
			} catch (ErrorResponseException ignored) {
			}
		}
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("HangMan Session", null, HM_URL);
		builder.setFooter("Session created by " + getCreator().getUser(getJDA()).getName(), Util.getAvatarUrl(getCreator().getUser(getJDA())));
		if (embedInfo.equals(EmbedInfo.INFO)) {
			builder.setDescription("Information about this session.");
		} else if (embedInfo.equals(EmbedInfo.GUESS_R)) {
			int matches = StringUtils.countMatches(getGuessedLetters(), '_');
			builder.setDescription("Nice, just more " + matches + " " + (matches > 1 ? "guesses" : "guess") + " to go!");
		} else if (embedInfo.equals(EmbedInfo.GUESS_W)) {
			builder.setDescription("Uh-oh... This doesn't seem to be right...");
		} else if (embedInfo.equals(EmbedInfo.WIN)) {
			builder.setDescription("Congratulations, you won! The word was '" + getWord().getWord() + "'!");
		} else if (embedInfo.equals(EmbedInfo.LOOSE)) {
			builder.setDescription("Aww man... You lost. The word was '" + getWord().getWord() + "'");
		} else if (embedInfo.equals(EmbedInfo.GUESSED)) {
			builder.setDescription("You already guessed this letter.");
		} else if (embedInfo.equals(EmbedInfo.GIVEUP)) {
			builder.setDescription("Aww man, why did you leave this session?");
		}
		builder.setColor(new Color((int)(Math.random() * 0x1000000)));
		builder.addField(getCurrentGuessesField(false));
		builder.addField(getInvitedUsersField(false));
		return builder.build();
	}
	
	public void end() {
		sessions.remove(this);
	}
	
	public enum EmbedInfo {
		INFO, GUESS_R, GUESS_W, WIN, LOOSE, GUESSED, GIVEUP
	}
}
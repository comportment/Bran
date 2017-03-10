package br.com.brjdevs.steven.bran.games.hangman;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.BranShard;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HangManGame {
	
	public static List<HangManGame> games = new ArrayList<>();
	
	public BranShard branShard;
	private long creatorId;
	private List<Long> invitedUsers;
	private LinkedHashMap<String, Boolean> word;
	private List<Character> mistakes;
	private List<String> givenTips;
	private long channelId;
	private boolean isPrivate;
	
	public HangManGame(BranShard branShard, MessageChannel channel, User creator, String word) {
		this.branShard = branShard;
		this.creatorId = Long.parseLong(creator.getId());
		this.invitedUsers = new ArrayList<>();
		this.word = new LinkedHashMap<>();
		this.channelId = Long.parseLong(channel.getId());
		this.isPrivate = channel instanceof PrivateChannel;
		this.mistakes = new ArrayList<>();
		this.givenTips = new ArrayList<>();
		
		Arrays.stream(word.split("")).forEach(c -> this.word.put(this.word.containsKey(c) ? c + StringUtils.randomName(3) : c, c.equals(" ")));
		games.add(this);
	}
	
	public static HangManGame getGame(User user) {
		return games.stream().filter(game -> game.getInvitedUsers().contains(user) || game.getCreator() != null && game.getCreator().equals(user)).findFirst().orElse(null);
	}
	
	public List<String> getGivenTips() {
		return givenTips;
	}
	
	public User getCreator() {
		return branShard.getJDA().getUserById(String.valueOf(creatorId));
	}
	
	public UserData getCreatorData() {
		return branShard.getBran().getDataManager().getUserDataManager().get().getUser(getCreator());
	}
	
	public boolean isMuliplayer() {
		return !invitedUsers.isEmpty();
	}
	
	public List<User> getInvitedUsers() {
		return invitedUsers.stream().map(id -> branShard.getJDA().getUserById(String.valueOf(id))).collect(Collectors.toList());
	}
	
	public List<UserData> getInvitedUserDatas() {
		return invitedUsers.stream().map(id -> branShard.getBran().getDataManager().getUserDataManager().get().getUser(branShard.getJDA().getUserById(String.valueOf(id)))).collect(Collectors.toList());
	}
	
	public int getMaximumMistakes() {
		return 5 + (invitedUsers.size() * 3);
	}
	
	public String getFullWord() {
		return word.keySet().stream().map(s -> s.substring(0, 1)).collect(Collectors.joining());
	}
	
	public MessageChannel getChannel() {
		return isPrivate ? branShard.getJDA().getPrivateChannelById(String.valueOf(channelId)) : branShard.getJDA().getTextChannelById(String.valueOf(channelId));
	}
	
	public EmbedBuilder baseEmbed() {
		User creator = getCreator();
		return new EmbedBuilder().setColor(Bran.COLOR).setTitle("Hang Man Game", null).setFooter("Game session started by " + creator.getName() + "#" + creator.getDiscriminator(), creator.getEffectiveAvatarUrl());
	}
	
	public String getGuessedLetters() {
		return word.entrySet().stream().map(entry -> entry.getValue() ? String.valueOf(entry.getKey().charAt(0)) : "\\_").collect(Collectors.joining());
	}
	
	public List<Character> getMistakes() {
		return mistakes;
	}
	
	public boolean isPrivate() {
		return isPrivate;
	}
	
	public void guess(char c, UserData userData) {
		String s = String.valueOf(c);
		if (!getFullWord().contains(s.toLowerCase()) && !getFullWord().contains(s.toUpperCase()) && !mistakes.contains(s.toLowerCase().charAt(0)) && !mistakes.contains(s.toUpperCase().charAt(0))) {
			reward(userData, 0, -4);
			getChannel().sendMessage(baseEmbed().setDescription(Emojis.DISAPPOINTED + " Nope, that is not in the word. Keep trying!\n\n\n**Guesses:** " + getGuessedLetters() + "\nYou've made " + mistakes.size() + " out of " + getMaximumMistakes() + "." + (mistakes.isEmpty() ? "" : " (" + mistakes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")") + "\n\n" + (givenTips.isEmpty() ? "You didn't ask for any tips." : "These are the current given tips:\n" + (String.join("\n", givenTips))) + "\nMultiplayer: " + (invitedUsers.isEmpty()) + (invitedUsers.isEmpty() ? "" : "\n" + getInvitedUsers().stream().map(Utils::getUser).collect(Collectors.joining(", ")))).build()).queue();
			mistakes.add(c);
			if (mistakes.size() > getMaximumMistakes())
				loose();
			return;
		}
		if (word.entrySet().stream().filter(entry -> !entry.getValue() && entry.getKey().substring(0, 1).equalsIgnoreCase(s)).findFirst() == null) {
			getChannel().sendMessage("You already Guessed this Letter!").queue();
		} else {
			word.entrySet().stream().filter(entry -> !entry.getValue() && entry.getKey().substring(0, 1).equalsIgnoreCase(s)).forEach(entry -> word.replace(entry.getKey(), true));
			if (getFullWord().equals(getGuessedLetters())) {
				win();
				return;
			}
			getChannel().sendMessage(baseEmbed().setDescription(Emojis.THUMBS_UP + " Alright you guessed a letter! Keep the good job!\n\n\n**Guesses:** " + getGuessedLetters() + "\nYou've made " + mistakes.size() + " out of " + getMaximumMistakes() + "." + (mistakes.isEmpty() ? "" : " (" + mistakes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")") + "\n\n" + (givenTips.isEmpty() ? "You didn't ask for any tips." : "These are the current given tips:\n" + (String.join("\n", givenTips))) + "\nMultiplayer: " + (isMuliplayer()) + (invitedUsers.isEmpty() ? "" : "\n" + getInvitedUsers().stream().map(Utils::getUser).collect(Collectors.joining(", ")))).build()).queue();
			reward(userData, 5, 1);
		}
	}
	
	private void win() {
		getChannel().sendMessage(Emojis.PARTY_POPPER + " Yay you won! The word was `" + getFullWord() + "`! " + (invitedUsers.isEmpty() ? "You" : "Everyone") + " won 20 coins and 10 experience.").queue();
		reward(getCreatorData(), 20, 10);
		getCreatorData().getProfile().getHMStats().addVictory();
		getInvitedUserDatas().forEach(userData -> {
			reward(userData, 20, 10);
			userData.getProfile().getHMStats().addVictory();
		});
		games.remove(this);
	}
	
	private void loose() {
		getChannel().sendMessage(Emojis.CRY + " Too bad, you lost! The word was `" + getFullWord() + "`! " + (invitedUsers.isEmpty() ? "You" : "Everyone") + " lost 15 coins and 5 experience.").queue();
		reward(getCreatorData(), -15, -5);
		getCreatorData().getProfile().getHMStats().addDefeat();
		getInvitedUserDatas().forEach(userData -> {
			reward(userData, -15, -5);
			userData.getProfile().getHMStats().addDefeat();
		});
		games.remove(this);
	}
	
	public void giveup() {
		getChannel().sendMessage(Emojis.FROWNING + " Aww, why did you give up? The word was `" + getFullWord() + "`.").queue();
		games.remove(this);
	}
	
	public void leave(User user) {
		getChannel().sendMessage(Utils.getUser(user) + " left the game!").queue();
		this.invitedUsers.remove(Long.parseLong(user.getId()));
	}
	
	public void invite(User user) {
		getChannel().sendMessage(Utils.getUser(user) + " joined the game!").queue();
		invitedUsers.add(Long.parseLong(user.getId()));
	}
	
	public void pass(User user) {
		getChannel().sendMessage(Utils.getUser(getCreator()) + " passed game ownership to " + Utils.getUser(user) + ".").queue();
		invitedUsers.add(creatorId);
		creatorId = Long.parseLong(user.getId());
		invitedUsers.remove(creatorId);
	}
	
	private void reward(UserData userData, long coins, long exp) {
		if (coins > 0)
			userData.getProfile().getBankAccount().addCoins(coins, BankAccount.MAIN_BANK);
		else
			userData.getProfile().getBankAccount().takeCoins(coins, BankAccount.MAIN_BANK);
		userData.getProfile().addExperience(exp);
	}
}

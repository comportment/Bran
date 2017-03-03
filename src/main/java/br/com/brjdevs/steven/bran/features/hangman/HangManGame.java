package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.core.client.ClientShard;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HangManGame {
	
	public static List<HangManGame> games = new ArrayList<>();
	
	public ClientShard clientShard;
	private long creatorId;
	private List<Long> invitedUsers;
	private LinkedHashMap<String, Boolean> word;
	private List<Character> mistakes;
	private List<String> givenTips;
	private long channelId;
	private boolean isPrivate;
	
	public HangManGame(ClientShard clientShard, MessageChannel channel, User creator, String word) {
		this.clientShard = clientShard;
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
		return games.stream().filter(game -> game.getInvitedUsers().contains(user) || game.getCreator().equals(user)).findFirst().orElse(null);
	}
	
	public List<String> getGivenTips() {
		return givenTips;
	}
	
	public User getCreator() {
		return clientShard.getJDA().getUserById(String.valueOf(creatorId));
	}
	
	public UserData getCreatorData() {
		return clientShard.getClient().getDiscordBotData().getDataHolderManager().get().getUser(getCreator());
	}
	
	public boolean isMuliplayer() {
		return !invitedUsers.isEmpty();
	}
	
	public List<User> getInvitedUsers() {
		return invitedUsers.stream().map(id -> clientShard.getJDA().getUserById(String.valueOf(id))).collect(Collectors.toList());
	}
	
	public List<UserData> getInvitedUserDatas() {
		return invitedUsers.stream().map(id -> clientShard.getClient().getDiscordBotData().getDataHolderManager().get().getUser(clientShard.getJDA().getUserById(String.valueOf(id)))).collect(Collectors.toList());
	}
	
	public int getMaximumMistakes() {
		return 5 + (invitedUsers.size() * 3);
	}
	
	public String getFullWord() {
		return word.keySet().stream().collect(Collectors.joining());
	}
	
	public MessageChannel getChannel() {
		return isPrivate ? clientShard.getJDA().getPrivateChannelById(String.valueOf(channelId)) : clientShard.getJDA().getTextChannelById(String.valueOf(channelId));
	}
	
	public EmbedBuilder baseEmbed() {
		User creator = getCreator();
		return new EmbedBuilder().setColor(Color.decode("#3C4044")).setTitle("Hang Man Game", null).setFooter("Game session started by " + creator.getName() + "#" + creator.getDiscriminator(), creator.getEffectiveAvatarUrl());
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
		if (!getFullWord().contains(s) && !mistakes.contains(c)) {
			reward(userData, 0, -4);
			getChannel().sendMessage(baseEmbed().setDescription(Emojis.DISAPPOINTED + " Nope, that is not in the word. Keep trying!\n\n\n**Guesses:** " + getGuessedLetters() + "\nYou've made " + mistakes.size() + " out of " + getMaximumMistakes() + "." + (mistakes.isEmpty() ? "" : " (" + mistakes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")") + "\n\n" + (givenTips.isEmpty() ? "You didn't ask for any tips." : "These are the current given tips:\n" + (String.join("\n", givenTips))) + "\nMultiplayer: " + (invitedUsers.isEmpty()) + (invitedUsers.isEmpty() ? "" : "\n" + getInvitedUsers().stream().map(Utils::getUser).collect(Collectors.joining(", ")))).build()).queue();
			mistakes.add(c);
			if (mistakes.size() > getMaximumMistakes())
				loose();
			return;
		}
		if (word.entrySet().stream().filter(entry -> !entry.getValue() && entry.getKey().equalsIgnoreCase(s)).findFirst() == null) {
			getChannel().sendMessage("You already Guessed this Letter!").queue();
		} else {
			word.entrySet().stream().filter(entry -> !entry.getValue() && entry.getKey().equalsIgnoreCase(s)).forEach(entry -> word.replace(entry.getKey(), true));
			if (getFullWord().equals(getGuessedLetters())) {
				win();
				return;
			}
			getChannel().sendMessage(baseEmbed().setDescription(Emojis.THUMBS_UP + " Alright you guessed a letter! Keep the good job!\n\n\n**Guesses:** " + getGuessedLetters() + "\nYou've made " + mistakes.size() + " out of " + getMaximumMistakes() + "." + (mistakes.isEmpty() ? "" : " (" + mistakes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")") + "\n\n" + (givenTips.isEmpty() ? "You didn't ask for any tips." : "These are the current given tips:\n" + (String.join("\n", givenTips))) + "\nMultiplayer: " + (invitedUsers.isEmpty()) + (invitedUsers.isEmpty() ? "" : "\n" + getInvitedUsers().stream().map(Utils::getUser).collect(Collectors.joining(", ")))).build()).queue();
			reward(userData, 5, 1);
		}
	}
	
	private void win() {
		getChannel().sendMessage(Emojis.PARTY_POPPER + " Yay you won! The word was `" + getFullWord() + "`! " + (invitedUsers.isEmpty() ? "You" : "Everyone") + " won 20 coins and 10 experience.").queue();
		games.remove(this);
	}
	
	private void loose() {
		getChannel().sendMessage(Emojis.CRY + " Too bad, you lost! The word was `" + getFullWord() + "`! " + (invitedUsers.isEmpty() ? "You" : "Everyone") + " lost 15 coins and 5 experience.").queue();
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
		userData.getProfile().getBankAccount().addCoins(coins, BankAccount.MAIN_BANK);
		userData.getProfile().addExperience(exp);
	}
}

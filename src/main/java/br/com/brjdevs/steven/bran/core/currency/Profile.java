package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Profile {
	
	private static String EMPTY = "\u00AD";
	
	public String customHex;
	private BankAccount bankAccount;
	private String userId;
	private HMStats HMStats;
	private Rank rank;
	private long level, experience;
	private Inventory inventory;
	private transient List<IProfileListener> listeners;
	
	public Profile(User user) {
		this.userId = user.getId();
		this.HMStats = new HMStats();
		this.rank = Rank.ROOKIE;
		this.level = 0;
		this.experience = 0;
		this.bankAccount = new BankAccount(user);
		this.customHex = null;
		this.inventory = new Inventory();
		this.listeners = new ArrayList<>();
	}
	
	public static double getPercentToLevelUp(long experience, long level) {
		return Math.floor(experience / expForNextLevel(level) * 10000) / 100;
	}
	
	public static long expForNextLevel(long level) {
		double expCalculate = 9 + Math.pow(1.3d, level);
		long expRequired = Math.round(expCalculate);
		if (expCalculate - expRequired > 0) expRequired++;
		return expRequired;
	}
	
	public BankAccount getBankAccount() {
		if (bankAccount == null) bankAccount = new BankAccount(userId);
		return bankAccount;
	}
	
	public String getCustomHex() {
		return customHex;
	}
	
	public Profile.HMStats getHMStats() {
		return HMStats;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public void setRank(Rank rank) {
		this.rank = rank;
	}
	
	public long getLevel() {
		return level;
	}
	
	private void setLevel(long level) {
		this.level = level;
	}
	
	public long getExperience() {
		return experience;
	}
	
	private void setExperience(long experience) {
		this.experience = experience;
	}
	
	public Inventory getInventory() {
		if (inventory == null) inventory = new Inventory();
		return inventory;
	}
	
	public User getUser(JDA jda) {
		return jda.getUserById(userId);
	}
	
	public void addExperience(long experience) {
		setExperience(this.experience + experience);
		if (getExperience() >= expForNextLevel(getLevel())) {
			setExperience(getExperience() - Profile.expForNextLevel(getLevel()));
			setLevel(getLevel() + 1);
			boolean rankUp = level >= rank.next().getLevel() && !rank.equals(Rank.EXPERT);
			if (rankUp)
				setRank(getRank().next());
			getRegisteredListeners().forEach(listener -> listener.onLevelUp(this, rankUp));
		} else if (getExperience() < 0 && getLevel() > 0) {
			setExperience(Profile.expForNextLevel(getLevel() - 1) + getExperience());
			setLevel(getLevel() - 1);
			boolean rankDown = getLevel() < rank.getLevel() && !rank.equals(Rank.ROOKIE);
			if (rankDown)
				setRank(getRank().previous());
			getRegisteredListeners().forEach(listener -> listener.onLevelDown(this, rankDown));
		}
	}
	
	public void reset() {
		this.HMStats = new HMStats();
		this.rank = Rank.ROOKIE;
		this.level = 0;
		this.level = 0;
		this.inventory = new Inventory();
	}
	
	public boolean setCustomColor(String hex) {
		this.customHex = hex;
		try {
			Color.decode(customHex);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public Color getCustomColor() {
		if (customHex == null) return null;
		return Color.decode(customHex);
	}
	
	public Color getEffectiveColor() {
		return customHex == null ? rank.getColor() : getCustomColor();
	}
	
	public List<IProfileListener> getRegisteredListeners() {
		return listeners;
	}
	
	public void setListeners(List<IProfileListener> listeners) {
		this.listeners = listeners;
	}
	
	public void registerListener(IProfileListener listener) {
		if (listeners == null) listeners = new ArrayList<>();
		this.listeners.add(listener);
	}
	
	public void unregisterListener(IProfileListener listener) {
		if (listeners == null) listeners = new ArrayList<>();
		this.listeners.remove(listener);
	}
	
	public MessageEmbed createEmbed(JDA jda) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor(getUser(jda).getName() + "'s profile information", null, Utils.getAvatarUrl(getUser(jda)));
		builder.setDescription(EMPTY + "\n" + EMPTY);
		builder.addField("\u2694 Level", String.valueOf(getLevel()), true);
		builder.addField("\uD83C\uDF1F Experience", String.valueOf(getExperience()), true);
		builder.addField("\u2b50 Experience to Next Level", String.valueOf(expForNextLevel(getLevel())), true);
		builder.addField("\uD83D\uDCB8 Coins", String.valueOf(bankAccount.getCoins()), true);
		builder.addField("\uD83D\uDCBC Inventory", String.valueOf(inventory.size(false)), true);
		builder.addField("\uD83C\uDF96 Rank", getRank().toString(), true).addBlankField(true).addField("\uD83C\uDFAE Game Stats", EMPTY, true).addBlankField(true);
		builder.addField("\uD83D\uDD79 Game", "HangMan", true).addField("\uD83C\uDFC6 Victories", String.valueOf(getHMStats().getVictories()), true).addField("☠ Defeats", String.valueOf(getHMStats().getDefeats()), true);
		builder.setColor(this.getEffectiveColor());
		return builder.build();
	}
	
	public enum Rank {
		ROOKIE(0, "#5838D6"),
		BEGINNER(5, "#38C9D6"),
		TALENTED(10, "#8438D6"),
		SKILLED(20, "#4EE07D"),
		INTERMEDIATE(35, "#93DA38"),
		SKILLFUL(40, "#C0DA38"),
		EXPERIENCED(50, "#DCF80C"),
		ADVANCED(70, "#F8C90C"),
		SENIOR(85, "#FFAD00"),
		EXPERT(100, "#DB2121");
		private static Rank[] vals = values();
		private final int level;
		private final String hex;
		
		Rank(int i, String hex) {
			this.level = i;
			this.hex = hex;
		}
		
		public int getLevel() {
			return level;
		}
		
		public Color getColor() {
			return Color.decode(hex);
		}
		
		public Rank next() {
			return vals[(this.ordinal() + 1) % vals.length];
		}
		
		public Rank previous() {
			if (this.equals(ROOKIE)) return ROOKIE;
			return vals[(this.ordinal() - 1) % vals.length];
		}
	}
	
	public static class HMStats {
		
		private int victory;
		private int defeats;
		
		public int getVictories() {
			return victory;
		}
		
		public int getDefeats() {
			return defeats;
		}
		
		public int addVictory() {
			this.victory++;
			return victory;
		}
		
		public int addDefeat() {
			this.defeats++;
			return defeats;
		}
	}
}

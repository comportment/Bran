package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import br.com.brjdevs.steven.bran.games.engine.GameReference;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileData {
    
    private static String EMPTY = "\u00AD";
    
    public String customHex;
    private BankAccount bankAccount;
	private String userId;
	private HMStats HMStats;
	private Rank rank;
	private long level, experience;
	private Inventory inv;
	private transient List<IProfileListener> listeners;
	private int stamina;
    private long lastDaily;
    private TicTacToeStats ticTacToeStats;
    private transient GameReference currentGame;
    
    public ProfileData(User user) {
        this(user.getId());
    }
    
    public ProfileData(String userId) {
        this.userId = userId;
        this.HMStats = new HMStats();
        this.rank = Rank.ROOKIE;
        this.level = 0;
        this.experience = 0;
        this.customHex = null;
        this.inv = new Inventory();
        this.listeners = new ArrayList<>();
        this.stamina = 100;
        this.lastDaily = 0;
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
    
    public TicTacToeStats getTicTacToeStats() {
        if (ticTacToeStats == null)
            ticTacToeStats = new TicTacToeStats();
        return ticTacToeStats;
    }
    
    public boolean takeStamina(int stamina) {
        if (stamina > this.stamina)
			return false;
		this.stamina -= stamina;
		if (this.stamina < 0)
			this.stamina = 0;
		return true;
	}
	
	public int getStamina() {
		return stamina;
	}
	
	public void setStamina(int stamina) {
		this.stamina = stamina;
	}
	
	public boolean hasBankAccount() {
		return bankAccount != null;
	}
	
	public BankAccount getBankAccount() {
		if (bankAccount == null) bankAccount = new BankAccount(userId);
		return bankAccount;
	}
	
	public String getCustomHex() {
		return customHex;
	}
    
    public ProfileData.HMStats getHMStats() {
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
	
	public void setLevel(long level) {
		this.level = level;
	}
	
	public long getExperience() {
		return experience;
	}
	
	private void setExperience(long experience) {
		this.experience = experience;
	}
	
	public Inventory getInventory() {
		if (inv == null) inv = new Inventory();
		return inv;
	}
	
	public User getUser(JDA jda) {
		return jda.getUserById(userId);
	}
	
	public void addExperience(long experience) {
		if (getExperience() <= 0 && getLevel() <= 0 && experience < 0) {
			return;
		}
		setExperience(this.experience + experience);
		if (getExperience() >= expForNextLevel(getLevel())) {
            setExperience(getExperience() - ProfileData.expForNextLevel(getLevel()));
            setLevel(getLevel() + 1);
			getRegisteredListeners().forEach(listener -> listener.onLevelUp(this));
		} else if (getExperience() < 0 && getLevel() > 0) {
            setExperience(ProfileData.expForNextLevel(getLevel() - 1) + getExperience());
            setLevel(getLevel() - 1);
			getRegisteredListeners().forEach(listener -> listener.onLevelDown(this));
		}
	}
    
    public long getLastDaily() {
        return lastDaily;
    }
    
    public void setLastDaily(long lastDaily) {
        this.lastDaily = lastDaily;
    }
    
    public void reset() {
        this.HMStats = new HMStats();
		this.rank = Rank.ROOKIE;
		this.level = 0;
		this.level = 0;
		this.inv = new Inventory();
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
		if (listeners == null)
			listeners = new ArrayList<>();
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
    
    public GameReference getCurrentGame() {
        return currentGame;
    }
    
    public void setCurrentGame(GameReference currentGame) {
        this.currentGame = currentGame;
    }
    
    public MessageEmbed createEmbed(JDA jda) {
        EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor(getUser(jda).getName() + "'s profile information", null, Utils.getAvatarUrl(getUser(jda)));
		builder.setDescription(EMPTY + "\n" + EMPTY);
		builder.addField("\uD83D\uDEB6 Stamina", getStamina() + " [`" + StringUtils.getProgressBar(getStamina()) + "`]", false);
		builder.addField("\u2694 Level", String.valueOf(getLevel()), true);
		builder.addField("\uD83C\uDF1F Experience", String.valueOf(getExperience()), true);
		builder.addField("\u2b50 Experience to Next Level", String.valueOf(expForNextLevel(getLevel())), true);
		builder.addField("\uD83D\uDCB8 Coins", String.valueOf(getBankAccount().getCoins()), true);
		builder.addField("\uD83D\uDCBC Inventory", String.valueOf(getInventory().size(false)), true);
		builder.addField("\uD83C\uDF96 Rank", getRank().toString(), true).addBlankField(true).addField("\uD83C\uDFAE Game Stats", EMPTY, true).addBlankField(true);
        builder.addField("<:hangman:295383212207767553> Hang Man", "\u00AD", true);
        builder.addField("Victories", String.valueOf(getHMStats().getVictories()), true);
        builder.addField("Defeats", String.valueOf(getHMStats().getDefeats()), true);
        builder.addField("<:tictactoe:295382903716839434> Tic Tac Toe", "\u00AD", true);
        builder.addField("Victories", String.valueOf(getTicTacToeStats().getVictories()), true);
        builder.addField("Defeats", String.valueOf(getTicTacToeStats().getDefeats()), true);
        //builder.addField("\uD83D\uDD79 Game", "<:hangman:295383212207767553> HangMan\n", true).addField("\uD83C\uDFC6 Victories", String.valueOf(getHMStats().getVictories()) + "\n" + getTicTacToeStats().getVictories(), true).addField("☠ Defeats", String.valueOf(getHMStats().getDefeats()) + "\n" + getTicTacToeStats().getDefeats(), true);
        builder.setColor(this.getEffectiveColor());
		return builder.build();
	}
	
	public enum Rank {
		ROOKIE(0, "#5838D6", 0),
		BEGINNER(5, "#38C9D6", 10),
		TALENTED(10, "#8438D6", 50),
		SKILLED(20, "#4EE07D", 100),
		INTERMEDIATE(35, "#93DA38", 1000),
		SKILLFUL(40, "#C0DA38", 1500),
		EXPERIENCED(50, "#DCF80C", 6000),
		ADVANCED(70, "#F8C90C", 10000),
		SENIOR(85, "#FFAD00", 15000),
		EXPERT(100, "#DB2121", 100000);
		private static Rank[] vals = values();
		private final int level;
		private final String hex;
		private long cost;
		
		Rank(int i, String hex, long cost) {
			this.level = i;
			this.hex = hex;
			this.cost = cost;
		}
		
		public int getLevel() {
			return level;
		}
		
		public Color getColor() {
			return Color.decode(hex);
		}
		
		public long getCost() {
			return cost;
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
    
    public static class TicTacToeStats {
        
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

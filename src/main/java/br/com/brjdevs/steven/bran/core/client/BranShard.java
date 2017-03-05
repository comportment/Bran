package br.com.brjdevs.steven.bran.core.client;

import br.com.brjdevs.steven.bran.core.utils.Utils;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;

public class BranShard {
	
	private final Bran bran;
	private final BranEventManager eventManager;
	private int shardId;
	private int totalShards;
	private volatile JDA jda;
	private long startup;
	private long lastReboot;
	private int currentGuildCount;
	
	public BranShard(int shardId, int totalShards, Bran bran) throws LoginException, InterruptedException, RateLimitedException {
		this.shardId = shardId;
		this.totalShards = totalShards;
		this.bran = bran;
		this.startup = 0;
		this.lastReboot = 0;
		this.eventManager = new BranEventManager(this);
		restartJDA();
	}
	
	public Bran getBran() {
		return bran;
	}
	
	public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(bran.getConfig().botToken);
		Game game = null;
		if (!Utils.isEmpty(bran.getConfig().defaultGame))
			game = bran.getConfig().gameStream ? Game.of(bran.getConfig().defaultGame, "https://twitch.tv/ ") : Game.of(bran.getConfig().defaultGame);
		if (totalShards > 1) {
			jdaBuilder.useSharding(shardId, totalShards);
			if (game != null)
				game = Game.of(game.getName() + " | [" + shardId + "]", game.getUrl());
		}
		jdaBuilder.setGame(game);
		jdaBuilder.setEventManager(eventManager);
		jdaBuilder.setBulkDeleteSplittingEnabled(false);
		jdaBuilder.setAutoReconnect(true);
		jda = jdaBuilder.buildBlocking();
		if (startup == 0)
			this.startup = System.currentTimeMillis();
		this.lastReboot = System.currentTimeMillis();
		this.currentGuildCount = jda.getGuilds().size();
	}
	
	public JDA getJDA() {
		return jda;
	}
	
	public int getId() {
		return shardId;
	}
	
	public long getLastReboot() {
		return lastReboot;
	}
	
	public long getStartup() {
		return startup;
	}
	
	public BranEventManager getEventManager() {
		return eventManager;
	}
	
	public void shutdown() {
		jda.getGuilds().forEach(g -> g.getAudioManager().closeAudioConnection());
		jda.shutdownNow(true);
		eventManager.executor.shutdown();
	}
	
	public String getInfo() {
		return "Hello, my name is " + getJDA().getSelfUser().getName() + "! I am a Discord Bot powered by JDA by DV8FromTheWorld#6297 and I was created by " + Utils.getUser(bran.getOwner()) + ". If you want me in your server *(how could you not?)* type `" + bran.getConfig().defaultPrefixes.get(0) + "bot inviteme`, and if you require support you can use that command too, it'll show you my guild invite, join it and ask my owner your question! Oh, and if you want a full list of my commands you can type `" + bran.getConfig().defaultPrefixes.get(0) + "help`. This is shard #" + shardId + " of " + totalShards + ", have Fun! :smile:";
	}
	
	public void updateCurrentGuildCount() {
		currentGuildCount = jda.getGuilds().size();
	}
	
	public int getCurrentGuildCount() {
		return currentGuildCount;
	}
	
	public void updateStats() throws UnirestException {
		JSONObject data = new JSONObject();
		data.put("server_count", jda.getGuilds().size());
		if (totalShards > 1) {
			data.put("shard_id", shardId);
			data.put("shard_count", totalShards);
		}
		try {
			Unirest.post("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
					.header("Authorization", bran.getConfig().discordBotsToken)
					.header("Content-Type", "application/json")
					.body(data.toString())
					.asJson();
		} catch (Exception e) {
			throw new UnirestException("Could not update server count at DiscordBots.pw");
		}
		try {
			Unirest.post("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
					.header("Authorization", bran.getConfig().discordBotsOrgToken)
					.header("Content-Type", "application/json")
					.body(data.toString())
					.asJson();
		} catch (Exception e) {
			throw new UnirestException("Could not update server cound at Discord Bots.org");
		}
	}
}

package br.net.brjdevs.steven.bran.core.client;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;

public class Client {
    
    private final BranEventManager eventManager;
	private int shardId;
	private int totalShards;
	private volatile JDA jda;
	private long startup;
	private long lastReboot;
	private int currentGuildCount;
    
    public Client(int shardId, int totalShards) throws LoginException, InterruptedException, RateLimitedException {
        this.shardId = shardId;
        this.totalShards = totalShards;
        this.startup = 0;
        this.lastReboot = 0;
        this.eventManager = new BranEventManager(this);
        restartJDA();
        //TaskManager.startAsyncTask("Shard " + shardId + " Guard", (service) -> {
        //if (Bran.getInstance().getLastEvents().get(shardId) + 15000 < System.currentTimeMillis()) {
        //Bran.getInstance().reboot(this);
        //}
        //}, 1);
    }
    
    public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(Bran.getInstance().getConfig().botToken);
        if (totalShards > 1) {
			jdaBuilder.useSharding(shardId, totalShards);
		}
		jdaBuilder.setEventManager(eventManager);
		jdaBuilder.setBulkDeleteSplittingEnabled(false);
		jdaBuilder.setAutoReconnect(true);
        jdaBuilder.setGame(Game.of("waking up..."));
        jdaBuilder.setStatus(OnlineStatus.IDLE);
        jdaBuilder.setCorePoolSize(5);
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
		jda.shutdown(true);
		eventManager.executor.shutdown();
	}
	
	public String getInfo() {
		return "Hello, I'm " + getJDA().getSelfUser().getName() + "! I am here to help and entertain your server! Here a little of what I can do:" +
				"```diff\n" +
				"+ Moderation tools (ban, kick, softban, prune, permissions, word filter, self role)\n" +
				"+ Useful commands (weather, urban, math, giveaways, guild and user infos, polls)\n" +
				"+ Fun commands (eight ball, choose, custom commands, random comics, yoda speak)\n" +
				"+ Currency system (BETA)" +
				"```";
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
                    .header("Authorization", Bran.getInstance().getConfig().discordBotsToken)
                    .header("Content-Type", "application/json")
					.body(data.toString())
					.asJson();
		} catch (Exception e) {
			throw new UnirestException("Could not update server count at DiscordBots.pw");
		}
		try {
			Unirest.post("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", Bran.getInstance().getConfig().discordBotsOrgToken)
                    .header("Content-Type", "application/json")
					.body(data.toString())
					.asJson();
		} catch (Exception e) {
			throw new UnirestException("Could not update server cound at Discord Bots.org");
		}
	}
}

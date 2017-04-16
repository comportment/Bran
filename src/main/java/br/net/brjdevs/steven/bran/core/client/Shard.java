package br.net.brjdevs.steven.bran.core.client;

import br.net.brjdevs.steven.bran.core.data.Config;
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

public class Shard {
    
    private final EventManager eventManager;
	private int shardId;
	private int totalShards;
	private volatile JDA jda;
	private long startup;
	private long lastReboot;
    
    public Shard(int shardId, int totalShards) throws LoginException, InterruptedException, RateLimitedException {
        this.shardId = shardId;
        this.totalShards = totalShards;
        this.startup = 0;
        this.lastReboot = 0;
        this.eventManager = new EventManager(this);
    }
    
    public void restartJDA(Config config) throws LoginException, InterruptedException, RateLimitedException {
		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(config.botToken);
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
	
	public EventManager getEventManager() {
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
}

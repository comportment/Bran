package br.com.brjdevs.steven.bran.core.client;

import br.com.brjdevs.steven.bran.core.utils.Utils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.util.List;

public class ClientShard {
	
	private final Client client;
	private final ClientEventManager eventManager;
	private int shardId;
	private int totalShards;
	private volatile JDA jda;
	private long startup;
	private long lastReboot;
	private int currentGuildCount;
	
	public ClientShard(int shardId, int totalShards, Client client) throws LoginException, InterruptedException, RateLimitedException {
		this.shardId = shardId;
		this.totalShards = totalShards;
		this.client = client;
		this.startup = 0;
		this.lastReboot = 0;
		this.eventManager = new ClientEventManager(this);
		restartJDA();
	}
	
	public Client getClient() {
		return client;
	}
	
	public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(client.getConfig().botToken);
		Game game = null;
		if (!Utils.isEmpty(client.getConfig().defaultGame))
			game = client.getConfig().gameStream ? Game.of(client.getConfig().defaultGame, "https://twitch.tv/ ") : Game.of(client.getConfig().defaultGame);
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
	
	public ClientEventManager getEventManager() {
		return eventManager;
	}
	
	public void shutdown() {
		jda.getGuilds().forEach(g -> g.getAudioManager().closeAudioConnection());
		jda.shutdownNow(true);
		eventManager.executor.shutdown();
	}
	
	public String getInfo() {
		return "Hello, my name is " + getJDA().getSelfUser().getName() + "! I am a Discord Bot powered by JDA by DV8FromTheWorld#6297 and I was created by " + Utils.getUser(client.getOwner()) + ". If you want me in your server *(how could you not?)* type `" + client.getConfig().defaultPrefixes.get(0) + "bot inviteme`, and if you require support you can use that command too, it'll show you my guild invite, join it and ask my owner your question! Oh, and if you want a full list of my commands you can type `" + client.getConfig().defaultPrefixes.get(0) + "help`. This is shard #" + shardId + " of " + totalShards + ", have Fun! :smile:";
	}
	
	public void updateCurrentGuildCount() {
		currentGuildCount = jda.getGuilds().size();
	}
	
	public int getCurrentGuildCount() {
		return currentGuildCount;
	}
	
	public int pruneGuilds(int lastMessage) {
		int leftGuilds = 0;
		for (Guild guild : jda.getGuilds()) {
			int i = 0;
			for (TextChannel channel : guild.getTextChannels()) {
				List<Message> messages = channel.getHistory().retrievePast(2).complete();
				if (!messages.isEmpty() && messages.get(0).getCreationTime().minusDays(lastMessage).getDayOfMonth() > 0
						|| messages.isEmpty() && channel.getCreationTime().minusDays(1).getDayOfMonth() > 0) {
					i++;
				}
			}
			if (i == guild.getTextChannels().size()) {
				//guild.leave().queue();
				jda.getTextChannelById("243337584057647104").sendMessage(guild.getName() + " - " + i).queue();
				leftGuilds++;
			}
		}
		return leftGuilds;
	}
	
	public HttpResponse<JsonNode> updateStats() throws UnirestException {
		JSONObject data = new JSONObject();
		data.put("server_count", jda.getGuilds().size());
		if (totalShards > 1) {
			data.put("shard_id", shardId);
			data.put("shard_count", totalShards);
		}
		return Unirest.post("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
				.header("Authorization", client.getConfig().discordBotsToken)
				.header("Content-Type", "application/json")
				.body(data.toString())
				.asJson();
	}
}
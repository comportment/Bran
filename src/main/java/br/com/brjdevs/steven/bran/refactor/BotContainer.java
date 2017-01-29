package br.com.brjdevs.steven.bran.refactor;

import br.com.brjdevs.steven.bran.core.audio.MusicPersistence;
import br.com.brjdevs.steven.bran.core.audio.MusicPlayerManager;
import br.com.brjdevs.steven.bran.core.command.CommandManager;
import br.com.brjdevs.steven.bran.core.data.DataManager;
import br.com.brjdevs.steven.bran.core.data.bot.BotData;
import br.com.brjdevs.steven.bran.core.data.bot.Config;
import br.com.brjdevs.steven.bran.core.managers.TaskManager;
import br.com.brjdevs.steven.bran.core.poll.PollPersistence;
import br.com.brjdevs.steven.bran.core.utils.Session;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.Stream;

public class BotContainer {
	
	private static SimpleLog LOG = SimpleLog.getLog("BotContainer");
	public Config config;
	public BotData data;
	public File workingDir;
	public MusicPersistence musicPersistence;
	public PollPersistence pollPersistence;
	public TaskManager taskManager;
	public DataManager dataManager;
	public CommandManager commandManager;
	public MusicPlayerManager musicPlayerManager;
	private Bot[] shards;
	private DiscordLog discordLog;
	private int totalShards;
	private AtomicLongArray lastEvents;
	private volatile User owner;
	private Session session;
	
	public BotContainer() throws LoginException, InterruptedException, RateLimitedException {
		this.workingDir = new File(System.getProperty("user.dir") + "/data/");
		if (!workingDir.exists() && !workingDir.mkdirs())
			throw new NullPointerException("Could not create config.json");
		this.config = Config.newConfig(this);
		this.data = new BotData(this);
		this.totalShards = getRecommendedShards();
		this.lastEvents = new AtomicLongArray(totalShards);
		this.shards = new Bot[totalShards];
		initShards();
		getOwner();
		this.discordLog = new DiscordLog(this);
		this.session = new Session(this);
		this.musicPersistence = new MusicPersistence(this);
		this.pollPersistence = new PollPersistence(this);
		this.dataManager = new DataManager(this);
		this.commandManager = new CommandManager(this);
		this.musicPlayerManager = new MusicPlayerManager(this);
		this.taskManager = new TaskManager(this);
	}
	
	public Bot[] getShards() {
		return shards;
	}
	
	public int getShardId(JDA jda) {
		if (jda.getShardInfo() == null) return 0;
		return jda.getShardInfo().getShardId();
	}
	
	public int getTotalShards() {
		return totalShards;
	}
	
	public DiscordLog getDiscordLog() {
		return discordLog;
	}
	
	public void setLastEvent(int shardId, long time) {
		lastEvents.set(shardId, time);
	}
	
	public Session getSession() {
		return session;
	}
	
	public List<Guild> getGuilds() {
		List<Guild> guilds = new ArrayList<>();
		for (Bot shard : shards) {
			guilds.addAll(shard.getJDA().getGuilds());
		}
		return guilds;
	}
	
	public List<User> getUsers() {
		List<User> users = new ArrayList<>();
		for (Bot shard : shards) {
			users.addAll(shard.getJDA().getUsers());
		}
		return users;
	}
	
	public List<TextChannel> getTextChannels() {
		List<TextChannel> channels = new ArrayList<>();
		for (Bot shard : shards) {
			channels.addAll(shard.getJDA().getTextChannels());
		}
		return channels;
	}
	
	public List<VoiceChannel> getVoiceChannels() {
		List<VoiceChannel> channels = new ArrayList<>();
		for (Bot shard : shards) {
			channels.addAll(shard.getJDA().getVoiceChannels());
		}
		return channels;
	}
	
	private int getRecommendedShards() {
		try {
			HttpResponse<JsonNode> shards = Unirest.get("https://discordapp.com/api/gateway/bot")
					.header("Authorization", "Bot " + config.getToken())
					.header("Content-Type", "application/json")
					.asJson();
			return shards.getBody().getObject().getInt("shards");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	private void initShards() throws LoginException, InterruptedException, RateLimitedException {
		for (int i = 0; i < shards.length; i++) {
			LOG.info("Starting shard #" + i + " of " + shards.length);
			shards[i] = new Bot(i, totalShards, this);
			LOG.info("Finished shard #" + i);
			Thread.sleep(5_000L);
		}
		for (Bot shard : shards) {
			setLastEvent(shard.getId(), System.currentTimeMillis());
		}
	}
	
	public User getOwner() {
		if (owner != null) return owner;
		for (Bot shard : shards) {
			owner = shard.getJDA().getUserById(config.getOwnerId());
			if (owner != null) break;
		}
		if (owner == null) LOG.fatal("Could not find Owner.");
		return owner;
	}
	
	private void loadPersistences() {
		musicPersistence.reloadPlaylists();
		pollPersistence.reloadPolls();
	}
	
	public void shutdownAll(int exitCode) {
		
		if (!musicPersistence.savePlaylists()) LOG.info("Could not complete MusicPersistence savePlaylists.");
		if (!pollPersistence.savePolls()) LOG.info("Could not complete PollPersistence savePolls.");
		
		dataManager.saveData();
		
		Stream.of(shards).forEach(Bot::shutdown);
		
		System.exit(exitCode);
	}
}

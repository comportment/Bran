package br.net.brjdevs.steven.bran.core.client;

import br.net.brjdevs.steven.bran.Version;
import br.net.brjdevs.steven.bran.core.audio.AudioUtils;
import br.net.brjdevs.steven.bran.core.audio.BranMusicManager;
import br.net.brjdevs.steven.bran.core.audio.GuildMusicManager;
import br.net.brjdevs.steven.bran.core.command.CommandManager;
import br.net.brjdevs.steven.bran.core.currency.ProfileData;
import br.net.brjdevs.steven.bran.core.data.DataManager;
import br.net.brjdevs.steven.bran.core.data.Config;
import br.net.brjdevs.steven.bran.core.managers.Messenger;
import br.net.brjdevs.steven.bran.core.managers.TaskManager;
import br.net.brjdevs.steven.bran.core.redis.RedisDatabase;
import br.net.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import br.net.brjdevs.steven.bran.core.utils.Session;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import br.net.brjdevs.steven.bran.log.DiscordLogBack;
import br.net.brjdevs.steven.bran.log.SimpleLogToSLF4JAdapter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bran {
	
	public static Color COLOR = Color.decode("#388BDF");
	private static Bran instance;
    private static Logger LOGGER = LoggerFactory.getLogger("Bran");
	public File workingDir;
	private TaskManager taskManager;
	private CommandManager commandManager;
	private BranMusicManager playerManager;
	private DataManager discordBotData;
    private Shard[] shards;
	private int totalShards;
	private AtomicLongArray lastEvents;
	private long ownerId;
	private int ownerShardId;
	private Session session;
    private long sessionId;
    private boolean isSetup;
    
    public void init() throws LoginException, InterruptedException, RateLimitedException {
    	this.isSetup = false;
        this.discordBotData = new DataManager();
		SQLDatabase.start(getConfig().dbPwd);
		this.ownerId = 0;
		this.ownerShardId = 0;
		this.workingDir = new File(System.getProperty("user.dir") + "/data/");
		if (!workingDir.exists() && !workingDir.mkdirs())
			throw new NullPointerException("Could not create config.json");
		this.totalShards = getRecommendedShards();
		this.lastEvents = new AtomicLongArray(totalShards);
        this.shards = new Shard[totalShards];
        initShards();
		getOwner();
		this.playerManager = new BranMusicManager();
		this.commandManager = new CommandManager();
		this.session = new Session();
		this.taskManager = new TaskManager();
        this.sessionId = new SnowflakeGenerator(3, 1).nextId();
        this.isSetup = true;
    }

	public boolean isSetup() {
		return isSetup;
	}
	public static Bran getInstance() {
		return instance;
	}
    
    public long getSessionId() {
        return sessionId;
    }
    
    public Shard[] getShards() {
        return shards;
	}
	
	public DataManager getDataManager() {
		return discordBotData;
	}
	
	public int getShardId(JDA jda) {
		if (jda.getShardInfo() == null) return 0;
		return jda.getShardInfo().getShardId();
	}
    
    public Shard getShard(JDA jda) {
        return getShards()[getShardId(jda)];
	}
    
    public ProfileData getProfile(User user) {
        return discordBotData.getData().get().getUserData(user).getProfileData();
    }

    public TextChannel getTextChannelById(String id) {
		return Arrays.stream(shards).map(shard -> shard.getJDA().getTextChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
	}
	
	public int getTotalShards() {
		return totalShards;
	}
    
    public Shard[] getOnlineShards() {
        return Arrays.stream(shards).filter(s -> s.getJDA().getStatus() == Status.CONNECTED).toArray(Shard[]::new);
    }
	
	public void setLastEvent(int shardId, long time) {
		lastEvents.set(shardId, time);
	}
	
	public Session getSession() {
		return session;
	}
	
	public Config getConfig() {
        return getDataManager().getConfig().get();
    }
	
	public List<Guild> getGuilds() {
		return Arrays.stream(shards).map(bot -> bot.getJDA().getGuilds()).flatMap(List::stream).collect(Collectors.toList());
	}
	
	public List<User> getUsers() {
		return Arrays.stream(shards).map(bot -> bot.getJDA().getUsers()).flatMap(List::stream).collect(Collectors.toList());
	}
	
	public User getUserById(String id) {
		return Arrays.stream(shards).map(shard -> shard.getJDA().getUserById(id)).filter(Objects::nonNull).findFirst().orElse(null);
	}
	
	public List<TextChannel> getTextChannels() {
		return Arrays.stream(shards).map(bot -> bot.getJDA().getTextChannels()).flatMap(List::stream).collect(Collectors.toList());
	}
	
	public List<VoiceChannel> getVoiceChannels() {
		return Arrays.stream(shards).map(bot -> bot.getJDA().getVoiceChannels()).flatMap(List::stream).collect(Collectors.toList());
	}
	
	public long getResponseTotal() {
		return Arrays.stream(shards).map(bot -> bot.getJDA().getResponseTotal()).mapToLong(Long::longValue).sum();
	}
	
	public AtomicLongArray getLastEvents() {
		return lastEvents;
	}
	
	public TaskManager getTaskManager() {
		return taskManager;
	}
    
    public synchronized boolean reboot(Shard shard) {
        try {
			Map<Long, ImmutablePair<Long, GuildMusicManager>> shardPlayers = new HashMap<>();
			Map<Long, GuildMusicManager> copy = new HashMap<>(playerManager.getMusicManagers());
			copy.forEach((guildId, musicManager) -> {
                try {
                    Guild guild = shard.getJDA().getGuildById(String.valueOf(guildId));
                    if (guild != null) {
                        if (guild.getAudioManager().getConnectedChannel() != null) {
                            shardPlayers.put(guildId, new ImmutablePair<>(Long.parseLong(guild.getAudioManager().getConnectedChannel().getId()), musicManager));
                            musicManager.getTrackScheduler().setPaused(true);
                            playerManager.unregister(guildId);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
			shard.getJDA().shutdown(false);
			Utils.sleep(5000);
			shard.restartJDA(getConfig());
			shardPlayers.forEach((id, pair) -> {
				VoiceChannel channel = shard.getJDA().getVoiceChannelById(String.valueOf(pair.left));
				GuildMusicManager musicManager = pair.right;
				if (channel == null) return;
				channel.getGuild().getAudioManager().setSendingHandler(musicManager.getSendHandler());
				AudioUtils.connect(channel, musicManager.getTrackScheduler().getCurrentTrack().getContext());
				playerManager.getMusicManagers().put(id, musicManager);
				musicManager.getTrackScheduler().setPaused(false);
				
			});
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public BranMusicManager getMusicManager() {
		return playerManager;
	}
	
	private int getRecommendedShards() {
		try {
			HttpResponse<JsonNode> shards = Unirest.get("https://discordapp.com/api/gateway/bot")
					.header("Authorization", "Bot " + getConfig().botToken)
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
			LOGGER.info("Starting shard #" + i + " of " + shards.length);
            Shard s = new Shard(i, totalShards);
            s.restartJDA(getConfig());
            shards[i] = s;
            LOGGER.info("Finished shard #" + i);
			Thread.sleep(5_000L);
		}
        for (Shard shard : shards) {
            setLastEvent(shard.getId(), System.currentTimeMillis());
		}
	}
	
	public User getOwner() {
		if (ownerId != 0) return getShards()[ownerShardId].getJDA().getUserById(ownerId);
        for (Shard shard : shards) {
            User u = shard.getJDA().getUserById(getConfig().ownerId);
			if (u != null) {
				ownerId = Long.parseLong(u.getId());
				break;
			}
		}
		if (ownerId == 0) LOGGER.error("Could not find owner id " + ownerId);
		return getShards()[ownerShardId].getJDA().getUserById(ownerId);
	}
	
	public int getShardId(long discordGuildId) {
		return (int) ((discordGuildId >> 22) % totalShards);
	}
	
	public void shutdownAll(int exitCode) {
        
        Stream.of(shards).forEach(shard ->
                shard.getEventManager().getRegisteredListeners().clear()
        );
        
        new HashMap<>(playerManager.getMusicManagers()).forEach((guildId, musicManager) -> {
            try {
				if (musicManager.getTrackScheduler().getCurrentTrack() == null) return;
				TextChannel channel = musicManager.getTrackScheduler().getCurrentTrack().getContext();
				if (channel != null && channel.canTalk())
                    channel.sendMessage("Hey, I'm sorry to bother you but I need to restart. I'll be back bigger, stronger and better.").complete();
            } catch (Exception ignored) {
			}
		});
        
        getDataManager().getPolls().update();
        getDataManager().getData().update();
        getDataManager().getConfig().update();
        getDataManager().getHangmanWords().update();
        
        Stream.of(shards).forEach(Shard::shutdown);
        
        System.exit(exitCode);
	}

	public static void main(String[] args) {
    	try {
			RedisDatabase.getDB();
			SimpleLogToSLF4JAdapter.install();
			instance = new Bran();
			instance.init();
			DiscordLogBack.enable();

			/*TaskManager.startAsyncTask("DiscordBots Thread", (service) -> Arrays.stream(Bran.getInstance().getShards()).forEach(shard -> {
				try {
					JSONObject data = new JSONObject();
					data.put("server_count", shard.getJDA().getGuilds().size());
					if (instance.getTotalShards() > 1) {
						data.put("shard_id", shard.getId());
						data.put("shard_count", instance.getTotalShards());
					}
					try {
						Unirest.post("https://bots.discord.pw/api/bots/" + shard.getJDA().getSelfUser().getId() + "/stats")
								.header("Authorization", Bran.getInstance().getConfig().discordBotsToken)
								.header("Content-Type", "application/json")
								.body(data.toString())
								.asJson();
					} catch (Exception e) {
						throw new UnirestException("Could not update server count at DiscordBots.pw");
					}
					try {
						Unirest.post("https://discordbots.org/api/bots/" + shard.getJDA().getSelfUser().getId() + "/stats")
								.header("Authorization", Bran.getInstance().getConfig().discordBotsOrgToken)
								.header("Content-Type", "application/json")
								.body(data.toString())
								.asJson();
					} catch (Exception e) {
						throw new UnirestException("Could not update server cound at Discord Bots.org");
					}
					LOGGER.info("Successfully updated shard " + shard.getId() + " server_count!");
				} catch (UnirestException e) {
					LOGGER.error("Failed to update shard " + shard.getId() + " server_count!", e);
				}
			}), 3600);*/

			LOGGER.info("Started Bran instance at version " + Version.VERSION);
		} catch (Exception e) {
    		LOGGER.error("Failed to start Bran instance!", e);
		}
	}
}

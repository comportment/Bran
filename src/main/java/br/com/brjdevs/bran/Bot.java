package br.com.brjdevs.bran;

import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.data.DataManager;
import br.com.brjdevs.bran.core.data.bot.BotData;
import br.com.brjdevs.bran.core.data.bot.Config;
import br.com.brjdevs.bran.core.managers.TaskManager;
import br.com.brjdevs.bran.core.utils.Session;
import br.com.brjdevs.bran.core.utils.Util;
import br.com.brjdevs.bran.jdaLoader.JDALoader;
import br.com.brjdevs.bran.jdaLoader.LoaderType;
import br.com.brjdevs.bran.jdaLoader.impl.JDALoaderImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.SneakyThrows;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot implements EventListener {
    public static final int MAX_PREFIXES = 5;
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	private static final Bot instance = new Bot();
	private static final File CONFIG_FILE = new File(getInstance().getWorkingDirectory() + "config.json");
	private static final String abal = "https://bots.discord.pw/api/bots/{0}/stats";
	public static SimpleLog LOG = SimpleLog.getLog("Application");
	public static TextChannel LOG_CHANNEL;
	private static Map<Integer, JDA> shards = new HashMap<>();
	private final Session session = new Session();
	private User OWNER = null;
    private Config CONFIG = new Config();
    private String WORKING_DIR = System.getProperty("user.dir") + "/data/";
    private BotData DATA = new BotData();
    private String[] PREFIXES = {"!!", "."};
    private long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) {
	    try {
		    File DIR = new File(getInstance().WORKING_DIR);
		    if (!DIR.exists() && !DIR.mkdirs()) {
			    LOG.warn("Couldn't create expected directory to save files, please create a folder named 'data' in the current directory to make sure it won't happen again.");
			    System.exit(0);
		    }
		    getInstance().setConfig();
		    getInstance().build();
		    LOG_CHANNEL = shards.entrySet().stream().filter(entry -> entry.getValue().getTextChannelById("249971874430320660") != null).findFirst().get().getValue().getTextChannelById("249971874430320660");
		    LOG.info("Logged in as " + Util.getUser(getInstance().getSelfUser(shards.get(0))));
		    getInstance().OWNER = shards.entrySet().stream().filter(entry -> entry.getValue().getUserById(getInstance().getConfig().getOwnerId()) != null).findFirst().get().getValue().getUserById(getInstance().getConfig().getOwnerId());
		    if (getInstance().OWNER == null) {
			    LOG.fatal("Could not find user with ID '" + getInstance().getConfig().getOwnerId() + "', please set a valid ID in config.json.");
			    System.exit(0);
		    }
		    LOG.info("Recognized owner as " + Util.getUser(getInstance().OWNER));
		    CommandManager.load();
		    LOG.info("Registered Commands.");
		    DataManager.loadData();
		    LOG.info("Loaded Bot and Guild data.");
		    TaskManager.startAsyncTasks();
		    LOG.info("Started async tasks.");
		    LOG.info("Finished loading. Time taken: " + getInstance().getSession().getUptime());
	    } catch (LoginException e) {
		    LOG.fatal("The set Token is invalid, please insert a valid token!");
		    System.exit(0);
	    } catch (Exception e) {
		    LOG.log(e);
		    System.exit(0);
	    }
	    System.out.println("-------------------------");
    }
	
	public static Bot getInstance() {
		return instance;
	}
	
	public User getOwner() {
        return OWNER;
    }
	
	public SelfUser getSelfUser(JDA jda) {
        return jda.getSelfUser();
    }
	
	public Config getConfig() {
        return CONFIG;
    }
	
	public String getWorkingDirectory() {
        return WORKING_DIR;
    }
	
	public BotData getData() {
        return DATA;
    }
	
	public void setData(BotData botData) {
		DATA = botData;
	}
	
	public long getStartTime() {
        return START_TIME;
    }
	
	public Map<Integer, JDA> getShards() {
        return shards;
    }
	
	public int getShardId(JDA jda) {
    	return isSharded() ? jda.getShardInfo().getShardId() : 0;
    }
	
	public boolean isSharded() {
        return shards.size() > 1;
    }
	
	public JDA getShard(int id) {
		if (id > shards.size()) return null;
		return shards.entrySet().stream().filter(entry -> entry.getKey() == id).findFirst().orElse(null).getValue();
    }
	
	public List<Guild> getGuilds() {
        List<Guild> guilds = new ArrayList<>();
        shards.forEach((i, shard) -> guilds.addAll(shard.getGuilds()));
        return guilds;
    }
	
	public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        shards.forEach((i, shard) -> users.addAll(shard.getUsers()));
        return users;
    }
	
	public List<TextChannel> getTextChannels() {
        List<TextChannel> channels = new ArrayList<>();
        shards.forEach((i, shard) -> channels.addAll(shard.getTextChannels()));
        return channels;
    }
	
	public List<VoiceChannel> getVoiceChannels() {
        List<VoiceChannel> channels = new ArrayList<>();
        shards.forEach((i, shard) -> channels.addAll(shard.getVoiceChannels()));
        return channels;
    }
	
	public long getOnlineShards() {
        return shards.entrySet().stream().filter(entry -> entry.getValue().getStatus() == Status.CONNECTED).count();
    }
	
	public String getInfo() {
        return "Hello, my name is " + getSelfUser(shards.get(0)).getName() + "! I am a Discord Bot powered by JDA by DV8FromTheWorld#6297 and I was created by " + Util.getUser(OWNER) + ". If you want me in your server *(how could you not?)* type `" + getDefaultPrefixes()[0] + "bot inviteme`, and if you require support you can use that command too, it'll show you my guild invite, join it and ask my owner your question! Oh, and if you want a full list of my commands you can type `" + getDefaultPrefixes()[0] + "help`. Have Fun! :smile:";
    }
	
	public String[] getDefaultPrefixes() {
        return PREFIXES;
    }
    
    public Session getSession() {
        return session;
    }
	
	@Override
    public void onEvent (Event e) {
        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = ((MessageReceivedEvent) e);
            Bot.getInstance().getSession().readMessage(event.getAuthor().getId().equals(getSelfUser(event.getJDA()).getId()));
        }
    }
	
	public int getRequiredShards() throws IOException, UnirestException {
        HttpResponse<JsonNode> shards = Unirest.get("https://discordapp.com/api/gateway/bot")
                .header("Authorization", "Bot " + getConfig().getToken())
                .header("Content-Type", "application/json")
                .asJson();
        return new JsonParser().parse(shards.getBody().toString()).getAsJsonObject().get("shards").getAsInt();
    }
	
	public boolean updateStats() {
        if (Util.isEmpty(getConfig().getDiscordBotsToken())) {
            return false;
        }
        try {
            Unirest.post(abal.replace("{0}", getSelfUser(shards.get(0)).getId()))
                    .header("Authorization", getConfig().getDiscordBotsToken())
                    .header("Content-Type", "application/json")
                    .body(new JSONObject().put("server_count", getGuilds().size()).toString())
                    .asJson();
            return true;
        } catch (UnirestException e) {
            Bot.LOG.info("Could not update stats into DiscordBots:");
            Bot.LOG.log(e);
            return false;
        }
    }
	
	@SneakyThrows(Exception.class)
	private void setConfig() {
		if (!CONFIG_FILE.exists()) {
			LOG.warn("No config.json file was found, creating example config.json...");
			if (CONFIG_FILE.createNewFile()) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE));
				writer.write(GSON.toJson(new Config()));
				writer.close();
				LOG.warn("Please, populate the config.json file with valid properties before running the jar again.");
			} else
				LOG.fatal("Failed to generate config.json.");
			System.exit(0);
		} else {
			LOG.info("Found config.json, loading properties...");
			BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
			CONFIG = GSON.fromJson(reader, Config.class);
			reader.close();
			if (Util.isEmpty(CONFIG.getToken())) {
				LOG.fatal("The set Token is invalid, please insert a valid token!");
				System.exit(0);
			}
			if (Util.isEmpty(CONFIG.getOwnerId())) {
				LOG.fatal("The set Owner ID is invalid.");
				System.exit(0);
			}
			if (Util.isEmpty(CONFIG.getDiscordBotsToken())) {
				LOG.info("Discord Bots Token was not provided, disabled command bot.updateStats.");
			}
			LOG.info("Loaded config.json!");
		}
	}
	
	private void build() throws LoginException, RateLimitedException, UnirestException, IOException {
        int requiredShards = getInstance().getRequiredShards();
        LOG.info(requiredShards < 2 ? "Discord does not ask for sharding yet." : "Discord recommends " + requiredShards + " shards.");
        JDALoader jdaLoader = new JDALoaderImpl(requiredShards < 2 ? LoaderType.SINGLE : LoaderType.SHARDED);
        shards = jdaLoader.build(true, requiredShards);
    }
}
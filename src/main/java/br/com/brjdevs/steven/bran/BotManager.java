package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.core.audio.MusicPersistence;
import br.com.brjdevs.steven.bran.core.command.CommandManager;
import br.com.brjdevs.steven.bran.core.data.DataManager;
import br.com.brjdevs.steven.bran.core.data.bot.Config;
import br.com.brjdevs.steven.bran.core.managers.TaskManager;
import br.com.brjdevs.steven.bran.core.poll.PollPersistence;
import br.com.brjdevs.steven.bran.core.utils.Util;
import br.com.brjdevs.steven.bran.jdaLoader.JDALoader;
import br.com.brjdevs.steven.bran.jdaLoader.LoaderType;
import br.com.brjdevs.steven.bran.jdaLoader.impl.JDALoaderImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.SneakyThrows;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Objects;

public class BotManager {
	
	private static File CONFIG_FILE;
	private static SimpleLog LOG;
	private static boolean canShutdown;
	private static boolean canInit;
	
	static {
		CONFIG_FILE = new File(Bot.getInstance().getWorkingDirectory() + "config.json");
		LOG = SimpleLog.getLog("Bot Manager");
		canShutdown = false;
		canInit = false;
	}
	
	public static void preShutdown() {
		LOG.info("Initiating Pre Shutdown...");
		DataManager.saveData();
		if (!MusicPersistence.savePlaylists()) LOG.info("Could not complete MusicPersistence savePlaylists.");
		if (!PollPersistence.savePolls()) LOG.info("Could not complete PollPersistence savePolls.");
		canShutdown = true;
		LOG.info("Ready for shutdown.");
	}
	
	public static void shutdown(boolean force) {
		if (!canShutdown && !force) {
			LOG.fatal("Attempted to init Shutdown without calling savePlaylists()");
			return;
		}
		LOG.info("Shutting system down...");
		Bot.getInstance().getShards().forEach((i, shard) -> shard.shutdown());
		System.exit(0);
	}
	
	@SneakyThrows(Exception.class)
	public static void preInit() {
		LOG.info("Pre initiating system...");
		File DIR = new File(Bot.getInstance().getWorkingDirectory());
		if (!DIR.exists() && !DIR.mkdirs()) {
			LOG.warn("Couldn't create expected directory to save files, please create a folder named 'data' in the current directory to make sure it won't happen again.");
			System.exit(0);
		}
		if (!CONFIG_FILE.exists()) {
			LOG.warn("No config.json file was found, creating example config.json...");
			if (CONFIG_FILE.createNewFile()) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE));
				writer.write(Bot.GSON.toJson(new Config()));
				writer.close();
				LOG.warn("Please, populate the config.json file with valid properties before running the jar again.");
			} else
				LOG.fatal("Failed to generate config.json.");
			System.exit(0);
		} else {
			LOG.info("Found config.json, loading properties...");
			BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
			Bot.getInstance().CONFIG = Bot.GSON.fromJson(reader, Config.class);
			reader.close();
			if (Util.isEmpty(Bot.getInstance().getConfig().getToken())) {
				LOG.fatal("The set Token is invalid, please insert a valid token!");
				System.exit(0);
			}
			if (Util.isEmpty(Bot.getInstance().getConfig().getOwnerId())) {
				LOG.fatal("The set Owner ID is invalid.");
				System.exit(0);
			}
			if (Util.isEmpty(Bot.getInstance().getConfig().getDiscordBotsToken())) {
				LOG.info("Discord Bots Token was not provided, disabled command bot.updateStats.");
			}
			LOG.info("Loaded config.json!");
		}
		canInit = true;
		LOG.info("Ready for Init.");
	}
	
	public static void init() throws LoginException, RateLimitedException, UnirestException, IOException {
		if (!canInit) {
			LOG.fatal("Attempted to init without pre initiating.");
			return;
		}
		LOG.info("Initiating...");
		int requiredShards = Bot.getInstance().getRequiredShards();
		LOG.info(requiredShards < 2 ? "Discord does not ask for sharding yet." : "Discord recommends " + requiredShards + " shards.");
		JDALoader jdaLoader = new JDALoaderImpl(requiredShards < 2 ? LoaderType.SINGLE : LoaderType.SHARDED);
		
		Bot.getInstance().shards = jdaLoader.build(true, requiredShards);
		Bot.LOG_CHANNEL = Bot.getInstance().shards.entrySet().stream().map(entry -> entry.getValue().getTextChannelById("249971874430320660")).filter(Objects::nonNull).findFirst().orElse(null);
		LOG.info("Logged in as " + Util.getUser(Bot.getInstance().getSelfUser(Bot.getInstance().shards.get(0))));
		Bot.getInstance().OWNER = Bot.getInstance().shards.entrySet().stream().map(entry -> entry.getValue().getUserById(Bot.getInstance().getConfig().getOwnerId())).filter(Objects::nonNull).findFirst().orElse(null);
		if (Bot.getInstance().OWNER == null) {
			LOG.fatal("Could not find user with ID '" + Bot.getInstance().getConfig().getOwnerId() + "', please set a valid ID in config.json.");
			System.exit(0);
		}
		LOG.info("Recognized owner as " + Util.getUser(Bot.getInstance().OWNER));
		CommandManager.load();
		LOG.info("Registered Commands.");
		DataManager.loadData();
		LOG.info("Loaded Bot and Guild data.");
		TaskManager.startAsyncTasks();
		LOG.info("Started async tasks.");
		if (!MusicPersistence.reloadPlaylists())
			LOG.fatal("Executed MusicPersistence with errors.");
		else
			LOG.info("Executed MusicPersistence without errors.");
		if (!PollPersistence.reloadPolls())
			LOG.fatal("Executed PollPersistence with errors.");
		else
			LOG.info("Executed PollPersistence without errors.");
		
		LOG.info("Finished loading. Time taken: " + Bot.getInstance().getSession().getUptime());
	}
}
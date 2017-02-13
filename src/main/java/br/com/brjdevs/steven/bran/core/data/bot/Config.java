package br.com.brjdevs.steven.bran.core.data.bot;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.Main;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
	
	private static final SimpleLog LOG = SimpleLog.getLog("Config Load");
	
	private String JENKINS_USER = "";
	private String JENKINS_PASS = "";
	private String JENKINS_TOKEN = "";
	private String JENKINS_LATEST_BUILD = "http://144.217.94.249:8080/job/Bran/lastSuccessfulBuild/artifact/target/DiscordBot-1.0-SNAPSHOT.jar";
	private String BOT_TOKEN = "";
	private String OWNER_ID = "";
	private String DBOTS_TOKEN = "";
	private String MASHAPE_KEY = "";
	private String DEFAULT_GAME = "";
	private boolean STREAM = false;
	private List<String> DEFAULT_PREFIXES = new ArrayList<>();
	private boolean TOO_FAST_PROTECTION = true;
	private boolean MUSIC_PERSISTENCE = true;
	private boolean POLL_PERSISTENCE = true;
	
	public static Config newConfig(BotContainer botContainer) {
		Config config = null;
		try {
			File configFile = new File(botContainer.workingDir, "config.json");
			if (!configFile.exists()) {
				LOG.warn("No config.json file was found, creating example config.json...");
				if (configFile.createNewFile()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
					writer.write(Main.GSON.toJson(new Config()));
					writer.close();
					LOG.warn("Please, populate the config.json file with valid properties before running the jar again.");
				} else
					LOG.fatal("Failed to generate config.json.");
				System.exit(0);
			} else {
				LOG.info("Found config.json, loading properties...");
				BufferedReader reader = new BufferedReader(new FileReader(configFile));
				config = Main.GSON.fromJson(reader, Config.class);
				reader.close();
				if (Util.isEmpty(config.getToken())) {
					LOG.fatal("The set Token is invalid, please insert a valid token!");
					System.exit(0);
				}
				if (Util.isEmpty(config.getOwnerId())) {
					LOG.fatal("The set Owner ID is invalid.");
					System.exit(0);
				}
				if (Util.isEmpty(config.getDiscordBotsToken())) {
					LOG.info("Discord Bots Token was not provided, disabled command bot.updateStats.");
				}
				LOG.info("Loaded config.json!");
			}
		} catch (Exception exception) {
			LOG.fatal(exception);
		}
		return config;
	}
	
	public String getJenkinsUser() {
		return JENKINS_USER;
	}
	
	public String getJenkinsPass() {
		return JENKINS_PASS;
	}
	
	public String getJenkinsToken() {
		return JENKINS_TOKEN;
	}
	
	public String getJenkinsLatestBuild() {
		return JENKINS_LATEST_BUILD;
	}
	
	public List<String> getDefaultPrefixes() {
		return DEFAULT_PREFIXES;
	}
	
	public String getMashapeKey() {
		return MASHAPE_KEY;
	}
	
	public String getDiscordBotsToken() {
		return DBOTS_TOKEN;
	}
	
	public String getOwnerId() {
		return OWNER_ID;
	}
	
	public String getToken() {
		return BOT_TOKEN;
	}
	
	public String getGame() {
		return DEFAULT_GAME;
	}
	
	public boolean isGameStream() {
		return STREAM;
	}
	
	public boolean isTooFastEnabled() {
		return TOO_FAST_PROTECTION;
	}
	
	public boolean isMusicPersistenceEnabled() {
		return MUSIC_PERSISTENCE;
	}
	
	public boolean isPollPersistenceEnabled() {
		return POLL_PERSISTENCE;
	}
	
	public void save(BotContainer botContainer) {
		try {
			File file = new File(botContainer.workingDir, "config.json");
			if (!file.exists()) file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(Main.GSON.toJson(this));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

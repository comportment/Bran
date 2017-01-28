package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.core.data.bot.BotData;
import br.com.brjdevs.steven.bran.core.data.bot.Config;
import br.com.brjdevs.steven.bran.core.utils.Session;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot implements EventListener {
	
	public static final int MAX_PREFIXES = 5;
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	private static final String abal = "https://bots.discord.pw/api/bots/{0}/stats";
	private static final Session session = new Session();
	public static SimpleLog LOG = SimpleLog.getLog("Application");
	public static TextChannel LOG_CHANNEL;
	static User OWNER = null;
	static Config CONFIG = new Config();
	static Map<Integer, JDA> shards = new HashMap<>();
	private static String WORKING_DIR = System.getProperty("user.dir") + "/data/";
	private static BotData DATA = new BotData();
	private static String[] PREFIXES = {"!!", "."};
	private static long START_TIME = System.currentTimeMillis();
	
	public static void main(String[] args) throws Throwable {
	    try {
		    BotManager.preInit();
		    BotManager.init();
	    } catch (LoginException e) {
		    if (!e.getMessage().contains("Connection reset")) {
			    LOG.fatal("The set Token is invalid, please insert a valid token!");
		    } else {
			    LOG.fatal("Could not connect with Discord.");
		    }
		    LOG.log(e);
		    System.exit(0);
	    } catch (Exception e) {
		    LOG.log(e);
		    System.exit(0);
	    }
    }
	
	public static User getOwner() {
		return OWNER;
	}
	
	public static String[] getPrefixes() {
		return PREFIXES;
	}
	
	public static SelfUser getSelfUser(JDA jda) {
		return jda.getSelfUser();
	}
	
	public static Config getConfig() {
		return CONFIG;
	}
	
	public static String getWorkingDirectory() {
		return WORKING_DIR;
	}
	
	public static BotData getData() {
		return DATA;
	}
	
	public static void setData(BotData botData) {
		DATA = botData;
	}
	
	public static long getStartTime() {
		return START_TIME;
	}
	
	public static Map<Integer, JDA> getShards() {
		return shards;
	}
	
	public static int getShardId(JDA jda) {
		return isSharded() ? jda.getShardInfo().getShardId() : 0;
	}
	
	public static boolean isSharded() {
		return shards.size() > 1;
	}
	
	public static JDA getShard(int id) {
		if (id > shards.size()) return null;
		return shards.entrySet().stream().filter(entry -> entry.getKey() == id).findFirst().orElse(null).getValue();
    }
	
	public static List<Guild> getGuilds() {
		List<Guild> guilds = new ArrayList<>();
		shards.forEach((i, shard) -> guilds.addAll(shard.getGuilds()));
        return guilds;
    }
	
	public static List<User> getUsers() {
		List<User> users = new ArrayList<>();
		shards.forEach((i, shard) -> users.addAll(shard.getUsers()));
        return users;
    }
	
	public static List<TextChannel> getTextChannels() {
		List<TextChannel> channels = new ArrayList<>();
		shards.forEach((i, shard) -> channels.addAll(shard.getTextChannels()));
        return channels;
    }
	
	public static List<VoiceChannel> getVoiceChannels() {
		List<VoiceChannel> channels = new ArrayList<>();
		shards.forEach((i, shard) -> channels.addAll(shard.getVoiceChannels()));
        return channels;
    }
	
	public static long getOnlineShards() {
		return shards.entrySet().stream().filter(entry -> entry.getValue().getStatus() == Status.CONNECTED).count();
	}
	
	public static String getInfo() {
		return "Hello, my name is " + getSelfUser(shards.get(0)).getName() + "! I am a Discord Bot powered by JDA by DV8FromTheWorld#6297 and I was created by " + Util.getUser(OWNER) + ". If you want me in your server *(how could you not?)* type `" + getDefaultPrefixes()[0] + "bot inviteme`, and if you require support you can use that command too, it'll show you my guild invite, join it and ask my owner your question! Oh, and if you want a full list of my commands you can type `" + getDefaultPrefixes()[0] + "help`. Have Fun! :smile:";
	}
	
	public static String[] getDefaultPrefixes() {
		return PREFIXES;
	}
	
	public static Session getSession() {
		return session;
	}
	
	public static int getRequiredShards() throws IOException, UnirestException {
		HttpResponse<JsonNode> shards = Unirest.get("https://discordapp.com/api/gateway/bot")
				.header("Authorization", "Bot " + getConfig().getToken())
                .header("Content-Type", "application/json")
                .asJson();
        return new JsonParser().parse(shards.getBody().toString()).getAsJsonObject().get("shards").getAsInt();
    }
	
	public static boolean updateStats() {
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
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof MessageReceivedEvent) {
			MessageReceivedEvent event = ((MessageReceivedEvent) e);
			Bot.getSession().readMessage(event.getAuthor().getId().equals(getSelfUser(event.getJDA()).getId()));
		}
	}
}
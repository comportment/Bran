package br.com.brjdevs.steven.bran.core.poll;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;

public class PollPersistence {
	
	private static final SimpleLog LOG;
	private static final Gson GSON;
	
	static {
		LOG = SimpleLog.getLog("Poll Persistence");
		GSON = new GsonBuilder().serializeNulls().create();
	}
	
	@SneakyThrows(Exception.class)
	public static boolean savePolls() {
		if (!Bot.getInstance().getConfig().isPollPersistenceEnabled()) {
			LOG.info("Poll Persistence is disabled in config.json.");
			return true;
		}
		LOG.info("Initiating PollPersistence pre shutdown.");
		File dir = new File(System.getProperty("user.dir") + "/poll_persistence");
		if (!dir.exists()) {
			try {
				if (!dir.mkdirs()) throw new RuntimeException("Could not create dir.");
			} catch (Exception e) {
				LOG.fatal("Could not create dir.");
				LOG.log(e);
				return false;
			}
		}
		for (Poll poll : Poll.getRunningPolls()) {
			FileUtils.writeStringToFile(new File(dir, poll.getGuildId()), GSON.toJson(poll), Charset.forName("UTF-8"));
		}
		LOG.info("Finished PollPersistence pre shutdown.");
		return true;
	}
	
	@SneakyThrows(Exception.class)
	public static boolean reloadPolls() {
		if (!Bot.getInstance().getConfig().isPollPersistenceEnabled()) {
			LOG.info("Poll Persistence is disabled in config.json.");
			return true;
		}
		File dir = new File(System.getProperty("user.dir") + "/poll_persistence");
		if (!dir.exists()) return true;
		File[] files = dir.listFiles();
		if (files == null) {
			LOG.fatal("Path is not a directory.");
			return false;
		}
		if (files.length == 0) {
			LOG.info("No files in path.");
			return true;
		}
		for (File file : files) {
			String guildId = file.getName();
			int shardId = (int) (Long.parseLong(guildId) >> 22) % Bot.getInstance().getShards().size();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Poll poll = GSON.fromJson(reader, Poll.class);
			poll.setShardId(shardId);
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle(poll.getPollName());
			builder.setFooter("This Poll was created by " + Util.getUser(poll.getCreator()), Util.getAvatarUrl(poll.getCreator()));
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("**Current Votes**\n");
			poll.getOptions().forEach(option ->
					stringBuilder.append("**" + (option.getIndex() + 1) + ".** " + option.getContent() + "    *(Votes: " + option.getVotes().size() + ")*\n"));
			builder.setDescription(stringBuilder.toString());
			builder.setColor(Color.decode("#F89F3F"));
			Message message = new MessageBuilder().setEmbed(builder.build())
					.append("I had to restart but I reloaded the Poll from where it stopped!")
					.build();
			poll.getChannel().sendMessage(message).queue();
			Poll.getRunningPolls().add(poll);
			if (!file.delete()) {
				LOG.fatal("Failed to delete File " + file.getPath());
			}
		}
		LOG.info("Reloaded all Polls.");
		return true;
	}
}

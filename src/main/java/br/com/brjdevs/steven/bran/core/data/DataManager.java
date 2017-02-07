package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.Main;
import br.com.brjdevs.steven.bran.core.data.bot.BotData;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import net.dv8tion.jda.core.entities.Guild;

import java.io.*;
import java.util.ArrayList;
import java.util.Map.Entry;

public class DataManager {
	
	private BotContainer botContainer;
	
	public DataManager(BotContainer botContainer) {
		this.botContainer = botContainer;
		loadData();
	}
	
	public void saveGuildData() {
		DiscordGuild.instances.entrySet().stream().map(Entry::getValue).forEach(discordGuild -> discordGuild.save(botContainer));
	}
	
	public void saveBotData() {
		botContainer.data.save(botContainer);
	}
	
	public void saveData() {
		saveGuildData();
		saveBotData();
	}
	
	public void loadData() {
		loadGuildData();
		loadBotData();
	}
	
	public void loadGuildData() {
		BufferedReader r;
		for (Guild guild : botContainer.getGuilds()) {
			try {
				r = new BufferedReader(new FileReader(new File(botContainer.workingDir, guild.getId() + ".json")));
				DiscordGuild.load(guild, Main.GSON.fromJson(r, DiscordGuild.class));
				r.close();
			} catch (FileNotFoundException e) {
				DiscordGuild.getInstance(guild, botContainer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void updateConfigs() {
		botContainer.config.save(botContainer);
	}
	
	public void loadBotData() {
		try {
			File file = new File(botContainer.workingDir, "botData.json");
			if (!file.exists()) return;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			botContainer.data = Main.GSON.fromJson(reader, BotData.class);
			reader.close();
			botContainer.getProfiles().forEach((id, profile) -> profile.setListeners(new ArrayList<>()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

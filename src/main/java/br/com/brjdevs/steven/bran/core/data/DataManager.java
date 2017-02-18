package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.Main;
import br.com.brjdevs.steven.bran.core.data.bot.BotData;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import net.dv8tion.jda.core.entities.Guild;

import java.io.*;
import java.util.ArrayList;
import java.util.Map.Entry;

public class DataManager {
	
	private Client client;
	
	public DataManager(Client client) {
		this.client = client;
		loadData();
	}
	
	public void saveGuildData() {
		DiscordGuild.instances.entrySet().stream().map(Entry::getValue).forEach(discordGuild -> discordGuild.save(client));
	}
	
	public void saveBotData() {
		client.data.save(client);
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
		for (Guild guild : client.getGuilds()) {
			try {
				r = new BufferedReader(new FileReader(new File(client.workingDir, guild.getId() + ".json")));
				DiscordGuild.load(guild, Main.GSON.fromJson(r, DiscordGuild.class));
				r.close();
			} catch (FileNotFoundException e) {
				DiscordGuild.getInstance(guild, client);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void updateConfigs() {
		client.config.save(client);
	}
	
	public void loadBotData() {
		try {
			File file = new File(client.workingDir, "botData.json");
			if (!file.exists()) return;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			client.data = Main.GSON.fromJson(reader, BotData.class);
			reader.close();
			client.getProfiles().forEach((id, profile) -> profile.setListeners(new ArrayList<>()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

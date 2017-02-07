package br.com.brjdevs.steven.bran.core.data.bot;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.Main;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Blacklist;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import net.dv8tion.jda.core.entities.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotData {
	
	private final Map<String, List<String>> HangManWords = new HashMap<>();
	private final Blacklist Blacklist = new Blacklist();
	private final Map<String, Profile> profiles = new HashMap<>();
	
	public BotData(BotContainer botContainer) {
		if (!botContainer.workingDir.exists())
			botContainer.workingDir.mkdirs();
	}
	
	public Blacklist getBlacklist() {
		return Blacklist;
	}
	
	public Map<String, Profile> getProfiles() {
		return profiles;
	}
	
	public Profile getProfile(User user) {
		return getProfiles().get(user.getId());
	}
	
	public Map<String, List<String>> getHangManWords() {
		return HangManWords;
	}
	
	public void save(BotContainer botContainer) {
		try {
			File file = new File(botContainer.workingDir, "botData.json");
			if (!file.exists()) assert file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(Main.GSON.toJson(this));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

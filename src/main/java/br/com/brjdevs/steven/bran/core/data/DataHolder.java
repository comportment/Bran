package br.com.brjdevs.steven.bran.core.data;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {
	
	public Map<String, GuildData> guilds = new HashMap<>();
	public Map<String, UserData> users = new HashMap<>();
	
	public GuildData getGuild(Guild guild) {
		return guilds.computeIfAbsent(guild.getId(), id -> new GuildData(guild));
	}
	
	public UserData getUser(User user) {
		return users.computeIfAbsent(user.getId(), id -> new UserData(user));
	}
	
	public UserData getUserById(String id) {
		return users.get(id);
	}
}

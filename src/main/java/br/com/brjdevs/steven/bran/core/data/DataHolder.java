package br.com.brjdevs.steven.bran.core.data;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.ConcurrentHashMap;

public class DataHolder {
	
	public ConcurrentHashMap<String, GuildData> guilds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<>();
	
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

package br.net.brjdevs.steven.bran.core.data;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.ConcurrentHashMap;

public class DataHolder {
	
	public ConcurrentHashMap<String, GuildData> guilds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<>();
    
    public GuildData getGuildData(Guild guild, boolean readOnly) {
        return !readOnly ? guilds.computeIfAbsent(guild.getId(), id -> new GuildData(guild)) : guilds.getOrDefault(guild.getId(), new GuildData(guild));
    }
    
    public UserData getUserData(User user) {
        return users.computeIfAbsent(user.getId(), id -> new UserData(user));
	}
    
    public UserData getUserDataById(String id) {
        return users.get(id);
	}
}

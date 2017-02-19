package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.data.bot.Config;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {
	
	private Map<Long, GuildData> guilds = new HashMap<>();
	private Map<Long, UserData> users = new HashMap<>();
	
	public GuildData getGuild(Guild guild, Config config) {
		return guilds.computeIfAbsent(Long.parseLong(guild.getId()), id -> new GuildData(guild, config));
	}
	
	public UserData getUser(User user) {
		return users.computeIfAbsent(Long.parseLong(user.getId()), id -> new UserData(user));
	}
}

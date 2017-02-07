package br.com.brjdevs.steven.bran.core.data.bot.settings;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blacklist {
	
	private final Map<BlacklistType, List<String>> BLACKLIST;
	
	public Blacklist() {
		this.BLACKLIST = new HashMap<>();
		this.BLACKLIST.put(BlacklistType.GUILD, new ArrayList<>());
		this.BLACKLIST.put(BlacklistType.USER, new ArrayList<>());
	}
	
	public boolean addGuild(Guild guild) {
		if (BLACKLIST.get(BlacklistType.GUILD).contains(guild.getId()))
			return false;
		return BLACKLIST.get(BlacklistType.GUILD).add(guild.getId());
	}
	
	public boolean addGuildById(String id) {
		if (BLACKLIST.get(BlacklistType.GUILD).contains(id))
			return false;
		return BLACKLIST.get(BlacklistType.GUILD).add(id);
	}
	
	public boolean addUser(User user) {
		if (BLACKLIST.get(BlacklistType.USER).contains(user.getId()))
			return false;
		return BLACKLIST.get(BlacklistType.USER).add(user.getId());
	}
	
	public boolean addUserById(String id) {
		if (BLACKLIST.get(BlacklistType.USER).contains(id))
			return false;
		return BLACKLIST.get(BlacklistType.USER).add(id);
	}
	
	public List<String> getBlacklist(BlacklistType type) {
		return BLACKLIST.get(type);
	}
	
	public List<String> getUserBlacklist() {
		return getBlacklist(BlacklistType.USER);
	}
	
	public List<String> getGuildBlacklist() {
		return getBlacklist(BlacklistType.GUILD);
	}
	
	public Map<BlacklistType, List<String>> asMap() {
		return BLACKLIST;
	}
	
	public enum BlacklistType {
		USER, GUILD
	}
}

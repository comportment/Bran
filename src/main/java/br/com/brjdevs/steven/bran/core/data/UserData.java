package br.com.brjdevs.steven.bran.core.data;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class UserData {
	
	private long userId;
	private Profile profile;
	private long globalPermission = -1;
	
	public UserData(User user, Profile profile) {
		this.userId = Long.parseLong(user.getId());
		this.profile = profile;
	}
	
	public UserData(User user) {
		this(user, new Profile(user));
	}
	
	public Profile getProfile() {
		return profile;
	}
	
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
	public User getUser(JDA jda) {
		return jda.getUserById(String.valueOf(userId));
	}
	
	public long getGlobalPermission() {
		return globalPermission;
	}
	
	public boolean hasGlobalPermission() {
		return globalPermission > 0;
	}
}

package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class UserData {
	
	private long userId;
	private Profile profile;
	private long globalPermission = Permissions.BASE_USR;
	
	public UserData(User user, Profile profile) {
		this.userId = Long.parseLong(user.getId());
		setProfile(profile);
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
	
	public boolean hasPermission(long perm) {
		return Permissions.hasPermission(getGlobalPermission(), perm);
	}
	
	public long getGlobalPermission() {
		if (globalPermission < 0)
			globalPermission = Permissions.BASE_USR;
		return globalPermission;
	}
}

package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.currency.ProfileData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class UserData {
	
	public long userId;
    private ProfileData profileData;
    private long globalPermission = Permissions.BASE_USR;
    
    public UserData(User user, ProfileData profileData) {
        this.userId = Long.parseLong(user.getId());
        setProfileData(profileData);
    }
	
	public UserData(User user) {
        this(user, new ProfileData(user));
    }
    
    public ProfileData getProfileData() {
        if (profileData == null) profileData = new ProfileData(String.valueOf(userId));
        return profileData;
    }
    
    public void setProfileData(ProfileData profileData) {
        this.profileData = profileData;
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

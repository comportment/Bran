package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.data.guild.settings.Profile;

public interface ProfileEvents {
	
	void onLevelUp(Profile profile, boolean rankUp);
	
	void onLevelDown(Profile profile, boolean rankDown);
}

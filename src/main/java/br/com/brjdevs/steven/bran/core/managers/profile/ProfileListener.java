package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;

public class ProfileListener implements ProfileEvents {
	
	@Override
	public void onLevelUp(Profile profile, boolean rankUp) {
		profile.setExperience(profile.getExperience() - Profile.expForNextLevel(profile.getLevel()));
		profile.setLevel(profile.getLevel() + 1);
		if (rankUp)
			profile.setRank(profile.getRank().next());
	}
	
	@Override
	public void onLevelDown(Profile profile, boolean rankDown) {
		profile.setExperience(Profile.expForNextLevel(profile.getLevel() - 1) + profile.getExperience());
		profile.setLevel(profile.getLevel() - 1);
		if (rankDown)
			profile.setRank(profile.getRank().previous());
	}
}

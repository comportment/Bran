package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.currency.Profile;

public interface IProfileListener {
	
	void onLevelUp(Profile profile, boolean rankUp);
	
	void onLevelDown(Profile profile, boolean rankDown);
}

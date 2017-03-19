package br.com.brjdevs.steven.bran.core.managers.profile;

import br.com.brjdevs.steven.bran.core.currency.ProfileData;

public interface IProfileListener {
    
    void onLevelUp(ProfileData profileData);
    
    void onLevelDown(ProfileData profileData);
}

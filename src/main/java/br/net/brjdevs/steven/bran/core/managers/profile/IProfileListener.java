package br.net.brjdevs.steven.bran.core.managers.profile;

import br.net.brjdevs.steven.bran.core.currency.ProfileData;

public interface IProfileListener {
    
    void onLevelUp(ProfileData profileData);
    
    void onLevelDown(ProfileData profileData);
}

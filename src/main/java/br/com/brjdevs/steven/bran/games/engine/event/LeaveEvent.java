package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.core.currency.ProfileData;
import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public class LeaveEvent extends GameEvent {
    
    private ProfileData player;
    
    public LeaveEvent(AbstractGame game, ProfileData player) {
        super(game);
        this.player = player;
    }
    
    public ProfileData getProfileData() {
        return player;
    }
}

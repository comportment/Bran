package br.net.brjdevs.steven.bran.games.engine.event;

import br.net.brjdevs.steven.bran.core.currency.ProfileData;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;

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

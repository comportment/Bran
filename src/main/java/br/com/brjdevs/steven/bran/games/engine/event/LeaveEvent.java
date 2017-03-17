package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;
import br.com.brjdevs.steven.bran.games.engine.GamePlayer;

public class LeaveEvent extends GameEvent {
    
    private GamePlayer player;
    
    public LeaveEvent(AbstractGame game, GamePlayer player) {
        super(game);
        this.player = player;
    }
    
    public GamePlayer getPlayer() {
        return player;
    }
}

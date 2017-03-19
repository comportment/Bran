package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.core.client.BranShard;
import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public abstract class GameEvent {
    
    private AbstractGame game;
    
    public GameEvent(AbstractGame game) {
        this.game = game;
    }
    
    public AbstractGame getGame() {
        return game;
    }
    
    public BranShard getShard() {
        return game.getShard();
    }
}

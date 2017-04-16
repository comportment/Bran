package br.net.brjdevs.steven.bran.games.engine.event;

import br.net.brjdevs.steven.bran.core.client.Shard;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;

public abstract class GameEvent {
    
    private AbstractGame game;
    
    public GameEvent(AbstractGame game) {
        this.game = game;
    }
    
    public AbstractGame getGame() {
        return game;
    }
    
    public Shard getShard() {
        return game.getShard();
    }
}

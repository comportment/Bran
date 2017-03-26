package br.com.brjdevs.steven.bran.games.tictactoe.events;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;
import br.com.brjdevs.steven.bran.games.engine.event.GameEvent;
import br.com.brjdevs.steven.bran.games.tictactoe.Tile;

public class MoveEvent extends GameEvent {
    
    Tile tile;
    
    public MoveEvent(AbstractGame game, Tile tile) {
        super(game);
        this.tile = tile;
    }
    
    public Tile getTile() {
        return tile;
    }
}

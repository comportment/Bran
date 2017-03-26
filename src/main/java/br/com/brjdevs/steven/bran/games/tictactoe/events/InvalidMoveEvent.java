package br.com.brjdevs.steven.bran.games.tictactoe.events;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;
import br.com.brjdevs.steven.bran.games.engine.event.GameEvent;
import br.com.brjdevs.steven.bran.games.tictactoe.Tile;

public class InvalidMoveEvent extends GameEvent {
    
    Tile tile;
    
    public InvalidMoveEvent(AbstractGame game, Tile tile) {
        super(game);
        this.tile = tile;
    }
    
    public Tile getTile() {
        return tile;
    }
}

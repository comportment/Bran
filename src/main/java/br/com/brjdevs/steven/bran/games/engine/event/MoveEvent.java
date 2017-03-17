package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public class MoveEvent extends GameEvent {
    
    private boolean isValidMove;
    
    public MoveEvent(AbstractGame game, boolean isValidMove) {
        super(game);
        this.isValidMove = isValidMove;
    }
    
    public boolean isValidMove() {
        return isValidMove;
    }
}

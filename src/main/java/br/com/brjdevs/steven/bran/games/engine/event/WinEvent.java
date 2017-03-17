package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public class WinEvent extends GameEvent {
    
    public WinEvent(AbstractGame game) {
        super(game);
    }
}

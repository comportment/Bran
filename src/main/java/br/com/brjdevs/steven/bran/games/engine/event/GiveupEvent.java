package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public class GiveupEvent extends GameEvent {
    
    public GiveupEvent(AbstractGame game) {
        super(game);
    }
}

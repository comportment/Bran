package br.com.brjdevs.steven.bran.games.engine.event;

import br.com.brjdevs.steven.bran.games.engine.AbstractGame;

public class GiveUpEvent extends GameEvent {
    
    public GiveUpEvent(AbstractGame game) {
        super(game);
    }
}

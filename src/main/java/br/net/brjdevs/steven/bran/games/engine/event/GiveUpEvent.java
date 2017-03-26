package br.net.brjdevs.steven.bran.games.engine.event;

import br.net.brjdevs.steven.bran.games.engine.AbstractGame;

public class GiveUpEvent extends GameEvent {
    
    public GiveUpEvent(AbstractGame game) {
        super(game);
    }
}

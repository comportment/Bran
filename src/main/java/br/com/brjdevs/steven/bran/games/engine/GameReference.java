package br.com.brjdevs.steven.bran.games.engine;

public class GameReference {
    
    private long gameId;
    
    public GameReference(long gameId) {
        this.gameId = gameId;
    }
    
    public long getGameId() {
        return gameId;
    }
    
    public boolean isInstanceOf(Class<? extends AbstractGame> clazz) {
        return clazz.isInstance(GameManager.getGame(this));
    }
}

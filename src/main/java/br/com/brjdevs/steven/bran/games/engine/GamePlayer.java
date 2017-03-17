package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.data.UserData;

public class GamePlayer {
    
    private UserData userData;
    private boolean isGameCreator;
    
    public GamePlayer(UserData userData, boolean isGameCreator) {
        this.userData = userData;
        this.isGameCreator = isGameCreator;
    }
    
    public boolean isGameCreator() {
        return isGameCreator;
    }
    
    public UserData getUserData() {
        return userData;
    }
}

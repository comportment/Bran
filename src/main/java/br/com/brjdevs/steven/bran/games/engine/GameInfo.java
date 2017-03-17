package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.data.UserData;

import java.util.LinkedList;

public class GameInfo {
    
    private boolean multiplayerAvailable;
    private LinkedList<GamePlayer> players;
    private boolean userVsuser;
    
    public GameInfo(UserData creator, boolean multiplayerAvailable, boolean userVsuser) {
        this.multiplayerAvailable = multiplayerAvailable;
        this.userVsuser = userVsuser;
        this.players = new LinkedList<>();
        players.add(new GamePlayer(creator, true));
    }
    
    public boolean isMultiplayerAvailable() {
        return multiplayerAvailable;
    }
    
    public boolean isUserVSUser() {
        return userVsuser;
    }
    
    public LinkedList<GamePlayer> getPlayers() {
        return players;
    }
    
    public boolean isInvited(UserData user) {
        return players.stream().anyMatch(player -> player.getUserData().userId == user.userId);
    }
}

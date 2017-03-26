package br.net.brjdevs.steven.bran.games.engine;

import br.net.brjdevs.steven.bran.core.data.UserData;

import java.util.LinkedList;

public class GameInfo {
    
    private boolean multiplayerAvailable;
    private LinkedList<UserData> players;
    private boolean userVsuser;
    
    public GameInfo(UserData creator, boolean multiplayerAvailable, boolean userVsuser) {
        this.multiplayerAvailable = multiplayerAvailable;
        this.userVsuser = userVsuser;
        this.players = new LinkedList<>();
        players.add(creator);
    }
    
    public boolean isMultiplayerAvailable() {
        return multiplayerAvailable;
    }
    
    public boolean isMultiplayer() {
        return players.size() > 1;
    }
    
    public boolean isUserVSUser() {
        return userVsuser;
    }
    
    public LinkedList<UserData> getPlayers() {
        return players;
    }
    
    public UserData getPlayer(UserData user) {
        return players.stream().filter(player -> player.userId == user.userId).findFirst().orElse(null);
    }
    
    public boolean isInvited(UserData user) {
        return players.stream().anyMatch(player -> player.userId == user.userId);
    }
}

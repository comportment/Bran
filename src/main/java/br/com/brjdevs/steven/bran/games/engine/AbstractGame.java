package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;

public abstract class AbstractGame {
    
    private GameLocation location;
    private GameInfo info;
    private GameState gameState;
    private IProfileListener listener;
    
    public AbstractGame(GameLocation location, GameInfo info, IProfileListener listener) {
        try {
            this.gameState = GameState.STARTING;
            this.location = location;
            this.info = info;
            this.listener = listener;
            if (listener != null)
                this.info.getPlayers().get(0).getUserData().getProfile().registerListener(listener);
            if (!setup())
                this.gameState = GameState.ERRORED;
            else
                this.gameState = GameState.READY;
        } catch (Exception e) {
            this.gameState = GameState.ERRORED;
            throw new RuntimeException(e);
        }
    }
    
    public abstract String getName();
    
    public abstract boolean setup();
    
    public GameLocation getLocation() {
        return location;
    }
    
    public void invite(UserData user) {
        if (listener != null)
            user.getProfile().registerListener(listener);
        info.getPlayers().add(new GamePlayer(user, false));
    }
    
    public boolean passOwnership(UserData user, boolean inviteOldOwner) {
        if (!info.isInvited(user))
            return false;
        GamePlayer oldOwner = info.getPlayers().pollFirst();
        info.getPlayers().addFirst(new GamePlayer(user, true));
        if (inviteOldOwner)
            invite(oldOwner.getUserData()); //invites the old owner
        return true;
    }
    
    public GameInfo getInfo() {
        return info;
    }
    
    public GameState getGameState() {
        return gameState;
    }
}

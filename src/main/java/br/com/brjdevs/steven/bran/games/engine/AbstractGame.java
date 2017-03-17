package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.currency.Profile;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;

import java.rmi.UnexpectedException;

public abstract class AbstractGame {
    
    private static final SnowflakeGenerator GAME_ID_GENERATOR = new SnowflakeGenerator(2, 1);
    private GameLocation location;
    private GameInfo info;
    private GameState gameState;
    private GameReference reference;
    private IProfileListener listener;
    
    public AbstractGame(GameLocation location, GameInfo info) {
        try {
            this.gameState = GameState.STARTING;
            this.location = location;
            this.info = info;
            this.reference = new GameReference(GAME_ID_GENERATOR.nextId());
            this.listener = new DefaultGameProfileListener(reference);
            Profile owner = this.info.getPlayers().get(0).getUserData().getProfile();
            if (listener != null)
                owner.registerListener(listener);
            owner.setCurrentGame(reference);
            if (!setup())
                throw new UnexpectedException("Failed to setup Game!");
            else
                this.gameState = GameState.READY;
        } catch (Exception e) {
            this.gameState = GameState.ERRORED;
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
        user.getProfile().setCurrentGame(reference);
        info.getPlayers().add(new GamePlayer(user, false));
    }
    
    public boolean passOwnership(UserData user, boolean inviteOldOwner) {
        if (!info.isInvited(user))
            return false;
        GamePlayer oldOwner = info.getPlayers().pollFirst();
        info.getPlayers().addFirst(new GamePlayer(user, true));
        user.getProfile().registerListener(listener);
        user.getProfile().setCurrentGame(reference);
        if (inviteOldOwner)
            invite(oldOwner.getUserData()); //invites the old owner
        else {
            oldOwner.getUserData().getProfile().unregisterListener(listener);
            oldOwner.getUserData().getProfile().setCurrentGame(reference);
        }
        return true;
    }
    
    public GameInfo getInfo() {
        return info;
    }
    
    public GameState getGameState() {
        return gameState;
    }
}

package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.BranShard;
import br.com.brjdevs.steven.bran.core.currency.ProfileData;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;
import br.com.brjdevs.steven.bran.games.engine.event.GameEventListener;
import net.dv8tion.jda.core.JDA;

import java.rmi.UnexpectedException;

public abstract class AbstractGame<T extends GameEventListener> {
    
    private static final SnowflakeGenerator GAME_ID_GENERATOR = new SnowflakeGenerator(2, 1);
    protected GameLocation location;
    protected GameInfo info;
    protected GameState gameState;
    protected GameReference reference;
    protected IProfileListener profileListener;
    protected T eventListener;
    protected int shardId;
    
    public AbstractGame(GameLocation location, GameInfo info, T eventListener, JDA shard) {
        try {
            this.gameState = GameState.STARTING;
            this.location = location;
            this.info = info;
            this.reference = new GameReference(GAME_ID_GENERATOR.nextId());
            this.eventListener = eventListener;
            this.profileListener = new DefaultGameProfileListener(reference);
            this.shardId = Bran.getInstance().getShardId(shard);
            ProfileData owner = this.info.getPlayers().get(0).getProfileData();
            if (profileListener != null)
                owner.registerListener(profileListener);
            owner.setCurrentGame(reference);
            if (!setup())
                throw new UnexpectedException("Failed to setup Game!");
            else {
                this.gameState = GameState.READY;
                GameManager.getGames().put(reference, this);
            }
        } catch (Exception e) {
            this.gameState = GameState.ERRORED;
        }
    }
    
    public abstract String getName();
    
    public abstract boolean setup();
    
    public void end() {
        GameManager.end(reference);
    }
    
    public abstract boolean leave(UserData user);
    
    public GameLocation getLocation() {
        return location;
    }
    
    public void invite(UserData user) {
        if (profileListener != null)
            user.getProfileData().registerListener(profileListener);
        user.getProfileData().setCurrentGame(reference);
        info.getPlayers().add(user);
    }
    
    public void kick(UserData user) {
        if (profileListener != null)
            user.getProfileData().unregisterListener(profileListener);
        user.getProfileData().setCurrentGame(null);
        info.getPlayers().remove(info.getPlayer(user));
    }
    
    public boolean passOwnership(UserData user, boolean inviteOldOwner) {
        if (!info.isInvited(user))
            return false;
        UserData oldOwner = info.getPlayers().pollFirst();
        info.getPlayers().addFirst(user);
        user.getProfileData().registerListener(profileListener);
        user.getProfileData().setCurrentGame(reference);
        if (inviteOldOwner)
            invite(oldOwner); //invites the old owner
        else {
            oldOwner.getProfileData().unregisterListener(profileListener);
            oldOwner.getProfileData().setCurrentGame(null);
        }
        return true;
    }
    
    public GameInfo getInfo() {
        return info;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public T getEventListener() {
        return eventListener;
    }
    
    public BranShard getShard() {
        return Bran.getInstance().getShards()[shardId];
    }
    
    public enum GameEndReason {
        GIVEUP, VICTORY, DEFEAT, ERROR
    }
}

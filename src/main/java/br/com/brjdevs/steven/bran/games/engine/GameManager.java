package br.com.brjdevs.steven.bran.games.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    
    private static final ConcurrentHashMap<GameReference, AbstractGame> games = new ConcurrentHashMap<>();
    
    public static AbstractGame getGame(GameReference reference) {
        return games.get(reference);
    }
    
    public static Map<GameReference, AbstractGame> getGames() {
        return games;
    }
    
    public static void end(GameReference ref) {
        AbstractGame game = games.remove(ref);
        if (game != null)
            game.getInfo().getPlayers().forEach(userData -> userData.getProfileData().setCurrentGame(null));
    }
}

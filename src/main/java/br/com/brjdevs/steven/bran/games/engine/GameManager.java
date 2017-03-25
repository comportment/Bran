package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.managers.TaskManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    
    private static final ConcurrentHashMap<GameReference, AbstractGame> games = new ConcurrentHashMap<>();
    
    static {
        TaskManager.startAsyncTask("Game Timeout Task",
                (service) -> games.values().stream()
                        .filter(abstractGame -> abstractGame.getTimeOut() < System.currentTimeMillis())
                        .forEach(abstractGame -> {
                            abstractGame.getLocation().send("You took too long to finish this game, you lost!").queue();
                            abstractGame.end();
                        }), 15);
    }
    
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

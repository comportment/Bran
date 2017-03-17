package br.com.brjdevs.steven.bran.games.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameManager {
    
    private static final ConcurrentHashMap<GameReference, AbstractGame> games = new ConcurrentHashMap<>();
    
    public static AbstractGame getGame(GameReference reference) {
        return games.get(reference);
    }
    
    public static Map<GameReference, AbstractGame> getGames() {
        return Collections.unmodifiableMap(new HashMap<>(games));
    }
    
    public static List<AbstractGame> getGames(GameState state) {
        return games.values().stream().filter(g -> g.getGameState() == state).collect(Collectors.toList());
    }
}
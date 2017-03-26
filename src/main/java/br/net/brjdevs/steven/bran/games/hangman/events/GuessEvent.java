package br.net.brjdevs.steven.bran.games.hangman.events;

import br.net.brjdevs.steven.bran.core.currency.ProfileData;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;
import br.net.brjdevs.steven.bran.games.engine.event.GameEvent;

public class GuessEvent extends GameEvent {
    
    private char c;
    private ProfileData profileData;
    private GuessType guessType;
    
    public GuessEvent(AbstractGame game, char c, ProfileData profileData, GuessType guessType) {
        super(game);
        this.c = c;
        this.profileData = profileData;
        this.guessType = guessType;
    }
    
    public char getChar() {
        return c;
    }
    
    public ProfileData getProfileData() {
        return profileData;
    }
    
    public GuessType getGuessType() {
        return guessType;
    }
    
    public enum GuessType {
        VALID, INVALID, ALREADY_GUESSED
    }
}


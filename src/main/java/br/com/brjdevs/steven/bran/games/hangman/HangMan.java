package br.com.brjdevs.steven.bran.games.hangman;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.currency.ProfileData;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.utils.CollectionUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.games.engine.AbstractGame;
import br.com.brjdevs.steven.bran.games.engine.GameInfo;
import br.com.brjdevs.steven.bran.games.engine.GameLocation;
import br.com.brjdevs.steven.bran.games.engine.event.LooseEvent;
import br.com.brjdevs.steven.bran.games.engine.event.WinEvent;
import br.com.brjdevs.steven.bran.games.hangman.events.GuessEvent;
import br.com.brjdevs.steven.bran.games.hangman.events.GuessEvent.GuessType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HangMan extends AbstractGame<HangManGameListener> {
    
    private LinkedHashMap<String, Boolean> word;
    private List<Character> mistakes;
    
    public HangMan(MessageChannel channel, UserData user) {
        super(new GameLocation(channel), new GameInfo(user, true, false), new HangManGameListener(), channel.getJDA());
    }
    
    private static String captalize(String s) {
        return Arrays.stream(s.split(" +")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }
    
    @Override
    public String getName() {
        return "HangMan";
    }
    
    @Override
    public boolean setup() {
        try {
            this.word = new LinkedHashMap<>();
            this.mistakes = new ArrayList<>();
            String word = CollectionUtils.getRandomEntry(Bran.getInstance().getDataManager().getHangmanWords().get()).getKey();
            for (char c : word.toLowerCase().toCharArray()) {
                String s = String.valueOf(c);
                while (word.contains(s))
                    s += StringUtils.randomName(3);
                this.word.put(s, s.charAt(0) == ' ');
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean leave(UserData user) {
        if (!getInfo().isInvited(user))
            return false;
        kick(user);
        return true;
    }
    
    public void guess(char c, ProfileData profileData) {
        String s = String.valueOf(c);
        if (mistakes.contains(c) || getGuesses().toLowerCase().contains(s)) { //is already guessed
            getEventListener().onGuess(new GuessEvent(this, c, profileData, GuessType.ALREADY_GUESSED));
        } else if (getFullWord().toLowerCase().contains(s)) {
            word.replaceAll((string, bool) -> string.charAt(0) == c && !bool ? true : bool);
            if (getFullWord().equals(getGuesses())) {
                end();
                getEventListener().onWin(new WinEvent(this));
                return;
            }
            getEventListener().onGuess(new GuessEvent(this, c, profileData, GuessType.VALID));
        } else {
            mistakes.add(c);
            if (mistakes.size() > getMaximumMistakes()) {
                end();
                getEventListener().onLoose(new LooseEvent(this));
                return;
            }
            getEventListener().onGuess(new GuessEvent(this, c, profileData, GuessType.INVALID));
        }
    }
    
    public List<Character> getMistakes() {
        return mistakes;
    }
    
    public String getFullWord() {
        return captalize(word.keySet().stream().map(s -> s.substring(0, 1)).collect(Collectors.joining()));
    }
    
    public String getGuesses() {
        return captalize(word.keySet().stream().map(c -> word.get(c) ? c.substring(0, 1) : "\\_").collect(Collectors.joining()));
    }
    
    public int getMaximumMistakes() {
        return 5 + ((info.getPlayers().size() - 1) * 3);
    }
    
    public EmbedBuilder baseEmbed(String description) {
        User creator = info.getPlayers().get(0).getUser(getShard().getJDA());
        return new EmbedBuilder().setColor(Bran.COLOR).setTitle("Hang Man Game", null).setFooter("Game session started by " + creator.getName() + "#" + creator.getDiscriminator(), creator.getEffectiveAvatarUrl()).setDescription(description + "\n\n**Guesses:** " + getGuesses() + "\nYou've made " + mistakes.size() + " out of " + getMaximumMistakes() + "." + (mistakes.isEmpty() ? "" : " (" + mistakes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")") + "\n\n" + "\nMultiplayer: " + (info.isMultiplayer()));
    }
}

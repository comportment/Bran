package br.net.brjdevs.steven.bran.games.hangman;

import br.net.brjdevs.steven.bran.core.currency.BankAccount;
import br.net.brjdevs.steven.bran.core.currency.ProfileData;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import br.net.brjdevs.steven.bran.games.engine.event.*;
import br.net.brjdevs.steven.bran.games.hangman.events.GuessEvent;
import net.dv8tion.jda.core.EmbedBuilder;

public class HangManGameListener extends GameEventListener {
    
    @Override
    public void onGiveUp(GiveUpEvent event) {
        event.getGame().getLocation().send(Emojis.DISAPPOINTED + " Why did you give up? This was fun!").queue();
        ProfileData profileData = event.getGame().getInfo().getPlayers().get(0).getProfileData();
        profileData.addExperience(-1);
        profileData.getBankAccount().takeCoins(10, BankAccount.MAIN_BANK);
    }
    
    @Override
    public void onLeaveGame(LeaveEvent event) {
        event.getGame().getLocation().send(Utils.getUser(event.getProfileData().getUser(event.getShard().getJDA()))
                + " has left the game. " + Emojis.DOOR).queue();
        event.getProfileData().addExperience(-1);
        event.getProfileData().getBankAccount().takeCoins(10, BankAccount.MAIN_BANK);
    }
    
    @Override
    public void onWin(WinEvent event) {
        event.getGame().getLocation().send(Emojis.PARTY_POPPER + " Yay, you won!! Congratulations! " + Emojis.PARTY_POPPER).queue();
        ProfileData profileData = event.getGame().getInfo().getPlayers().get(0).getProfileData();
        profileData.addExperience(2);
        profileData.getBankAccount().addCoins(15, BankAccount.MAIN_BANK);
    }
    
    @Override
    public void onLoose(LooseEvent event) {
        event.getGame().getLocation().send(Emojis.CRY + " Unfortunately you lost the game.").queue();
        ProfileData profileData = event.getGame().getInfo().getPlayers().get(0).getProfileData();
        profileData.addExperience(-3);
        profileData.getBankAccount().takeCoins(15, BankAccount.MAIN_BANK);
    }
    
    public void onGuess(GuessEvent event) {
        String s;
        ProfileData profileData = event.getGame().getInfo().getPlayers().get(0).getProfileData();
        switch (event.getGuessType()) {
            case VALID:
                s = Emojis.THUMBS_UP + " Yay, you've guessed a character!";
                profileData.addExperience(1);
                profileData.getBankAccount().addCoins(2, BankAccount.MAIN_BANK);
                break;
            case ALREADY_GUESSED:
                s = Emojis.FACEPALM + " You already guessed that character.";
                break;
            case INVALID:
                s = Emojis.THUMBS_DOWN + " Sadly that character isn't in the word!";
                profileData.addExperience(-1);
                profileData.getBankAccount().takeCoins(2, BankAccount.MAIN_BANK);
                break;
            default:
                s = "HOW THE FUCK DID YOU MANAGE TO BREAK THIS?";
                break;
        }
        EmbedBuilder builder = ((HangMan) event.getGame()).baseEmbed(s);
        event.getGame().getLocation().send(builder.build()).queue();
    }
}

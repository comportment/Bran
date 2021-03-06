package br.net.brjdevs.steven.bran.games.hangman;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.listeners.EventListener;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;
import br.net.brjdevs.steven.bran.games.engine.GameManager;
import br.net.brjdevs.steven.bran.games.engine.GameReference;
import br.net.brjdevs.steven.bran.games.engine.event.GiveUpEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GuessListener extends EventListener<MessageReceivedEvent> {
    
    public GuessListener() {
        super(MessageReceivedEvent.class);
    }
    
    @Override
    public void onEvent(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;
        GameReference ref = Bran.getInstance().getDataManager().getData().get().getUserData(event.getAuthor()).getProfileData().getCurrentGame();
        if (ref == null || !ref.isInstanceOf(HangMan.class))
            return;
        AbstractGame rawGame = GameManager.getGame(ref);
        UserData user = Bran.getInstance().getDataManager().getData().get().getUserData(event.getAuthor());
        HangMan game = (HangMan) rawGame;
        String guess = event.getMessage().getRawContent();
        if (guess.isEmpty())
            return;
        if (guess.charAt(0) != '^') return;
        if (guess.matches("\\^(give ?up|end|stop)")) {
            if (game.getInfo().isMultiplayer()) {
                event.getChannel().sendMessage("\\" + Emojis.X + " You cannot give up a MultiPlayer session.").queue();
            } else {
                game.end();
                game.getEventListener().onGiveUp(new GiveUpEvent(game));
            }
            return;
        } else if (guess.equals("^leave")) {
            if (!game.getInfo().isMultiplayer()) {
                event.getChannel().sendMessage("\\" + Emojis.X + " You cannot leave a SinglePlayer session.").queue();
            } else {
                game.leave(user);
            }
            return;
        }
        if (guess.length() == 2)
            game.guess(guess.toLowerCase().charAt(1), user.getProfileData());
    }
}

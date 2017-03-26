package br.net.brjdevs.steven.bran.games.tictactoe;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.listeners.EventListener;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;
import br.net.brjdevs.steven.bran.games.engine.GameManager;
import br.net.brjdevs.steven.bran.games.engine.GameReference;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MoveListener extends EventListener<MessageReceivedEvent> {
    
    public MoveListener() {
        super(MessageReceivedEvent.class);
    }
    
    @Override
    public void event(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;
        String msg = event.getMessage().getContent();
        if (!MathUtils.isInteger(msg))
            return;
        int tile = Integer.parseInt(msg);
        if (tile < 0 || tile > 9)
            return;
        UserData user = Bran.getInstance().getDataManager().getData().get().getUserData(event.getAuthor());
        GameReference ref = user.getProfileData().getCurrentGame();
        if (ref == null)
            return;
        AbstractGame abstractGame = GameManager.getGame(ref);
        if (abstractGame instanceof TicTacToe) {
            TicTacToe t = (TicTacToe) abstractGame;
            int player = t.getInfo().getPlayers().indexOf(user);
            t.move(player, tile - 1);
        }
    }
}

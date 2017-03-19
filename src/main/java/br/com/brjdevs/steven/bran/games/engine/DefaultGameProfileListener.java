package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.currency.ProfileData;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import net.dv8tion.jda.core.entities.MessageChannel;

public class DefaultGameProfileListener implements IProfileListener {
    
    private GameReference reference;
    
    public DefaultGameProfileListener(GameReference reference) {
        this.reference = reference;
    }
    
    @Override
    public void onLevelUp(ProfileData profileData) {
        AbstractGame game = GameManager.getGame(reference);
        if (game != null) {
            MessageChannel channel = game.getLocation().getChannel();
            game.getLocation().getChannel().sendTyping().queue(typed ->
                    channel.sendMessage(Emojis.PARTY_POPPER + " Congratulations, you leveled up from " + (profileData.getLevel() - 1)
                            + " to " + profileData.getLevel() + "!").queue()
            );
        }
    }
    
    @Override
    public void onLevelDown(ProfileData profileData) {
        AbstractGame game = GameManager.getGame(reference);
        if (game != null) {
            MessageChannel channel = game.getLocation().getChannel();
            game.getLocation().getChannel().sendTyping().queue(typed ->
                    channel.sendMessage(Emojis.THUMBS_DOWN + " Too bad, you leveled down from " + (profileData.getLevel() - 1)
                            + " to " + profileData.getLevel() + "!").queue()
            );
        }
    }
}

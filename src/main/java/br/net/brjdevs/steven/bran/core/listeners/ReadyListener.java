package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener extends EventListener<ReadyEvent> {
	
	private static final Logger LOG = LoggerFactory.getLogger("Ready Listener");
	
	public ReadyListener() {
		super(ReadyEvent.class);
	}
	
	@Override
	public void onEvent(ReadyEvent event) {
        if (event.getJDA().getShardInfo() != null)
            LOG.info("Got Ready Event on Shard " + event.getJDA().getShardInfo().getShardId());
        else
            LOG.info("Got Ready Event.");
        Game game = null;
        if (!Utils.isEmpty(Bran.getInstance().getConfig().defaultGame))
            game = Bran.getInstance().getConfig().gameStream ? Game.of(Bran.getInstance().getConfig().defaultGame, "https://twitch.tv/ ")
                    : Game.of(Bran.getInstance().getConfig().defaultGame);
        if (event.getJDA().getShardInfo() != null && game != null)
            game = Game.of(game.getName() + " | [" + event.getJDA().getShardInfo().getShardId() + "]", game.getUrl());
        event.getJDA().getPresence().setGame(game);
        event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
    }
}

package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ReadyListener extends EventListener<ReadyEvent> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("Ready Listener");
	
	public ReadyListener() {
		super(ReadyEvent.class);
	}
	
	@Override
	public void event(ReadyEvent event) {
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
    }
}

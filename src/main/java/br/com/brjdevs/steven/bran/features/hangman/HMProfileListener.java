package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;

public class HMProfileListener implements IProfileListener {
	
	private HangManGame game;
	private BotContainer container;
	
	public HMProfileListener(HangManGame game, BotContainer container) {
		this.game = game;
		this.container = container;
	}
	
	@Override
	public void onLevelUp(Profile profile, boolean rankUp) {
		game.getChannel(container).sendMessage("You leveled UP! You're now at level " + profile.getLevel() + (rankUp ? "and you ranked up from " + profile.getRank().previous() + " to " + profile.getRank() : "") + "!").queue();
	}
	
	@Override
	public void onLevelDown(Profile profile, boolean rankDown) {
		game.getChannel(container).sendMessage("You leveled down! You're now at level " + profile.getLevel() + (rankDown ? "and you ranked down from " + profile.getRank().next() + " to " + profile.getRank() : "") + "!").queue();
	}
}

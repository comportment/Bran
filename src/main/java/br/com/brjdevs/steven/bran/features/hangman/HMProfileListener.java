package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import br.com.brjdevs.steven.bran.refactor.BotContainer;
import net.dv8tion.jda.core.EmbedBuilder;

public class HMProfileListener implements IProfileListener {
	
	private HangManGame game;
	private BotContainer container;
	
	public HMProfileListener(HangManGame game, BotContainer container) {
		this.game = game;
		this.container = container;
	}
	
	@Override
	public void onLevelUp(Profile profile, boolean rankUp) {
		game.getChannel(container).sendMessage(new EmbedBuilder(profile.createEmbed(game.getShard(container).getJDA())).setDescription("**You leveled UP!**").build()).queue();
	}
	
	@Override
	public void onLevelDown(Profile profile, boolean rankDown) {
		game.getChannel(container).sendMessage(new EmbedBuilder(profile.createEmbed(game.getShard(container).getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
	}
}

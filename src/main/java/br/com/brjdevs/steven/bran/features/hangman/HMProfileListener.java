package br.com.brjdevs.steven.bran.features.hangman;

import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.managers.profile.IProfileListener;
import net.dv8tion.jda.core.EmbedBuilder;

public class HMProfileListener implements IProfileListener {
	
	private HangManGame game;
	
	public HMProfileListener(HangManGame game) {
		this.game = game;
	}
	
	@Override
	public void onLevelUp(Profile profile, boolean rankUp) {
		game.getChannel().sendMessage(new EmbedBuilder(profile.createEmbed(game.getJDA())).setDescription("**You leveled UP!**").build()).queue();
	}
	
	@Override
	public void onLevelDown(Profile profile, boolean rankDown) {
		game.getChannel().sendMessage(new EmbedBuilder(profile.createEmbed(game.getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
	}
}

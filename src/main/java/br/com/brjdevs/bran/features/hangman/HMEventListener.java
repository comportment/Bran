package br.com.brjdevs.bran.features.hangman;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile.Action;
import br.com.brjdevs.bran.features.hangman.HMSession.EmbedInfo;
import net.dv8tion.jda.core.EmbedBuilder;

public class HMEventListener implements IHMEvent {
	
	private HMSession session;
	
	public HMEventListener(HMSession session) {
		this.session = session;
	}
	
	@Override
	public void onAlreadyGuessed(boolean miss) {
		session.getChannel().sendMessage(session.createEmbed(EmbedInfo.GUESSED)).queue(session::setLastMessage);
	}
	
	@Override
	public void onGuess() {
		session.getChannel().sendMessage(session.createEmbed(EmbedInfo.GUESS_R)).queue(session::setLastMessage);
		session.getProfiles().forEach(p -> {
			p.addCoins(2);
			Action action = p.addExperience(1);
			if (action == Action.LEVEL_UP)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled UP!**").build()).queue();
		});
	}
	
	@Override
	public void onInvalidGuess(String s) {
		session.getChannel().sendMessage(session.createEmbed(EmbedInfo.GUESS_W)).queue(session::setLastMessage);
	}
	
	@Override
	public void onLoose() {
		session.getChannel().sendMessage(session.createEmbed(EmbedInfo.LOOSE)).queue(session::setLastMessage);
		session.end();
		session.getProfiles().forEach(p -> {
			p.addCoins(-2);
			p.getHMStats().defeats++;
			Action action = p.addExperience(-3);
			if (action == Action.LEVEL_DOWN)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
		});
	}
	
	@Override
	public void onWin() {
		session.getChannel().sendMessage(session.createEmbed(EmbedInfo.WIN)).queue();
		session.end();
		session.getProfiles().forEach(p -> {
			p.addCoins(2);
			p.getHMStats().victory++;
			Action action = p.addExperience(4);
			if (action == Action.LEVEL_UP)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled UP!**").build()).queue();
		});
	}
}

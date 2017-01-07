package br.com.brjdevs.bran.features.hangman.listener;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile.Action;
import br.com.brjdevs.bran.features.hangman.HangManGame;
import net.dv8tion.jda.core.EmbedBuilder;

public class HangManEventListener implements HangManEvents {
	
	private HangManGame session;
	
	public HangManEventListener(HangManGame session) {
		this.session = session;
	}
	
	@Override
	public void onAlreadyGuessed(boolean miss) {
		session.getChannel().sendMessage(session.createEmbed().setDescription(miss ? "I've already told you, this character is not in the word!" : "You've already guessed this character!").build()).queue(session::setLastMessage);
	}
	
	@Override
	public void onGuess(String s) {
		session.getChannel().sendMessage(session.createEmbed().setDescription("Oh yeah, the character '" + s + "' is in the word! Keep the good job.").build()).queue(session::setLastMessage);
		session.getProfiles().forEach(p -> {
			p.addCoins(2);
			Action action = p.addExperience(1);
			if (action == Action.LEVEL_UP)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled UP!**").build()).queue();
		});
	}
	
	@Override
	public void onInvalidGuess(String s) {
		session.getChannel().sendMessage(session.createEmbed().setDescription("Too bad, the character '" + s + "' is not in the word!").build()).queue(session::setLastMessage);
	}
	
	@Override
	public void onLeaveGame(Profile profile) {
		session.remove(profile);
		profile.getHMStats().addDefeat();
		session.getChannel().sendMessage(session.createEmbed().setDescription("Ooh boy, why did you leave? You were doing well!").build()).queue();
	}
	
	@Override
	public void onLoose(boolean giveUp) {
		session.getChannel().sendMessage(!giveUp ? session.createEmbed().setDescription("Well, I think you did great but it wasn't this time. The word was '" + session.getWord().asString() + "'.").build() : session.createEmbed().setDescription("Aww man... I know you could've done it! The word was '" + session.getWord().asString() + "'").build()).queue(session::setLastMessage);
		session.end();
		session.getProfiles().forEach(p -> {
			p.addCoins(-2);
			p.getHMStats().addDefeat();
			Action action = p.addExperience(-3);
			if (action == Action.LEVEL_DOWN)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled DOWN!**").build()).queue();
		});
	}
	
	@Override
	public void onWin() {
		session.getChannel().sendMessage(session.createEmbed().setDescription("\uD83C\uDF89 You did it! You win!! The word is '" + session.getWord().asString() + "'! \uD83C\uDF89").build()).queue();
		session.end();
		session.getProfiles().forEach(p -> {
			p.addCoins(2);
			p.getHMStats().addVictory();
			Action action = p.addExperience(4);
			if (action == Action.LEVEL_UP)
				session.getChannel().sendMessage(new EmbedBuilder(p.createEmbed(session.getJDA())).setDescription("**You leveled UP!**").build()).queue();
		});
	}
}

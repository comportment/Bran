package br.com.brjdevs.bran.features.hangman.events;

import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.features.hangman.HangManGame;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;

public class LeaveGameEvent extends HangManEvent {
	
	@Getter
	protected Profile profile;
	
	public LeaveGameEvent(HangManGame game, JDA jda, Profile profile) {
		super(game, jda);
		this.profile = profile;
	}
}

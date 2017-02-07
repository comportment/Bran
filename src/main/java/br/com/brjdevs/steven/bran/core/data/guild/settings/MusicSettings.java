package br.com.brjdevs.steven.bran.core.data.guild.settings;

import lombok.Getter;
import lombok.Setter;

public class MusicSettings {
	
	@Getter
	@Setter
	private boolean enabled;
	@Getter
	@Setter
	private long maxSongsPerUser;
	
	public MusicSettings() {
		this.maxSongsPerUser = -1L;
		this.enabled = true;
	}
}
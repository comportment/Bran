package br.com.brjdevs.bran.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class MusicPlayerManager {
	private final AudioPlayerManager playerManager;
	private final Map<Long, MusicManager> musicManagers;
	public MusicPlayerManager() {
		musicManagers = new HashMap<>();
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}
	
	public Map<Long, MusicManager> getMusicManagers() {
		return musicManagers;
	}
	
	public AudioPlayerManager getAudioPlayerManager() {
		return playerManager;
	}
	
	public synchronized MusicManager get(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		MusicManager musicManager = musicManagers.computeIfAbsent(guildId, k -> new MusicManager(playerManager, guild));
		
		if (guild.getAudioManager().getSendingHandler() == null)
			guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		
		return musicManager;
	}
	
	public void loadAndPlay(final User user, final TextChannel channel, final String trackUrl) {
		MusicManager musicManager = get(channel.getGuild());
		
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoader(channel, user, trackUrl, musicManager));
	}
}

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
	private final Map<Long, GuildMusicManager> musicManagers;
	public MusicPlayerManager() {
		musicManagers = new HashMap<>();
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}
	public Map<Long, GuildMusicManager> getMusicManagers() {
		return musicManagers;
	}
	public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);
		
		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, guild);
			musicManagers.put(guildId, musicManager);
		}
		if (guild.getAudioManager().getSendingHandler() == null)
			guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		
		return musicManager;
	}
	public void loadAndPlay(final User member, final TextChannel channel, final String trackUrl) {
		GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
		
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoader(channel, member, trackUrl, musicManager));
	}
}

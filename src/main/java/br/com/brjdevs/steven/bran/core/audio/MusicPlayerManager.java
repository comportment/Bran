package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.Client;
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
	private final Client client;
	
	public MusicPlayerManager(Client client) {
		this.client = client;
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
	}
	
	public Map<Long, MusicManager> getMusicManagers() {
		return musicManagers;
	}
	
	public void unregister(Long guildId) {
		if (musicManagers.containsKey(guildId)) {
			MusicManager manager = musicManagers.remove(guildId);
			client.taskManager.getChannelLeaveTimer().removeMusicPlayer(guildId.toString());
			client.taskManager.getMusicRegisterTimeout().removeMusicPlayer(guildId.toString());
			if (manager.getGuild() != null)
				manager.getGuild().getAudioManager().setSendingHandler(null);
		}
	}
	
	public AudioPlayerManager getAudioPlayerManager() {
		return playerManager;
	}
	
	public synchronized MusicManager get(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		MusicManager musicManager = musicManagers
				.computeIfAbsent(guildId, k -> new MusicManager(playerManager, guild, client));
		
		if (guild.getAudioManager().getSendingHandler() != musicManager.getSendHandler())
			guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		
		return musicManager;
	}
	
	public void loadAndPlay(final User user, final TextChannel channel, final String trackUrl) {
		MusicManager musicManager = get(channel.getGuild());
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoader(channel, user, trackUrl, musicManager));
	}
}

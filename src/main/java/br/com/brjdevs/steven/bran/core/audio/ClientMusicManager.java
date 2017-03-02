package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.core.client.Client;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class ClientMusicManager {
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	private final Client client;
	
	public ClientMusicManager(Client client) {
		this.client = client;
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
	}
	
	public Map<Long, GuildMusicManager> getMusicManagers() {
		return musicManagers;
	}
	
	public void unregister(Long guildId) {
		if (musicManagers.containsKey(guildId)) {
			GuildMusicManager manager = musicManagers.remove(guildId);
			client.getTaskManager().getChannelLeaveTimer().removeMusicPlayer(guildId.toString());
			client.getTaskManager().getMusicRegisterTimeout().removeMusicPlayer(guildId.toString());
			if (manager.getGuild() != null)
				manager.getGuild().getAudioManager().setSendingHandler(null);
		}
	}
	
	public AudioPlayerManager getAudioPlayerManager() {
		return playerManager;
	}
	
	public synchronized GuildMusicManager get(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers
				.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager, guild, client));
		
		if (guild.getAudioManager().getSendingHandler() == null)
			guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		
		return musicManager;
	}
	
	public void loadAndPlay(final User user, final TextChannel channel, final String trackUrl) {
		GuildMusicManager musicManager = get(channel.getGuild());
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoader(channel, user, trackUrl, musicManager));
	}
}

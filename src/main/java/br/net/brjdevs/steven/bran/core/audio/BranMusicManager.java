package br.net.brjdevs.steven.bran.core.audio;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.sql.SQLAction;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BranMusicManager {
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	public AtomicLong playedSongs;
	
	public BranMusicManager() {
		this.musicManagers = new HashMap<>();
		this.playedSongs = new AtomicLong(0);
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
        
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    conn.prepareStatement("CREATE TABLE IF NOT EXISTS MUSIC (" +
                            "id varchar(15)," +
                            "played int, PRIMARY KEY(id) " +
                            ");").executeUpdate();
                } catch (SQLException e) {
                    SQLAction.LOGGER.log(e);
                }
            }).queue();
        } catch (SQLException e) {
            SQLAction.LOGGER.log(e);
        }
    }
	
	public Map<Long, GuildMusicManager> getMusicManagers() {
		return musicManagers;
	}
	
	public void unregister(Long guildId) {
		if (musicManagers.containsKey(guildId)) {
			GuildMusicManager manager = musicManagers.remove(guildId);
			Bran.getInstance().getTaskManager().getChannelLeaveTimer().removeMusicPlayer(guildId.toString());
			Bran.getInstance().getTaskManager().getMusicRegisterTimeout().removeMusicPlayer(guildId.toString());
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
				.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager, guild));
		
		if (guild.getAudioManager().getSendingHandler() == null)
			guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		
		return musicManager;
	}
    
    public void loadAndPlay(final User user, final TextChannel channel, final String trackUrl, boolean force) {
        GuildMusicManager musicManager = get(channel.getGuild());
        AudioLoadResultHandler loader = force ? new ForcePlayAudioLoader(channel, user, trackUrl, musicManager) : new AudioLoader(channel, user, trackUrl, musicManager);
        playerManager.loadItemOrdered(musicManager, trackUrl, loader);
    }
}

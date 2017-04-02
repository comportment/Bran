package br.net.brjdevs.steven.bran.core.audio;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.Client;
import br.net.brjdevs.steven.bran.core.sql.SQLAction;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GuildMusicManager {
	
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private final int shard;
	private Long guildId;
	
	public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
		player = manager.createPlayer();
		this.guildId = Long.parseLong(guild.getId());
		this.shard = Bran.getInstance().getShardId(guild.getJDA());
		scheduler = new TrackScheduler(this, player, guildId, shard);
		player.addListener(new AudioPlayerListener(getTrackScheduler()));
        player.addListener(new AudioEventAdapter() {
            
            @Override
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                super.onTrackStart(player, track);
                try {
                    SQLDatabase.getInstance().run((conn) -> {
                        try {
                            PreparedStatement statement = conn.prepareStatement("INSERT INTO MUSIC " +
                                    "VALUES(" +
                                    "?, " +
                                    "1" +
                                    ") ON DUPLICATE KEY UPDATE played = played + 1;");
                            statement.setString(1, track.getInfo().identifier);
                            statement.executeUpdate();
                        } catch (SQLException e) {
                            SQLAction.LOGGER.log(e);
                        }
                    }).queue();
                } catch (SQLException e) {
                    SQLAction.LOGGER.log(e);
                }
            }
        });
    }
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
    
    public Client getShard() {
        return Bran.getInstance().getShards()[shard];
	}
	public TrackScheduler getTrackScheduler() {
		return scheduler;
	}
	public AudioPlayer getPlayer() {
		return player;
	}
	
	public PlayerSendHandler getSendHandler() {
		return new PlayerSendHandler(player);
	}
}

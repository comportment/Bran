package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.BranShard;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;

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
	}
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public BranShard getShard() {
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

package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.client.ClientShard;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {
	
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private final int shard;
	public Client client;
	private Long guildId;
	
	public GuildMusicManager(AudioPlayerManager manager, Guild guild, Client client) {
		player = manager.createPlayer();
		this.guildId = Long.parseLong(guild.getId());
		this.shard = client.getShardId(guild.getJDA());
		this.client = client;
		scheduler = new TrackScheduler(this, player, guildId, shard, client);
		player.addListener(new AudioPlayerListener(getTrackScheduler()));
	}
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public ClientShard getShard() {
		return client.getShards()[shard];
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

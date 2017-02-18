package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.ClientShard;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;

public class MusicManager {
	
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private final int shard;
	public Client client;
	private Long guildId;
	private PlayerSendHandler sendHandler;
	
	public MusicManager(AudioPlayerManager manager, Guild guild, Client client) {
		player = manager.createPlayer();
		player.addListener(new AudioPlayerListener(getTrackScheduler()));
		this.guildId = Long.parseLong(guild.getId());
		this.shard = client.getShardId(guild.getJDA());
		this.client = client;
		scheduler = new TrackScheduler(this, player, guildId, shard, client);
		this.sendHandler = new PlayerSendHandler(player);
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
		return sendHandler;
	}
}

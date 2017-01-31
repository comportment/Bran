package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.BotContainer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;

public class MusicManager {
	
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private final int shard;
	public BotContainer container;
	private Long guildId;
	private PlayerSendHandler sendHandler;
	
	public MusicManager(AudioPlayerManager manager, Guild guild, BotContainer container) {
		player = manager.createPlayer();
		this.guildId = Long.parseLong(guild.getId());
		this.shard = container.getShardId(guild.getJDA());
		this.container = container;
		scheduler = new TrackScheduler(player, guildId, shard, container);
		player.addListener(scheduler);
		this.sendHandler = new PlayerSendHandler(player);
	}
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public Bot getShard() {
		return container.getShards()[shard];
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

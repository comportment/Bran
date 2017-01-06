package br.com.brjdevs.bran.core.audio;

import br.com.brjdevs.bran.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {
	
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private Long guildId;
	private final int shard;
	
	public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
		player = manager.createPlayer();
		this.guildId = Long.parseLong(guild.getId());
		this.shard = Bot.getInstance().getShardId(guild.getJDA());
		scheduler = new TrackScheduler(player, guildId, shard);
		player.addListener(scheduler);
	}
	public Guild getGuild() {
		return getJDA().getGuildById(String.valueOf(guildId));
	}
	public JDA getJDA() {
		return Bot.getInstance().getShards().entrySet().stream().filter(entry -> entry.getKey() == shard).findFirst().orElse(null).getValue();
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

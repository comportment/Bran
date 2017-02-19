package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.ClientShard;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TrackScheduler {
	
	public Client client;
	private AudioPlayer player;
	private long guildId;
	private List<String> voteSkips;
	private int shard;
	private MusicManager musicManager;
	private FairQueue<TrackContext> queue;
	
	public TrackScheduler(MusicManager musicManager, AudioPlayer player, long guildId, int shard, Client client) {
		this.player = player;
		this.guildId = guildId;
		this.voteSkips = new ArrayList<>();
		this.shard = shard;
		this.client = client;
		this.musicManager = musicManager;
		this.queue = new FairQueue<>(musicManager);
	}
	
	public String getQueueDuration() {
		long duration = 0;
		if (queue.getCurrentTrack() != null)
			duration += queue.getCurrentTrack().getInfo().length - queue.getCurrentTrack().getTrack().getPosition();
		for (TrackContext track : queue.getRawQueue())
			duration += track.getTrack().getInfo().length;
		return AudioUtils.format(duration);
	}
	public List<String> getVoteSkips() {
		return voteSkips;
	}
	
	public void stop() {
		getQueue().stop();
	}
	
	public AudioPlayer getAudioPlayer() {
		return player;
	}
	
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public FairQueue<TrackContext> getQueue() {
		return queue;
	}
	
	public MusicManager getMusicManager() {
		return musicManager;
	}
	
	public int getPosition(TrackContext trackContext) {
		return new ArrayList<>(queue.getRawQueue()).indexOf(trackContext);
	}
	
	public TrackContext getByPosition(int index) {
		LinkedList<TrackContext> list = new LinkedList<>(queue.getRawQueue());
		if (index >= list.size()) return null;
		return list.get(index);
	}
	
	public boolean isPaused() {
		return getAudioPlayer().isPaused();
	}
	
	public void setPaused(boolean paused) {
		getAudioPlayer().setPaused(paused);
	}
	
	public ClientShard getShard() {
		return client.getShards()[shard];
	}
	
	public List<TrackContext> getTracksBy(User user) {
		return queue.getRawQueue().stream()
				.filter(track -> track.getDJId().equals(user.getId())).collect(Collectors.toList());
	}
	
	public int getRequiredSkipVotes() {
		int listeners = (int) getVoiceChannel().getMembers().stream()
				.filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
		return (int) Math.ceil(listeners * .55);
	}
	
	public void skip() {
		voteSkips.clear();
		getQueue().next(true);
	}
	
	public VoiceChannel getVoiceChannel() {
		return getGuild().getAudioManager().getConnectedChannel();
	}
}

package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.ClientShard;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FairQueue<T extends TrackContext> {
	
	private BlockingQueue<T> queue;
	private MusicManager musicManager;
	private boolean repeat;
	private T currentTrack;
	private T previousTrack;
	
	public FairQueue(MusicManager musicManager) {
		this(new LinkedBlockingQueue<>(), musicManager);
	}
	
	public FairQueue(BlockingQueue<T> queue, MusicManager musicManager) {
		this.queue = queue;
		this.musicManager = musicManager;
		this.repeat = false;
		this.currentTrack = null;
		this.previousTrack = null;
	}
	
	private static <E extends TrackContext> int getMatches(BlockingQueue<E> queue, E track) {
		int i = 0;
		for (E e : queue) {
			if (e.getTrack().getInfo().identifier.equals(track.getInfo().identifier)) i++;
		}
		return i;
	}
	
	private static <E extends TrackContext> int getMatches(List<E> list, E track) {
		int i = 0;
		for (E e : list) {
			if (e.getTrack().getInfo().identifier.equals(track.getInfo().identifier)) i++;
		}
		return i;
	}
	
	public MusicManager getMusicManager() {
		return musicManager;
	}
	
	public TrackScheduler getTrackScheduler() {
		return getMusicManager().getTrackScheduler();
	}
	
	public BlockingQueue<T> getRawQueue() {
		return queue;
	}
	
	public T getCurrentTrack() {
		return currentTrack;
	}
	
	public T getPreviousTrack() {
		return previousTrack;
	}
	
	public boolean isRepeat() {
		return repeat;
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public List<T> getRequestsFrom(User user) {
		return queue.stream().filter(track -> track.getDJ().equals(user)).collect(Collectors.toList());
	}
	
	public AudioPlayer getAudioPlayer() {
		return musicManager.getPlayer();
	}
	
	public JDA getJDA() {
		return getShard().getJDA();
	}
	
	public ClientShard getShard() {
		return getMusicManager().getShard();
	}
	
	public Guild getGuild() {
		return musicManager.getGuild();
	}
	
	public boolean isNextTrackAvailable() {
		return !queue.isEmpty();
	}
	
	public int size() {
		return getRawQueue().size();
	}
	
	public boolean restartSong(TextChannel textChannel) {
		if (getCurrentTrack() == null && getPreviousTrack() == null) {
			textChannel.sendMessage("The player has never played a song in the last 30 minutes so it cannot restart a song!").queue();
			return false;
		} else if (getCurrentTrack() == null) {
			textChannel.sendMessage("Restarting the previous track...").queue();
			start(getPreviousTrack(), false);
		} else {
			textChannel.sendMessage("Restarting the current track...").queue();
			getAudioPlayer().getPlayingTrack().setPosition(0);
		}
		return true;
	}
	
	public boolean shuffle() {
		if (queue.isEmpty()) return false;
		List<T> tracks = new ArrayList<>();
		queue.drainTo(tracks);
		Collections.shuffle(tracks);
		queue.addAll(tracks);
		tracks.clear();
		return true;
	}
	
	public void next(boolean skip) {
		if (isRepeat() && !skip && getCurrentTrack() != null) {
			start(getCurrentTrack(), false);
			return;
		}
		start(queue.poll(), false);
	}
	
	private void start(T track, boolean noInterrupt) {
		if (getCurrentTrack() != null) previousTrack = currentTrack;
		currentTrack = track;
		getAudioPlayer().startTrack(track == null ? null : track.getTrack().getState() == AudioTrackState.FINISHED ? track.getTrack().makeClone() : track.getTrack(), noInterrupt);
		if (track == null) onQueueStop();
	}
	
	public boolean request(T track, boolean silent) {
		if (track.getInfo().length > AudioUtils.MAX_SONG_LENGTH) {
			track.getContext().sendMessage("Could not add `" + track.getInfo().title + "` to the queue. Reason: `This song it's too long, the maximum supported length is 3 hours!").queue();
			if (isEmpty() && getCurrentTrack() == null) getGuild().getAudioManager().closeAudioConnection();
			return false;
		}
		if (!canRequest(track)) {
			String s;
			switch (DiscordGuild.getInstance(getMusicManager().getGuild(), musicManager.client).getMusicSettings().getFairQueueLevel()) {
				case 1:
					s = "Oops, it looks like you already asked for this song, why don't you try another one? (FairQueue: 1)";
					break;
				case 2:
					s = "Oops, it looks like this song is already in the queue, why don't you try another one? (FairQueue: 2)";
					break;
				default:
					s = "Unrecognized FairQueue Level '" +
							DiscordGuild.getInstance(getMusicManager().getGuild(), musicManager.client).getMusicSettings().getFairQueueLevel() + "'\nReport this Message to my Master.";
			}
			track.getContext().sendMessage(s).queue();
			return false;
		}
		if (!silent) {
			track.getContext().sendMessage("Queueing `" + track.getInfo().title + "` requested by " + OtherUtils.getUser(track.getDJ())).queue(msg -> {
				getRawQueue().offer(track);
				msg.editMessage(track.getDJ().getAsMention() + " has added `" + track.getInfo().title + "` to the queue! (`" + AudioUtils.format(track.getInfo().length) + "`)[`" + queue.size() + "`]").queue();
				if (getAudioPlayer().getPlayingTrack() == null) next(false);
			});
		} else {
			getRawQueue().offer(track);
			if (getAudioPlayer().getPlayingTrack() == null) next(false);
		}
		return true;
	}
	
	public boolean canRequest(T track) {
		switch (DiscordGuild.getInstance(getMusicManager().getGuild(), musicManager.client).getMusicSettings().getFairQueueLevel()) {
			case 0:
				return true;
			case 1:
				return getMatches(getRequestsFrom(track.getDJ()), track) < 1;
			case 2:
				return getMatches(queue, track) < 1;
			default:
				throw new UnsupportedOperationException("Unrecognized FairQueue Level '" +
						DiscordGuild.getInstance(getMusicManager().getGuild(), musicManager.client).getMusicSettings().getFairQueueLevel() + "'");
		}
	}
	
	private void onQueueStop() {
		if (getPreviousTrack() != null && getPreviousTrack().getContext() != null && getPreviousTrack().getContext().canTalk()) {
			getPreviousTrack().getContext().sendMessage("Finished playing queue, disconnecting... If you want to play more music use `!!music play [SONG]`.").queue();
		}
		musicManager.client.taskManager.getMusicRegisterTimeout().addMusicPlayer(String.valueOf(getGuild().getId()), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
		getGuild().getAudioManager().closeAudioConnection();
	}
}

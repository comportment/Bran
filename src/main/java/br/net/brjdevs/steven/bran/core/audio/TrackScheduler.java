package br.net.brjdevs.steven.bran.core.audio;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.Shard;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TrackScheduler {
	
	private AudioPlayer player;
	private long guildId;
	private List<String> voteSkips;
	private int shard;
	private BlockingQueue<TrackContext> queue;
	private GuildMusicManager musicManager;
	private boolean repeat;
	private TrackContext currentTrack;
	private TrackContext previousTrack;
	
	public TrackScheduler(GuildMusicManager musicManager, AudioPlayer player, long guildId, int shard) {
		this.player = player;
		this.guildId = guildId;
		this.voteSkips = new ArrayList<>();
		this.shard = shard;
		this.musicManager = musicManager;
		this.queue = new LinkedBlockingQueue<>();
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
	
	public TrackScheduler getTrackScheduler() {
		return getMusicManager().getTrackScheduler();
	}
	
	public BlockingQueue<TrackContext> getRawQueue() {
		return queue;
	}
	
	public TrackContext getCurrentTrack() {
		return currentTrack;
	}
	
	public TrackContext getPreviousTrack() {
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
	
	public List<TrackContext> getRequestsFrom(User user) {
		return queue.stream().filter(track -> track.getDJ().equals(user)).collect(Collectors.toList());
	}
	
	public JDA getJDA() {
		return getShard().getJDA();
	}
	
	public boolean isNextTrackAvailable() {
		return !queue.isEmpty();
	}
	
	public int size() {
		return getRawQueue().size();
	}
	
	public boolean restartSong(TextChannel textChannel) {
		try {
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
		} catch (Exception e) {
			textChannel.sendMessage("Could not restart track!").queue();
			return false;
		}
		return true;
	}
	
	public boolean shuffle() {
		if (queue.isEmpty()) return false;
		List<TrackContext> tracks = new ArrayList<>();
		queue.drainTo(tracks);
		Collections.shuffle(tracks);
		queue.addAll(tracks);
		tracks.clear();
		return true;
	}
	
	public void next(boolean skip) {
		if (isRepeat() && !skip && getCurrentTrack() != null) {
			start(getCurrentTrack().makeClone(), false);
			return;
		}
		start(queue.poll(), false);
	}
	
	public boolean isStopped() {
		return isEmpty() && getCurrentTrack() == null;
	}
	
	public void stop() {
		queue.clear();
		next(true);
		getGuild().getAudioManager().closeAudioConnection();
	}
	
	private void start(TrackContext track, boolean noInterrupt) {
		try {
			if (getCurrentTrack() != null) previousTrack = currentTrack;
			currentTrack = track;
			if (track == null) {
				getAudioPlayer().startTrack(null, noInterrupt);
			} else if (track.getTrack().getState() != AudioTrackState.INACTIVE) {
				getAudioPlayer().startTrack(track.getTrack().makeClone(), noInterrupt);
			} else {
				getAudioPlayer().startTrack(track.getTrack(), noInterrupt);
			}
			if (track == null) onQueueStop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean request(TrackContext track, boolean silent) {
		if (track.getInfo().length > AudioUtils.MAX_SONG_LENGTH) {
			track.getContext().sendMessage("Could not join `" + track.getInfo().title + "` to the queue. Reason: `This song it's too long, the maximum supported length is 3 hours!").queue();
			if (isEmpty() && getCurrentTrack() == null) getGuild().getAudioManager().closeAudioConnection();
			return false;
		}
		if (!canRequest(track)) {
			String s;
            switch (Bran.getInstance().getDataManager().getData().get().getGuildData(getGuild(), true).fairQueueLevel) {
                case 1:
					s = "Oops, it looks like you already asked for this song, why don't you try another one? (FairQueue: 1)";
					break;
				case 2:
					s = "Oops, it looks like this song is already in the queue, why don't you try another one? (FairQueue: 2)";
					break;
				default:
					s = "Unrecognized FairQueue Level '" +
                            Bran.getInstance().getDataManager().getData().get().getGuildData(getGuild(), true).fairQueueLevel + "'\nReport this Message to my Master.";
            }
			track.getContext().sendMessage(s).queue();
			return false;
		}
		if (!silent) {
			track.getContext().sendMessage("Queueing `" + track.getInfo().title + "` requested by " + Utils.getUser(track.getDJ())).queue(msg -> {
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
	
	public boolean canRequest(TrackContext track) {
        switch (Bran.getInstance().getDataManager().getData().get().getGuildData(getGuild(), true).fairQueueLevel) {
            case 0:
				return true;
			case 1:
				return getMatches(getRequestsFrom(track.getDJ()), track) < 1;
			case 2:
				return getMatches(queue, track) < 1;
			default:
				throw new UnsupportedOperationException("Unrecognized FairQueue Level '" +
                        Bran.getInstance().getDataManager().getData().get().getGuildData(getGuild(), true).fairQueueLevel + "'");
        }
	}
	
	private void onQueueStop() {
		if (getPreviousTrack() != null && getPreviousTrack().getContext() != null && getPreviousTrack().getContext().canTalk()) {
            getPreviousTrack().getContext().sendMessage("Finished playing queue, disconnecting... If you want to play more music use `!!play [SONG]`.").queue();
        }
		Bran.getInstance().getTaskManager().getMusicRegisterTimeout().addMusicPlayer(String.valueOf(getGuild().getId()), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
		getGuild().getAudioManager().closeAudioConnection();
	}
	
	public String getQueueDuration() {
		long duration = 0;
		if (getCurrentTrack() != null)
			duration += getCurrentTrack().getInfo().length - getCurrentTrack().getTrack().getPosition();
		for (TrackContext track : getRawQueue())
			duration += track.getTrack().getInfo().length;
		return AudioUtils.format(duration);
	}
	public List<String> getVoteSkips() {
		return voteSkips;
	}
	
	public AudioPlayer getAudioPlayer() {
		return player;
	}
	
	public Guild getGuild() {
		return getShard().getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public BlockingQueue<TrackContext> getQueue() {
		return queue;
	}
	
	public GuildMusicManager getMusicManager() {
		return musicManager;
	}
	
	public int getPosition(TrackContext trackContext) {
		return new ArrayList<>(queue).indexOf(trackContext);
	}
	
	public TrackContext getByPosition(int index) {
		LinkedList<TrackContext> list = new LinkedList<>(queue);
		if (index >= list.size()) return null;
		return list.get(index);
	}
	
	public boolean isPaused() {
		return getAudioPlayer().isPaused();
	}
	
	public void setPaused(boolean paused) {
		getAudioPlayer().setPaused(paused);
	}
    
    public Shard getShard() {
        return Bran.getInstance().getShards()[shard];
	}
	
	public List<TrackContext> getTracksBy(User user) {
		return queue.stream()
				.filter(track -> track.getDJId().equals(user.getId())).collect(Collectors.toList());
	}
	
	public int getRequiredSkipVotes() {
		int listeners = (int) getVoiceChannel().getMembers().stream()
				.filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
		return (int) Math.ceil(listeners * .55);
	}
	
	public void skip() {
		voteSkips.clear();
		next(true);
	}
	
	public VoiceChannel getVoiceChannel() {
		return getGuild().getAudioManager().getConnectedChannel();
	}
}

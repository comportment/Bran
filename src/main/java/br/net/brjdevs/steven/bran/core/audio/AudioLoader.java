package br.net.brjdevs.steven.bran.core.audio;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.net.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ResponseEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ResponseTimeoutEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AudioLoader implements AudioLoadResultHandler {
	
	public static final long MAX_SONG_LENGTH = 10800000;
	public static final long MAX_PLAYLIST_LENGTH = 108000000;
	public static final int MAX_QUEUE_SIZE = 600;
	
	private TextChannel channel;
	private User user;
	private String trackUrl;
	private GuildMusicManager musicManager;
	
	public AudioLoader(TextChannel channel, User user, String trackUrl, GuildMusicManager musicManager) {
		this.channel = channel;
		this.user = user;
		this.trackUrl = trackUrl;
		this.musicManager = musicManager;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
        GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(channel.getGuild(), true);
        if (track.getInfo().length > guildData.maxSongDuration) {
            channel.sendMessage("This song is too long! The maximum supported length is " + AudioUtils.format(guildData.maxSongDuration)).queue();
            if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
				channel.getGuild().getAudioManager().closeAudioConnection();
			return;
		}
		if (musicManager.getTrackScheduler().getQueue().size() > MAX_QUEUE_SIZE) {
			channel.sendMessage("Queue has reached its limit! (" + MAX_QUEUE_SIZE + ")").queue();
			return;
		}
		musicManager.getTrackScheduler().request(new TrackContext(track, user, channel, musicManager.getTrackScheduler()), false);
	}
	
	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		if (musicManager.getTrackScheduler().getQueue().size() > MAX_QUEUE_SIZE) {
			channel.sendMessage("Queue has reached its limit! (" + MAX_QUEUE_SIZE + ")").queue();
			if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
				channel.getGuild().getAudioManager().closeAudioConnection();
			return;
		}
		if (playlist.getTracks().isEmpty()) {
			channel.sendMessage("This playlist is empty!").queue();
		}
		if (playlist.getSelectedTrack() != null) {
			trackLoaded(playlist.getSelectedTrack());
		} else {
			List<TrackContext> playlistTracks = playlist.getTracks().stream().map(track -> new TrackContext(track, user, channel, musicManager.getTrackScheduler())).collect(Collectors.toList());
			if (playlist.isSearchResult()) {
                List<TrackContext> tracks = playlistTracks.stream().filter(track -> playlistTracks.indexOf(track) < 3).collect(Collectors.toList());
				channel.sendMessage(Utils.getUser(user) + ", results found by `" + trackUrl.substring(9).trim() + "`:\n" + String.join("\n", tracks.stream().map(track -> "`[" + (tracks.indexOf(track) + 1) + "]` " + track.getTrack().getInfo().title + " (`" + AudioUtils.format(track.getTrack().getInfo().length) + "`)").collect(Collectors.toList())) + "\n*This will expire in 30 seconds*").queue(msg -> {
					String[] inputs = new String[2 + tracks.size()];
					inputs[0] = "c";
					inputs[1] = "cancel";
					for (int i = tracks.size(); i > 0; i--) {
						inputs[1 + i] = String.valueOf(i);
					}
                    GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(channel.getGuild(), true);
                    new ResponseWaiter(user, channel, musicManager.getShard(), 30000, inputs, ExpectedResponseType.MESSAGE,
							(ResponseEvent responseEvent) -> {
                                try {
                                    if (responseEvent instanceof ValidResponseEvent) {
                                        Message m = ((Message) ((ValidResponseEvent) responseEvent).response);
                                        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                                            m.delete().queue();
                                        String response = m.getContent();
                                        if (!MathUtils.isInteger(response)) {
                                            channel.sendMessage("Query canceled!").queue();
                                            if (msg != null)
                                                msg.delete().queue();
                                            if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
                                                channel.getGuild().getAudioManager().closeAudioConnection();
                                            return;
                                        }
                                        int i = Integer.parseInt(response);
                                        TrackContext trackContext = tracks.get(i - 1);
                                        if (trackContext.getInfo().length > guildData.maxSongDuration) {
                                            channel.sendMessage("This song is too long! The maximum supported length is " + AudioUtils.format(guildData.maxSongDuration)).queue();
                                            if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
                                                channel.getGuild().getAudioManager().closeAudioConnection();
                                            return;
                                        }
                                        musicManager.getTrackScheduler().request(trackContext, false);
                                        if (msg != null)
                                            msg.delete().queue();
                                    } else if (responseEvent instanceof UnexpectedResponseEvent) {
                                        channel.sendMessage("You didn't type " + StringUtils.replaceLast(Arrays.stream(inputs).collect(Collectors.joining(", ")), ", ", " or ") + ", query canceled!").queue();
                                        if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
                                            channel.getGuild().getAudioManager().closeAudioConnection();
                                    } else if (responseEvent instanceof ResponseTimeoutEvent) {
                                        channel.sendMessage("You took too long to pick a song so I've picked the first song!").queue();
                                        musicManager.getTrackScheduler().request(tracks.get(0), false);
                                        
                                    }
                                } catch (NullPointerException ignored) {
                                }
                            });
				});
				return;
			}
			long duration = AudioUtils.getLength(playlist);
			if (duration > MAX_PLAYLIST_LENGTH) {
				if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
					channel.getGuild().getAudioManager().closeAudioConnection();
				channel.sendMessage("This playlist is too long! The maximum supported length is 30 hours. *" + AudioUtils.format(duration) + "/" + AudioUtils.format(MAX_PLAYLIST_LENGTH) + "*").queue();
				if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
					channel.getGuild().getAudioManager().closeAudioConnection();
				return;
			}
            GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(channel.getGuild(), true);
            channel.sendMessage("Queueing playlist " + playlist.getName() + " by " + Utils.getUser(user)).queue(msg -> {
				int queued = 0;
				for (TrackContext trackContext : playlistTracks) {
					if (trackContext.getInfo().length > guildData.maxSongDuration) {
						channel.sendMessage("Could not queue " + trackContext.getInfo().title + ": track is too long!").queue();
						continue;
					}
					musicManager.getTrackScheduler().request(trackContext, true);
					queued++;
					if (musicManager.getTrackScheduler().getQueue().size() > MAX_QUEUE_SIZE) {
						channel.sendMessage("Queue has reached its limit! (" + MAX_QUEUE_SIZE + "), queued " + queued + " tracks from playlist `" + playlist.getName() + "`.").queue();
						return;
					}
				}
				msg.editMessage(user.getAsMention() + " has added playlist `" + playlist.getName() + "` (`" + AudioUtils.format(duration) + "`) to the queue!").queue();
			});
		}
	}
	
	@Override
	public void noMatches() {
		channel.sendMessage("Nothing found by `" + (trackUrl.startsWith("ytsearch:") ? trackUrl.substring(9).trim() : trackUrl) + "`").queue();
		if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null && channel.getGuild().getAudioManager().isConnected())
			channel.getGuild().getAudioManager().closeAudioConnection();
	}
	
	@Override
	public void loadFailed(FriendlyException exception) {
		channel.sendMessage("Could not play the requested song: `" + exception.getMessage() + " (Severity: " + exception.severity + ")`").queue();
		if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null)
			channel.getGuild().getAudioManager().closeAudioConnection();
	}
}

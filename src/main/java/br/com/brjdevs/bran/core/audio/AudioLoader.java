package br.com.brjdevs.bran.core.audio;

import br.com.brjdevs.bran.core.action.Action;
import br.com.brjdevs.bran.core.action.Action.onInvalidResponse;
import br.com.brjdevs.bran.core.action.ActionType;
import br.com.brjdevs.bran.core.audio.impl.TrackContextImpl;
import br.com.brjdevs.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AudioLoader implements AudioLoadResultHandler {
	
	private TextChannel channel;
	private User user;
	private String trackUrl;
	private MusicManager musicManager;
	
	public AudioLoader(TextChannel channel, User user, String trackUrl, MusicManager musicManager) {
		this.channel = channel;
		this.user = user;
		this.trackUrl = trackUrl;
		this.musicManager = musicManager;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		String url = !track.getSourceManager().getSourceName().equalsIgnoreCase("youtube") ? trackUrl : "https://www.youtube.com/watch?v=" + track.getInfo().identifier;
		musicManager.getTrackScheduler().queue(track, url, user, channel);
	}
	
	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		if (playlist.getSelectedTrack() != null) {
			trackLoaded(playlist.getSelectedTrack());
		} else {
			List<TrackContext> playlistTracks = playlist.getTracks().stream().map(track -> new TrackContextImpl(track, !track.getSourceManager().getSourceName().equalsIgnoreCase("youtube") ? trackUrl : "https://www.youtube.com/watch?v=" + track.getInfo().identifier, user, channel)).collect(Collectors.toList());
			if (playlist.isSearchResult()) {
				List<TrackContext> tracks = playlistTracks.stream().filter(track -> playlistTracks.indexOf(track) < 3).collect(Collectors.toList());
				channel.sendMessage("Results found by `" + trackUrl.substring(9).trim() + "` *(say the number to choose one):*\n" + String.join("\n", tracks.stream().map(track -> "`[" + AudioUtils.decimal(tracks.indexOf(track) + 1) + "]` " + track.getTrack().getInfo().title + " (`" + AudioUtils.format(track.getTrack().getInfo().length) + "`)").collect(Collectors.toList()))).queue(msg -> {
					List<String> inputs = new ArrayList<>();
					inputs.add("c");
					inputs.add("cancel");
					for (int i = tracks.size(); i > 0; i--) {
						inputs.add(String.valueOf(i));
					}
					Action action = new Action(ActionType.MESSAGE, onInvalidResponse.CANCEL, msg, response -> {
						if (response.matches("^(c|cancel)$")) {
							if (msg != null)
								msg.editMessage("Query canceled!").queue();
							else
								channel.sendMessage("Query canceled!").queue();
							if (musicManager.getTrackScheduler().isStopped())
								channel.getGuild().getAudioManager().closeAudioConnection();
						} else if (Util.containsEqualsIgnoreCase(inputs, response)) {
							int i = Integer.parseInt(response);
							TrackContext trackContext = tracks.get(i - 1);
							musicManager.getTrackScheduler().queue(trackContext);
							if (msg != null)
								msg.deleteMessage().queue();
						} else {
							String string = "You didn't type " + StringUtils.replaceLast((String.join(", ", inputs.stream().map(s -> "`" + s + "`").collect(Collectors.toList()))), ", ", " or ") + ", query canceled!";
							if (msg != null)
								msg.editMessage(string).queue();
							else
								channel.sendMessage(string).queue();
							if (musicManager.getTrackScheduler().isStopped())
								channel.getGuild().getAudioManager().closeAudioConnection();
						}
					}, inputs);
					action.addUser(user);
				});
				return;
			}
			musicManager.getTrackScheduler().queue(playlist, playlistTracks, user, channel);
		}
	}
	
	@Override
	public void noMatches() {
		if (!trackUrl.startsWith("ytsearch:")) {
			AudioUtils.getManager().loadAndPlay(user, channel, "ytsearch:" + trackUrl);
		} else {
			channel.sendMessage("Nothing found by `" + trackUrl.substring(9).trim() + "`").queue();
			if (musicManager.getTrackScheduler().isStopped() && channel.getGuild().getAudioManager().isConnected())
				channel.getGuild().getAudioManager().closeAudioConnection();
		}
	}
	
	@Override
	public void loadFailed(FriendlyException exception) {
		channel.sendMessage("Could not play the requested song. `" + exception.getMessage() + "`").queue();
		if (musicManager.getTrackScheduler().isStopped())
			channel.getGuild().getAudioManager().closeAudioConnection();
	}
}

package br.com.brjdevs.bran.core.audio;

import br.com.brjdevs.bran.core.audio.utils.AudioUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AudioLoader implements AudioLoadResultHandler {
	
	private TextChannel channel;
	private User member;
	private String trackUrl;
	private GuildMusicManager musicManager;
	
	public AudioLoader(TextChannel channel, User member, String trackUrl, GuildMusicManager musicManager) {
		this.channel = channel;
		this.member = member;
		this.trackUrl = trackUrl;
		this.musicManager = musicManager;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		String url = !track.getSourceManager().getSourceName().equalsIgnoreCase("youtube") ? trackUrl : "https://www.youtube.com/watch?v=" + track.getInfo().identifier;
		musicManager.getTrackScheduler().queue(track, url, member, channel);
	}
	
	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		List<AudioTrack> tracks = playlist.getTracks().stream().filter(track -> playlist.getTracks().indexOf(track) < 3).collect(Collectors.toList());
		if (playlist.isSearchResult()) {
			channel.sendMessage("Results found by `" + trackUrl.substring(9).trim() + "` *(say the number to choose one):*\n" + String.join("\n", tracks.stream().map(track -> "`[" + AudioUtils.decimal(tracks.indexOf(track) + 1) + "]` " + track.getInfo().title + " (`" + AudioUtils.format(track.getInfo().length) + "`)").collect(Collectors.toList()))).queue(msg -> {
				Map<AudioTrack, String> map = new LinkedHashMap<>();
				for (AudioTrack track : tracks) {
					String url = !track.getSourceManager().getSourceName().equalsIgnoreCase("youtube") ? trackUrl : "https://www.youtube.com/watch?v=" + track.getInfo().identifier;
					map.put(track, url);
				}
				new Choice(member, channel, map, msg);
			});
			return;
		}
		Map<AudioTrack, String> map = new HashMap<>();
		for (AudioTrack track : tracks) {
			String url = !track.getSourceManager().getSourceName().equalsIgnoreCase("youtube") ? trackUrl : "https://www.youtube.com/watch?v=" + track.getInfo().identifier;
			map.put(track, url);
		}
		musicManager.getTrackScheduler().queue(playlist, map, member, channel);
	}
	
	@Override
	public void noMatches() {
		//List<YoutubeVideo> found = YoutubeAPI.searchForVideos(trackUrl);
		//if (found.isEmpty())
			//channel.sendMessage("Nothing found by `" + trackUrl + "`.").queue();
		//else
			//AudioUtils.getManager().loadAndPlay(member, channel, found.get(0).getId());
		if (!trackUrl.startsWith("ytsearch:")) {
			AudioUtils.getManager().loadAndPlay(member, channel, "ytsearch:" + trackUrl);
		} else {
			channel.sendMessage("Nothing found by `" + trackUrl.substring(9).trim() + "`").queue();
		}
	}
	
	@Override
	public void loadFailed(FriendlyException exception) {
		channel.sendMessage("Could not play the requested song. `" + exception.getMessage() + "`").queue();
	}
}

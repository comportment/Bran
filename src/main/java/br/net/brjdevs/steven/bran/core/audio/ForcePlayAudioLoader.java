package br.net.brjdevs.steven.bran.core.audio;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class ForcePlayAudioLoader implements AudioLoadResultHandler {
    
    public static final long MAX_SONG_LENGTH = 10800000;
    public static final long MAX_PLAYLIST_LENGTH = 108000000;
    public static final int MAX_QUEUE_SIZE = 600;
    
    private TextChannel channel;
    private User user;
    private String trackUrl;
    private GuildMusicManager musicManager;
    
    public ForcePlayAudioLoader(TextChannel channel, User user, String trackUrl, GuildMusicManager musicManager) {
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
                trackLoaded(playlist.getTracks().get(0));
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

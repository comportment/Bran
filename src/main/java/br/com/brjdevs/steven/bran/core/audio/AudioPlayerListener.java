package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.concurrent.BlockingQueue;

public class AudioPlayerListener extends AudioEventAdapter {
	
	private static final String announce = "\uD83D\uDD0A Now Playing in **%s**: `%s` (`%s`) added by %s.";
	private static final SimpleLog LOG = SimpleLog.getLog("AudioPlayerListener");
	private TrackScheduler scheduler;
	private long messageId;
	private long channelId;
	
	public AudioPlayerListener(TrackScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public BlockingQueue<TrackContext> getQueue() {
		return scheduler.getQueue();
	}
	
	public VoiceChannel getVoiceChannel() {
		return scheduler.getGuild().getAudioManager().isAttemptingToConnect() ? scheduler.getGuild().getAudioManager().getQueuedAudioConnection() : scheduler.getGuild().getAudioManager().getConnectedChannel();
	}
	
	private TextChannel getTextChannel() {
		return scheduler.getShard().getJDA().getTextChannelById(String.valueOf(channelId));
	}
	
	private Message getMessage() {
		try {
			return getTextChannel().getMessageById(String.valueOf(messageId)).complete();
		} catch (Exception e) {
			return null;
		}
	}
	
	private void setMessage(Message message) {
		this.messageId = Long.parseLong(message.getId());
		this.channelId = Long.parseLong(message.getChannel().getId());
	}
	
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		Bran.getInstance().getMusicManager().playedSongs.incrementAndGet();
		Bran.getInstance().getTaskManager().getMusicRegisterTimeout().removeMusicPlayer(scheduler.getGuild().getId());
		if (scheduler.getCurrentTrack() == null) {
			LOG.fatal("Got TrackStartEvent with null CachedAudioTrack!");
			return;
		}
		if (scheduler.getCurrentTrack().getContext() != null && scheduler.getCurrentTrack().getContext().canTalk()) {
			scheduler.getCurrentTrack().getContext().sendMessage(String.format(announce, getVoiceChannel() == null ? "Not Connected." : getVoiceChannel().getName(), track.getInfo().title, AudioUtils.format(track.getInfo().length), Utils.getUser(scheduler.getCurrentTrack().getDJ()))).queue(message -> {
				Message m = getMessage();
				if (m != null && !m.isEdited()) m.delete().queue();
				setMessage(message);
			});
		}
	}
	
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		scheduler.getVoteSkips().clear();
		if (endReason.mayStartNext) {
			scheduler.next(false);
		}
	}
	
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		if (scheduler.getCurrentTrack().getContext() != null && scheduler.getCurrentTrack().getContext().canTalk()) {
			String string = "\u274c Failed to play `" + track.getInfo().title + "`!\n" +
					(exception.severity.equals(Severity.COMMON) ? StringUtils.capitalize(track.getSourceManager().getSourceName()) + " said: " : exception.severity.equals(Severity.SUSPICIOUS) ? "I don't know what exactly caused it, but I've got this: " : "This error might be caused by the library (Lavaplayer) or an external unidentified factor: ") + "`" + exception.getMessage() + "`";
			Message msg = getMessage();
			if (msg != null)
				msg.editMessage(string).queue();
			else
				scheduler.getCurrentTrack().getContext().sendMessage(string).queue();
		}
	}
	
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		if (scheduler.getCurrentTrack().getContext() != null && scheduler.getCurrentTrack().getContext().canTalk())
			scheduler.getCurrentTrack().getContext().sendMessage("Track got stuck, skipping...").queue();
	}
}

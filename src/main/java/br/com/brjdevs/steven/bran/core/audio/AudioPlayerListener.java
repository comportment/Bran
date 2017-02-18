package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
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

public class AudioPlayerListener extends AudioEventAdapter {
	
	private static final String announce = "\uD83D\uDD0A Now Playing in **%s**: `%s` (`%s`) added by %s.";
	private static final SimpleLog LOG = SimpleLog.getLog("AudioPlayerListener");
	private TrackScheduler scheduler;
	private Client client;
	private long messageId;
	private long channelId;
	
	public AudioPlayerListener(TrackScheduler scheduler) {
		this.scheduler = scheduler;
		this.client = scheduler.client;
	}
	
	public FairQueue<TrackContext> getQueue() {
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
		client.taskManager.getMusicRegisterTimeout().removeMusicPlayer(scheduler.getGuild().getId());
		if (getQueue().getCurrentTrack() == null) {
			LOG.fatal("Got TrackStartEvent with null CachedAudioTrack!");
			return;
		}
		if (getQueue().getCurrentTrack().getContext() != null && getQueue().getCurrentTrack().getContext().canTalk()) {
			getQueue().getCurrentTrack().getContext().sendMessage(String.format(announce, getVoiceChannel() == null ? "Not Connected." : getVoiceChannel().getName(), track.getInfo().title, AudioUtils.format(track.getInfo().length), OtherUtils.getUser(getQueue().getCurrentTrack().getDJ()))).queue(message -> {
				Message m = getMessage();
				if (m != null && !m.isEdited()) m.delete().queue();
				setMessage(message);
			});
		}
	}
	
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		scheduler.getVoteSkips().clear();
		if (endReason.mayStartNext) {
			getQueue().next(false);
		}
	}
	
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		if (getQueue().getCurrentTrack().getContext() != null && getQueue().getCurrentTrack().getContext().canTalk()) {
			String string = "\u274c Failed to play `" + track.getInfo().title + "`!\n" +
					(exception.severity.equals(Severity.COMMON) ? StringUtils.neat(track.getSourceManager().getSourceName()) + " said: " : exception.severity.equals(Severity.SUSPICIOUS) ? "I don't know what exactly caused it, but I've got this: " : "This error might be caused by the library (Lavaplayer) or an external unidentified factor: ") + "`" + exception.getMessage() + "`";
			Message msg = getMessage();
			if (msg != null)
				msg.editMessage(string).queue();
			else
				getQueue().getCurrentTrack().getContext().sendMessage(string).queue();
			getQueue().next(true);
		}
	}
	
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		if (getQueue().getCurrentTrack().getContext() != null && getQueue().getCurrentTrack().getContext().canTalk())
			getQueue().getCurrentTrack().getContext().sendMessage("Track got stuck, skipping...").queue();
		getQueue().next(true);
	}
	
	public void onPlayerPause(AudioPlayer player) {
	}
	
	public void onPlayerResume(AudioPlayer player) {
	}
}

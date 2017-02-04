package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.audio.utils.VoiceChannelListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ConnectionListenerImpl implements net.dv8tion.jda.core.audio.hooks.ConnectionListener {
	
	public BotContainer container;
	private int attempts;
	private long guildId;
	private int shard;
	private Message message;
	
	public ConnectionListenerImpl(Guild guild, BotContainer container) {
		this.attempts = -1;
		this.guildId = Long.parseLong(guild.getId());
		this.container = container;
		this.shard = container.getShardId(guild.getJDA());
	}
	
	@Override
	public void onPing(long l) {
	}
	
	@Override
	public void onStatusChange(ConnectionStatus connectionStatus) {
		TrackScheduler scheduler = getMusicManager().getTrackScheduler();
		if (scheduler.isStopped()) return;
		if (connectionStatus == ConnectionStatus.CONNECTING_AWAITING_ENDPOINT) {
			send("Connecting to " + getGuild().getAudioManager().getQueuedAudioConnection().getName() + "... *(Attempt " + attempts + ")*");
		} else if (connectionStatus == ConnectionStatus.CONNECTED) {
			if (attempts > 0)
				send("Stabilized connection with Voice Channel `" + getGuild().getAudioManager().getConnectedChannel().getName() + "`!");
			if (!scheduler.isStopped() && scheduler.isPaused())
				scheduler.setPaused(false);
			attempts = 0;
			if (AudioUtils.isAlone(getGuild().getAudioManager().getConnectedChannel()) && !container.taskManager.getChannelLeaveTimer().has(getGuild().getId()))
				VoiceChannelListener.onLeave(getGuild(), getGuild().getAudioManager().getConnectedChannel());
		} else if (connectionStatus == ConnectionStatus.AUDIO_REGION_CHANGE) {
			scheduler.setPaused(true);
			send("I detected a Region Change in this Guild, just give me a second to create a new connection, okay?");
		} else if (connectionStatus.name().startsWith("ERROR_")) {
			attempts += 1;
			if (attempts > 3) {
				send("I failed to reconnect 3 times, have you tried changing the server region?");
				getGuild().getAudioManager().closeAudioConnection();
				this.attempts = 0;
				return;
			}
			send("Uh-oh, I had a small problem with the Audio Connection (" + connectionStatus.name() + "), I'll try to reconnect tree times, if I can't do it, I'll stop the Music Player.");
		} else if (connectionStatus == ConnectionStatus.DISCONNECTED_CHANNEL_DELETED) {
			send("The channel I was connected to got deleted, stopped the queue.");
			scheduler.stop();
		}
	}
	
	@Override
	public void onUserSpeaking(User user, boolean b) {
	}
	
	public MusicManager getMusicManager() {
		return container.playerManager.get(getGuild());
	}
	
	public Guild getGuild() {
		return container.getShards()[shard].getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public void send(String content) {
		if (message != null) message.deleteMessage().queue();
		TrackScheduler scheduler = getMusicManager().getTrackScheduler();
		JDA jda = scheduler.getShard().getJDA();
		TextChannel textChannel = null;
		if (scheduler.getCurrentTrack() != null)
			textChannel = scheduler.getCurrentTrack().getContext(jda);
		if (textChannel == null && scheduler.getPreviousTrack() != null)
			textChannel = scheduler.getPreviousTrack().getContext(jda);
		if (textChannel != null && textChannel.canTalk()) {
			textChannel.sendMessage(content).queue(this::setMessage);
		}
	}
	
	private void setMessage(Message message) {
		this.message = message;
	}
}

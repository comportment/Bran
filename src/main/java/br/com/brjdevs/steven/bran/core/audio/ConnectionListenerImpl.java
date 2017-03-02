package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.core.client.Client;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ConnectionListenerImpl implements net.dv8tion.jda.core.audio.hooks.ConnectionListener {
	
	public Client client;
	private int attempts;
	private long guildId;
	private int shard;
	private Message message;
	
	public ConnectionListenerImpl(Guild guild, Client client) {
		this.attempts = -1;
		this.guildId = Long.parseLong(guild.getId());
		this.client = client;
		this.shard = client.getShardId(guild.getJDA());
	}
	
	@Override
	public void onPing(long l) {
	}
	
	@Override
	public void onStatusChange(ConnectionStatus connectionStatus) {
		TrackScheduler scheduler = getMusicManager().getTrackScheduler();
		if (scheduler.getQueue().isEmpty() && scheduler.getCurrentTrack() == null) return;
		if (connectionStatus == ConnectionStatus.CONNECTING_AWAITING_ENDPOINT) {
			send("Connecting to " + getGuild().getAudioManager().getQueuedAudioConnection().getName() + "... *(Attempt " + attempts + ")*");
		} else if (connectionStatus == ConnectionStatus.CONNECTED) {
			if (attempts > 0)
				send("Stabilized connection with Voice Channel `" + getGuild().getAudioManager().getConnectedChannel().getName() + "`!");
			if (!(scheduler.getQueue().isEmpty() && scheduler.getQueue().isEmpty()) && scheduler.isPaused())
				scheduler.setPaused(false);
			attempts = 0;
		} else if (connectionStatus == ConnectionStatus.AUDIO_REGION_CHANGE) {
			scheduler.setPaused(true);
			send("I detected a Region Change in this Guild, just give me a second to create a new connection, okay?");
		} else if (connectionStatus.name().startsWith("ERROR_")) {
			attempts += 1;
			if (attempts > 3) {
				send("I failed to reconnect 3 times, have you tried changing the server region?");
				getMusicManager().getTrackScheduler().stop();
				this.attempts = 0;
				return;
			}
			send("Uh-oh, I had a small problem with the Audio Connection (" + connectionStatus.name() + "), I'll try to reconnect tree times, if I can't do it, I'll stop the Music Player.");
		} else if (connectionStatus == ConnectionStatus.DISCONNECTED_CHANNEL_DELETED) {
			send("The channel I was connected to got deleted, stopped the queue.");
			scheduler.stop();
		} else if (connectionStatus == ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD) {
			client.getMusicManager().unregister(guildId);
		}
	}
	
	@Override
	public void onUserSpeaking(User user, boolean b) {
	}
	
	public GuildMusicManager getMusicManager() {
		return client.getMusicManager().get(getGuild());
	}
	
	public Guild getGuild() {
		return client.getShards()[shard].getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public void send(String content) {
		if (message != null) message.delete().queue();
		TrackScheduler scheduler = getMusicManager().getTrackScheduler();
		if (scheduler.getCurrentTrack() != null) {
			TextChannel textChannel = scheduler.getCurrentTrack().getContext();
			textChannel.sendMessage(content).queue(this::setMessage);
		}
	}
	
	private void setMessage(Message message) {
		this.message = message;
	}
}

package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.audio.AudioUtils;
import br.net.brjdevs.steven.bran.core.audio.GuildMusicManager;
import br.net.brjdevs.steven.bran.core.audio.TrackContext;
import br.net.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;

import java.util.concurrent.TimeUnit;

public class VoiceChannelListener extends EventListener<GenericGuildVoiceEvent> {
	
	public VoiceChannelListener() {
		super(GenericGuildVoiceEvent.class);
	}
	
	private void onJoin(Guild guild, VoiceChannel voiceChannel, Member member) {
		ChannelLeaveTimer timer = Bran.getInstance().getTaskManager().getChannelLeaveTimer();
		if (!timer.has(guild.getId())) return;
		GuildMusicManager player = Bran.getInstance().getMusicManager().get(guild);
		TrackContext track = player.getTrackScheduler().getCurrentTrack();
		VoiceChannel channel = guild.getJDA().getVoiceChannelById(timer.get(guild.getId()).getRight());
		if (voiceChannel != channel && !member.equals(guild.getSelfMember()))
			return;
		if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect())
			AudioUtils.connect(channel, track.getContext());
		player.getTrackScheduler().setPaused(false);
		if (track != null && track.getContext() != null && track.getContext().canTalk())
			track.getContext().sendMessage(member.equals(guild.getSelfMember()) ? "Resumed the player!" : Utils.getUser(member.getUser()) + " joined the channel, resumed the player!").queue();
		timer.removeMusicPlayer(guild.getId());
	}
	
	public void onLeave(Guild guild, VoiceChannel voiceChannel) {
		if (guild == null || guild.getSelfMember() == null) return;
		ChannelLeaveTimer timer = Bran.getInstance().getTaskManager().getChannelLeaveTimer();
		if (!AudioUtils.isAlone(voiceChannel)) return;
		GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(guild);
		TrackContext track = musicManager.getTrackScheduler().getCurrentTrack();
		if (musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().getCurrentTrack() == null) {
			guild.getAudioManager().closeAudioConnection();
			return;
		}
		musicManager.getTrackScheduler().setPaused(true);
		if (track == null) track = musicManager.getTrackScheduler().getCurrentTrack();
		if (track != null && track.getContext() != null && track.getContext().canTalk())
			track.getContext().sendMessage("I was left alone in `" + voiceChannel.getName() + "`, so I paused the player. If nobody reenter in this channel I'll stop que player, clean the queue and leave the channel.").queue();
		timer.addMusicPlayer(guild.getId(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2), voiceChannel.getId());
	}
	
	
	@Override
	public void onEvent(GenericGuildVoiceEvent event) {
		if (event.getGuild() == null) return;
		if (event instanceof GuildVoiceMoveEvent) {
			VoiceChannel joined = ((GuildVoiceMoveEvent) event).getChannelJoined();
			VoiceChannel left = ((GuildVoiceMoveEvent) event).getChannelLeft();
			boolean isSelf = event.getMember().equals(event.getGuild().getSelfMember());
			if (isSelf) {
                if (AudioUtils.isAlone(joined))
                    onLeave(joined.getGuild(), joined);
				else
					onJoin(joined.getGuild(), joined, event.getMember());
			} else {
                if (AudioUtils.isAlone(left) && left == event.getGuild().getAudioManager().getConnectedChannel())
                    onLeave(left.getGuild(), left);
                else if (!AudioUtils.isAlone(joined) && joined == event.getGuild().getAudioManager().getConnectedChannel())
                    onJoin(joined.getGuild(), joined, event.getMember());
			}
			
		} else if (event instanceof GuildVoiceJoinEvent) {
			if (((GuildVoiceJoinEvent) event).getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
				onJoin(event.getGuild(), ((GuildVoiceJoinEvent) event).getChannelJoined(), event.getMember());
			}
		} else if (event instanceof GuildVoiceLeaveEvent) {
			if (((GuildVoiceLeaveEvent) event).getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel())
				onLeave(event.getGuild(), ((GuildVoiceLeaveEvent) event).getChannelLeft());
		}
	}
}
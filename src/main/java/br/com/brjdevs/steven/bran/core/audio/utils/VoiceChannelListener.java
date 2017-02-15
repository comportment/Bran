package br.com.brjdevs.steven.bran.core.audio.utils;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.concurrent.TimeUnit;

import static br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils.isAlone;

public class VoiceChannelListener implements EventListener {
	
	public static BotContainer container;
	
	public VoiceChannelListener(BotContainer container) {
		VoiceChannelListener.container = container;
	}
	
	private static void onJoin(Guild guild, VoiceChannel voiceChannel, Member member) {
		ChannelLeaveTimer timer = container.taskManager.getChannelLeaveTimer();
		if (!timer.has(guild.getId())) return;
		MusicManager player = container.playerManager.get(guild);
		TrackContext track = player.getTrackScheduler().getCurrentTrack();
		if (track == null) track = player.getTrackScheduler().getPreviousTrack();
		VoiceChannel channel = guild.getJDA().getVoiceChannelById(timer.get(guild.getId()).right);
		if (voiceChannel != channel && !member.equals(guild.getSelfMember())) return;
		if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect())
			AudioUtils.connect(channel, track.getContext(channel.getJDA()), container);
		player.getTrackScheduler().setPaused(false);
		if (track != null && track.getContext(guild.getJDA()) != null && track.getContext(guild.getJDA()).canTalk())
			track.getContext(guild.getJDA()).sendMessage(member.equals(guild.getSelfMember()) ? "Resumed the player!" : Util.getUser(member.getUser()) + " joined the channel, resumed the player!").queue();
		timer.removeMusicPlayer(guild.getId());
	}
	
	public static void onLeave(Guild guild, VoiceChannel voiceChannel) {
		if (guild == null) return;
		ChannelLeaveTimer timer = container.taskManager.getChannelLeaveTimer();
		if (!AudioUtils.isAlone(voiceChannel)) return;
		MusicManager musicManager = container.playerManager.get(guild);
		TrackContext track = musicManager.getTrackScheduler().getCurrentTrack();
		if (musicManager.getTrackScheduler().isStopped()) {
			guild.getAudioManager().closeAudioConnection();
			return;
		}
		musicManager.getTrackScheduler().setPaused(true);
		if (track == null) track = musicManager.getTrackScheduler().getPreviousTrack();
		if (track != null
				&& track.getContext(guild.getJDA()) != null
				&& track.getContext(guild.getJDA()).canTalk())
			track.getContext(guild.getJDA()).sendMessage("I was left alone in `" + voiceChannel.getName() + "`, so I paused the player. If nobody reenter in this channel I'll stop que player, clean the queue and leave the channel.").queue();
		timer.addMusicPlayer(guild.getId(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2), voiceChannel.getId());
	}
	
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof GenericGuildVoiceEvent) {
			GenericGuildVoiceEvent event = (GenericGuildVoiceEvent) e;
			if (event.getGuild() == null) return;
			if (event instanceof GuildVoiceMoveEvent) {
				VoiceChannel joined = ((GuildVoiceMoveEvent) event).getChannelJoined();
				VoiceChannel left = ((GuildVoiceMoveEvent) event).getChannelLeft();
				boolean isSelf = event.getMember().equals(event.getGuild().getSelfMember());
				if (isSelf) {
					if (isAlone(joined))
						onLeave(joined.getGuild(), joined);
					else
						onJoin(joined.getGuild(), joined, event.getMember());
				} else {
					if (isAlone(left) && left == event.getGuild().getAudioManager().getConnectedChannel())
						onLeave(left.getGuild(), left);
					else if (!isAlone(joined) && joined == event.getGuild().getAudioManager().getConnectedChannel())
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
}
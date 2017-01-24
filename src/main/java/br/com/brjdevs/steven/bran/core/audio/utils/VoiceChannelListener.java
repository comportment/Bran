package br.com.brjdevs.steven.bran.core.audio.utils;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class VoiceChannelListener implements EventListener {
	
	public static final JsonObject musicTimeout = new JsonObject();
	
	private static void onJoin(Guild guild, VoiceChannel voiceChannel, Member member) {
		if (member.equals(guild.getSelfMember()) || !musicTimeout.has(guild.getId())) return;
		MusicManager player = AudioUtils.getManager().get(guild);
		TrackContext track = player.getTrackScheduler().getCurrentTrack();
		if (track == null) track = player.getTrackScheduler().getPreviousTrack();
		JsonObject info = musicTimeout.get(guild.getId()).getAsJsonObject();
		VoiceChannel channel = guild.getJDA().getVoiceChannelById(info.get("channelId").getAsString());
		if (channel == null || channel != voiceChannel) return;
		if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) AudioUtils.connect(channel, track.getContext(channel.getJDA()));
		player.getPlayer().setPaused(false);
		if (track != null && track.getContext(guild.getJDA()) != null && track.getContext(guild.getJDA()).canTalk())
			track.getContext(guild.getJDA()).sendMessage(Util.getUser(member.getUser()) + " joined the channel, resumed the player!").queue();
		musicTimeout.remove(guild.getId());
	}

	private static void onLeave(Guild guild, VoiceChannel voiceChannel) {
		if (!AudioUtils.isAlone(voiceChannel)) return;
		MusicManager musicManager = AudioUtils.getManager().get(guild);
		TrackContext track = musicManager.getTrackScheduler().getCurrentTrack();
		if (musicManager.getTrackScheduler().isStopped()) {
			guild.getAudioManager().closeAudioConnection();
			return;
		}
		musicManager.getPlayer().setPaused(true);
		if (track == null) track = musicManager.getTrackScheduler().getPreviousTrack();
		if (track != null && track.getContext(guild.getJDA()) != null && track.getContext(guild.getJDA()).canTalk())
			track.getContext(guild.getJDA()).sendMessage("I was left alone in `" + voiceChannel.getName() + "`, so I paused the player. If nobody reenter in this channel I'll stop que player, clean the queue and leave the channel.").queue();
		JsonObject info = new JsonObject();
		info.addProperty("channelId", voiceChannel.getId());
		info.addProperty("timeout", 120);
		info.addProperty("shard", Bot.getShardId(guild.getJDA()));
		musicTimeout.add(guild.getId(), info);
	}
	
	
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GenericGuildVoiceEvent)) return;
		GenericGuildVoiceEvent event = (GenericGuildVoiceEvent) e;
		if (event instanceof GuildVoiceMoveEvent) {
			onLeave(event.getGuild(), ((GuildVoiceMoveEvent) event).getChannelLeft());
			onJoin(event.getGuild(), ((GuildVoiceMoveEvent) event).getChannelJoined(), event.getMember());
		} else if (event instanceof GuildVoiceJoinEvent)
			onJoin(event.getGuild(), ((GuildVoiceJoinEvent) event).getChannelJoined(), event.getMember());
		else if (event instanceof GuildVoiceLeaveEvent)
			onLeave(event.getGuild(), ((GuildVoiceLeaveEvent) event).getChannelLeft());
	}
}
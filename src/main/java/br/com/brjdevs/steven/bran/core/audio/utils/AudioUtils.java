package br.com.brjdevs.steven.bran.core.audio.utils;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.ConnectionListenerImpl;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class AudioUtils {
	
	public static String format(long length) {
		long hours = length / 3600000L % 24,
				minutes = length / 60000L % 60,
				seconds = length / 1000L % 60;
		return (hours == 0 ? "" : decimal(hours) + ":")
				+ (minutes == 0 ? "00" : decimal(minutes)) + ":" + (seconds == 0 ? "00" : decimal(seconds));
	}
	
	public static boolean canConnect(BotContainer container) {
		return container.getSession().cpuUsage < 40;
	}
	public static long getLength(AudioPlaylist audioPlaylist) {
		long[] total = {0};
		audioPlaylist.getTracks().forEach(track -> total[0] += track.getInfo().length);
		return total[0];
	}
	public static String format(AudioTrack track) {
		return format(track.getInfo().length);
	}
	public static String decimal(long num) {
		if (num > 9) return String.valueOf(num);
		return "0" + num;
	}
	
	public static VoiceChannel connect(VoiceChannel vchan, TextChannel tchan, BotContainer container) {
		if (!canConnect(container)) {
			tchan.sendMessage("I'm sorry about this but I'm a bit overloaded right now (" + container.getSession().cpuUsage + "% cpu) so can you ask me to join the channel later? I can't push myself too hard, I'm still in testing phase \uD83D\uDE26").queue();
			return null;
		}
		String warning = "Please note, this guild is located in Brazil so if you notice some slutter or can't hear the song consider changing the server region. (Recommended Region: US South)";
		boolean shouldWarn = vchan.getGuild().getRegion() == Region.BRAZIL;
		if (!vchan.getGuild().getSelfMember().hasPermission(vchan, Permission.VOICE_CONNECT)) {
			tchan.sendMessage("I can't connect to `" + vchan.getName() + "` due to a lack of permission!").queue();
			return null;
		}
		Member selfMember = vchan.getGuild().getSelfMember();
		if (!selfMember.hasPermission(vchan, Permission.VOICE_CONNECT)) {
			tchan.sendMessage("I can't join `" + vchan.getName() + "` due to a lack of permission. (VOICE_CONNECT)").queue();
			return null;
		}
		if (!selfMember.hasPermission(vchan, Permission.VOICE_SPEAK)) {
			tchan.sendMessage("I won't join `" + vchan.getName() + "` because I don't have `VOICE_SPEAK` permission!").queue();
			return null;
		}
		if (selfMember.getVoiceState().isGuildMuted()) {
			tchan.sendMessage("I won't join `" + vchan.getName() + "` because someone's muted me!").queue();
			return null;
		}
		AudioManager audioManager = vchan.getGuild().getAudioManager();
		if (selfMember.getVoiceState().isSelfMuted())
			audioManager.setSelfMuted(false);
		if (audioManager.isConnected()) return audioManager.getConnectedChannel();
		try {
			audioManager.setSelfDeafened(true);
			audioManager.setConnectionListener(new ConnectionListenerImpl(vchan.getGuild(), container));
			audioManager.openAudioConnection(vchan);
		} catch (Exception e) {
			tchan.sendMessage("I couldn't connect to the voice channel! " + (shouldWarn ? "\n" + warning : "I'm not sure why... `" + e.getMessage())).queue();
			return null;
		}
		while (audioManager.isAttemptingToConnect())
			Util.sleep(100);
		return audioManager.getConnectedChannel();
	}
	public static boolean isAlone(VoiceChannel channel) {
		for (Member member : channel.getMembers())
			if (!member.getUser().isBot()) return false;
		return true;
	}
	public static boolean isAllowed(User user, TrackContext context) {
		return context.getDJ(user.getJDA()) != null && context.getDJId().equals(user.getId());
	}
}

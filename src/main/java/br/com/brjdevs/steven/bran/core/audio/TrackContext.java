package br.com.brjdevs.steven.bran.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public interface TrackContext {
	
	String getSourceName();
	
	String getURL();
	
	String getDJId();
	
	String getContextId();
	
	JDA getJDA();
	
	default User getDJ() {
		return getJDA().getUserById(getDJId());
	}
	
	default TextChannel getContext() {
		return getJDA().getTextChannelById(getContextId());
	}
	
	default AudioTrackInfo getInfo() {
		return getTrack().getInfo();
	}
	
	void setPosition(long position);
	
	AudioTrack getTrack();
	
	<T extends TrackContext> T makeClone();
	
	TrackScheduler getTrackScheduler();
}

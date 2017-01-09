package br.com.brjdevs.bran.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public interface TrackContext {
	
	String getSourceName();
	
	String getURL();
	
	String getDJId();
	
	String getContextId();
	
	default User getDJ(JDA jda) {
		return jda.getUserById(getDJId());
	}
	
	default TextChannel getContext(JDA jda) {
		return jda.getTextChannelById(getContextId());
	}
	
	void setPosition(long position);
	
	AudioTrack getTrack();
	
	TrackContext makeClone();
}

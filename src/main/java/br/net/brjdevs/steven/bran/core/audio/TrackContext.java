package br.net.brjdevs.steven.bran.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class TrackContext {
	
	private TrackScheduler scheduler;
	private AudioTrack track;
	private String djId;
	private String contextId;
	
	public TrackContext(AudioTrack track, User dj, TextChannel context, TrackScheduler scheduler) {
		this.scheduler = scheduler;
		this.track = track;
		this.djId = dj.getId();
		this.contextId = context.getId();
	}
	
	public String getSourceName() {
		return track.getSourceManager().getSourceName();
	}
	
	public String getURL() {
		return getInfo().uri;
	}
	
	public String getDJId() {
		return djId;
	}
	
	public String getContextId() {
		return contextId;
	}
	
	public JDA getJDA() {
		return scheduler.getShard().getJDA();
	}
	
	public void setPosition(long position) {
		getTrack().setPosition(position);
	}
	
	public AudioTrack getTrack() {
		return track;
	}
	
	public TrackContext makeClone() {
		this.track = track.makeClone();
		return this;
	}
	
	public TextChannel getContext() {
		return getJDA().getTextChannelById(getContextId());
	}
	
	public User getDJ() {
		return getJDA().getUserById(getDJId());
	}
	
	public TrackScheduler getTrackScheduler() {
		return scheduler;
	}
	
	public AudioTrackInfo getInfo() {
		return getTrack().getInfo();
	}
}

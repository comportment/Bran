package br.com.brjdevs.bran.core.audio.impl;

import br.com.brjdevs.bran.core.audio.TrackContext;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class TrackContextImpl implements TrackContext {
	private AudioTrack track;
	private String url;
	private String djId;
	private String contextId;
	
	public TrackContextImpl(AudioTrack track, String url, User dj, TextChannel context) {
		this.track = track;
		this.url = url;
		this.djId = dj.getId();
		this.contextId = context.getId();
	}
	
	@Override
	public String getSourceName() {
		return track.getSourceManager().getSourceName();
	}
	
	@Override
	public String getURL() {
		return url;
	}
	
	@Override
	public String getDJId() {
		return djId;
	}
	
	@Override
	public String getContextId() {
		return contextId;
	}
	
	@Override
	public void setPosition(long position) {
		getTrack().setPosition(position);
	}
	
	@Override
	public AudioTrack getTrack() {
		return track;
	}
	
	@Override
	public TrackContext makeClone() {
		this.track = track.makeClone();
		return this;
	}
}

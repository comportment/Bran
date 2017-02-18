package br.com.brjdevs.steven.bran.core.data.guild.settings;

public class MusicSettings {
	
	private long maxSongsPerUser;
	private int fairQueueLevel;
	
	public MusicSettings() {
		this.maxSongsPerUser = -1L;
		this.fairQueueLevel = 0;
	}
	
	public int getFairQueueLevel() {
		return fairQueueLevel;
	}
	
	public void setFairQueueLevel(int fairQueueLevel) {
		this.fairQueueLevel = fairQueueLevel;
	}
	
	public long getMaxSongsPerUser() {
		return maxSongsPerUser;
	}
	
	public void setMaxSongsPerUser(long maxSongsPerUser) {
		this.maxSongsPerUser = maxSongsPerUser;
	}
}
package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskManager {
	
	public Client client;
	private MusicRegisterTimeout musicRegisterTimeout;
	private ChannelLeaveTimer channelLeaveTimer;
	
	public TaskManager(Client client) {
		this.client = client;
		this.musicRegisterTimeout = new MusicRegisterTimeout(client);
		this.channelLeaveTimer = new ChannelLeaveTimer(client);
		startAsyncTasks();
	}
	
	public ChannelLeaveTimer getChannelLeaveTimer() {
		return channelLeaveTimer;
	}
	
	public void setChannelLeaveTimer(ChannelLeaveTimer channelLeaveTimer) {
		this.channelLeaveTimer = channelLeaveTimer;
	}
	
	public MusicRegisterTimeout getMusicRegisterTimeout() {
		return musicRegisterTimeout;
	}
	
	public void setMusicRegisterTimeout(MusicRegisterTimeout musicRegisterTimeout) {
		this.musicRegisterTimeout = musicRegisterTimeout;
	}
	
	public void startAsyncTask(Runnable run, int seconds) {
		Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Async Task-%d").build()).scheduleAtFixedRate(
				run, 0, seconds, TimeUnit.SECONDS);
	}
	
	private void startAsyncTasks() {
	    final OperatingSystemMXBean os =
			    ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
	    startAsyncTask(
			    () -> client.getSession().cpuUsage = (Math.floor(os.getProcessCpuLoad() * 10000) / 100), 5);
		startAsyncTask(() -> Arrays.stream(client.getShards()).forEach(shard -> {
			if (shard.getCurrentGuildCount() > shard.getJDA().getGuilds().size()) return;
			try {
				shard.updateStats();
				shard.updateCurrentGuildCount();
				client.getDiscordLog().logToDiscord("Updated server_count in DiscordBots (Shard[" + shard.getId() + "])", "Successfully updated server_count at [Discord Bots](https://bots.discord.pw/bots/" + shard.getJDA().getSelfUser().getId() + ")", Level.INFO);
			} catch (UnirestException e) {
				client.getDiscordLog()
						.logToDiscord("Failed to update Shard Stats at DiscordBots",
								"Unexpected exception occurred while updating Shard " + shard.getId() + " server count!\n" + Hastebin.post(OtherUtils.getStackTrace(e)),
								Level.WARN);
			}
		}), 216000);
	}
}

package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskManager {
	
	public BotContainer container;
	private MusicRegisterTimeout musicRegisterTimeout;
	private ChannelLeaveTimer channelLeaveTimer;
	
	public TaskManager(BotContainer container) {
		this.container = container;
		this.musicRegisterTimeout = new MusicRegisterTimeout(container);
		this.channelLeaveTimer = new ChannelLeaveTimer(container);
		startAsyncTasks();
	}
	
	public ChannelLeaveTimer getChannelLeaveTimer() {
		return channelLeaveTimer;
	}
	
	public MusicRegisterTimeout getMusicRegisterTimeout() {
		return musicRegisterTimeout;
	}
	
	public void startAsyncTask(Runnable run, int seconds) {
		Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Async Task-%d").build()).scheduleAtFixedRate(
				run, 0, seconds, TimeUnit.SECONDS);
	}
	
	private void startAsyncTasks() {
	    final OperatingSystemMXBean os =
			    ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
	    startAsyncTask(
			    () -> container.getSession().cpuUsage = (Math.floor(os.getProcessCpuLoad() * 10000) / 100), 5);
		startAsyncTask(() -> Arrays.stream(container.getShards()).forEach(shard -> {
			try {
				shard.updateStats();
			} catch (UnirestException e) {
				container.getDiscordLog().logToDiscord("Failed to update Shard Stats at DiscordBots", "Unexpected exception occurred while updating Shard " + shard.getId() + " server count!\n" + Hastebin.post(Util.getStackTrace(e)), Level.WARN);
			}
		}), 216000);
	}
}

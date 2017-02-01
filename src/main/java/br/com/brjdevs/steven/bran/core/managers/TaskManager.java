package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
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
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                run, 0, seconds, TimeUnit.SECONDS);
    }
	
	private void startAsyncTasks() {
	    final OperatingSystemMXBean os =
			    ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
	    startAsyncTask(
			    () -> container.getSession().cpuUsage = (Math.floor(os.getProcessCpuLoad() * 10000) / 100), 5);
	}
}

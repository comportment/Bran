package br.net.brjdevs.steven.bran.core.managers;

import br.net.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.net.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import br.net.brjdevs.steven.bran.core.client.Bran;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskManager {
	
	private MusicRegisterTimeout musicRegisterTimeout;
	private ChannelLeaveTimer channelLeaveTimer;
	
	public TaskManager() {
		this.musicRegisterTimeout = new MusicRegisterTimeout();
		this.channelLeaveTimer = new ChannelLeaveTimer();
		startAsyncTasks();
	}
	
	public static void startAsyncTask(String task, Consumer<ScheduledExecutorService> scheduled, int everySeconds) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, task + " [Executor]"));
		scheduledExecutorService.scheduleAtFixedRate(() -> scheduled.accept(scheduledExecutorService), 0, everySeconds, TimeUnit.SECONDS);
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
	
	private void startAsyncTasks() {
	    final OperatingSystemMXBean os =
			    ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
		startAsyncTask("CPU Thread",
				(service) -> Bran.getInstance().getSession().cpuUsage = Bran.getInstance().getSession().calculateCpuUsage(os), 5);
		startAsyncTask("Stamina Regenerator", (service) ->
                        Bran.getInstance().getDataManager().getData().get().users.values().stream().filter(userData -> userData.getProfileData().getStamina() < 100).forEach(userData -> userData.getProfileData().setStamina(userData.getProfileData().getStamina() + 5))
                , 300);
	}
}

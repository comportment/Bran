package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.DiscordLog.Level;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.management.OperatingSystemMXBean;

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
		startAsyncTask("DiscordBots Thread", (service) -> Arrays.stream(Bran.getInstance().getShards()).forEach(shard -> {
			try {
				shard.updateStats();
				shard.updateCurrentGuildCount();
				Bran.getInstance().getDiscordLog().logToDiscord("Updated server_count in DiscordBots (Shard[" + shard.getId() + "])", "Successfully updated server_count at [Discord Bots/PW](https://bots.discord.pw/bots/" + shard.getJDA().getSelfUser().getId() + ") and [Discord Bots/ORG](https://discordbots.org/bot/" + shard.getJDA().getSelfUser().getId() + ")", Level.INFO);
			} catch (UnirestException e) {
				Bran.getInstance().getDiscordLog()
						.logToDiscord(e.getMessage(),
								"Unexpected exception occurred while updating Shard " + shard.getId() + " server count!", Level.WARN);
			}
		}), 3600);
		startAsyncTask("Stamina Regenerator", (service) ->
						Bran.getInstance().getDataManager().getDataHolderManager().get().users.values().stream().filter(userData -> userData.getProfile().getStamina() < 210).forEach(userData -> userData.getProfile().setStamina(userData.getProfile().getStamina() + 5))
				, 300);
	}
}

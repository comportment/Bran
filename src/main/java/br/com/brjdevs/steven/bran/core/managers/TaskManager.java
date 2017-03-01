package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.audio.timers.ChannelLeaveTimer;
import br.com.brjdevs.steven.bran.core.audio.timers.MusicRegisterTimeout;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
				(service) -> client.getSession().cpuUsage = client.getSession().calculateCpuUsage(os), 5);
		startAsyncTask("DiscordBots Thread", (service) -> Arrays.stream(client.getShards()).forEach(shard -> {
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
		}), 3600);
	}
}

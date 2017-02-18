package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
	
	public static Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	private static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client();
			Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
				throwable.printStackTrace();
				String url = Hastebin.post(OtherUtils.getStackTrace(throwable));
				client.getDiscordLog().logToDiscord("Uncaught exception in Thread " + thread.getName(), "An unexpected `" + throwable.getClass().getSimpleName() + "` occurred.\nMessage: " + throwable.getMessage() + "\nStackTrace: " + url, Level.FATAL);
			});
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					for (ClientShard clientShard : client.getOnlineShards()) {
						clientShard.getJDA().shutdown();
					}
					client.dataManager.saveData();
				} catch (Exception e) {
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

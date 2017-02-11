package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
	
	public static Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	private static BotContainer container;
	
	public static void main(String[] args) {
		System.out.println(String.join("\n", Argument.split("\"\"\" b\"", 2)));
		try {
			container = new BotContainer();
			Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
				throwable.printStackTrace();
				String url = Hastebin.post(Util.getStackTrace(throwable));
				container.getDiscordLog().logToDiscord("Uncaught exception in Thread " + thread.getName(), "An unexpected `" + throwable.getClass().getSimpleName() + "` occurred.\nMessage: " + throwable.getMessage() + "\nStackTrace: " + url, Level.FATAL);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

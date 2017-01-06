package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@RegisterCommand
public class ShortenURLCommand {
	public ShortenURLCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("shorten")
				.setName("Shorten URL Command")
				.setDescription("Shortens URLs for you!")
				.setArgs("[URL]")
				.setAction((event) -> {
					String url = event.getArgs(2)[1];
					if (url.isEmpty()) {
						event.sendMessage("You have to tell me a URL to shorten!").queue();
						return;
					}
					try {
						String shorten = shorten(url);
						event.sendMessage(shorten).queue();
					} catch (IOException e) {
						event.sendMessage(e.getMessage()).queue();
					}
				})
				.build());
	}
	private static String baseURL = "https://is.gd/create.php?format=simple&url=%s";
	public static String shorten(String url) throws IOException {
		try {
			URLConnection conn = new URL(String.format(baseURL, url)).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
			in.close();
			return stringBuilder.toString();
		} catch (IOException e1) {
			throw new IOException("Please, input a valid URL.");
		}
		catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}
	
}

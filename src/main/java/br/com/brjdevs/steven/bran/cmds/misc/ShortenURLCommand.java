package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ShortenURLCommand {
	
	private static String baseURL = "https://is.gd/create.php?format=simple&url=%s";
	
	@Command
	private static ICommand shortenUrl() {
		return new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("shorten")
				.setName("Shorten URL Command")
				.setDescription("Shortens URLs for you!")
				.setArgs(new Argument<>("url", String.class))
				.setAction((event) -> {
					String url = ((String) event.getArgument("url").get());
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
				.build();
	}
	
	private static String shorten(String url) throws IOException {
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

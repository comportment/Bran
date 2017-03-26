package br.net.brjdevs.steven.bran.cmds.misc;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.utils.HttpUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class ShortenURLCommand {
	
	private static String baseURL = "https://is.gd/create.php?format=simple&url=%s";
	
	@Command
	private static ICommand shortenUrl() {
		return new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("shorten")
				.setName("Shorten URL Command")
				.setDescription("Shortens URLs for you!")
				.setArgs(new Argument("url", String.class))
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
			return HttpUtils.read(String.format(baseURL, URLEncoder.encode(url, "UTF-8")));
		} catch (IOException e1) {
			throw new IOException("Please, input a valid URL.");
		}
		catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}
	
}

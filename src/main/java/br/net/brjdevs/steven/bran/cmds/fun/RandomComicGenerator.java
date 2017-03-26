package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class RandomComicGenerator {
	
	@Command
	private static ICommand rcg() {
		return new CommandBuilder(Category.FUN)
				.setName("Random Comic Generator")
				.setDescription("Shit-post")
				.setArgs(new Argument("amount", Integer.class, true))
				.setAliases("rcg", "randomcomicgenerator")
				.setAction((event) -> {
					if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
						event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
						return;
					}
					Argument argument = event.getArgument("amount");
					int times = argument.isPresent() && (int) argument.get() > 0 ? (int) argument.get() : 1;
					try{
						if(times <= 0) {
							event.sendMessage("You want me to send a comic " + times + " times...?").queue();
							return;
						}
						if(times > 3) {
							event.sendMessage("You can't request more than 3 comics per command.").queue();
							return;
						}
						int attempts = 0;
						while(times != 0) {
							if (attempts > 3) throw new UnirestException("Failed to connect.");
							try {
								
								Element element = Jsoup.connect("http://explosm.net/rcg/?promo=false")
										.get().getElementById("rcg-comic")
										.getElementsByTag("img").get(0);
								
								String url = element.absUrl("src");
								event.sendMessage(new EmbedBuilder().setImage(url).build()).queue();
								times--;
							} catch (Exception e1) {
								attempts++;
							}
						}
					} catch (Exception ex) {
						event.sendMessage("Unable to grab random Cyanide and Happiness comic.").queue();
					}
				})
				.build();
	}
}

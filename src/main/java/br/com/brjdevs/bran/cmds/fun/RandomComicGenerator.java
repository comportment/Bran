package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.utils.MathUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

@RegisterCommand
public class RandomComicGenerator {
	public RandomComicGenerator() {
		CommandManager.addCommand(new CommandBuilder(Category.FUN)
				.setName("Random Comic Generator")
				.setDescription("Shit-post")
				.setArgs("<AMOUNT>")
				.setAliases("rcg", "randomcomicgenerator")
				.setAction((event, args) -> {
					String s = StringUtils.splitArgs(args, 2)[1];
					int times = 1;
					if (MathUtils.isInteger(s))
						times = Integer.parseInt(s);
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
				.build());
	}
}

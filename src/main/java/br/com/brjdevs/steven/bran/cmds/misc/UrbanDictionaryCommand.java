package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class UrbanDictionaryCommand {
	
	@Command
	private static ICommand urban() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("urban", "ud")
				.setDescription("Searches definitions from Urban Dictionary.")
				.setArgs(new Argument<>("term", String.class))
				.setName("Urban Command")
				.setAction((event, args) -> {
					String params = ((String) event.getArgument("term").get());
					if (params.isEmpty()) {
						event.sendMessage("You didn't tell me a definition to search! Use `" + event.getPrefix() + "urban [term]").queue();
						return;
					}
					try {
						String query = params.replaceAll("\\s+", "%20");
						URL url = new URL("http://api.urbandictionary.com/v0/define?term=" + query);
						InputStream in = url.openStream();
						Scanner scan = new Scanner(in);
						String jsonstring = "";
						while(scan.hasNext()){
							jsonstring += scan.next() + " ";
						}
						scan.close();
						JsonObject json = Bot.GSON.fromJson(jsonstring, JsonElement.class).getAsJsonObject();
						if(json.get("result_type").getAsString().equals("no_results")){
							event.sendMessage("There aren't any definitions for `" + params + "` yet.").queue();
							return;
						}
						JsonObject result = json.get("list").getAsJsonArray().get(0).getAsJsonObject();
						String definition = result.get("definition").getAsString();
						int thumbsup = result.get("thumbs_up").getAsInt();
						String author = result.get("author").getAsString();
						String example = result.get("example").getAsString();
						int thumbsdown = result.get("thumbs_down").getAsInt();
						String word = result.get("word").getAsString();
						EmbedBuilder builder = new EmbedBuilder()
										.setFooter("Powered by Urban Dictionary", "https://cdn.discordapp.com/attachments/225694598465454082/248910958280441868/photo.png")
										.setAuthor(word + " definition by " + author, null, null)
										.setUrl("https://www.urbandictionary.com/define.php?term=" + query)
										.addField("Definition: ", Util.isEmpty(definition) ? "No definition provided." : definition.length() > EmbedBuilder.VALUE_MAX_LENGTH ? "Definition is too big, click [here](https://www.urbandictionary.com/define.php?term=" + query +") to see it." : definition, false)
										.addField("\u00ad\nExample: ", Util.isEmpty(example) ? "No example provided." : example.length() > EmbedBuilder.VALUE_MAX_LENGTH ? "Example is too big, click [here](https://www.urbandictionary.com/define.php?term=" + query +") to see it." : example, false)
										.addField("\uD83D\uDC4D", String.valueOf(thumbsup), true)
										.addField("\uD83D\uDC4E", String.valueOf(thumbsdown), true)
								.setColor(event.getGuild().getSelfMember().getColor() == null ? Color.decode("#002b79") : event.getGuild().getSelfMember().getColor());
						event.sendMessage(builder.build()).queue(null, throwable -> {
							event.sendMessage("An unexpected error occurred.").queue();
							throwable.printStackTrace();
						});
					} catch (MalformedURLException e) {
					} catch (IOException | IndexOutOfBoundsException e) {
						event.sendMessage("Something went wrong!").queue();
						e.printStackTrace();
					}
				})
				.build();
	}
}
